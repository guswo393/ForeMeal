from pydantic import BaseModel, Field


class PredictRequest(BaseModel):
    imageUrl: str


class DetectedItem(BaseModel):
    name: str
    quantity: float = Field(default=1.0, ge=0)
    confidence: float = Field(ge=0, le=1)


class PredictResponse(BaseModel):
    items: list[DetectedItem]
