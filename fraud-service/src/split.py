from pathlib import Path

from sklearn.model_selection import train_test_split
import pandas as pd

PATH = Path(__file__).resolve().parents[1]/"data"/"transactions.csv"

def split_data(path):
    df = pd.read_csv(path)

    df["occurredAt"] = pd.to_datetime(df["occurredAt"], utc=True)
    df = df.sort_values(by="occurredAt").reset_index(drop=True)

    y = df["isFraud"]
    x = df.drop(columns=["isFraud", "transactionId", "merchantId"])

    x_train, x_temp, y_train, y_temp = train_test_split(
        x, y, test_size=0.30, shuffle=False)

    x_val, x_test, y_val, y_test = train_test_split(
        x_temp, y_temp, test_size=0.50, shuffle=False)

    assert x_train["occurredAt"].max() <= x_val["occurredAt"].min()
    assert x_val["occurredAt"].max() <= x_test["occurredAt"].min()

    return x_train, x_val, x_test, y_train, y_val, y_test

def main():
    x_train, x_val, x_test, y_train, y_val, y_test = split_data(PATH)
    print(f"y_train mean: {y_train.mean()}")
    print(f"y_val mean: {y_val.mean()}")
    print(f"y_test mean: {y_test.mean()}")
    print(f"y_val sum: {y_val.sum()}")
    print(f"y_test sum: {y_test.sum()}")

if __name__ == '__main__':
    main()