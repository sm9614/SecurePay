import joblib
from pathlib import Path

from xgboost import XGBClassifier
from sklearn.metrics import average_precision_score, confusion_matrix, precision_recall_curve

from src import split
from src import features
from src.evaluate import pick_threshold

MODEL_DIR = Path(__file__).resolve().parents[1]/ "models"
TARGET_PRECISION = 0.70

def train_boosted():
    x_train, x_val, _, y_train, y_val, _ = split.split_data(split.PATH)

    x_train = features.build_features(x_train)
    x_val = features.build_features(x_val)

    negative_count = (y_train == 0).sum()
    positive_count = (y_train == 1).sum()

    scale_pos_weight = negative_count / positive_count

    print("Training samples:", len(y_train))
    print("Validation samples:", len(y_val))
    print("Positive samples:", positive_count)
    print("Negative samples:", negative_count)
    print("Scale pos weight:", scale_pos_weight)

    clf = XGBClassifier(
        n_estimators=500,
        max_depth=5,
        learning_rate=0.05,
        subsample=0.8,
        colsample_bytree=0.8,
        min_child_weight=3,
        gamma=0,
        reg_alpha=0.1,
        reg_lambda=1,
        scale_pos_weight=scale_pos_weight,
        objective="binary:logistic",
        eval_metric="aucpr",
        random_state=42,
        n_jobs=-1,
        tree_method="hist"
    )

    clf.fit(x_train,
            y_train,
            eval_set=[(x_val, y_val)],
            verbose=False
    )

    val_prob = clf.predict_proba(x_val)[:,1]
    print("Validation PR-AUC:", average_precision_score(y_val, val_prob))
    print("Validation base rate:", y_val.mean())

    threshold, precision, recall = pick_threshold(y_val, val_prob, TARGET_PRECISION)

    print(f"Threshold: {threshold:.4f}, Precision: {precision:.3f}, Recall: {recall:.3f}")

    val_pred = val_prob >= threshold

    print("Validation confusion matrix:")
    print(confusion_matrix(y_val, val_pred))

    MODEL_DIR.mkdir(exist_ok=True)
    joblib.dump(
        {
            "model": clf,
            "threshold": float(threshold),
            "feature_columns": features.FEATURE_COLUMNS,
        },
        MODEL_DIR / "xgboost.joblib",
        )

    return clf, threshold

def main():
    train_boosted()

if __name__ == "__main__":
    main()