import os
from collections import defaultdict
from io import BytesIO

import requests
from PIL import Image

from app.schemas import DetectedItem

MODEL_NAME = os.getenv("YOLO_MODEL", "yolo11n.pt")
FALLBACK_MODEL_NAME = os.getenv("YOLO_FALLBACK_MODEL", "yolo11n.pt")
FALLBACK_ENABLED = os.getenv("YOLO_FALLBACK_ENABLED", "true").lower() == "true"
CONFIDENCE_THRESHOLD = float(os.getenv("YOLO_CONFIDENCE", "0.25"))
FALLBACK_CONFIDENCE_THRESHOLD = float(os.getenv("YOLO_FALLBACK_CONFIDENCE", "0.25"))

_models = {}


def _get_model(model_name: str):
    if model_name not in _models:
        from ultralytics import YOLO

        _models[model_name] = YOLO(model_name)
    return _models[model_name]


def _load_image(image_url: str) -> Image.Image:
    response = requests.get(
        image_url,
        headers={"User-Agent": "ForeMeal-AI/0.1"},
        timeout=15,
    )
    response.raise_for_status()
    content_type = response.headers.get("content-type", "")
    if "image" not in content_type.lower():
        raise ValueError(f"URL did not return an image. content-type={content_type}")
    return Image.open(BytesIO(response.content)).convert("RGB")


def _predict(image: Image.Image, model_name: str, confidence_threshold: float) -> list[DetectedItem]:
    model = _get_model(model_name)
    results = model.predict(image, conf=confidence_threshold, verbose=False)
    detected: dict[str, list[float]] = defaultdict(list)

    for result in results:
        names = result.names
        for box in result.boxes:
            class_id = int(box.cls[0])
            confidence = float(box.conf[0])
            detected[names[class_id]].append(confidence)

    return [
        DetectedItem(
            name=name,
            confidence=max(confidences),
        )
        for name, confidences in detected.items()
    ]


def detect_items(image_url: str) -> list[DetectedItem]:
    image = _load_image(image_url)
    items = _predict(image, MODEL_NAME, CONFIDENCE_THRESHOLD)

    if items or not FALLBACK_ENABLED or MODEL_NAME == FALLBACK_MODEL_NAME:
        return items

    return _predict(image, FALLBACK_MODEL_NAME, FALLBACK_CONFIDENCE_THRESHOLD)
