import serial

PORT = "COM6"   # adapte si besoin
BAUDRATE = 9600

ser = serial.Serial(PORT, BAUDRATE, timeout=1)

print("Lecture continue des capteurs...\n")

while True:
    data = ser.readline().decode("utf-8").strip()

    if not data:
        continue

    parts = data.split(",")

    if len(parts) != 6:
        continue

    soilA4, soilA2, gas, rain, temp, hum = parts

    print(f"Humidité sol A4 : {soilA4}")
    print(f"Humidité sol A2 : {soilA2}")
    print(f"Gaz (A5)       : {gas}")
    print(f"Pluie (A3)     : {rain}")
    print(f"Température    : {temp} °C")
    print(f"Humidité air   : {hum} %")
    print("-" * 40)