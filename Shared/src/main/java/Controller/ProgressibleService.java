package Controller;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyFloatProperty;
import javafx.beans.property.ReadOnlyFloatWrapper;

public abstract class ProgressibleService<T1, T2> {
    private T1 params;
    private boolean daemon = true;
    private final ReadOnlyFloatWrapper progress = new ReadOnlyFloatWrapper(0);

    protected abstract T2 backgroundTask(T1 param);

    protected abstract void finalTask(T2 param);

    public ReadOnlyFloatProperty getProgressProperty() {
        return progress.getReadOnlyProperty();
    }

    protected void setProgress(float progressParams) {
        progress.set(progressParams);
    }

    protected void addProgress(float progressParams) {
        progress.set(progress.floatValue() + progressParams);
    }

    protected  final Thread backGroundThread = new Thread(new Runnable() {
        @Override
        public void run() {

            final T2 param = backgroundTask(params);

            Platform.runLater(() -> finalTask(param));
        }
    });

    public void execute(final T1 params) {
        this.params = params;
        Platform.runLater(() -> {
            backGroundThread.setDaemon(daemon);
            backGroundThread.start();
        });
    }

    public void setDaemon(boolean daemon) {
        this.daemon = daemon;
    }
    public final boolean isInterrupted() {
        return this.backGroundThread.isInterrupted();
    }
    public final boolean isAlive() {
        return this.backGroundThread.isAlive();
    }
}