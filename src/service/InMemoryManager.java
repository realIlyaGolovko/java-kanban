package service;

import exception.ValidationException;
import model.Epic;
import model.SubTask;
import model.Task;
import model.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class InMemoryManager implements TaskManager {
    protected final Map<Integer, Epic> epicStorage;
    protected final Map<Integer, SubTask> subTaskStorage;
    protected final Map<Integer, Task> taskStorage;
    protected int id;
    protected final HistoryManager historyManager;
    protected final Map<LocalDateTime, Task> prioritizedTasks;

    public InMemoryManager(HistoryManager historyManager) {
        this.historyManager = historyManager;
        this.epicStorage = new HashMap<>();
        this.subTaskStorage = new HashMap<>();
        this.taskStorage = new HashMap<>();
        this.id = 0;
        this.prioritizedTasks = new TreeMap<>();
    }

    @Override
    public int getNextId() {
        id++;
        return id;
    }

    //Task
    @Override
    public int createTask(Task task) {
        validateInputTask(task);
        int newTaskId = getNextId();
        task.setId(newTaskId);
        task.setStatus(TaskStatus.NEW);
        prioritizedTasks.put(task.getStartTime(), task);
        taskStorage.put(newTaskId, task);
        return newTaskId;
    }

    @Override
    public void updateTask(Task task) {
        validateInputTask(task);
        int taskId = task.getId();
        Optional.ofNullable(taskStorage.get(taskId))
                .ifPresentOrElse(original -> {
                    LocalDateTime prevStartTime = original.getStartTime();
                    prioritizedTasks.remove(prevStartTime);
                    taskStorage.put(taskId, task);
                    prioritizedTasks.put(task.getStartTime(), task);
                }, () -> createTask(task));
    }

    @Override
    public void deleteTasks() {
        taskStorage.values().forEach(task -> {
            historyManager.remove(task.getId());
            prioritizedTasks.remove(task.getStartTime());
        });
        taskStorage.clear();
    }

    @Override
    public void deleteTask(int taskId) {
        Task original = taskStorage.get(taskId);
        Optional.ofNullable(original).orElseThrow(() -> new ValidationException("Task " + taskId + " does not exist."));
        prioritizedTasks.remove(original.getStartTime());
        historyManager.remove(taskId);
        taskStorage.remove(taskId);
    }

    @Override
    public Task getTask(int taskId) {
        Task task = taskStorage.get(taskId);
        Optional.ofNullable(task).orElseThrow(() -> new ValidationException("Task with id " + taskId + " not found."));
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
        validateInputTask(subTask);
        int epicId = subTask.getEpicId();
        Epic epic = epicStorage.get(epicId);
        Optional.ofNullable(epic).orElseThrow(() -> new ValidationException("Epic with id " + epicId + " not found."));
        int newSubtaskId = getNextId();
        subTask.setId(newSubtaskId);
        subTask.setStatus(TaskStatus.NEW);
        epic.addSubTaskId(newSubtaskId);
        subTaskStorage.put(newSubtaskId, subTask);
        prioritizedTasks.put(subTask.getStartTime(), subTask);
        updateEpicStatus(epicId);
        updateEpicTime(epicId);
        return newSubtaskId;
    }

    @Override
    public void updateSubTask(SubTask subTask) {
        validateInputTask(subTask);
        int subTaskId = subTask.getId();
        int epicId = subTask.getEpicId();
        Optional.ofNullable(subTaskStorage.get(subTaskId))
                .ifPresentOrElse(
                        original -> {
                            LocalDateTime prevStartTime = original.getStartTime();
                            prioritizedTasks.remove(prevStartTime);
                            prioritizedTasks.put(subTask.getStartTime(), subTask);
                            subTaskStorage.put(subTaskId, subTask);
                            updateEpicStatus(epicId);
                            updateEpicTime(epicId);
                        },
                        () -> createSubTask(subTask)
                );
    }

    @Override
    public void deleteSubTask(int subTaskId) {
        SubTask original = subTaskStorage.get(subTaskId);
        Optional.ofNullable(original).orElseThrow(() ->
                new ValidationException("SubTask with id " + subTaskId + " not found."));
        LocalDateTime prevStartTime = original.getStartTime();
        Epic epic = epicStorage.get(original.getEpicId());
        subTaskStorage.remove(subTaskId);
        epic.removeSubTask(subTaskId);
        historyManager.remove(subTaskId);
        prioritizedTasks.remove(prevStartTime);
        updateEpicStatus(epic.getId());
        updateEpicTime(epic.getId());
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
        subTaskStorage.values().forEach(subTask -> {
            historyManager.remove(subTask.getId());
            prioritizedTasks.remove(subTask.getStartTime());
        });
        epicStorage.values().forEach(epic -> {
            epic.cleanSubTaskIds();
            updateEpicStatus(epic.getId());
            updateEpicTime(epic.getId());
        });
        subTaskStorage.clear();
    }

    @Override
    public List<SubTask> getSubtasksOfEpic(int epicId) {
        return Optional.ofNullable(epicStorage.get(epicId))
                .map(Epic::getSubTaskIds)
                .orElseGet(Collections::emptyList)
                .stream()
                .map(subTaskStorage::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    //Epic
    @Override
    public int createEpic(Epic epic) {
        Optional.ofNullable(epic).orElseThrow(() -> new ValidationException("Epic cannot be null"));
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
        epicStorage.keySet().forEach(epicId -> {
            getSubtasksOfEpic(epicId).stream()
                    .peek(subTask -> historyManager.remove(subTask.getId()))
                    .map(SubTask::getStartTime)
                    .forEach(prioritizedTasks::remove);
            historyManager.remove(epicId);
        });
        epicStorage.clear();
        subTaskStorage.clear();
    }

    @Override
    public void deleteEpic(int epicId) {
        Optional.ofNullable(epicStorage.get(epicId))
                .orElseThrow(() -> new ValidationException("Epic with id= " + epicId + " not found"))
                .getSubTaskIds().stream()
                .map(subTaskStorage::get)
                .forEach(subTask -> Optional.ofNullable(subTask)
                        .ifPresent(task -> {
                            prioritizedTasks.remove(task.getStartTime());
                            subTaskStorage.remove(task.getId());
                            historyManager.remove(task.getId());
                        }));

        epicStorage.remove(epicId);
        historyManager.remove(epicId);
    }

    @Override
    public void updateEpic(Epic epic) {
        Optional.ofNullable(epic).orElseThrow(() -> new ValidationException("Epic cannot be null"));
        Optional.ofNullable(epicStorage.get(epic.getId()))
                .ifPresentOrElse(originalEpic -> {
                            originalEpic.setName(epic.getName());
                            originalEpic.setDescription(epic.getDescription());
                        },
                        () -> createEpic(epic)
                );
    }

    //History
    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks.values());
    }

    protected void updateEpicStatus(int epicId) {
        Epic epic = epicStorage.get(epicId);
        List<Integer> subTaskIds = epic.getSubTaskIds();
        TaskStatus newStatus = recalculateEpicStatus(subTaskIds);
        epic.setStatus(newStatus);

    }

    private TaskStatus recalculateEpicStatus(List<Integer> subTaskIds) {
        if (subTaskIds.isEmpty()) {
            return TaskStatus.NEW;
        }

        Map<TaskStatus, Long> statusCounts = subTaskIds.stream()
                .map(subTaskStorage::get)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(SubTask::getStatus, Collectors.counting()));
        long countOfNewSubTasks = statusCounts.getOrDefault(TaskStatus.NEW, 0L);
        long countOfDoneSubTasks = statusCounts.getOrDefault(TaskStatus.DONE, 0L);
        long countOfSubTasks = subTaskIds.size();

        if (countOfNewSubTasks == countOfSubTasks) {
            return TaskStatus.NEW;
        } else if (countOfDoneSubTasks == countOfSubTasks) {
            return TaskStatus.DONE;
        } else {
            return TaskStatus.IN_PROGRESS;
        }
    }


    protected void updateEpicTime(int epicId) {
        Epic epic = epicStorage.get(epicId);
        List<SubTask> childSubTasks = getSubtasksOfEpic(epicId);
        if (childSubTasks.isEmpty()) {
            epic.setStartTime(null);
            epic.setEndTime(null);
            epic.setDuration(null);
            return;
        }
        LocalDateTime minStartTime = LocalDateTime.MAX;
        LocalDateTime maxEndTime = LocalDateTime.MIN;
        Duration sumOfDuration = Duration.ZERO;
        for (SubTask subTask : childSubTasks) {
            if (minStartTime.isAfter(subTask.getStartTime())) {
                minStartTime = subTask.getStartTime();
            }
            if (maxEndTime.isBefore(subTask.getEndTime())) {
                maxEndTime = subTask.getEndTime();
            }
            sumOfDuration = sumOfDuration.plus(subTask.getDuration());
        }
        epic.setStartTime(minStartTime);
        epic.setEndTime(maxEndTime);
        epic.setDuration(sumOfDuration);
    }

    private <T extends Task> boolean isOverlapInExecutionTime(T first, T second) {
        /*
         Если начало первого интервала раньше или в то же время, что и конец второго интервала,
         и начало второго интервала раньше или в то же время, что и конец первого интервала,
         то интервалы пересекаются.
         */
        // Проверка, что первый интервал не заканчивается до начала второго
        boolean firstNotEndBeforeSecondStarts = !first.getStartTime().isAfter(second.getEndTime());
        // Проверка, что второй интервал не заканчивается до начала первого
        boolean secondNotEndBeforeFirstStarts = !second.getStartTime().isAfter(first.getEndTime());

        // Интервалы пересекаются, если первый интервал не заканчивается до начала второго
        // и второй интервал не заканчивается до начала первого
        return firstNotEndBeforeSecondStarts && secondNotEndBeforeFirstStarts;
    }

    protected <T extends Task> void validateInputTask(T task) {
        Optional.ofNullable(task).orElseThrow(() -> new ValidationException("Task cannot be null."));
        Optional.ofNullable(task.getStartTime()).orElseThrow(() ->
                new ValidationException("StartTime cannot be null."));
        validateOverlapExecutionTime(task);
    }

    private void validateOverlapExecutionTime(Task task) {
        getPrioritizedTasks().stream()
                .filter(savedTask -> !(task.equals(savedTask)))
                .filter(savedTask -> isOverlapInExecutionTime(task, savedTask))
                .findAny()
                .ifPresent(savedTask -> {
                    throw new ValidationException("There is an intersection in execution time with task number="
                            + savedTask.getId());
                });
    }
}

