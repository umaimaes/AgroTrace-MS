import serial
import time
from typing import Dict, Optional


class CaptersIOT:
    """
    Class to handle Arduino sensor data reading via serial port.
    Reads environmental data: soil humidity (A4, A2), gas, rain, temperature, and air humidity.
    """
    
    def __init__(self, port: str = "COM6", baudrate: int = 9600, timeout: int = 1):
        """
        Initialize the sensor reader.
        
        Args:
            port: Serial port name (e.g., "COM6" on Windows, "/dev/ttyUSB0" on Linux)
            baudrate: Communication speed (default: 9600)
            timeout: Read timeout in seconds (default: 1)
        """
        self.port = port
        self.baudrate = baudrate
        self.timeout = timeout
        self.ser = None
        self.is_connected = False
    
    def connect(self) -> bool:
        """
        Establish connection with the Arduino device.
        
        Returns:
            bool: True if connection successful, False otherwise
        """
        try:
            self.ser = serial.Serial(self.port, self.baudrate, timeout=self.timeout)
            time.sleep(2)  # Wait for Arduino to initialize
            self.is_connected = True
            print(f"✓ Connected to Arduino on {self.port}")
            return True
        except serial.SerialException as e:
            print(f"✗ Failed to connect to {self.port}: {e}")
            self.is_connected = False
            return False
        except Exception as e:
            print(f"✗ Unexpected error connecting to Arduino: {e}")
            self.is_connected = False
            return False
    
    def disconnect(self):
        """
        Close the serial connection.
        """
        if self.ser and self.ser.is_open:
            self.ser.close()
            self.is_connected = False
            print(f"✓ Disconnected from {self.port}")
    
    def read_single(self) -> Optional[Dict[str, str]]:
        """
        Read a single sensor data line from Arduino.
        Expected format: "soilA4,soilA2,gas,rain,temp,hum"
        
        Returns:
            dict: Sensor data dictionary or None if reading failed
        """
        if not self.is_connected or not self.ser:
            print("✗ Not connected to Arduino. Call connect() first.")
            return None
        
        try:
            # Read one line from serial port
            data = self.ser.readline().decode("utf-8").strip()
            
            if not data:
                return None
            
            # Parse CSV data
            parts = data.split(",")
            
            if len(parts) != 6:
                print(f"⚠ Invalid data format (expected 6 values, got {len(parts)}): {data}")
                return None
            
            soilA4, soilA2, gas, rain, temp, hum = parts
            
            sensor_data = {
                "soil_humidity_A4": soilA4,
                "soil_humidity_A2": soilA2,
                "gas_A5": gas,
                "rain_A3": rain,
                "temperature": temp,
                "air_humidity": hum,
                "timestamp": time.strftime("%Y-%m-%d %H:%M:%S")
            }
            
            return sensor_data
            
        except UnicodeDecodeError as e:
            print(f"✗ Error decoding data: {e}")
            return None
        except Exception as e:
            print(f"✗ Error reading sensor data: {e}")
            return None
    
    def read_sensors(self, max_attempts: int = 5) -> Optional[Dict[str, str]]:
        """
        Read sensor data with multiple attempts to ensure valid data.
        
        Args:
            max_attempts: Maximum number of read attempts (default: 5)
        
        Returns:
            dict: Sensor data dictionary or None if all attempts failed
        """
        if not self.is_connected:
            if not self.connect():
                return None
        
        for attempt in range(max_attempts):
            data = self.read_single()
            if data:
                return data
            time.sleep(0.1)  # Small delay between attempts
        
        print(f"✗ Failed to read valid data after {max_attempts} attempts")
        return None
    
    def display_data(self, data: Dict[str, str]):
        """
        Display sensor data in a formatted way.
        
        Args:
            data: Sensor data dictionary
        """
        if not data:
            print("No data to display")
            return
        
        print("\n" + "=" * 50)
        print(f"Timestamp       : {data.get('timestamp', 'N/A')}")
        print(f"Humidité sol A4 : {data.get('soil_humidity_A4', 'N/A')}")
        print(f"Humidité sol A2 : {data.get('soil_humidity_A2', 'N/A')}")
        print(f"Gaz (A5)        : {data.get('gas_A5', 'N/A')}")
        print(f"Pluie (A3)      : {data.get('rain_A3', 'N/A')}")
        print(f"Température     : {data.get('temperature', 'N/A')} °C")
        print(f"Humidité air    : {data.get('air_humidity', 'N/A')} %")
        print("=" * 50)
    
    def read_continuous(self, callback=None):
        """
        Continuously read sensor data (blocking operation).
        
        Args:
            callback: Optional function to call with each reading
        """
        if not self.is_connected:
            if not self.connect():
                return
        
        print("Starting continuous sensor reading... (Ctrl+C to stop)")
        
        try:
            while True:
                data = self.read_single()
                if data:
                    self.display_data(data)
                    if callback:
                        callback(data)
        except KeyboardInterrupt:
            print("\n\n✓ Stopped by user")
        finally:
            self.disconnect()
    
    def __enter__(self):
        """Context manager entry."""
        self.connect()
        return self
    
    def __exit__(self, exc_type, exc_val, exc_tb):
        """Context manager exit."""
        self.disconnect()


# Example usage
if __name__ == "__main__":
    # Method 1: Using context manager (recommended)
    with CaptersIOT(port="COM6") as sensor:
        data = sensor.read_sensors()
        sensor.display_data(data)
    
    # Method 2: Manual connection
    # sensor = CaptersIOT(port="COM6")
    # sensor.connect()
    # data = sensor.read_sensors()
    # sensor.display_data(data)
    # sensor.disconnect()
    
    # Method 3: Continuous reading
    # sensor = CaptersIOT(port="COM6")
    # sensor.read_continuous()
