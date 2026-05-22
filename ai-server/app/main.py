from fastapi import FastAPI, HTTPException
import requests

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
