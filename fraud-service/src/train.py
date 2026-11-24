import joblib
import numpy as np
from pathlib import Path
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import average_precision_score, confusion_matrix, precision_recall_curve
from sklearn.preprocessing import StandardScaler

from src import split
from src import features
from src.evaluate import pick_threshold, TARGET_PRECISION

MODEL_DIR = Path(__file__).resolve().parents[1] / "models"

def train():
    x_train, x_val, _, y_train, y_val, _ = split.split_data(split.PATH)

    x_train = features.build_features(x_train)
    x_val = features.build_features(x_val)

    scaler = StandardScaler()
    x_train = scaler.fit_transform(x_train)
    x_val = scaler.transform(x_val)

    clf = LogisticRegression(class_weight="balanced", max_iter=1000)
    clf.fit(x_train, y_train)

    val_prob = clf.predict_proba(x_val)[:,1]
    print("Validation PR-AUC:", average_precision_score(y_val, val_prob))
    print("Validation base rate:", y_val.mean())

    threshold, precision, recall = pick_threshold(y_val, val_prob, TARGET_PRECISION)
    print(f"Threshold: {threshold:.4f}, Precision: {precision:.3f}, Recall: {recall:.3f}")

    val_pred = val_prob >= threshold
    print("Validation confusion matrix at the chosen threshold:")
    print(confusion_matrix(y_val, val_pred))

    MODEL_DIR.mkdir(exist_ok=True)
    joblib.dump(
        {
            "model": clf,
            "scaler": scaler,
            "threshold": float(threshold),
            "feature_columns": features.FEATURE_COLUMNS,
        },
        MODEL_DIR / "logistic.joblib",
    )

    return clf, scaler, threshold

def main():
    train()

if __name__ == "__main__":
    main()
