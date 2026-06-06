from fastapi import FastAPI, HTTPException
import requests

from app.detector import detect_items, is_demo_mode
from app.schemas import PredictResponse

app = FastAPI(title="ForeMeal AI Server")


@app.get("/health")
def health() -> dict[str, str | bool]:
    return {"status": "ok", "pantryDemoMode": is_demo_mode()}


def _number(value, default: float) -> float:
    try:
        if value is None:
            return default
        return float(value)
    except (TypeError, ValueError):
        return default


def _activity_factor(activity_level: str | None) -> float:
    value = (activity_level or "").lower()
    if value in {"high", "active", "many"}:
        return -12
    if value in {"low", "sedentary", "little"}:
        return 8
    return 0


def _risk_level(glucose_peak: float, systolic: int, diastolic: int) -> str:
    if glucose_peak >= 200 or systolic >= 140 or diastolic >= 90:
        return "HIGH"
    if glucose_peak >= 140 or systolic >= 130 or diastolic >= 80:
        return "CAUTION"
    return "NORMAL"


def _predict_glucose(payload: dict) -> dict:
    foods = payload.get("foods") or []
    total_carbs = sum(_number(food.get("carbs"), 0) * _number(food.get("quantity"), 1) for food in foods)
    total_sugar = sum(_number(food.get("sugar"), 0) * _number(food.get("quantity"), 1) for food in foods)
    total_sodium = sum(_number(food.get("sodium"), 0) * _number(food.get("quantity"), 1) for food in foods)

    base_glucose = _number(payload.get("currentGlucose"), 100)
    base_systolic = int(_number(payload.get("systolicBp"), 120))
    base_diastolic = int(_number(payload.get("diastolicBp"), 80))
    glucose_delta = total_carbs * 1.15 + total_sugar * 0.75
    activity = _activity_factor(payload.get("activityLevel"))
    peak = round(base_glucose + glucose_delta + activity, 1)
    pred_1h = int(round(base_glucose + glucose_delta * 0.82 + activity))

    systolic = int(round(base_systolic + min(total_sodium / 700, 8)))
    diastolic = int(round(base_diastolic + min(total_sodium / 1200, 5)))
    curve = []
    for minute, ratio in [(0, 0), (30, 0.55), (60, 0.82), (90, 1.0), (120, 0.72), (180, 0.35)]:
        curve.append({
            "minute": minute,
            "glucoseMgdl": round(base_glucose + (peak - base_glucose) * ratio, 1),
            "systolicBp": int(round(base_systolic + (systolic - base_systolic) * ratio)),
            "diastolicBp": int(round(base_diastolic + (diastolic - base_diastolic) * ratio)),
        })

    return {
        "modelVersion": "rule-based-v1",
        "baseGlucose": base_glucose,
        "pred1hMgdl": pred_1h,
        "predictedPeak": peak,
        "predictedSystolicBp": systolic,
        "predictedDiastolicBp": diastolic,
        "riskLevel": _risk_level(peak, systolic, diastolic),
        "predictionCurve": curve,
        "evidenceSummary": (
            f"탄수화물 {total_carbs:.1f}g, 당류 {total_sugar:.1f}g, "
            f"나트륨 {total_sodium:.1f}mg 기준 예측입니다."
        ),
    }


@app.post("/predict")
def predict(request: dict):
    if "imageUrl" not in request:
        return _predict_glucose(request)

    try:
        return PredictResponse(items=detect_items(request["imageUrl"]))
    except requests.HTTPError as exc:
        status_code = exc.response.status_code if exc.response is not None else 502
        if status_code == 429:
            detail = "Image host rate limited the request. Try another image URL or retry later."
        else:
            detail = f"Image download failed with HTTP {status_code}."
        raise HTTPException(status_code=status_code, detail=detail) from exc
    except requests.RequestException as exc:
        raise HTTPException(status_code=502, detail=f"Image download failed: {exc}") from exc
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc)) from exc
    except Exception as exc:
        raise HTTPException(status_code=500, detail=str(exc)) from exc
