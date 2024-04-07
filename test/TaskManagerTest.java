import exception.NotFoundException;
import exception.ValidationException;
import model.Epic;
import model.SubTask;
import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import service.TaskManager;
import util.testdata.RandomTask;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static util.check.TaskComparator.compareListOfTasks;
import static util.check.TaskComparator.compareTasks;


abstract class TaskManagerTest<T extends TaskManager> {
    //sut -> system under test
    protected T sut;

    protected LocalDateTime startTime = LocalDateTime.now();
    //just random value for testing
    protected Duration duration = Duration.ofMinutes(10);

    protected Task getRandomTask() {
        Task task = RandomTask.initRandomTask();
        sut.createTask(task);
        return task;
    }

    protected Task getRandomTask(Duration duration, LocalDateTime startTime) {
        Task task = RandomTask.initRandomTask(duration, startTime);
        sut.createTask(task);
        return task;
    }

    protected List<Task> getRandomTasks() {
        List<Task> tasks = new ArrayList<>();
        for (int i = 1; i < RandomTask.random.nextInt(5); i++) {
            tasks.add(getRandomTask());
        }
        return tasks;
    }

    protected Epic getRandomEpic() {
        Epic epic = RandomTask.initRandomEpic();
        sut.createEpic(epic);
        return epic;
    }

    protected List<Epic> getRandomEpics() {
        List<Epic> epics = new ArrayList<>();
        for (int i = 0; i < RandomTask.random.nextInt(5); i++) {
            epics.add(getRandomEpic());
        }
        return epics;
    }

    protected SubTask getRandomSubTask(int epicId) {
        SubTask subTask = RandomTask.initRandomSubTask(epicId);
        sut.createSubTask(subTask);
        return subTask;
    }

    protected SubTask getRandomSubTask(int epicId, Duration duration, LocalDateTime startTime) {
        SubTask subTask = RandomTask.initRandomSubTask(epicId, duration, startTime);
        sut.createSubTask(subTask);
        return subTask;
    }

    protected List<SubTask> getRandomSubTasksByEpic(int epicId, int countOfSubTasks) {
        List<SubTask> subTasks = new ArrayList<>();
        for (int i = 0; i < countOfSubTasks; i++) {
            subTasks.add(getRandomSubTask(epicId));
        }
        return subTasks;
    }

    protected List<Task> getListOfRandomDifferentTasks() {
        List<Task> tasks = new ArrayList<>();
        Task task = getRandomTask();
        Epic epic = getRandomEpic();
        SubTask subTask = getRandomSubTask(epic.getId());
        tasks.add(task);
        tasks.add(epic);
        tasks.add(subTask);
        return tasks;
    }

    protected void markSubTasksAsWatched(List<SubTask> subTasks) {
        subTasks.forEach(subTask -> sut.getSubTask(subTask.getId()));
    }

    protected void markTasksAsWatched(List<Task> tasks) {
        tasks.forEach(task -> sut.getTask(task.getId()));
    }

    protected void markTaskAsWatched(Task task) {
        sut.getTask(task.getId());
    }

    protected void markSubTaskAsWatched(SubTask subTask) {
        sut.getSubTask(subTask.getId());
    }

    protected void markEpicAsWatched(Epic epic) {
        sut.getEpic(epic.getId());
    }

    @Test
    @DisplayName("При создании задачи, она должна сохраняться в памяти в статусе NEW.")
    public void createTaskShouldSaveNewTask() {
        Task expected = RandomTask.initRandomTask();

        int savedTask = sut.createTask(expected);
        Task actual = sut.getTask(savedTask);

        compareTasks(expected, actual);
        assertEquals(expected.getStatus(), TaskStatus.NEW, "Should be the NEW status");
    }

    @Test
    @DisplayName("При создании null задачи, должна быть ошибка.")
    public void createTaskWithNullShouldReturnErrorCode() {
        assertThrows(NotFoundException.class, () -> sut.createTask(null),
                "Should be the ValidationException");
    }

    @Test
    @DisplayName("У таски можно изменить наименование, описание и статус.")
    public void updateTaskShouldUpdateSavedTask() {
        Task saved = getRandomTask();
        Task expected = new Task("newTaskName", "newTaskDescription", saved.getId());
        expected.setStatus(TaskStatus.DONE);

        sut.updateTask(expected);
        Task actual = sut.getTask(saved.getId());

        compareTasks(expected, actual);
    }

    @Test
    @DisplayName("При изменении null задачи, должна быть ошибка.")
    public void updateTaskWithNullShouldReturnErrorCode() {
        assertThrows(NotFoundException.class, () -> sut.updateTask(null),
                "Should be the exception");
    }

    @Test
    @DisplayName("При изменении несуществующей задачи, она должна быть создана.")
    public void updateTaskWithNotExistingIdShouldCreateNewTask() {
        Task expected = RandomTask.initRandomTask();

        sut.updateTask(expected);
        Task actual = sut.getTask(expected.getId());

        compareTasks(expected, actual);
    }


    @Test
    @DisplayName("Если задачи не существует, должен возвращаться null")
    public void getInvalidTaskShouldReturnNull() {
        assertThrows(NotFoundException.class, () -> sut.getTask(RandomTask.random.nextInt()),
                "Should be the exception");
    }

    @Test
    @DisplayName("При удалении задачи, она должна удаляться из памяти.")
    public void deleteTaskShouldDeleteSavedTask() {
        Task saved = getRandomTask();

        sut.deleteTask(saved.getId());
        List<Task> actual = sut.getTasks();

        assertEquals(0, actual.size(), "Should be empty");
    }

