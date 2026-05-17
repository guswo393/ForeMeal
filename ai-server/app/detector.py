import os
from collections import defaultdict
from io import BytesIO

import requests
from PIL import Image

from app.schemas import DetectedItem

MODEL_NAME = os.getenv("YOLO_MODEL", "yolo11n.pt")
CONFIDENCE_THRESHOLD = float(os.getenv("YOLO_CONFIDENCE", "0.25"))

_model = None


def _get_model():
    global _model
    if _model is None:
        from ultralytics import YOLO

        _model = YOLO(MODEL_NAME)
    return _model


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


def detect_items(image_url: str) -> list[DetectedItem]:
    image = _load_image(image_url)
    model = _get_model()
    results = model.predict(image, conf=CONFIDENCE_THRESHOLD, verbose=False)

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
