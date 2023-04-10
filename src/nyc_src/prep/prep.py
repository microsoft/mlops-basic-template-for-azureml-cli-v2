import argparse
import os
from typing import Dict, List, Sequence
import pandas as pd


def main(raw_data: str, prep_data: str):
    print("hello training world...")

    lines = [
        f"Raw data path: {raw_data}",
        f"Data output path: {prep_data}",
    ]

    for line in lines:
        print(line)

    print("mounted_path files: ")
    files = os.listdir(raw_data)
    print(files)

    # Prep the green and yellow taxi data
    df_list: List[pd.DataFrame] = []
    for file in files:
        if file.endswith(".csv"):
            df_list.append(pd.read_csv(os.path.join(raw_data, file)))

    data_prep(files, df_list, prep_data)

def data_prep(filenames: Sequence[str], dataframes: Sequence[pd.DataFrame], destination_path: str):
    # Define useful columns needed for the Azure Machine Learning NYC Taxi tutorial

    useful_columns = [
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
            "vendor"
        ]
    print(f"Useful columns: {useful_columns}")

    # Rename columns as per Azure Machine Learning NYC Taxi tutorial
    column_renames = {
            "vendorID": "vendor",
            "lpepPickupDatetime": "pickup_datetime",
            "lpepDropoffDatetime": "dropoff_datetime",
            "pickupLongitude": "pickup_longitude",
            "pickupLatitude": "pickup_latitude",
            "dropoffLongitude": "dropoff_longitude",
            "dropoffLatitude": "dropoff_latitude",
            "passengerCount": "passengers",
            "tpepPickupDateTime": "pickup_datetime",
            "tpepDropoffDateTime": "dropoff_datetime",
            "storeAndFwdFlag": "store_forward",
            "startLon": "pickup_longitude",
            "startLat": "pickup_latitude",
            "endLon": "dropoff_longitude",
            "endLat": "dropoff_latitude",
            "fareAmount": "cost",
            "tripDistance": "distance",
        }

    cleaned_dataframes: List[pd.DataFrame] = []
    for df in dataframes:
        cleaned_dataframes.append(cleanse_data(df, column_renames, useful_columns))

    # Append yellow data to green data
    combined_df = pd.concat(cleaned_dataframes, ignore_index=True)
    combined_df.reset_index(inplace=True, drop=True)

    # Create files (each cleaned version and the final combined)
    os.makedirs(destination_path, exist_ok=True)
    for i, cleaned_df in enumerate(cleaned_dataframes):
        # remove filename extension
        name_and_extension = filenames[i].split(".", maxsplit=1)
        cleaned_filename = name_and_extension[0] + "_cleaned.csv"
        cleaned_df.to_csv(os.path.join(destination_path, cleaned_filename))

    combined_df.to_csv(os.path.join(destination_path, "merged_data.csv"))

    print("Finished")

def cleanse_data(
        data: pd.DataFrame,
        columns_renames: Dict[str, str],
        useful_columns: List[str]) -> pd.DataFrame:
    """Cleanses the data by removing rows with all null values and renaming columns"""
    new_df = data.dropna(how="all").rename(columns=columns_renames)
    series_with_useful_columns = new_df[useful_columns]

    series_with_useful_columns.reset_index(inplace=True, drop=True)
    return series_with_useful_columns


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--raw_data",
        type=str,
        default="../data/raw_data",
        help="Path to raw data",
    )
    parser.add_argument(
        "--prep_data",
        type=str,
        default="../data/prep_data",
        help="Path to prep data"
    )

    args = parser.parse_args()
    main(args.raw_data, args.prep_data)