    @Test
    @DisplayName("При удалении несуществующей задачи, должна быть ошибка.")
    public void deleteTaskWithInvalidIdShouldReturnErrorCode() {
        assertThrows(NotFoundException.class, () -> sut.deleteTask(RandomTask.random.nextInt()),
                "Should be the exception");
    }

    @Test
    @DisplayName("Если созданных задач в памяти нет, то должен возвращать пустой список.")
    public void getAllTasksReturnEmptyListWhenNoTasks() {
        List<Task> result = sut.getTasks();
        assertTrue(result.isEmpty(), "Should be empty");
    }

    @Test
    @DisplayName("Должен возвращать только сохраненные таски.")
    public void getAllTasksReturnOnlyAllSavedTasks() {
        List<Task> expected = getRandomTasks();
        getRandomEpics();

        List<Task> actual = sut.getTasks();

        compareListOfTasks(expected, actual);
    }

    @Test
    @DisplayName("Должен удалять все сохраненные таски.")
    public void deleteAllTasksShouldDeleteAllSavedTasks() {
        getRandomTasks();

        sut.deleteTasks();
        List<Task> actual = sut.getTasks();

        assertTrue(actual.isEmpty(), "Should be empty");
    }

    @Test
    @DisplayName("Должен сохранять эпик в памяти в статусе NEW.")
    public void createEpicShouldSaveNewEpic() {
        Epic expected = RandomTask.initRandomEpic();

        int savedEpic = sut.createEpic(expected);
        Epic actual = sut.getEpic(savedEpic);


        compareTasks(expected, actual);
        assertEquals(TaskStatus.NEW, actual.getStatus(), "Should be new");
    }

    @Test
    @DisplayName("При попытке сохранить пустой эпик, должен возвращать код ошибки.")
    public void createEpicWithNullShouldReturnErrorCode() {
        assertThrows(ValidationException.class, () -> sut.createEpic(null), "Should be exception");
    }

    @Test
    @DisplayName("Эпик должен быть в статусе NEW, если все его подзадачи в статусе NEW.")
    public void epicStatusShouldBeNewWhenAllSubTasksAreNew() {
        Epic epic = getRandomEpic();
        List<SubTask> savedSubtasks = getRandomSubTasksByEpic(epic.getId(), 2);
        SubTask subTaskOfEpic = savedSubtasks.getFirst();
        subTaskOfEpic.setStatus(TaskStatus.IN_PROGRESS);
        sut.updateSubTask(subTaskOfEpic);
        subTaskOfEpic.setStatus(TaskStatus.NEW);

        sut.updateSubTask(subTaskOfEpic);
        Epic actual = sut.getEpic(epic.getId());

        assertEquals(TaskStatus.NEW, actual.getStatus(), "Should be new");
    }

    @Test
    @DisplayName("Эпик должен быть в статусе IN_PROGRESS, если все его подзадачи в разных статусах.")
    public void epicStatusShouldBeInProgressWhenAnySubTaskIsInProgress() {
        Epic epic = getRandomEpic();
        List<SubTask> savedSubtasks = getRandomSubTasksByEpic(epic.getId(), 2);
        SubTask subTaskOfEpic = savedSubtasks.getFirst();
        subTaskOfEpic.setStatus(TaskStatus.DONE);

        sut.updateSubTask(subTaskOfEpic);
        Epic actual = sut.getEpic(epic.getId());

        assertEquals(TaskStatus.IN_PROGRESS, actual.getStatus(), "Should be in progress");
    }

    @Test
    @DisplayName("Эпик должен быть в статусе DONE, если все его подзадачи завершены.")
    public void epicStatusShouldBeDoneWhenAllSubTasksAreDone() {
        Epic epic = getRandomEpic();
        List<SubTask> savedSubtasks = getRandomSubTasksByEpic(epic.getId(), 3);

        for (SubTask subTask : savedSubtasks) {
            subTask.setStatus(TaskStatus.DONE);
            sut.updateSubTask(subTask);
        }
        Epic actual = sut.getEpic(epic.getId());

        assertEquals(TaskStatus.DONE, actual.getStatus(), "Should be done");
    }

    @Test
    @DisplayName("Должен обновлять наименование и описание  сохраненного эпика в памяти.")
    public void updateEpicShouldUpdateSavedEpic() {
        Epic saved = getRandomEpic();
        Epic expected = new Epic("newEpicName", "newEpicDescription", saved.getId());

        sut.updateEpic(expected);
        Epic actual = sut.getEpic(saved.getId());

        compareTasks(expected, actual);
    }

    @Test
    @DisplayName("При обновлении несуществующего эпика, должен создать новый.")
    public void updateEpicShouldCreateNewEpicIfThisEpicNotExist() {
        Epic expectedEpic = RandomTask.initRandomEpic();

        sut.updateEpic(expectedEpic);
        Epic actual = sut.getEpic(expectedEpic.getId());

        compareTasks(expectedEpic, actual);
    }

    @Test
    @DisplayName("Если эпика не существует, должно выбрасываться исключение.")
    public void getInvalidEpicShouldReturnNull() {
        assertThrows(NotFoundException.class, () -> sut.getEpic(RandomTask.random.nextInt()));
    }

