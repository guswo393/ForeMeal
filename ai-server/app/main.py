from fastapi import FastAPI, HTTPException

from app.detector import detect_items
from app.schemas import PredictRequest, PredictResponse

app = FastAPI(title="ForeMeal AI Server")


@app.get("/health")
def health() -> dict[str, str]:
    return {"status": "ok"}


@app.post("/predict", response_model=PredictResponse)
def predict(request: PredictRequest) -> PredictResponse:
    try:
        return PredictResponse(items=detect_items(request.imageUrl))
    except Exception as exc:
        raise HTTPException(status_code=500, detail=str(exc)) from exc
