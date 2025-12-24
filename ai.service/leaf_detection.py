from ultralytics import YOLO
from PIL import Image
import io
import os
import numpy as np

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
DEFAULT_MODEL_PATH = os.path.join(
    BASE_DIR,
    "yolov11s_model_plant",
    "yolov11s_model_plant.pt"
)

class LeafDiseaseDetector:
    def __init__(self, model_path: str = DEFAULT_MODEL_PATH):
        self.model_path = model_path
        self.model = None
        self.is_ready = False


    def load_model(self):
        if os.path.exists(self.model_path):
            print(f"Loading YOLO Model from: {self.model_path}")
            try:
                self.model = YOLO(self.model_path)
                self.is_ready = True
                print("YOLO Model Loaded Successfully.")
            except Exception as e:
                print(f"Failed to load YOLO model: {e}")
        else:
            print(f"Error: Model not found at {self.model_path}")

    def predict(self, image_bytes: bytes):
        if not self.is_ready:
            return {"error": "Model not loaded"}

        try:
            # Convert bytes to PIL Image
            image = Image.open(io.BytesIO(image_bytes))
            
            # Run Inference
            results = self.model(image)
            
            # Process Results
            detections = []
            for r in results:
                for box in r.boxes:
                    # Get Class Name
                    class_id = int(box.cls[0])
                    class_name = self.model.names[class_id]
                    confidence = float(box.conf[0])
                    
                    # Bounding Box
                    x1, y1, x2, y2 = box.xyxy[0].tolist()
                    
                    detections.append({
                        "class": class_name,
                        "confidence": round(confidence, 2),
                        "bbox": [round(x1), round(y1), round(x2), round(y2)]
                    })
            
            # Determine overall health status
            if not detections:
                status = "Uncertain (No leaf detected)"
            else:
                # If any disease detected
                diseases = [d['class'] for d in detections if d['class'].lower() != 'healthy']
                if diseases:
                    status = f"Diseased: {', '.join(set(diseases))}"
                else:
                    status = "Healthy"
                    
            return {
                "status": status,
                "detections": detections
            }
            
        except Exception as e:
            return {"error": f"Prediction failed: {str(e)}"}
