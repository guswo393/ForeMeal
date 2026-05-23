import json
import os
from contextlib import contextmanager
from datetime import datetime, timezone
from typing import Any

import psycopg2
import psycopg2.extras
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field


MODEL_VERSION = "rule-based-v1-glucose"

app = FastAPI(title="ForeMeal Prediction API")


class FoodItem(BaseModel):
    foodId: int | None = None
    name: str | None = None
    calories: float | None = 0
    carbs: float | None = 0
    sugar: float | None = 0
    sodium: float | None = 0
    quantity: float | None = 1


class PredictionRequest(BaseModel):
    userId: int
    mealId: int | None = None
    foodId: int | None = None
    currentGlucose: float | None = Field(default=None, description="mg/dL")
    activityLevel: str | None = "normal"
    foods: list[FoodItem] = Field(default_factory=list)


def db_config() -> dict[str, Any]:
    return {
        "host": os.getenv("DB_HOST", "localhost"),
        "port": int(os.getenv("DB_PORT", "5432")),
        "dbname": os.getenv("DB_NAME", "foremeal"),
        "user": os.getenv("DB_USER", "postgres"),
        "password": os.getenv("DB_PASSWORD", "postgres"),
    }


@contextmanager
def db_connection():
    conn = psycopg2.connect(**db_config())

    try:
        yield conn
        conn.commit()
    except Exception:
        conn.rollback()
        raise
    finally:
        conn.close()


def number(value: Any, default: float) -> float:
    try:
        if value is None:
            return default
        return float(value)
    except (TypeError, ValueError):
        return default


def get_health_profile(conn, user_id: int) -> dict[str, Any]:
    with conn.cursor(cursor_factory=psycopg2.extras.RealDictCursor) as cur:
        cur.execute("select * from health_profile where user_id = %s", (user_id,))
        profile = cur.fetchone()
        return dict(profile) if profile else {}


def activity_factor(activity_level: str | None) -> float:
    value = (activity_level or "normal").lower()

    if value in {"high", "active", "many"}:
        return -12.0

    if value in {"low", "sedentary", "little"}:
        return 8.0

    return 0.0


def sugar_ratio_factor(total_carbs: float, total_sugar: float) -> float:
    if total_carbs <= 0:
        return 1.0

    ratio = total_sugar / total_carbs

    if ratio >= 0.5:
        return 1.2

    if ratio >= 0.2:
        return 1.1

    return 1.0


def glucose_sensitivity_factor(base_glucose: float, profile: dict[str, Any]) -> float:
    has_diabetes = bool(profile.get("has_diabetes", False))

    if has_diabetes:
        carb_factor = 1.6
    else:
        carb_factor = 1.2

    if base_glucose >= 180:
        return carb_factor * 1.1

    return carb_factor


def glucose_risk_level(predicted_peak: float) -> str:
    if predicted_peak >= 180:
        return "HIGH"

    if predicted_peak >= 140:
        return "CAUTION"

    return "NORMAL"


