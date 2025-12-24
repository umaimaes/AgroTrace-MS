import asyncio
import json
import os
from aiokafka import AIOKafkaConsumer, AIOKafkaProducer
from leaf_detection import LeafDiseaseDetector

class KafkaService:
    def __init__(self, detector: LeafDiseaseDetector, rec_engine, bootstrap_servers='localhost:9092'):
        self.detector = detector
        self.rec_engine = rec_engine
        self.bootstrap_servers = bootstrap_servers
        self.consumer = None
        self.producer = None
        self.running = False
        
        # Topics
        self.leaf_req_topic = "leaf.detection.request"
        self.leaf_res_topic = "leaf.detection.response"
        
        self.rec_req_topic = "recommendation.request"
        self.rec_res_topic = "recommendation.response"

    async def start(self):
        self.running = True
        
        # Initialize Producer
        self.producer = AIOKafkaProducer(bootstrap_servers=self.bootstrap_servers)
        await self.producer.start()
        
        # Initialize Consumer (Listening to multiple topics)
        self.consumer = AIOKafkaConsumer(
            self.leaf_req_topic,
            self.rec_req_topic,
            bootstrap_servers=self.bootstrap_servers,
            group_id="ai_service_group"
        )
        await self.consumer.start()
        
        print(f"Kafka Service Started. Listening on {self.leaf_req_topic} and {self.rec_req_topic}...")
        
        # Start Consumption Loop
        asyncio.create_task(self.consume_loop())

    async def stop(self):
        self.running = False
        if self.consumer:
            await self.consumer.stop()
        if self.producer:
            await self.producer.stop()
        print("Kafka Service Stopped.")

    async def consume_loop(self):
        try:
            async for msg in self.consumer:
                if not self.running:
                    break
                
                try:
                    topic = msg.topic
                    payload = json.loads(msg.value.decode('utf-8'))
                    print(f"Received Request on {topic}: {payload}")
                    
                    if topic == self.leaf_req_topic:
                        await self.handle_leaf_request(payload)
                    elif topic == self.rec_req_topic:
                        await self.handle_rec_request(payload)
                        
                except Exception as e:
                    print(f"Error processing message: {e}")
                    
        except Exception as e:
            print(f"Kafka Consumer Crash: {e}")

    async def handle_leaf_request(self, payload):
        req_id = payload.get("id")
        image_path = payload.get("image_path")
        
        result = {"id": req_id, "type": "leaf_detection", "status": "error", "message": "Invalid Input"}
        
        if image_path and os.path.exists(image_path):
            try:
                with open(image_path, "rb") as f:
                    img_bytes = f.read()
                prediction = self.detector.predict(img_bytes)
                result = {
                    "id": req_id,
                    "type": "leaf_detection",
                    "status": "success",
                    "analysis": prediction
                }
            except Exception as e:
                result["message"] = str(e)
        else:
            result["message"] = f"File not found: {image_path}"
        
        await self.send_response(self.leaf_res_topic, result)

    async def handle_rec_request(self, payload):
        req_id = payload.get("id")
        # Payload should contain the climatic conditions directly or nested
        # Format expected: {"id": "...", "data": { "stage": 1, "temperature": 25... }}
        data = payload.get("data")
        
        result = {"id": req_id, "type": "recommendation", "status": "error", "message": "Invalid Input"}
        
        if data:
            try:
                # Map keys if necessary, strictly ensuring we match what the engine expects
                # Using a safe helper to extract known keys
                mapped_data = {
                    'Crop Coefficient stage': data.get('stage'),
                    'Temperature [_ C]': data.get('temperature'),
                    'Humidity [%]': data.get('humidity'),
                    'Soil moisture': data.get('soil_moisture'),
                    'Nitrogen [mg/kg]': data.get('nitrogen', 0),
                    'Phosphorus [mg/kg]': data.get('phosphorus', 0),
                    'Potassium': data.get('potassium', 0),
                    'pH': data.get('ph', 6.5),
                    'Solar Radiation ghi': data.get('solar_radiation', 0),
                    'Wind Speed': data.get('wind_speed', 0)
                }
                
                analysis = self.rec_engine.analyze(mapped_data)
                result = {
                    "id": req_id,
                    "type": "recommendation",
                    "status": "success",
                    "result": analysis
                }
            except Exception as e:
                result["message"] = str(e)
        else:
            result["message"] = "Missing 'data' field in payload"
            
        await self.send_response(self.rec_res_topic, result)

    async def send_response(self, topic, data):
        if self.producer:
            await self.producer.send_and_wait(
                topic, 
                json.dumps(data).encode('utf-8')
            )
            print(f"Sent Response to {topic}: {data.get('status')}")
