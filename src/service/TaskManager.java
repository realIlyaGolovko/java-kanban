package service;

import model.Epic;
import model.SubTask;
import model.Task;
import model.TaskStatus;

import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {

    private final TaskKeeper taskKeeper;

    public TaskManager(TaskKeeper taskKeeper) {
        this.taskKeeper = taskKeeper;
    }

    public void createTask(Task task) {
        if (!isExistingTask(task)) {
            task.setId(TaskSequence.getNextId());
            taskKeeper.putTask(task);
        }
    }

    public void createSubTask(SubTask subTask) {
        if (!isExistingSubTask(subTask) && isExistingParentEpic(subTask)) {
            subTask.setId(TaskSequence.getNextId());
            taskKeeper.putSubTask(subTask);
        }
    }

    public void createEpic(Epic epic) {
        if (!isExistingEpic(epic)) {
            epic.setId(TaskSequence.getNextId());
            taskKeeper.putEpic(epic);
        }
    }


    public ArrayList<Task> getTasks() {
        return new ArrayList<>(taskKeeper.getTaskStorage().values());
    }

    public ArrayList<SubTask> getSubTasks() {
        return new ArrayList<>(taskKeeper.getSubTaskStorage().values());
    }

    public ArrayList<Epic> getEpics() {
        return new ArrayList<>(taskKeeper.getEpicStorage().values());
    }

    public Task getTask(int taskId) {
        return taskKeeper.getTaskStorage().get(taskId);

    }

    public SubTask getSubTask(int subTaskId) {
        return taskKeeper.getSubTaskStorage().get(subTaskId);
    }

    public Epic getEpic(int epicId) {
        return taskKeeper.getEpicStorage().get(epicId);
    }

    public void deleteTasks() {
        taskKeeper.clearTasks();
    }

    public void deleteSubTasks() {
        taskKeeper.clearSubTasks();
        ArrayList<Epic> epics = getEpics();
        for (Epic epic : epics) {
            updateEpic(epic.getId());
        }
    }

    public void deleteEpics() {
        taskKeeper.clearEpics();
        taskKeeper.clearSubTasks();
    }

    public void deleteTask(int taskId) {
        taskKeeper.deleteTask(taskId);
    }

    public void deleteSubTask(int subTaskId) {
        SubTask subTask = getSubTask(subTaskId);
        taskKeeper.deleteSubTask(subTaskId);
        updateEpic(subTask.getEpicId());
    }

    public void deleteEpic(int epicId) {
        ArrayList<SubTask> subTasksOfEpic = getSubtasksOfEpic(epicId);
        for (SubTask subTask : subTasksOfEpic) {
            taskKeeper.deleteSubTask(subTask.getId());
        }
        taskKeeper.deleteEpics(epicId);
    }

    public void updateTask(Task task) {
        if (isExistingTask(task)) {
            taskKeeper.putTask(task);
        }
    }

    public void updateSubTask(SubTask subTask) {
        if (isExistingSubTask(subTask) && isExistingParentEpic(subTask)) {
            taskKeeper.putSubTask(subTask);
            updateEpic(subTask.getEpicId());
        }
    }

    private void updateEpic(int epicId) {
        Epic epic = getEpic(epicId);
        if (epic != null) {
            ArrayList<SubTask> subTasks = getSubtasksOfEpic(epicId);
            int countOfSubTasks = subTasks.size();
            if (subTasks.isEmpty()) {
                epic.setStatus(TaskStatus.NEW);
            } else {
                int counterOfNewSubTasks = 0;
                int counterOfDoneSubTasks = 0;
                for (SubTask subTask : subTasks) {
                    TaskStatus status = subTask.getStatus();
                    if (status == TaskStatus.NEW) {
                        counterOfNewSubTasks++;
                    } else if (status == TaskStatus.DONE) {
                        counterOfDoneSubTasks++;
                    }
                }
                if (counterOfNewSubTasks == countOfSubTasks) {
                    epic.setStatus(TaskStatus.NEW);
                } else if (counterOfDoneSubTasks == countOfSubTasks) {
                    epic.setStatus(TaskStatus.DONE);
                } else {
                    epic.setStatus(TaskStatus.IN_PROGRESS);
                }
            }
        }
    }

    public ArrayList<SubTask> getSubtasksOfEpic(int epicId) {
        HashMap<Integer, SubTask> subTasks = taskKeeper.getSubTaskStorage();
        ArrayList<SubTask> result = new ArrayList<>();
        for (SubTask subTask : subTasks.values()) {
            if (subTask.getEpicId() == epicId) {
                result.add(subTask);
            }
        }
        return result;
    }

    private boolean isExistingTask(Task task) {
        return getTask(task.getId()) != null;
    }

    private boolean isExistingSubTask(SubTask subTask) {
        return (getSubTask(subTask.getId()) != null);
    }

    private boolean isExistingEpic(Epic epic) {
        return getEpic(epic.getId()) != null;
    }

    private boolean isExistingParentEpic(SubTask subTask) {
        return getEpic(subTask.getEpicId()) != null;
    }

    @Override
    public String toString() {
        return "TaskManager{" +
                "taskKeeper=" + taskKeeper +
                '}';
    }
}