def predict(request: PredictionRequest, profile: dict[str, Any]) -> dict[str, Any]:
    foods = request.foods or []

    total_carbs = sum(number(food.carbs, 0) * number(food.quantity, 1) for food in foods)
    total_sugar = sum(number(food.sugar, 0) * number(food.quantity, 1) for food in foods)
    total_sodium = sum(number(food.sodium, 0) * number(food.quantity, 1) for food in foods)

    base_glucose = number(
        request.currentGlucose,
        number(profile.get("fasting_glucose"), number(profile.get("glucose"), 100)),
    )

    carb_factor = glucose_sensitivity_factor(base_glucose, profile)
    sugar_factor = sugar_ratio_factor(total_carbs, total_sugar)
    activity_adjustment = activity_factor(request.activityLevel)

    glucose_delta = total_carbs * carb_factor * sugar_factor

    peak = round(base_glucose + glucose_delta + activity_adjustment, 1)
    pred_1h = int(round(base_glucose + glucose_delta * 0.85 + activity_adjustment))

    risk = glucose_risk_level(peak)

    curve = []

    for minute, ratio in [
        (0, 0.0),
        (30, 0.55),
        (60, 0.85),
        (90, 1.0),
        (120, 0.65),
        (180, 0.25),
    ]:
        glucose = round(base_glucose + (peak - base_glucose) * ratio, 1)

        curve.append({
            "minute": minute,
            "glucoseMgdl": glucose,
        })

    food_name = foods[0].name if foods and foods[0].name else "이 음식"

    return {
        "modelVersion": MODEL_VERSION,
        "baseGlucose": base_glucose,
        "pred1hMgdl": pred_1h,
        "predictedPeak": peak,
        "riskLevel": risk,
        "predictionCurve": curve,
        "evidenceSummary": (
            f"현재 기준 혈당 {base_glucose:.0f}에서 {food_name}을 먹으면 "
            f"식후 혈당이 최대 {peak:.0f} mg/dL까지 오를 수 있어요. "
            f"탄수화물 {total_carbs:.1f}g, 당류 {total_sugar:.1f}g, "
            f"나트륨 {total_sodium:.1f}mg, 활동량 {request.activityLevel or 'normal'} 기준입니다."
        ),
    }


def save_prediction(conn, request: PredictionRequest, result: dict[str, Any]) -> tuple[int, int]:
    food_id = request.foodId

    if food_id is None and request.foods:
        food_id = request.foods[0].foodId

    with conn.cursor() as cur:
        cur.execute(
            """
            insert into glucose_prediction
                (meal_id, user_id, food_id, model_version, pred_1h_mgdl, risk_level)
            values (%s, %s, %s, %s, %s, %s)
            returning prediction_id
            """,
            (
                request.mealId,
                request.userId,
                food_id,
                result["modelVersion"],
                result["pred1hMgdl"],
                result["riskLevel"],
            ),
        )

        prediction_id = cur.fetchone()[0]

        curve_data = {
            "generatedAt": datetime.now(timezone.utc).isoformat(),
            "baseGlucose": result["baseGlucose"],
            "curve": result["predictionCurve"],
            "evidenceSummary": result["evidenceSummary"],
        }

        cur.execute(
            """
            insert into daily_vital_summary
                (expected_glucose_peak, simulation_curve_data, prediction_id, meal_id, user_id, food_id)
            values (%s, %s::jsonb, %s, %s, %s, %s)
            returning pred_id
            """,
            (
                result["predictedPeak"],
                json.dumps(curve_data, ensure_ascii=False),
                prediction_id,
                request.mealId,
                request.userId,
                food_id,
            ),
        )

        daily_summary_id = cur.fetchone()[0]

    return prediction_id, daily_summary_id


@app.get("/health")
def health():
    return {
        "status": "ok",
        "modelVersion": MODEL_VERSION,
    }


@app.post("/predict")
def predict_only(request: PredictionRequest):
    profile = {}

    try:
        with db_connection() as conn:
            profile = get_health_profile(conn, request.userId)
    except psycopg2.Error:
        profile = {}

    return predict(request, profile)


@app.post("/predict/glucose")
def predict_glucose(request: PredictionRequest):
    try:
        with db_connection() as conn:
            profile = get_health_profile(conn, request.userId)
            result = predict(request, profile)
            prediction_id, daily_summary_id = save_prediction(conn, request, result)
    except psycopg2.Error as exc:
        raise HTTPException(status_code=500, detail=f"database error: {exc}") from exc

    return {
        "predictionId": prediction_id,
        "dailySummaryId": daily_summary_id,
        "userId": request.userId,
        "mealId": request.mealId,
        "foodId": request.foodId,
        **result,
    }


if __name__ == "__main__":
    import uvicorn

    uvicorn.run(app, host="0.0.0.0", port=8000)