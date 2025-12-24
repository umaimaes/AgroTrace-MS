from fastapi import FastAPI, HTTPException, UploadFile, File
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field
from leaf_detection import LeafDiseaseDetector
from typing import List, Optional
import os
import asyncio
from apscheduler.schedulers.asyncio import AsyncIOScheduler

# --- MODULES ---
from system_of_recommendation import RecommendationEngine
from leaf_detection import LeafDiseaseDetector
from kafka_service import KafkaService
from CaptersIOT import CaptersIOT

app = FastAPI(title="Tomato Irrigation AI Service", version="2.0")
app.add_middleware(
    CORSMiddleware,
    allow_origins=[
        "http://localhost:5174",
        "http://localhost:5173",
        "http://127.0.0.1:5174",
        "http://127.0.0.1:5173",
    ],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# --- PATHS ---
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
PROJECT_DIR = os.path.dirname(BASE_DIR)

# Model Path (As specified by user)
YOLO_MODEL_PATH = os.path.join(
    BASE_DIR,
    "yolov11s_model_plant",
    "yolov11s_model_plant.pt"
)

# --- SERVICES ---
rec_engine = RecommendationEngine(BASE_DIR)
leaf_detector = LeafDiseaseDetector(YOLO_MODEL_PATH)
kafka_service = KafkaService(leaf_detector, rec_engine)
sensor_reader = CaptersIOT(port="COM6", baudrate=9600)

# --- SCHEDULER ---
scheduler = AsyncIOScheduler()

# --- LIFECYCLE ---
@app.on_event("startup")
async def startup_event():
    # 1. Load Recommendation System
    rec_engine.load_and_train()
    
    # 2. Load Leaf Detection Model
    leaf_detector.load_model()
    
    # 3. Start Kafka Service (Background)
    # We wrap in try-except so app doesn't crash if Kafka is down
    try:
        await kafka_service.start()
    except Exception as e:
        print(f"WARNING: Kafka Connection Failed: {e}")
        print("Service will run in REST-only mode.")
    
    # 4. Start Scheduler for Sensor Reading (every 8 hours)
    scheduler.add_job(read_sensors_scheduled, 'interval', hours=8, id='sensor_reading')
    scheduler.start()
    print("✓ Scheduler started: Sensor reading will run every 8 hours")

@app.on_event("shutdown")
async def shutdown_event():
    await kafka_service.stop()
    scheduler.shutdown()
    sensor_reader.disconnect()
    print("✓ Scheduler and sensors shut down")

# --- SCHEMAS ---
import requests

# ... (rest of imports are fine, just adding requests)

# --- SCHEMAS ---
class ClimaticConditions(BaseModel):
    # Metadata for notification
    plant_id: int
    plant_name: str
    user_email: str
    
    # Climatic Data
    stage: int
    temperature: float
    humidity: float
    soil_moisture: float
    nitrogen: Optional[float] = 0.0
    phosphorus: Optional[float] = 0.0
    potassium: Optional[float] = 0.0
    ph: Optional[float] = 6.5
    solar_radiation: Optional[float] = 0.0
    wind_speed: Optional[float] = 0.0

# --- ROUTES ---

@app.get("/")
def home():
    return {
        "service": "Tomato Irrigation AI Service",
        "modules": {
            "recommendation": "Active",
            "leaf_detection": "Active" if leaf_detector.is_ready else "Inactive",
            "kafka": "Active" if kafka_service.running else "Inactive"
        }
    }

# 1. Recommendation Endpoint
@app.post("/recommend")
def recommend(data: ClimaticConditions):
    input_data = data.dict(by_alias=True)
    # Mapping keys manually to match training data exactly
    map_data = {
        'Crop Coefficient stage': data.stage,
        'Temperature [_ C]': data.temperature,
        'Humidity [%]': data.humidity,
        'Soil moisture': data.soil_moisture,
        'Nitrogen [mg/kg]': data.nitrogen,
        'Phosphorus [mg/kg]': data.phosphorus,
        'Potassium': data.potassium,
        'pH': data.ph,
        'Solar Radiation ghi': data.solar_radiation,
        'Wind Speed': data.wind_speed
    }
    
    try:
        # 1. Get Recommendation
        analysis_result = rec_engine.analyze(map_data)
        
        # 2. Forward to Notify Server
        notify_payload = {
            "recommendation": analysis_result,
            "plantId": data.plant_id,
            "plantName": data.plant_name,
            "userEmail": data.user_email
        }
        
        try:
            # Fire and forget (or log error) - don't block response on notification failure ? 
            # User requirement: "notify.server gonna be the last end point"
            requests.post("http://localhost:8089/notify/send", json=notify_payload)
            print(f"Forwarded recommendation for plant {data.plant_name} to Notify Server.")
        except Exception as e:
            print(f"Failed to contact Notify Server: {e}")

        return analysis_result
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

# 2. Leaf Detection Endpoint (REST Fallback)
@app.post("/detect")
async def detect_leaf(file: UploadFile = File(...)):
    if not leaf_detector.is_ready:
        raise HTTPException(status_code=503, detail="Model not loaded or found.")
    
    try:
        contents = await file.read()
        result = leaf_detector.predict(contents)
        return result
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

# 3. Sensor Reading Endpoint
@app.post("/sensors/read")
def read_sensors_endpoint():
    """
    Manually trigger sensor reading from Arduino (COM6).
    Returns the latest sensor data: soil humidity, gas, rain, temperature, air humidity.
    """
    try:
        # Use context manager for automatic connection/disconnection
        with sensor_reader as sensor:
            data = sensor.read_sensors(max_attempts=5)
            
            if data:
                sensor.display_data(data)
                return {
                    "status": "success",
                    "message": "Sensor data retrieved successfully",
                    "data": data
                }
            else:
                raise HTTPException(
                    status_code=503, 
                    detail="Failed to read sensor data. Check Arduino connection on COM6."
                )
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Sensor reading error: {str(e)}")

# Scheduled function for sensor reading
def read_sensors_scheduled():
    """
    Scheduled task to read sensors every 8 hours.
    This function is called automatically by APScheduler.
    """
    print("\n" + "*" * 60)
    print("📡 Reading sensors (scheduled task)...")
    print("*" * 60)
    
    try:
        with sensor_reader as sensor:
            data = sensor.read_sensors(max_attempts=5)
            
            if data:
                sensor.display_data(data)
                print("✓ Scheduled sensor reading completed successfully")
                
                # TODO: Store data in database or send to another service
                # Example: save_to_database(data)
                # Example: send_to_service(data)
            else:
                print("✗ Failed to read sensor data in scheduled task")
    except Exception as e:
        print(f"✗ Error in scheduled sensor reading: {e}")
