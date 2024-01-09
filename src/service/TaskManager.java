package service;

import model.Epic;
import model.SubTask;
import model.Task;
import model.TaskStatus;

import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {

    private final HashMap<Integer, Epic> epicStorage = new HashMap<>();
    private final HashMap<Integer, SubTask> subTaskStorage = new HashMap<>();
    private final HashMap<Integer, Task> taskStorage = new HashMap<>();
    private int id;

    public int getNextId() {
        id++;
        return id;
    }

    public int getCurrentId() {
        return id;
    }

    public void createTask(Task task) {
        task.setId(getNextId());
        taskStorage.put(task.getId(), task);
    }

    public void createSubTask(SubTask subTask) {
        Epic epic = epicStorage.get(subTask.getEpicId());
        if (epic != null) {
            int newSubtaskId = getNextId();
            subTask.setId(newSubtaskId);
            epic.addSubTaskId(newSubtaskId);
            subTaskStorage.put(newSubtaskId, subTask);
        }
    }

    public void createEpic(Epic epic) {
        epic.setId(getNextId());
        epicStorage.put(epic.getId(), epic);
    }


    public ArrayList<Task> getTasks() {
        return new ArrayList<>(taskStorage.values());
    }

    public ArrayList<SubTask> getSubTasks() {
        return new ArrayList<>(subTaskStorage.values());
    }

    public ArrayList<Epic> getEpics() {
        return new ArrayList<>(epicStorage.values());
    }

    public Task getTask(int taskId) {
        return taskStorage.get(taskId);
    }

    public SubTask getSubTask(int subTaskId) {
        return subTaskStorage.get(subTaskId);
    }

    public Epic getEpic(int epicId) {
        return epicStorage.get(epicId);
    }

    public void deleteTasks() {
        taskStorage.clear();
    }

    public void deleteSubTasks() {
        subTaskStorage.clear();
        if (!epicStorage.isEmpty()) {
            for (Epic epic : epicStorage.values()) {
                epic.cleanSubTaskIds();
                updateEpicStatus(epic.getId());
            }
        }
    }

    public void deleteEpics() {
        epicStorage.clear();
        subTaskStorage.clear();
    }

    public void deleteTask(int taskId) {
        taskStorage.remove(taskId);
    }

    public void deleteSubTask(int subTaskId) {
        SubTask subTask = subTaskStorage.get(subTaskId);
        if (subTask != null) {
            Epic epic = epicStorage.get(subTask.getEpicId());
            if (epic != null) {
                epic.removeSubTask(subTaskId);
                subTaskStorage.remove(subTaskId);
                updateEpicStatus(epic.getId());
            }
        }
    }

    public void deleteEpic(int epicId) {
        Epic epic = epicStorage.get(epicId);
        if (epic != null) {
            ArrayList<Integer> subTasksOfEpic = epic.getSubTaskIds();
            if (!subTasksOfEpic.isEmpty()) {
                for (Integer subTaskId : subTasksOfEpic) {
                    subTaskStorage.remove(subTaskId);
                }
                epicStorage.remove(epicId);
            }
        }
    }

    public void updateTask(Task task) {
        if (isExistingTask(task)) {
            taskStorage.put(task.getId(), task);
        }
    }

    public void updateSubTask(SubTask subTask) {
        if (isExistingSubTask(subTask) && isExistingParentEpic(subTask)) {
            subTaskStorage.put(subTask.getId(), subTask);
            updateEpicStatus(subTask.getEpicId());
        }
    }

    public void updateEpic(Epic epic) {
        Epic savedEpic = epicStorage.get(epic.getId());
        if (savedEpic != null) {
            savedEpic.setName(epic.getName());
            savedEpic.setDescription(epic.getDescription());
        }
    }

    public ArrayList<Integer> getSubtasksOfEpic(int epicId) {
        ArrayList<Integer> result = new ArrayList<>();
        Epic epic = epicStorage.get(epicId);
        if (epic != null) {
            result.addAll(epic.getSubTaskIds());
        }
        return result;
    }

    private void updateEpicStatus(int epicId) {
        Epic epic = epicStorage.get(epicId);
        if (epic != null) {
            ArrayList<Integer> subTaskIds = epic.getSubTaskIds();
            epic.setStatus(recalculateEpicStatus(subTaskIds));
        }
    }

    private TaskStatus recalculateEpicStatus(ArrayList<Integer> subTaskIds) {
        TaskStatus result;
        int countOfSubTasks = subTaskIds.size();
        if (subTaskIds.isEmpty()) {
            result = TaskStatus.NEW;
        } else {
            int counterOfNewSubTasks = 0;
            int counterOfDoneSubTasks = 0;
            for (Integer subTaskId : subTaskIds) {
                SubTask subTask = subTaskStorage.get(subTaskId);
                if (subTask != null) {
                    TaskStatus status = subTask.getStatus();
                    if (status == TaskStatus.NEW) {
                        counterOfNewSubTasks++;
                    } else if (status == TaskStatus.DONE) {
                        counterOfDoneSubTasks++;
                    }
                }
            }
            if (counterOfNewSubTasks == countOfSubTasks) {
                result = TaskStatus.NEW;
            } else if (counterOfDoneSubTasks == countOfSubTasks) {
                result = TaskStatus.DONE;
            } else {
                result = TaskStatus.IN_PROGRESS;
            }
        }
        return result;
    }

    private boolean isExistingTask(Task task) {
        return taskStorage.get(task.getId()) != null;
    }

    private boolean isExistingSubTask(SubTask subTask) {
        return subTaskStorage.get(subTask.getId()) != null;
    }

    private boolean isExistingParentEpic(SubTask subTask) {
        return epicStorage.get(subTask.getEpicId()) != null;
    }

}

