
import os
import pandas as pd
from pathlib import Path
from src.nyc_src.prep.prep import main

TEST_DATA_BASE_PATH = os.path.join(
    os.path.dirname(os.path.abspath(__file__)), "test_data"
)

def test_prep_creates_single_file(tmp_path: Path):
    """Validates that the preparation will create a single file with expected columns"""
    # ARRANGE

    # ACT
    main(TEST_DATA_BASE_PATH, str(tmp_path))

    # ASSERT
    assert os.path.isfile(os.path.join(tmp_path, "file1_cleaned.csv"))
    assert os.path.isfile(os.path.join(tmp_path, "file2_cleaned.csv"))
    expected_merged_csv_file = os.path.join(tmp_path, "merged_data.csv")
    assert os.path.isfile(expected_merged_csv_file)
    actual_df = pd.read_csv(expected_merged_csv_file)
    expected_columns = [
            "cost",
            "distance",
            "dropoff_datetime",
            "dropoff_latitude",
            "dropoff_longitude",
            "passengers",
            "pickup_datetime",
            "pickup_latitude",
            "pickup_longitude",
            "store_forward",
            "vendor"]
    actual_columns = list(actual_df.columns)
    assert all(item in actual_columns for item in expected_columns)