    @Test
    @DisplayName("При удалении эпика, он должен удаляться из памяти.")
    public void deleteEpicWithoutSubTasksShouldDeleteSavedEpic() {
        Epic saved = getRandomEpic();

        sut.deleteEpic(saved.getId());
        List<Epic> actual = sut.getEpics();

        assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("При удалении эпика, все его дочерние подзадачи должны удаляться из памяти.")
    public void deleteEpicWithSubTasksShouldDeleteSavedEpicAndSubTasks() {
        Epic epic = getRandomEpic();
        getRandomSubTasksByEpic(epic.getId(), 3);

        sut.deleteEpic(epic.getId());
        List<SubTask> actualSubTasks = sut.getSubTasks();

        assertTrue(actualSubTasks.isEmpty(), "Should be empty");
    }

    @Test
    @DisplayName("Должен возвращать только сохраненные эпики.")
    public void getAllEpicsReturnOnlyAllSavedEpics() {
        List<Epic> expected = getRandomEpics();

        List<Epic> actual = sut.getEpics();

        compareListOfTasks(expected, actual);
    }

    @Test
    @DisplayName("При удалении всех эпиков, все сабтаски должны быть удалены.")
    public void deleteAllEpicsShouldDeleteAllSavedEpicsAnsSubTasks() {
        List<Epic> epics = getRandomEpics();
        for (Epic epic : epics) {
            getRandomSubTasksByEpic(epic.getId(), 2);
        }

        sut.deleteEpics();
        List<Epic> actualEpics = sut.getEpics();
        List<SubTask> actualSubTasks = sut.getSubTasks();

        assertTrue(actualEpics.isEmpty(), "Should be empty");
        assertTrue(actualSubTasks.isEmpty(), "Should be empty");
    }

    @Test
    @DisplayName("Должен сохранять новую подзадачу в памяти.")
    public void createSubtaskShouldSaveNewSubtaskWithParent() {
        Epic epic = RandomTask.initRandomEpic();
        int createdEpic = sut.createEpic(epic);
        SubTask expected = RandomTask.initRandomSubTask(createdEpic);

        int savedSubTask = sut.createSubTask(expected);
        SubTask actual = sut.getSubTask(savedSubTask);

        compareTasks(expected, actual);
    }

    @Test
    @DisplayName("Сабтаска не должны сохраняться в памяти, при попытке сохранить подзадачу с несуществующим эпиком.")
    public void createSubTaskWithInvalidEpicShouldNotSave() {
        assertThrows(NotFoundException.class, () -> sut.createSubTask(RandomTask.initRandomSubTask(RandomTask.random.nextInt())));
    }

    @Test
    @DisplayName("Должно выбрасываться исключение, при попытке сохранить подзадачу с null.")
    public void createSubTaskWithNullShouldReturnErrorCode() {
        assertThrows(NotFoundException.class, () -> sut.createSubTask(null), "Should throw exception");
    }

    @Test
    @DisplayName("Должен обновлять наименование, описание, статус.")
    public void updateSubTaskShouldUpdateSavedSubTask() {
        Epic epic = getRandomEpic();
        SubTask saved = getRandomSubTask(epic.getId());
        SubTask expected = new SubTask("newSubTaskName", "newSubTaskDescription", saved.getId(),
                epic.getId());
        expected.setStatus(TaskStatus.DONE);
        sut.updateSubTask(saved);

        sut.updateSubTask(expected);
        SubTask actual = sut.getSubTask(saved.getId());

        compareTasks(expected, actual);
    }

    @Test
    @DisplayName("При обновлении null подзадачи, должна быть ошибка.")
    public void updateSubTaskWithNullShouldReturnErrorCode() {
        assertThrows(NotFoundException.class, () -> sut.updateSubTask(null), "Should throw exception");
    }

    @Test
    @DisplayName("При обновлении несуществующей подзадачи, она должна быть создана.")
    public void updateSubTaskWithNewSubTaskShouldCreateNewSubTask() {
        int epicId = getRandomEpic().getId();
        SubTask expectedSubTask = RandomTask.initRandomSubTask(epicId);
        sut.updateSubTask(expectedSubTask);

        SubTask actual = sut.getSubTask(expectedSubTask.getId());

        compareTasks(expectedSubTask, actual);
    }


    @Test
    @DisplayName("При удалении подзадачи, она должна удаляться из памяти.")
    public void deleteSubTaskShouldDeleteSavedSubTask() {
        Epic epic = getRandomEpic();
        SubTask saved = getRandomSubTask(epic.getId());

        sut.deleteSubTask(saved.getId());
        List<SubTask> actual = sut.getSubTasks();

        assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("При удалении несуществующей подзадачи, должна быть ошибка.")
    public void deleteSubTaskWithInvalidIdShouldReturnErrorCode() {
        assertThrows(NotFoundException.class, () -> sut.deleteSubTask(-1),
                "Should throw exception");
    }

    @Test
    @DisplayName("Если подзадач не существует, должен возвращаться null.")
    public void getSubTasksShouldReturnEmptyListWhenNoSubTasks() {
        List<SubTask> actual = sut.getSubTasks();

        assertTrue(actual.isEmpty(), "Should be empty");
    }

    @Test
    @DisplayName("Должен возвращать только сохраненные подзадачи.")
    public void getSubTasksShouldReturnOnlyAllSavedSubTasks() {
        Epic epic = getRandomEpic();
        List<SubTask> expected = getRandomSubTasksByEpic(epic.getId(), 2);

        List<SubTask> actual = sut.getSubTasks();

        compareListOfTasks(expected, actual);
    }

    @Test
    @DisplayName("Должен удалять все сохраненные подзадачи.")
    public void deleteSubTasksShouldDeleteAllSavedSubTasks() {
        Epic epic = getRandomEpic();
        getRandomSubTasksByEpic(epic.getId(), 2);

        sut.deleteSubTasks();
        List<SubTask> actual = sut.getSubTasks();

        assertTrue(actual.isEmpty(), "Should be empty");
    }

    @Test
    @DisplayName("Должен возвращать пустой список, если у эпика нет дочерних подзадач.")
    public void getSubTasksOfEpicShouldReturnEmptyListWhenNoSubTasks() {
        Epic epic = getRandomEpic();

        List<SubTask> actual = sut.getSubtasksOfEpic(epic.getId());

        assertTrue(actual.isEmpty(), "Should be empty");
    }

    @Test
    @DisplayName("Должен возвращать дочерние подзадачи эпика.")
    public void getSubTasksOfEpicShouldReturnOnlyChildSubTasks() {
        Epic epic = getRandomEpic();
        List<SubTask> expected = getRandomSubTasksByEpic(epic.getId(), 2);

        List<SubTask> actual = sut.getSubtasksOfEpic(epic.getId());

        compareListOfTasks(expected, actual);
    }

    @Test
    @DisplayName("Если задачи не просматривались, история должна быть пустой.")
    public void getHistoryShouldReturnEmptyListWhenHistoryIsClear() {
        List<Task> actual = sut.getHistory();

        assertTrue(actual.isEmpty(), "Should be empty");
    }

    @Test
    @DisplayName("В истории должны быть сохранены только просмотренные таски.")
    public void getHistoryShouldReturnOnlyWatchedTasks() {
        getRandomTask();
        Task expected = getRandomTask();

        markTaskAsWatched(expected);
        List<Task> actual = sut.getHistory();

        compareTasks(expected, actual.getFirst());
        assertEquals(1, actual.size(), "Should be only one");

    }

    @Test
    @DisplayName("В истории могут содержаться разные типы задач.")
    public void historyCanWorkWithDifferentTypesOfTasks() {
        Task expectedTask = getRandomTask();
        markTaskAsWatched(expectedTask);
        Epic expectedEpic = getRandomEpic();
        markEpicAsWatched(expectedEpic);
        SubTask expecteSubTask = getRandomSubTask(expectedEpic.getId());
        markSubTaskAsWatched(expecteSubTask);

        List<Task> actual = sut.getHistory();

        assertEquals(3, actual.size(), "Should be 3");
        assertEquals(expectedTask, actual.getFirst(), "Should be the same task");
        assertEquals(expectedEpic.getId(), actual.get(1).getId(), "Should be the same epic");
        assertEquals(expecteSubTask.getId(), actual.getLast().getId(), "Should be the same subtask");
    }

    @Test
    @DisplayName("Единственная задача просмотренная дважды, должна быть в истории один раз.")
    public void aSingleTaskViewedTwiceShouldAppearInTheHistoryOnce() {
        Task expected = getRandomTask();
        markTaskAsWatched(expected);
        markTaskAsWatched(expected);

        List<Task> actual = sut.getHistory();

        compareTasks(expected, actual.getFirst());
        assertEquals(1, actual.size(), "Should be only one");
    }

    @Test
    @DisplayName("Когда просмотренная задача уже была в начале истории, дубликаты не создаются.")
    public void whenTheViewedTaskIsAtTheBeginningOfTheHistoryThereAreNoDuplicatesThere() {
        Task expected = getRandomTask();
        markTaskAsWatched(expected);
        Task anotherTask = getRandomTask();
        markTaskAsWatched(anotherTask);
        markTaskAsWatched(expected);

        List<Task> actual = sut.getHistory();

        compareTasks(anotherTask, actual.getFirst());
        compareTasks(expected, actual.getLast());
        assertEquals(2, actual.size(), "Should be only one");
    }

    @Test
    @DisplayName("Когда просмотренная задача уже была в конце истории, дубликаты не создаются.")
    public void whenTheViewedTaskIsAtTheEndingOfTheHistoryThereAreNoDuplicatesThere() {
        Task anotherTask = getRandomTask();
        markTaskAsWatched(anotherTask);
        Task expected = getRandomTask();
        markTaskAsWatched(expected);
        markTaskAsWatched(expected);

        List<Task> actual = sut.getHistory();

        compareTasks(anotherTask, actual.getFirst());
        compareTasks(expected, actual.getLast());
        assertEquals(2, actual.size(), "Should be only one");
    }

    @Test
    @DisplayName("Когда просмотренная задача уже была в середине истории, дубликаты не создаются.")
    public void whenTheViewedTaskIsAtTheMiddleOfTheHistoryThereAreNoDuplicatesThere() {
        Task firstTask = getRandomTask();
        markTaskAsWatched(firstTask);
        Task expected = getRandomTask();
        markTaskAsWatched(expected);
        Task anotherTask = getRandomTask();
        markTaskAsWatched(anotherTask);


        markTaskAsWatched(expected);
        List<Task> actual = sut.getHistory();

        compareTasks(firstTask, actual.getFirst());
        compareTasks(anotherTask, actual.get(1));
        compareTasks(expected, actual.getLast());
        assertEquals(3, actual.size(), "Should be only one");
    }


    @Test
    @DisplayName("В истории не должно быть удаленных задач.")
    public void historyShouldNotContainDeletedTasks() {
        Task expected = getRandomTask();
        markTaskAsWatched(expected);
        sut.deleteTask(expected.getId());

        List<Task> actual = sut.getHistory();

        assertTrue(actual.isEmpty(), "Should be empty");
    }

    @Test
    @DisplayName("В истории не должно быть удаленных задач.")
    public void historyShouldNotContainDeletedSubTasks() {
        int epicId = getRandomEpic().getId();
        SubTask expected = getRandomSubTask(epicId);
        markSubTaskAsWatched(expected);

        sut.deleteSubTask(expected.getId());
        List<Task> actual = sut.getHistory();

        assertTrue(actual.isEmpty(), "Should be empty");
    }

    @Test
    @DisplayName("В истории не должно быть удаленных эпиков с дочерними подзадачами.")
    public void historyShouldNotContainDeletedEpicAndChildSubTasks() {
        Epic expectedEpic = getRandomEpic();
        markEpicAsWatched(expectedEpic);
        List<SubTask> subTasks = getRandomSubTasksByEpic(expectedEpic.getId(), 3);
        markSubTasksAsWatched(subTasks);

        sut.deleteEpic(expectedEpic.getId());
        List<Task> actual = sut.getHistory();

        assertTrue(actual.isEmpty(), "Should be empty");
    }

    @Test
    @DisplayName("В истории не должно быть null.")
    public void historyShouldNotContainNull() {
        assertThrows(NotFoundException.class, () -> sut.getTask(RandomTask.random.nextInt()), "Should be exception");
    }

    @Test
    @DisplayName("Удаление всех задач из памяти, должно удалять задачи из истории.")
    public void deleteTasksShouldClearAllTasksFromHistory() {
        List<Task> tasksForClear = getRandomTasks();
        markTasksAsWatched(tasksForClear);
        Epic expectedEpic = getRandomEpic();
        markEpicAsWatched(expectedEpic);
        SubTask expectedSubTask = getRandomSubTask(expectedEpic.getId());
        markSubTaskAsWatched(expectedSubTask);

        sut.deleteTasks();
        List<Task> actual = sut.getHistory();

        assertEquals(2, actual.size(), "Should contains only epic with subtask");
        assertEquals(expectedEpic, actual.getFirst(), "Should be same epic");
        assertEquals(expectedSubTask, actual.getLast(), "Should be same subtask");
    }

    @Test
    @DisplayName("Удаление всех подзадач из памяти, должно удалять подзадачи из истории.")
    public void deleteSubTasksShouldClearAllSubTasksFromHistory() {
        Epic expectedEpic = getRandomEpic();
        markEpicAsWatched(expectedEpic);
        Task expectedTask = getRandomTask();
        markTaskAsWatched(expectedTask);
        List<SubTask> subTasksForClear = getRandomSubTasksByEpic(expectedEpic.getId(), 2);
        markSubTasksAsWatched(subTasksForClear);


        sut.deleteSubTasks();
        List<Task> actual = sut.getHistory();

        assertEquals(2, actual.size(), "Should contains only epic and task");
        assertEquals(expectedEpic, actual.getFirst(), "Should be same epic");
        assertEquals(expectedTask, actual.getLast(), "Should be same task");
    }

    @Test
    @DisplayName("Удаление всех эпиков из памяти, должно удалять эпики и подзадачи из истории.")
    public void deleteEpicsShouldClearAllEpicsAndSubTasksFromHistory() {
        Epic epicForClear = getRandomEpic();
        markEpicAsWatched(epicForClear);
        List<SubTask> subTasksForClear = getRandomSubTasksByEpic(epicForClear.getId(), 2);
        markSubTasksAsWatched(subTasksForClear);
        List<Task> expectedTasks = getRandomTasks();
        markTasksAsWatched(expectedTasks);

        sut.deleteEpics();
        List<Task> actual = sut.getHistory();

        compareListOfTasks(expectedTasks, actual);
    }

    @Test
    @DisplayName("Метод должен возвращать сумму даты начала и длительности задачи.")
    public void getEndTimeShouldReturnTheEndTimeOfTheTask() {
        Duration durationOfTask = RandomTask.getRandomDuration();
        LocalDateTime startOfTask = LocalDateTime.now();
        LocalDateTime expectedTime = startOfTask.plus(durationOfTask);
        Task task = getRandomTask(durationOfTask, startOfTask);

        LocalDateTime actualEndOfTask = task.getEndTime();

        assertEquals(expectedTime, actualEndOfTask, "Should be same time");
    }

    @Test
    @DisplayName("Установка нового начала задачи, должно обновлять ее окончание.")
    public void setStartTimeShouldUpdateEndTime() {
        Task task = RandomTask.initRandomTask(duration, startTime);
        LocalDateTime expectedStartTime = LocalDateTime.of(2024, 1, 1, 13, 0);

        task.setStartTime(expectedStartTime);

        assertEquals(expectedStartTime, task.getStartTime());
        assertEquals(expectedStartTime.plus(duration), task.getEndTime(), "EndTime should be updated.");
    }

    @Test
    @DisplayName("Установка новой длительности задачи, должно обновлять ее окончание.")
    public void setDurationTimeShouldUpdateEndTime() {
        Task task = RandomTask.initRandomTask(duration, startTime);
        Duration expectedDuration = Duration.ofMinutes(20);

        task.setDuration(expectedDuration);

        assertEquals(expectedDuration, task.getDuration());
        assertEquals(startTime.plus(expectedDuration), task.getEndTime(), "EndTime should be updated.");
    }

    @Test
    @DisplayName("Время окончания задачи должно быть текущим по-умолчанию.")
    public void taskEndShouldBeTheCurrentDefault() {
        Task task = getRandomTask();

        LocalDateTime actualEndOfTask = task.getEndTime();

        assertEquals(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES),
                actualEndOfTask.truncatedTo(ChronoUnit.MINUTES), "Should be same time");
    }

    @Test
    @DisplayName("Время задачи должно быть null, если начало startTime==null.")
    public void taskTimeShouldBeNullWhenStartTimeIsNull() {
        Task task = RandomTask.initRandomTask(duration, null);
        LocalDateTime actualEndTime = task.getEndTime();
        LocalDateTime actualStartTime = task.getStartTime();

        assertNull(actualEndTime, "EndTime should be null");
        assertNull(actualStartTime, "StartTime should be null");
    }

    @Test
    @DisplayName("Время задачи должно быть null, если установлено startTime==null.")
    public void taskTimeShouldBeNullWhenSetStartTimeIsNull() {
        Task task = RandomTask.initRandomTask(duration, startTime);
        task.setStartTime(null);
        LocalDateTime actualEndTime = task.getEndTime();
        LocalDateTime actualStartTime = task.getStartTime();

        assertNull(actualEndTime, "EndTime should be null");
        assertNull(actualStartTime, "StartTime should be null");
    }

    @Test
    @DisplayName("При попытке создать задачу с пустым startTime, должна быть ошибка.")
    public void createTaskWithNullStartTimeShouldThrowException() {
        assertThrows(ValidationException.class, () -> getRandomTask(duration, null));
    }

    @Test
    @DisplayName("Метод должен возвращать отсортированный по времени начала список задач.")
    public void getPrioritizedTasksShouldReturnListOfTasksSortedByStartTime() {
        Task thirdTask = getRandomTask(duration, startTime.plus(Duration.ofMinutes(40)));
        Epic epic = getRandomEpic();
        SubTask firstTask = getRandomSubTask(epic.getId(), duration, startTime);
        Task secondTask = getRandomTask(duration, startTime.plus(Duration.ofMinutes(20)));

        List<Task> actualTasks = sut.getPrioritizedTasks();

        compareTasks(firstTask, actualTasks.getFirst());
        compareTasks(secondTask, actualTasks.get(1));
        compareTasks(thirdTask, actualTasks.getLast());
    }


    @Test
    @DisplayName("Задача с уникальным интервалом, ранее существующего должна быть сохранена.")
    public void createTaskShouldSaveTaskWithSmallerUniqueInterval() {
        getRandomTask(duration, startTime);
        Task expectedTask = RandomTask.initRandomTask(duration, startTime.minus(Duration.ofMinutes(20)));

        int actualTaskId = sut.createTask(expectedTask);
        Task actualTask = sut.getTask(actualTaskId);

        compareTasks(expectedTask, actualTask);
    }

    @Test
    @DisplayName("Задача с уникальным интервалом, позже существующего должна быть сохранена.")
    public void createTaskShouldSaveTaskWithBiggerUniqueInterval() {
        getRandomTask(duration, startTime);
        Task expectedTask = RandomTask.initRandomTask(duration, startTime.plus(Duration.ofMinutes(20)));

        int actualTaskId = sut.createTask(expectedTask);
        Task actualTask = sut.getTask(actualTaskId);

        compareTasks(expectedTask, actualTask);
    }

    @Test
    @DisplayName("Ошибка при создании задачи, когда ее начало пересекается с существующим интервалом.")
    public void createTaskShouldThrowExceptionWhenStartTimeIntersects() {
        getRandomTask(duration, startTime);
        Task task = RandomTask.initRandomTask(duration, startTime.plus(Duration.ofMinutes(5)));

        assertThrows(ValidationException.class, () -> sut.createTask(task), "Should throw exception");
    }

    @Test
    @DisplayName("Ошибка при создании задачи, когда ее окончание пересекается с существующим интервалом.")
    public void createTaskShouldThrowExceptionWhenEndTimeIntersects() {
        getRandomTask(duration, startTime);
        Task task = RandomTask.initRandomTask(duration, startTime.minus(Duration.ofMinutes(5)));

        assertThrows(ValidationException.class, () -> sut.createTask(task), "Should throw exception");
    }

    @Test
    @DisplayName("Ошибка при создании задачи, когда ее интервал полностью совпадает с существующим интервалом.")
    public void createTaskShouldThrowExceptionWhenIntervalEquals() {
        getRandomTask(duration, startTime);
        Task task = RandomTask.initRandomTask(duration, startTime);

        assertThrows(ValidationException.class, () -> sut.createTask(task), "Should throw exception");
    }

    @Test
    @DisplayName("Ошибка при создании задачи, когда ее интервал включает существующий интервал.")
    public void createTaskShouldThrowExceptionWhenIntervalIncludes() {
        getRandomTask(duration, startTime);
        Task task = RandomTask.initRandomTask(Duration.ZERO, startTime.plus(Duration.ofMinutes(5)));

        assertThrows(ValidationException.class, () -> sut.createTask(task), "Should throw exception");
    }

    @Test
    @DisplayName("Задача без пересечений интервалов, должна быть обновлена.")
    public void updateTaskShouldSaveTaskWithUniqueInterval() {
        getRandomTask(duration, startTime);
        Task expectedTask = getRandomTask(duration, startTime.plus(Duration.ofMinutes(20)));
        expectedTask.setStartTime(expectedTask.getStartTime().plus(Duration.ofMinutes(40)));

        sut.updateTask(expectedTask);
        Task actualTask = sut.getTask(expectedTask.getId());

        compareTasks(expectedTask, actualTask);
    }

    @Test
    @DisplayName("Задача без обновления интервала, должна быть сохранена.")
    public void updateTaskShouldSaveTaskWithSameInterval() {
        Task expectedTask = getRandomTask(RandomTask.getRandomDuration(), startTime);
        expectedTask.setStartTime(startTime);

        sut.updateTask(expectedTask);
        Task actual = sut.getTask(expectedTask.getId());

        compareTasks(expectedTask, actual);
    }

    @Test
    @DisplayName("Ошибка при обновлении таски с пересечением интервалов.")
    public void updateTaskShouldThrowExceptionWhenIntervalIsNotUnique() {
        getRandomTask(duration, startTime);
        Task task = getRandomTask(duration, startTime.plus(Duration.ofMinutes(20)));
        task.setStartTime(startTime);

        assertThrows(ValidationException.class, () -> sut.updateTask(task), "Should be exception");
    }

    @Test
    @DisplayName("Обновление интервала у задачи, должно изменять ее приоритет.")
    public void updateTaskShouldChangeHisPriority() {
        Task expectedFirstTask = getRandomTask(duration, startTime.plus(Duration.ofMinutes(30)));
        Task task = getRandomTask(duration, startTime);
        Task updatedTask = new Task(task.getName(), task.getDescription(), task.getStatus(), task.getId(), duration,
                startTime.plus(Duration.ofMinutes(60)));

        sut.updateTask(updatedTask);
        List<Task> actualTasks = sut.getPrioritizedTasks();

        compareTasks(expectedFirstTask, actualTasks.getFirst());
        compareTasks(updatedTask, actualTasks.getLast());
    }

    @Test
    @DisplayName("Удаление задачи должно удалять ее из приоритизированного списка.")
    public void deleteTaskShouldDeleteTaskFromPrioritizedTasks() {
        Task expectedTask = getRandomTask(duration, startTime);
        Task taskForDelete = getRandomTask(duration, startTime.plus(Duration.ofMinutes(20)));

        sut.deleteTask(taskForDelete.getId());
        List<Task> actualTasks = sut.getPrioritizedTasks();

        compareTasks(expectedTask, actualTasks.getFirst());
        assertEquals(1, actualTasks.size());
    }

    @Test
    @DisplayName("Удаление всех тасок, должно удалять их из приоритизированного списка.")
    public void deleteAllTasksShouldDeleteAllTasksFromPrioritizedTasks() {
        getRandomTask(duration, startTime);
        getRandomTask(duration, startTime.plus(Duration.ofMinutes(20)));

        sut.deleteTasks();
        List<Task> actualTasks = sut.getPrioritizedTasks();

        assertEquals(0, actualTasks.size());
    }

    @Test
    @DisplayName("Создание подзадачи должно добавлять ее в список приоритизированных задач.")
    public void createSubTaskShouldAddSubTaskToPrioritizedTasks() {
        Epic epic = getRandomEpic();
        SubTask expectedSubTask = RandomTask.initRandomSubTask(epic.getId());

        sut.createSubTask(expectedSubTask);
        List<Task> actualTasks = sut.getPrioritizedTasks();

        compareTasks(expectedSubTask, actualTasks.getFirst());
    }

    @Test
    @DisplayName("Создание подзадачи с пересечением интервалов, должно вызвать исключение.")
    public void createSubTaskShouldThrowExceptionWhenIntervalIntersects() {
        Epic epic = getRandomEpic();
        getRandomSubTask(epic.getId(), duration, startTime);
        SubTask subTaskWithIntersectedInterval = RandomTask.initRandomSubTask(epic.getId(), duration, startTime);

        assertThrows(ValidationException.class, () -> sut.createSubTask(subTaskWithIntersectedInterval),
                "Should be exception");
    }

    @Test
    @DisplayName("Обновление подзадачи c новым интервалом, должно изменить ее приоритизированном списке.")
    public void updateSubTaskShouldChangeSubTaskPriority() {
        Epic epic = getRandomEpic();
        SubTask expectedFirstSubTask = getRandomSubTask(epic.getId(), duration, startTime.plus(Duration.ofMinutes(30)));
        SubTask subTask = getRandomSubTask(epic.getId(), duration, startTime);
        SubTask updatedSubTask = new SubTask(subTask.getName(), subTask.getDescription(), subTask.getId(),
                subTask.getStatus(), subTask.getEpicId(), duration, startTime.plus(Duration.ofMinutes(60)));

        sut.updateSubTask(updatedSubTask);
        List<Task> actualTasks = sut.getPrioritizedTasks();

        compareTasks(expectedFirstSubTask, actualTasks.getFirst());
        compareTasks(updatedSubTask, actualTasks.getLast());
    }

    @Test
    @DisplayName("Обновление подзадачи с пересечением интервалов, должно вызвать исключение.")
    public void updateSubTaskShouldThrowExceptionWhenIntervalIntersects() {
        Epic epic = getRandomEpic();
        getRandomSubTask(epic.getId(), duration, startTime.plus(Duration.ofMinutes(30)));
        SubTask subTask = getRandomSubTask(epic.getId(), duration, startTime);
        SubTask subTaskWithIntersectedInterval = new SubTask(subTask.getName(), subTask.getDescription(),
                subTask.getId(), subTask.getStatus(), subTask.getEpicId(), duration,
                startTime.plus(Duration.ofMinutes(20)));

        assertThrows(ValidationException.class, () -> sut.updateSubTask(subTaskWithIntersectedInterval),
                "Should be exception");
    }

    @Test
    @DisplayName("Удаление подзадачи должно удалять ее из приоритизированного списка.")
    public void deleteSubTaskShouldDeleteSubTaskFromPrioritizedTasks() {
        Epic epic = getRandomEpic();
        SubTask expectedSubTask = getRandomSubTask(epic.getId(), duration, startTime);
        SubTask subTaskForDelete = getRandomSubTask(epic.getId(), duration, startTime.plus(Duration.ofMinutes(20)));

        sut.deleteSubTask(subTaskForDelete.getId());
        List<Task> actualTasks = sut.getPrioritizedTasks();

        compareTasks(expectedSubTask, actualTasks.getFirst());
        assertEquals(1, actualTasks.size());
    }

    @Test
    @DisplayName("Удаление всех подзадач, должно удалять их из приоритизированного списка.")
    public void deleteAllSubTasksShouldDeleteAllSubTasksFromPrioritizedTasks() {
        Epic epic = getRandomEpic();
        getRandomSubTask(epic.getId(), duration, startTime);
        getRandomSubTask(epic.getId(), duration, startTime.plus(Duration.ofMinutes(20)));

        sut.deleteSubTasks();
        List<Task> actualTasks = sut.getPrioritizedTasks();

        assertEquals(0, actualTasks.size());
    }

    @Test
    @DisplayName("Длительность эпика без подзадач, должна быть равна 0.")
    public void getEpicDurationShouldReturnZeroWhenEpicHasNoSubTasks() {
        Epic epic = getRandomEpic();

        assertEquals(Duration.ZERO, epic.getDuration());
    }

    @Test
    @DisplayName("Длительность эпика с подзадачами, должна быть равна сумме длительностей подзадач.")
    public void getEpicDurationShouldReturnSumOfSubTasksDurationWhenEpicHasSubTasks() {
        Epic epic = getRandomEpic();
        SubTask first = getRandomSubTask(epic.getId(), duration, startTime);
        SubTask second = getRandomSubTask(epic.getId(), duration, startTime.plus(Duration.ofMinutes(20)));
        Duration expectedDuration = first.getDuration().plus(second.getDuration());

        assertEquals(expectedDuration, epic.getDuration(), "Duration should be sum of child durations");
    }

    @Test
    @DisplayName("Время  начала у эпика без подзадач,должно быть null.")
    public void getEpicStartTimeShouldReturnNullWhenEpicHasNoSubTasks() {
        Epic epic = getRandomEpic();
        LocalDateTime expectedStartTime = epic.getStartTime();

        assertNull(expectedStartTime, "StartTime should be null");
    }

    @Test
    @DisplayName("Время  начала у эпика без подзадач,должно быть null.")
    public void getEpicEndTimeShouldReturnNullWhenEpicHasNoSubTasks() {
        Epic epic = getRandomEpic();
        LocalDateTime expectedEndTime = epic.getEndTime();

        assertNull(expectedEndTime, "EndTime should be null");
    }

    @Test
    @DisplayName("Время эпика с подзадачами, должно быть равно времени начала первой подзадачи.")
    public void getEpicStartTimeShouldReturnStartTimeOfFirstSubTask() {
        Epic epic = getRandomEpic();
        LocalDateTime expectedStartTime = startTime.minus(Duration.ofDays(1));
        getRandomSubTask(epic.getId(), duration, startTime.plus(Duration.ofMinutes(20)));
        getRandomSubTask(epic.getId(), Duration.ZERO, expectedStartTime);
        getRandomSubTask(epic.getId(), duration, startTime);

        LocalDateTime actualStartTime = epic.getStartTime();

        assertEquals(expectedStartTime, actualStartTime, "Should be start time of first child");
    }

    @Test
    @DisplayName("Время эпика с подзадачами, должно быть равно времени окончания последней подзадачи.")
    public void getEpicEndTimeShouldReturnEndTimeOfLastSubTask() {
        LocalDateTime expectedEndTime = startTime.plusDays(1);
        Epic epic = getRandomEpic();
        getRandomSubTask(epic.getId(), duration, startTime.plusHours(1));
        getRandomSubTask(epic.getId(), Duration.ZERO, expectedEndTime);
        getRandomSubTask(epic.getId(), duration, startTime);

        LocalDateTime actualEndTime = epic.getEndTime();

        assertEquals(expectedEndTime, actualEndTime, "Should be end time of last child");
    }

    @Test
    @DisplayName("Удаление эпика должно удалять дочернюю подзадачу из приоритизированного списка.")
    public void deleteEpicShouldDeleteSubTasksFromPrioritizedTasks() {
        Epic epic = getRandomEpic();
        getRandomSubTask(epic.getId(), duration, startTime);

        sut.deleteEpic(epic.getId());
        List<Task> actualTasks = sut.getPrioritizedTasks();

        assertEquals(0, actualTasks.size());
    }

    @Test
    @DisplayName("Удаление всех эпиков должно удалять подзадачи из приоритизированного списка.")
    public void deleteEpicsShouldDeleteSubTasksFromPrioritizedTasks() {
        Epic epic = getRandomEpic();
        getRandomSubTask(epic.getId(), duration, startTime);

        sut.deleteEpics();
        List<Task> actualTasks = sut.getPrioritizedTasks();

        assertEquals(0, actualTasks.size());
    }
}
