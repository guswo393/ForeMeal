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

## Endpoints

- `GET /health`
- `POST /predict`

Example request:

```json
{
  "imageUrl": "https://example.com/fridge.jpg"
}
```
