import requests
import json
import os

BASE_URL = "http://127.0.0.1:8000"
TEST_IMAGE = r"C:\Users\marou\OneDrive\Desktop\Project\Data\image\Tomate\Tomato___Leaf_Mold\022a4dfb-84cc-45cb-81c5-a620817e31cb___Crnl_L.Mold 6926.JPG"

def test_home():
    print("Testing Home Endpoint...")
    try:
        resp = requests.get(f"{BASE_URL}/")
        print(f"Status: {resp.status_code}")
        print(f"Response: {resp.json()}")
    except Exception as e:
        print(f"FAILED: {e}")

def test_recommendation():
    print("\nTesting Recommendation Endpoint...")
    payload = {
        "stage": 1,
        "temperature": 35.0,  # Hot
        "humidity": 40.0,     # Dry
        "soil_moisture": 30.0,
        "nitrogen": 140,
        "phosphorus": 70,
        "potassium": 200,
        "ph": 6.5,
        "solar_radiation": 600,
        "wind_speed": 5.0
    }
    try:
        resp = requests.post(f"{BASE_URL}/recommend", json=payload)
        print(f"Status: {resp.status_code}")
        print(f"Response: {json.dumps(resp.json(), indent=2)}")
    except Exception as e:
        print(f"FAILED: {e}")

def test_detection():
    print("\nTesting Leaf Detection Endpoint...")
    if not os.path.exists(TEST_IMAGE):
        print(f"Image not found: {TEST_IMAGE}")
        return

    try:
        with open(TEST_IMAGE, "rb") as f:
            files = {"file": f}
            resp = requests.post(f"{BASE_URL}/detect", files=files)
            print(f"Status: {resp.status_code}")
            print(f"Response: {json.dumps(resp.json(), indent=2)}")
    except Exception as e:
        print(f"FAILED: {e}")

if __name__ == "__main__":
    test_home()
    test_recommendation()
    test_detection()
