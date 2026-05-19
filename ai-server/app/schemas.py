from pydantic import BaseModel, Field


class PredictRequest(BaseModel):
    imageUrl: str


class DetectedItem(BaseModel):
    name: str
    confidence: float = Field(ge=0, le=1)


class PredictResponse(BaseModel):
    items: list[DetectedItem]
