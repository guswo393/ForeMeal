# Colab YOLO Training

ForeMeal ingredient detector training can be run on Google Colab with GPU.

## 1. Prepare Colab

In Colab, open `Runtime > Change runtime type`, then set:

- Hardware accelerator: `T4 GPU`

## 2. Upload the project files

Recommended upload target:

```text
/content/ai-server
```

The training files must include:

```text
ai-server/
  requirements.txt
  datasets/
    ingredients.yaml
    train/
      images/
      labels/
    test/
      images/
      labels/
  scripts/
    train_ingredient_yolo.py
    extract_yolo_classes.py
```

If uploading manually, zip the local `ai-server` folder first, upload it to Colab, then unzip:

```python
from google.colab import files
uploaded = files.upload()
```

```bash
unzip ai-server.zip -d /content
cd /content/ai-server
```

If cloning from GitHub instead, use:

```bash
git clone YOUR_REPOSITORY_URL ForeMeal
cd /content/ForeMeal/ai-server
```

## 3. Install dependencies

```bash
pip install -r requirements.txt
```

If OpenCV causes Colab display/runtime issues, install the headless package:

```bash
pip uninstall -y opencv-python
pip install opencv-python-headless
```

## 4. Check GPU

```bash
nvidia-smi
```

## 5. Train

Quick smoke test:

```bash
python scripts/train_ingredient_yolo.py --data datasets/ingredients.yaml --epochs 3 --imgsz 640 --batch 8
```

Full training:

```bash
python scripts/train_ingredient_yolo.py --data datasets/ingredients.yaml --epochs 80 --imgsz 640 --batch 8
```

If Colab runs out of memory, lower the batch size:

```bash
python scripts/train_ingredient_yolo.py --data datasets/ingredients.yaml --epochs 80 --imgsz 640 --batch 4
```

## 6. Save the trained model

The best model is written to:

```text
runs/ingredient-detect/yolo11n-ingredients/weights/best.pt
```

Download it:

```python
from google.colab import files
files.download("runs/ingredient-detect/yolo11n-ingredients/weights/best.pt")
```

Back in this project, place it at:

```text
ai-server/models/ingredient-yolo.pt
```

Then run the API server with:

```powershell
$env:YOLO_MODEL="models\ingredient-yolo.pt"
uvicorn app.main:app --reload --port 8000
```

## 7. Export class metadata

After copying the model into `ai-server/models/ingredient-yolo.pt`, update the class files:

```powershell
cd ai-server
python .\scripts\extract_yolo_classes.py --model .\models\ingredient-yolo.pt --output-dir .
```

Use the generated class list to update Korean ingredient aliases if needed.
