package service;

import model.Epic;
import model.SubTask;
import model.Task;

import java.util.HashMap;

class TaskKeeper {
    private final HashMap<Integer, Epic> epicStorage = new HashMap<>();
    private final HashMap<Integer, SubTask> subTaskStorage = new HashMap<>();
    private final HashMap<Integer, Task> taskStorage = new HashMap<>();

    public void putTask(Task task) {
        taskStorage.put(task.getId(), task);
    }

    public void putSubTask(SubTask subTask) {
        subTaskStorage.put(subTask.getId(), subTask);
    }

    public void putEpic(Epic epic) {
        epicStorage.put(epic.getId(), epic);
    }


    public HashMap<Integer, Epic> getEpicStorage() {
        return epicStorage;
    }

    public HashMap<Integer, SubTask> getSubTaskStorage() {
        return subTaskStorage;
    }

    public HashMap<Integer, Task> getTaskStorage() {
        return taskStorage;
    }

    public void clearTasks() {
        taskStorage.clear();
    }

    public void clearSubTasks() {
        subTaskStorage.clear();
    }

    public void clearEpics() {
        epicStorage.clear();
    }

    public void deleteTask(int taskId) {
        taskStorage.remove(taskId);
    }

    public void deleteSubTask(int subTaskId) {
        subTaskStorage.remove(subTaskId);
    }

    public void deleteEpics(int epicId) {
        epicStorage.remove(epicId);
    }

    @Override
    public String toString() {
        return "TaskKeeper{" +
                "epicStorage=" + epicStorage.size() +
                ", subTaskStorage=" + subTaskStorage.size() +
                ", taskStorage=" + taskStorage.size() +
                '}';
    }
}