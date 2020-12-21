package Controller;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;

public interface Progressible {
    final ReadOnlyDoubleWrapper progress = new ReadOnlyDoubleWrapper(0);

    default void setProgress(double progressParams) {
        progress.set(progressParams);
    }

    default void addProgress(double progressParams) {
        progress.set(progress.getValue() + progressParams);
    }

    default double getProgress() { return progress.getValue(); }

    default ReadOnlyDoubleProperty getProgressProperty() { return progress.getReadOnlyProperty(); }
}