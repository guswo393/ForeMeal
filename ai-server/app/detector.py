from app.schemas import DetectedItem


def detect_items(image_url: str) -> list[DetectedItem]:
    # Placeholder until the YOLO model is connected.
    return [
        DetectedItem(name="egg", quantity=1.0, confidence=0.91),
        DetectedItem(name="milk", quantity=1.0, confidence=0.84),
    ]
