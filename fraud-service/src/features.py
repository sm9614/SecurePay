import pandas as pd
import numpy as np

KNOWN_CURRENCIES = ["USD", "EUR", "GBP", "CAD"]

FEATURE_COLUMNS = [
                      "logAmount",
                      "logAmountRatio",
                      "velocityShare5min",
                      "velocityCount5min",
                      "velocityCount1hr",
                      "priorDeclineCount24h",
                      "priorRefundRate",
                      "merchantTransactionCountToday",
                      "isNewMerchant",
                  ] + [f"currency_{c}" for c in KNOWN_CURRENCIES]


def build_features(df):
    df = df.copy()
    amount_ratio = df["amountMinorUnits"] / df["merchantAverageAmountMinorUnits"]
    df["logAmount"] = np.log1p(df["amountMinorUnits"])
    df["logAmountRatio"] = np.log1p(amount_ratio)

    df["velocityShare5min"] = df["velocityCount5min"] / df["velocityCount1hr"].clip(lower=1)

    df["isNewMerchant"] = df["isNewMerchant"].astype(int)

    known = df["currency"].where(df["currency"].isin(KNOWN_CURRENCIES))
    currency = known.astype(pd.CategoricalDtype(categories=KNOWN_CURRENCIES))
    dummies = pd.get_dummies(currency, prefix="currency", dtype=int)
    df = pd.concat([df, dummies], axis=1)

    return df[FEATURE_COLUMNS]


def main():
    row = pd.DataFrame([{
        "amountMinorUnits": 5000, "merchantAverageAmountMinorUnits": 12000,
        "velocityCount5min": 0, "velocityCount1hr": 0,
        "priorDeclineCount24h": 1, "priorRefundRate": 0.05,
        "merchantTransactionCountToday": 10, "isNewMerchant": False,
        "currency": "JPY",
    }])
    out = build_features(row)
    print(out.T)
    assert list(out.columns) == FEATURE_COLUMNS
    assert not out.isna().any().any()

if __name__ == "__main__":
    main()
