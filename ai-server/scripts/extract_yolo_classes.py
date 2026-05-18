import argparse
import json
from pathlib import Path

COCO80_CLASS_NAMES = {
    0: "person",
    1: "bicycle",
    2: "car",
    3: "motorcycle",
    4: "airplane",
    5: "bus",
    6: "train",
    7: "truck",
    8: "boat",
    9: "traffic light",
    10: "fire hydrant",
    11: "stop sign",
    12: "parking meter",
    13: "bench",
    14: "bird",
    15: "cat",
    16: "dog",
    17: "horse",
    18: "sheep",
    19: "cow",
    20: "elephant",
    21: "bear",
    22: "zebra",
    23: "giraffe",
    24: "backpack",
    25: "umbrella",
    26: "handbag",
    27: "tie",
    28: "suitcase",
    29: "frisbee",
    30: "skis",
    31: "snowboard",
    32: "sports ball",
    33: "kite",
    34: "baseball bat",
    35: "baseball glove",
    36: "skateboard",
    37: "surfboard",
    38: "tennis racket",
    39: "bottle",
    40: "wine glass",
    41: "cup",
    42: "fork",
    43: "knife",
    44: "spoon",
    45: "bowl",
    46: "banana",
    47: "apple",
    48: "sandwich",
    49: "orange",
    50: "broccoli",
    51: "carrot",
    52: "hot dog",
    53: "pizza",
    54: "donut",
    55: "cake",
    56: "chair",
    57: "couch",
    58: "potted plant",
    59: "bed",
    60: "dining table",
    61: "toilet",
    62: "tv",
    63: "laptop",
    64: "mouse",
    65: "remote",
    66: "keyboard",
    67: "cell phone",
    68: "microwave",
    69: "oven",
    70: "toaster",
    71: "sink",
    72: "refrigerator",
    73: "book",
    74: "clock",
    75: "vase",
    76: "scissors",
    77: "teddy bear",
    78: "hair drier",
    79: "toothbrush",
}


def parse_args():
    parser = argparse.ArgumentParser(description="Extract class names from a YOLO model.")
    parser.add_argument(
        "--model",
        default="yolo11n.pt",
        help="YOLO model path or name. Defaults to yolo11n.pt.",
    )
    parser.add_argument(
        "--output-dir",
        default=".",
        help="Directory where output files will be written.",
    )
    parser.add_argument(
        "--no-fallback",
        action="store_true",
        help="Fail instead of using the built-in COCO80 list when ultralytics is unavailable.",
    )
    return parser.parse_args()


def load_class_names(model_path, no_fallback):
    try:
        from ultralytics import YOLO

        model = YOLO(model_path)
        return dict(sorted(model.names.items()))
    except ModuleNotFoundError:
        if no_fallback:
            raise
        print("ultralytics is not installed. Falling back to the default YOLO COCO80 class list.")
        return COCO80_CLASS_NAMES


def main():
    args = parse_args()
    output_dir = Path(args.output_dir)
    output_dir.mkdir(parents=True, exist_ok=True)

    class_names = load_class_names(args.model, args.no_fallback)

    txt_path = output_dir / "yolo11_classes.txt"
    prompt_json_path = output_dir / "yolo11_prompt_base.json"
    alias_json_path = output_dir / "yolo11_alias_seed_base.json"

    with txt_path.open("w", encoding="utf-8") as file:
        for class_id, class_name in class_names.items():
            file.write(f"{class_id}: {class_name}\n")

    prompt_base = {class_name: "" for class_name in class_names.values()}
    with prompt_json_path.open("w", encoding="utf-8") as file:
        json.dump(prompt_base, file, indent=2, ensure_ascii=False)
        file.write("\n")

    alias_seed_base = [
        {
            "detectedName": class_name,
            "searchKeyword": "",
            "displayName": "",
            "food": None,
        }
        for class_name in class_names.values()
    ]
    with alias_json_path.open("w", encoding="utf-8") as file:
        json.dump(alias_seed_base, file, indent=2, ensure_ascii=False)
        file.write("\n")

    print(f"Extracted {len(class_names)} classes")
    print(f"- {txt_path}")
    print(f"- {prompt_json_path}")
    print(f"- {alias_json_path}")


if __name__ == "__main__":
    main()
