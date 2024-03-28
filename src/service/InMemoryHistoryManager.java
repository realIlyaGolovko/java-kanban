package service;

import model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {
    private final Map<Integer, Node> history = new HashMap<>();

    private Node first;
    private Node last;

    @Override
    public void add(Task task) {
        if (task == null) {
            return;
        }
        Node node = history.get(task.getId());
        removeNode(node);
        linkLast(task);
    }

    @Override
    public void remove(int id) {
        Node node = history.get(id);
        removeNode(node);

    }

    @Override
    public List<Task> getHistory() {
        List<Task> result = new ArrayList<>();
        Node current = first;
        while (current != null) {
            result.add(current.item);
            current = current.next;
        }
        return result;
    }

    private void linkLast(Task task) {
        final Node l = last;
        final Node newNode = new Node(l, task, null);
        last = newNode;
        if (l == null) {
            first = newNode;
        } else {
            l.next = newNode;
        }
        history.put(task.getId(), newNode);
    }

    private void removeNode(Node x) {
        if (x == null) {
            return;
        }
        final Node next = x.next;
        final Node prev = x.prev;

        if (prev == null) {
            first = next;
        } else {
            prev.next = next;
        }

        if (next == null) {
            last = prev;
        } else {
            next.prev = prev;
        }
        history.remove(x.item.getId());
    }

    private static class Node {
        private final Task item;
        private Node next;
        private Node prev;

        private Node(Node prev, Task item, Node next) {
            this.item = item;
            this.next = next;
            this.prev = prev;
        }

    }

}
