package Controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;

public class BackgroundThreader extends Thread {
    ObservableList<Thread> threads = FXCollections.observableArrayList();
    ObservableList<Thread> threadsReadOnly = FXCollections.unmodifiableObservableList(threads);
    int indexcount = -1;

    public BackgroundThreader () {
    }

    /**
     * Adds a thread to the latest index.
     * @param runnable The runnable task/service to spin off as a thread.
     * @return the index of the runnable in the list.
     */
    public int addThread(Runnable runnable) {
        indexcount++;
        threads.add(indexcount, new Thread(runnable));
        return indexcount;
    }

    public int removeThread(Runnable runnable) {
        indexcount++;
        threads.remove(indexcount);
        return indexcount;
    }

    public ObservableList<Thread> getThreads() {
        return threadsReadOnly;
    }

    public int serviceCount() {
        return threads.size();
    }

    public boolean removeAll() {
        return threads.removeAll();
    }

    public Boolean startThread(int index) {
        if (threads.get(index).getState().equals(State.NEW)) {
            threads.get(index).start();
            return true;
        } else {
            return false;
        }
    }

    public State getState(int index) {
        return threads.get(index).getState();
    }
}



