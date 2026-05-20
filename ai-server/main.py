import json
import os
from contextlib import contextmanager
from datetime import datetime, timezone
from typing import Any

import psycopg2
import psycopg2.extras
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field


MODEL_VERSION = "rule-based-v1"

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
    systolicBp: int | None = None
    diastolicBp: int | None = None
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
    value = (activity_level or "").lower()
    if value in {"high", "active", "many"}:
        return -12
    if value in {"low", "sedentary", "little"}:
        return 8
    return 0


def risk_level(glucose_peak: float, systolic: int, diastolic: int) -> str:
    if glucose_peak >= 200 or systolic >= 140 or diastolic >= 90:
        return "HIGH"
    if glucose_peak >= 140 or systolic >= 130 or diastolic >= 80:
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
    base_systolic = int(number(request.systolicBp, number(profile.get("systolic_bp"), 120)))
    base_diastolic = int(number(request.diastolicBp, number(profile.get("diastolic_bp"), 80)))

    insulin_sensitivity = number(profile.get("insulin_sensitivity"), 1.0)
    glucose_delta = (total_carbs * 1.15 + total_sugar * 0.75) / max(insulin_sensitivity, 0.5)
    peak = round(base_glucose + glucose_delta + activity_factor(request.activityLevel), 1)
    pred_1h = int(round(base_glucose + glucose_delta * 0.82 + activity_factor(request.activityLevel)))

    systolic = int(round(base_systolic + min(total_sodium / 700, 8)))
    diastolic = int(round(base_diastolic + min(total_sodium / 1200, 5)))

    curve = []
    for minute, ratio in [(0, 0), (30, 0.55), (60, 0.82), (90, 1.0), (120, 0.72), (180, 0.35)]:
        glucose = round(base_glucose + (peak - base_glucose) * ratio, 1)
        curve.append({
            "minute": minute,
            "glucoseMgdl": glucose,
            "systolicBp": int(round(base_systolic + (systolic - base_systolic) * ratio)),
            "diastolicBp": int(round(base_diastolic + (diastolic - base_diastolic) * ratio)),
        })

    return {
        "modelVersion": MODEL_VERSION,
        "pred1hMgdl": pred_1h,
        "predictedPeak": peak,
        "predictedSystolicBp": systolic,
        "predictedDiastolicBp": diastolic,
        "riskLevel": risk_level(peak, systolic, diastolic),
        "predictionCurve": curve,
        "evidenceSummary": (
            f"Predicted from carbs {total_carbs:.1f}g, sugar {total_sugar:.1f}g, "
            f"and sodium {total_sodium:.1f}mg."
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
            "bloodPressure": {
                "systolic": result["predictedSystolicBp"],
                "diastolic": result["predictedDiastolicBp"],
            },
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
    return {"status": "ok", "modelVersion": MODEL_VERSION}


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
