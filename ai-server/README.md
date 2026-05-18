# ForeMeal AI Server

FastAPI server for fridge ingredient image detection.

## Setup

```powershell
conda create -n foremeal-ai python=3.11
conda activate foremeal-ai
pip install -r requirements.txt
```

## Run

```powershell
uvicorn app.main:app --reload --port 8000
```

To run with a custom ingredient model:

```powershell
$env:YOLO_MODEL="models\ingredient-yolo.pt"
uvicorn app.main:app --reload --port 8000
```

## Endpoints

- `GET /health`
- `POST /predict`

Example request:

```json
{
  "imageUrl": "https://example.com/fridge.jpg"
}
```

## Custom Ingredient Model

The default `yolo11n.pt` model uses the COCO 80-class label set, so it only detects a few food classes.
For fridge ingredients, train a custom model with ingredient labels such as `onion`, `egg`, `milk`,
`tofu`, `garlic`, `potato`, and `tomato`.

Expected dataset layout:

```text
datasets/ingredients/
  images/train/*.jpg
  images/val/*.jpg
  labels/train/*.txt
  labels/val/*.txt
```

Each label file must use YOLO format:

```text
class_id x_center y_center width height
```

Train:

```powershell
python .\scripts\train_ingredient_yolo.py --data .\datasets\ingredients.yaml --epochs 80 --imgsz 640
```

After training, copy `runs/ingredient-detect/yolo11n-ingredients/weights/best.pt` to
`models/ingredient-yolo.pt`, then run the API with `YOLO_MODEL` pointing to that file.

After swapping the model, rerun:

```powershell
python .\scripts\extract_yolo_classes.py --model .\models\ingredient-yolo.pt --output-dir .
```

Use the generated class list to seed `ingredient_alias` with Korean names.
