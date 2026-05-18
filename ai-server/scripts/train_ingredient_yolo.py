import argparse
from pathlib import Path

from ultralytics import YOLO


def parse_args():
    parser = argparse.ArgumentParser(description="Train a custom YOLO ingredient detector.")
    parser.add_argument("--base-model", default="yolo11n.pt", help="Base YOLO model to fine-tune.")
    parser.add_argument("--data", default="datasets/ingredients.yaml", help="YOLO dataset YAML path.")
    parser.add_argument("--epochs", type=int, default=80)
    parser.add_argument("--imgsz", type=int, default=640)
    parser.add_argument("--batch", type=int, default=8)
    parser.add_argument("--project", default="runs/ingredient-detect")
    parser.add_argument("--name", default="yolo11n-ingredients")
    return parser.parse_args()


def main():
    args = parse_args()
    model = YOLO(args.base_model)
    results = model.train(
        data=args.data,
        epochs=args.epochs,
        imgsz=args.imgsz,
        batch=args.batch,
        project=args.project,
        name=args.name,
    )

    save_dir = Path(results.save_dir)
    best_model = save_dir / "weights" / "best.pt"
    print(f"Training complete: {best_model}")
    print("Run the API server with:")
    print(f"  $env:YOLO_MODEL='{best_model}'")
    print("  uvicorn app.main:app --reload --port 8000")


if __name__ == "__main__":
    main()
