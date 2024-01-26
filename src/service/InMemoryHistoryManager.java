package service;

import model.Task;

import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    public final static int MAX_HISTORY_SIZE = 10;
    private final List<Task> history = new ArrayList<>(MAX_HISTORY_SIZE);

    @Override
    public void add(Task task) {
        if (task == null) {
            return;
        }
        if (isFull()) {
            history.removeFirst();
        }
        history.add(task);
    }

    @Override
    public List<Task> getHistory() {
        return history;
    }

    private boolean isFull() {
        return history.size() == MAX_HISTORY_SIZE;
    }
}
