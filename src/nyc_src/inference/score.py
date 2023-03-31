import argparse
import pandas as pd
import os
from pathlib import Path
from sklearn.linear_model import LinearRegression
import pickle
from sklearn.metrics import mean_squared_error, r2_score
import mlflow
import joblib
import logging
import numpy
import json

def init():
    global model
    print("hello scoring world...")
    model_path = os.path.join(
        os.getenv("AZUREML_MODEL_DIR")
    )
    os.listdir(model_path)

    model = joblib.load(f"{model_path}/model/model.pkl")
    logging.info("Init complete")



# Print the results of scoring the predictions against actual values in the test data

def run(raw_data):
    logging.info("model 1: request received")
    data = json.loads(raw_data)["data"]
    data = numpy.array(data)
    result = model.predict(data)
    logging.info("Request processed")
    return result.tolist()
