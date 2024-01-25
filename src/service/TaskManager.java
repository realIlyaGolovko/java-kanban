package service;

import model.Epic;
import model.SubTask;
import model.Task;

import java.util.List;

public interface TaskManager {
    int getNextId();

    int createTask(Task task);

    int createSubTask(SubTask subTask);

    int createEpic(Epic epic);

    List<Task> getTasks();

    List<SubTask> getSubTasks();

    List<Epic> getEpics();

    Task getTask(int taskId);

    SubTask getSubTask(int subTaskId);

    Epic getEpic(int epicId);

    List<SubTask> getSubtasksOfEpic(int epicId);

    List<Task> getHistory();

    void deleteTasks();

    void deleteSubTasks();

    void deleteEpics();

    void deleteTask(int taskId);

    void deleteSubTask(int subTaskId);

    void deleteEpic(int epicId);

    void updateTask(Task task);

    void updateSubTask(SubTask subTask);

    void updateEpic(Epic epic);
}
