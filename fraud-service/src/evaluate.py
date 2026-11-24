import numpy as np
from sklearn.metrics import precision_recall_curve

def pick_threshold(y_true, y_prob, target_precision):
    precision, recall, thresholds = precision_recall_curve(y_true, y_prob)
    ok = precision[:-1] >= target_precision
    if not ok.any():
        raise ValueError(f"No threshold reaches precision {target_precision}")
    best = np.argmax(np.where(ok, recall[:-1], -1))
    return thresholds[best], precision[best], recall[best]