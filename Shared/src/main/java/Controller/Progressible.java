package Controller;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;

public interface Progressible {
    ReadOnlyDoubleWrapper progress = new ReadOnlyDoubleWrapper(0);
    ReadOnlyStringWrapper state = new ReadOnlyStringWrapper("Initialising");

    default void setProgress(double progressParams) {
        progress.set(progressParams);
    }
    default void addProgress(double progressParams) {
        progress.set(progress.getValue() + progressParams);
    }
    default double getProgress() { return progress.getValue(); }
    default ReadOnlyDoubleProperty getProgressProperty() { return progress.getReadOnlyProperty(); }

    default void setProgressState(String newState) {state.set(newState);}
    default double getProgressState() { return progress.getValue(); }
    default ReadOnlyStringProperty getStateProperty() { return state.getReadOnlyProperty(); }
}