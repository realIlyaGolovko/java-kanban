package service;

import model.Epic;
import model.SubTask;
import model.Task;
import model.TaskStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryManager implements TaskManager {
    private final Map<Integer, Epic> epicStorage;
    private final Map<Integer, SubTask> subTaskStorage;
    private final Map<Integer, Task> taskStorage;
    private int id;
    private final HistoryManager historyManager;

    public InMemoryManager(HistoryManager historyManager) {
        this.historyManager = historyManager;
        this.epicStorage = new HashMap<>();
        this.subTaskStorage = new HashMap<>();
        this.taskStorage = new HashMap<>();
        this.id = 0;
    }

    @Override
    public int getNextId() {
        id++;
        return id;
    }

    //Task
    @Override
    public int createTask(Task task) {
        if (task == null) {
            return -1;
        }
        int newTaskId = getNextId();
        task.setStatus(TaskStatus.NEW);
        taskStorage.put(newTaskId, task);
        return newTaskId;
    }

    @Override
    public void updateTask(Task task) {
        if (task == null) {
            return;
        }
        int taskId = task.getId();
        if (taskStorage.containsKey(taskId)) {
            taskStorage.put(taskId, task);
        }
    }

    @Override
    public void deleteTasks() {
        taskStorage.clear();
    }

    @Override
    public void deleteTask(int taskId) {
        historyManager.remove(taskId);
        taskStorage.remove(taskId);
    }

    @Override
    public Task getTask(int taskId) {
        Task task = taskStorage.get(taskId);
        historyManager.add(task);
        return task;
    }

    @Override
    public List<Task> getTasks() {
        return new ArrayList<>(taskStorage.values());
    }

    //Subtask
    @Override
    public int createSubTask(SubTask subTask) {
        if (subTask == null) {
            return -1;
        }
        int epicId = subTask.getEpicId();
        Epic epic = epicStorage.get(epicId);
        if (epic == null) {
            return -1;
        }
        int newSubtaskId = getNextId();
        subTask.setId(newSubtaskId);
        subTask.setStatus(TaskStatus.NEW);
        epic.addSubTaskId(newSubtaskId);
        updateEpicStatus(epicId);
        subTaskStorage.put(newSubtaskId, subTask);
        return newSubtaskId;
    }

    @Override
    public void updateSubTask(SubTask subTask) {
        if (subTask == null) {
            return;
        }
        int subTaskId = subTask.getId();
        int epicId = subTask.getEpicId();
        if (subTaskStorage.containsKey(subTaskId) && epicStorage.containsKey(epicId)) {
            subTaskStorage.put(subTaskId, subTask);
            updateEpicStatus(epicId);
        }
    }

    @Override
    public void deleteSubTask(int subTaskId) {
        SubTask subTask = subTaskStorage.remove(subTaskId);
        if (subTask != null) {
            Epic epic = epicStorage.get(subTask.getEpicId());
            if (epic != null) {
                epic.removeSubTask(subTaskId);
                historyManager.remove(subTaskId);
                updateEpicStatus(epic.getId());
            }
        }
    }

    @Override
    public List<SubTask> getSubTasks() {
        return new ArrayList<>(subTaskStorage.values());
    }

    @Override
    public SubTask getSubTask(int subTaskId) {
        SubTask subTask = subTaskStorage.get(subTaskId);
        historyManager.add(subTask);
        return subTask;
    }

    @Override
    public void deleteSubTasks() {
        subTaskStorage.clear();
        for (Epic epic : epicStorage.values()) {
            epic.cleanSubTaskIds();
            updateEpicStatus(epic.getId());
        }
    }

    @Override
    public List<SubTask> getSubtasksOfEpic(int epicId) {
        ArrayList<SubTask> result = new ArrayList<>();
        Epic epic = epicStorage.get(epicId);
        if (epic != null) {
            for (Integer subTaskId : epic.getSubTaskIds()) {
                SubTask subTask = subTaskStorage.get(subTaskId);
                if (subTask != null) {
                    result.add(subTask);
                }
            }
        }
        return result;
    }


    //Epic
    @Override
    public int createEpic(Epic epic) {
        if (epic == null) {
            return -1;
        }
        int newEpicId = getNextId();
        epic.setId(newEpicId);
        epic.setStatus(TaskStatus.NEW);
        epicStorage.put(newEpicId, epic);
        return newEpicId;
    }

    @Override
    public List<Epic> getEpics() {
        return new ArrayList<>(epicStorage.values());
    }

    @Override
    public Epic getEpic(int epicId) {
        Epic epic = epicStorage.get(epicId);
        historyManager.add(epic);
        return epic;
    }

    @Override
    public void deleteEpics() {
        epicStorage.clear();
        subTaskStorage.clear();
    }

    @Override
    public void deleteEpic(int epicId) {
        Epic epic = epicStorage.get(epicId);
        if (epic != null) {
            List<Integer> subTasksOfEpic = epic.getSubTaskIds();
            for (Integer subTaskId : subTasksOfEpic) {
                subTaskStorage.remove(subTaskId);
                historyManager.remove(subTaskId);
            }
            epicStorage.remove(epicId);
            historyManager.remove(epicId);
        }
    }

    @Override
    public void updateEpic(Epic epic) {
        if (epic == null) {
            return;
        }
        Epic savedEpic = epicStorage.get(epic.getId());
        if (savedEpic == null) {
            return;
        }
        savedEpic.setName(epic.getName());
        savedEpic.setDescription(epic.getDescription());
    }


    private void updateEpicStatus(int epicId) {
        Epic epic = epicStorage.get(epicId);
        if (epic != null) {
            List<Integer> subTaskIds = epic.getSubTaskIds();
            epic.setStatus(recalculateEpicStatus(subTaskIds));
        }
    }

    private TaskStatus recalculateEpicStatus(List<Integer> subTaskIds) {
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

    //History
    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }
}

