import pandas as pd
import numpy as np
import os
from typing import List, Dict, Optional, Any

# --- CUSTOM CLASSIFIER ---
class SimpleKNNClassifier:
    """
    Custom KNN Classifier using Numpy to avoid Scikit-Learn dependencies.
    """
    def __init__(self, k=5):
        self.k = k
        self.X_train = None
        self.y_train = None
        self.min_vals = None
        self.max_vals = None
    
    def fit(self, X, y):
        X_arr = np.array(X, dtype=float)
        self.y_train = np.array(y, dtype=int)
        self.min_vals = X_arr.min(axis=0)
        self.max_vals = X_arr.max(axis=0)
        # Avoid division by zero
        diff = self.max_vals - self.min_vals
        diff[diff == 0] = 1.0
        self.X_train = (X_arr - self.min_vals) / diff
        
    def predict_one(self, x_row):
        diff = self.max_vals - self.min_vals
        diff[diff == 0] = 1.0
        x_scaled = (x_row - self.min_vals) / diff
        dists = np.sqrt(np.sum((self.X_train - x_scaled)**2, axis=1))
        # Sort and take top k
        k_indices = np.argsort(dists)[:self.k]
        k_labels = self.y_train[k_indices]
        # Vote
        prediction = 1 if np.sum(k_labels) > (self.k / 2) else 0
        proba = np.sum(k_labels) / self.k
        return prediction, proba

# --- MAIN ENGINE ---
class RecommendationEngine:
    def __init__(self, base_dir: str):
        self.project_dir = base_dir # AiService is base_dir
        self.augmented_file = os.path.join(self.project_dir, "data", "augmented_dataset.csv")
        self.utility_file = os.path.join(self.project_dir, "data", "utility_matrix.csv")
        
        self.model = SimpleKNNClassifier(k=5)
        self.utility_matrix = pd.DataFrame()
        self.is_ready = False
        
        self.features_list = [
            'Crop Coefficient stage', 
            'Temperature [_ C]', 'Humidity [%]', 'Soil moisture', 
            'Nitrogen [mg/kg]', 'Phosphorus [mg/kg]', 'Potassium',
            'pH', 'Solar Radiation ghi', 'Wind Speed'
        ]

    def load_and_train(self):
        """
        Loads data and trains the KNN model.
        """
        # 1. Load Utility Matrix
        if os.path.exists(self.utility_file):
            print(f"Loading Utility Matrix from {self.utility_file}")
            df_u = pd.read_csv(self.utility_file)
            if 'Crop Coefficient stage' in df_u.columns:
                df_u.set_index('Crop Coefficient stage', inplace=True)
            self.utility_matrix = df_u
        else:
            print(f"WARNING: Utility Matrix not found at {self.utility_file}")

        # 2. Train Model
        if os.path.exists(self.augmented_file):
            print(f"Loading Training Data from {self.augmented_file}")
            df = pd.read_csv(self.augmented_file)
            
            # Verify columns
            available_features = [f for f in self.features_list if f in df.columns]
            
            X = df[available_features]
            y = df['Target']
            
            print(f"Training KNN Model on {len(df)} samples...")
            self.model.fit(X, y)
            self.is_ready = True
            print("Model Trained Successfully.")
        else:
            print(f"ERROR: Augmented Dataset not found at {self.augmented_file}")
            
    def analyze(self, input_data: Dict[str, float]) -> Dict[str, Any]:
        """
        Main logic: Predicts suitability + Generates Recommendations.
        """
        if not self.is_ready:
            raise Exception("Model is not trained. Check dataset paths.")

        # 1. Map Input to Dictionary for features
        # Note: Input keys must match the model's feature order
        values = []
        for f in self.features_list:
            values.append(input_data.get(f, 0.0))
        
        input_arr = np.array(values)
        
        # 2. Predict
        prediction, proba = self.model.predict_one(input_arr)
        
        status = "SUITABLE" if prediction == 1 else "UNSUITABLE"
        final_conf = proba if prediction == 1 else (1 - proba)
        
        # 3. Recommend
        recommendations = []
        stage = input_data.get('Crop Coefficient stage')
        
        if stage is not None and stage in self.utility_matrix.index:
            ideals = self.utility_matrix.loc[stage]
            
            for name, current_val in input_data.items():
                if name in ideals:
                    ideal_val = ideals[name]
                    diff = ideal_val - current_val
                    # 10% tolerance rule
                    if abs(diff) > (ideal_val * 0.1):
                        if diff > 0:
                            recommendations.append(f"INCREASE {name} by {diff:.2f} (Ideal: {ideal_val:.2f})")
                        else:
                            recommendations.append(f"DECREASE {name} by {abs(diff):.2f} (Ideal: {ideal_val:.2f})")
        
        if not recommendations and prediction == 0:
             recommendations.append("Condition unsuitable but no single factor has huge deviation. Check all parameters.")
        if not recommendations and prediction == 1:
            recommendations.append("Conditions are optimal.")
            
        return {
            "prediction": status,
            "confidence": round(final_conf * 100, 2),
            "is_suitable": bool(prediction == 1),
            "recommendations": recommendations
        }
