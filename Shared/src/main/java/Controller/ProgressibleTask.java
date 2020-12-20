package Controller;

import javafx.application.Platform;

public abstract class ProgressibleTask<T1, T2> implements Progressible {
    private T1 params;
    private boolean daemon = true;
    private volatile boolean ranOnce = false;

    protected  final Thread backGroundThread = new Thread(new Runnable() {
        @Override
        public void run() {

            final T2 param = backgroundTask(params);

            Platform.runLater(() -> finalTask(param));
        }
    });

    protected abstract T2 backgroundTask(T1 param);
    protected abstract void finalTask(T2 param);
    public boolean hasRan() {return ranOnce;}

    public void execute(final T1 params) {
        ranOnce = true;
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