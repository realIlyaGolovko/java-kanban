import model.Epic;
import model.SubTask;
import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import service.Managers;
import service.TaskManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;


@DisplayName("Тесты менеджера задач в памяти.")
public class InMemoryManagerTest {

    //sut -> system under test
    private TaskManager sut;
    private static final Random random = new Random();

    private Task initRandomTask() {
        return new Task("taskName" + random.nextInt(), "taskDescription" + random.nextInt(),
                random.nextInt());
    }

    private Task getRandomTask() {
        Task task = initRandomTask();
        task.setId(sut.createTask(task));
        return task;
    }

    private List<Task> getRandomTasks() {
        List<Task> tasks = new ArrayList<>();
        for (int i = 0; i < random.nextInt(5); i++) {
            tasks.add(getRandomTask());
        }
        return tasks;
    }

    private Epic initRandomEpic() {
        return new Epic("epicName" + random.nextInt(), "epicDescription" + random.nextInt(),
                random.nextInt());
    }

    private Epic getRandomEpic() {
        Epic epic = initRandomEpic();
        epic.setId(sut.createEpic(epic));
        return epic;
    }

    private List<Epic> getRandomEpics() {
        List<Epic> epics = new ArrayList<>();
        for (int i = 0; i < random.nextInt(5); i++) {
            epics.add(getRandomEpic());
        }
        return epics;
    }

    private SubTask initRandomSubTask(int epicId) {
        return new SubTask("subTaskName" + random.nextInt(), "subTaskDescription" + random.nextInt(),
                random.nextInt(), epicId);
    }

    private SubTask getRandomSubTask(int epicId) {
        SubTask subTask = initRandomSubTask(epicId);
        subTask.setId(sut.createSubTask(subTask));
        return subTask;
    }

    private List<SubTask> getRandomSubTasksByEpic(int epicId, int countOfSubTasks) {
        List<SubTask> subTasks = new ArrayList<>();
        for (int i = 0; i < countOfSubTasks; i++) {
            subTasks.add(getRandomSubTask(epicId));
        }
        return subTasks;
    }

    private void markSubTasksAsWatched(List<SubTask> subTasks) {
        for (SubTask subTask : subTasks) {
            sut.getSubTask(subTask.getId());
        }
    }

    private void markTaskAsWatched(Task task) {
        sut.getTask(task.getId());
    }

    private void markSubTaskAsWatched(SubTask subTask) {
        sut.getSubTask(subTask.getId());
    }

    private void markEpicAsWatched(Epic epic) {
        sut.getEpic(epic.getId());
    }

    private static void compareTasks(Task expected, Task actual) {
        assertEquals(expected.getId(), actual.getId(), "Should be the same Ids");
        assertEquals(expected.getName(), actual.getName(), "Should be the same names");
        assertEquals(expected.getDescription(), actual.getDescription(), "Should be the same descriptions");
        assertEquals(expected.getStatus(), actual.getStatus(), "Should be the same statuses");
        assertEquals(expected.getTaskType(), actual.getTaskType(), "Should be the same types");
    }

    private static void compareEpics(Epic expected, Epic actual) {
        compareTasks(expected, actual);
        assertEquals(expected.getSubTaskIds(), actual.getSubTaskIds(), "Should be the same subTaskIds");
    }

    private static void compareSubTasks(SubTask expected, SubTask actual) {
        compareTasks(expected, actual);
        assertEquals(expected.getEpicId(), actual.getEpicId(), "Should be the same epicIds");
    }

    private static <T extends Task> boolean compareListOfTasks(List<T> expected, List<T> actual) {
        if (expected.size() != actual.size()) {
            return false;
        } else {
            for (int e = 0; e < expected.size(); e++) {
                if (!expected.get(e).equals(actual.get(e))) {
                    return false;
                }
            }
            return true;
        }
    }

    @BeforeEach
    public void setUp() {
        sut = Managers.getDefault();
    }

    @Test
    @DisplayName("При создании задачи, она должна сохраняться в памяти в статусе NEW.")
    public void createTaskShouldSaveNewTask() {
        Task expected = initRandomTask();

        int savedTask = sut.createTask(expected);
        Task actual = sut.getTask(savedTask);

        compareTasks(expected, actual);
        assertEquals(expected.getStatus(), TaskStatus.NEW, "Should be the NEW status");
    }

    @Test
    @DisplayName("При создании задачи, если задача с таким именем уже существует, то должен возвращаться код ошибки.")
    public void createTaskWithNullShouldReturnErrorCode() {
        int actual = sut.createTask(null);

        assertEquals(-1, actual);
    }

    @Test
    @DisplayName("У таски можно изменить наименование, описание и статус.")
    public void updateTaskShouldUpdateSavedTask() {
        Task saved = getRandomTask();
        Task expected = new Task("newTaskName", "newTaskDescription", saved.getId(), TaskStatus.DONE);

        sut.updateTask(expected);
        Task actual = sut.getTask(saved.getId());

        compareTasks(expected, actual);
    }

    @Test
    @DisplayName("Если задачи не существует, должен возвращаться null")
    public void getInvalidTaskShouldReturnNull() {
        Task actual = sut.getTask(random.nextInt());

        assertNull(actual, "Should not be found");
    }

    @Test
    @DisplayName("При удалении задачи, она должна удаляться из памяти.")
    public void deleteTaskShouldDeleteSavedTask() {
        Task saved = getRandomTask();

        sut.deleteTask(saved.getId());
        Task actual = sut.getTask(saved.getId());

        assertNull(actual, "Should not be found");
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

        assertTrue(compareListOfTasks(expected, actual), "Should be same tasks");
    }

    @Test
    @DisplayName("Должен удаллять все сохраненные таски.")
    public void deleteAllTasksShouldDeleteAllSavedTasks() {
        getRandomTasks();

        sut.deleteTasks();
        List<Task> actual = sut.getTasks();

        assertTrue(actual.isEmpty(), "Should be empty");
    }

    @Test
    @DisplayName("Должен сохранять эпик в памяти в статусе NEW.")
    public void createEpicShouldSaveNewEpic() {
        Epic expected = initRandomEpic();

        int savedEpic = sut.createEpic(expected);
        Epic actual = sut.getEpic(savedEpic);


        compareEpics(expected, actual);
        assertEquals(TaskStatus.NEW, actual.getStatus(), "Should be new");
    }

    @Test
    @DisplayName("При попытке сохранить пустой эпик, должен возвращать код ошибки.")
    public void createEpicWithNullShouldReturnErrorCode() {
        int actual = sut.createEpic(null);

        assertEquals(-1, actual);
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

        compareEpics(expected, actual);
    }

    @Test
    @DisplayName("Если эпика не существует, должен возвращаться null")
    public void getInvalidEpicShouldReturnNull() {
        Epic actual = sut.getEpic(random.nextInt());

        assertNull(actual, "Should not be found");
    }

    @Test
    @DisplayName("При удалении эпика, он должен удаляться из памяти.")
    public void deleteEpicWithoutSubTasksShouldDeleteSavedEpic() {
        Epic saved = getRandomEpic();

        sut.deleteEpic(saved.getId());
        Epic actual = sut.getEpic(saved.getId());

        assertNull(actual, "Should not be found");
    }

    @Test
    @DisplayName("При удалении эпика, все его дочерние подзадачи должны удаляться из памяти.")
    public void deleteEpicWithSubTasksShouldDeleteSavedEpicAndSubTasks() {
        Epic epic = getRandomEpic();
        getRandomSubTasksByEpic(epic.getId(), 3);

        sut.deleteEpic(epic.getId());
        Epic actual = sut.getEpic(epic.getId());
        List<SubTask> actualSubTasks = sut.getSubtasksOfEpic(epic.getId());

        assertNull(actual, "Should not be found");
        assertTrue(actualSubTasks.isEmpty(), "Should be empty");
    }

    @Test
    @DisplayName("Должен возвращать только сохраненные эпики.")
    public void getAllEpicsReturnOnlyAllSavedEpics() {
        List<Epic> expected = getRandomEpics();

        List<Epic> actual = sut.getEpics();

        assertTrue(compareListOfTasks(expected, actual), "Should be the same list");
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
        Epic epic = initRandomEpic();
        int createdEpic = sut.createEpic(epic);
        SubTask expected = initRandomSubTask(createdEpic);

        int savedSubTask = sut.createSubTask(expected);
        SubTask actual = sut.getSubTask(savedSubTask);

        compareSubTasks(expected, actual);
    }

    @Test
    @DisplayName("Сабтаска не должны сохраняться в памяти, при попытке сохранить подзадачу с несуществующим эпиком.")
    public void createSubTaskWithInvalidEpicShouldNotSave() {
        SubTask expected = initRandomSubTask(random.nextInt());

        int savedSubTask = sut.createSubTask(expected);
        SubTask actual = sut.getSubTask(savedSubTask);

        assertNull(actual, "Should not be saved");
    }

    @Test
    @DisplayName("Должен возвращать код ошибки, при попытке сохранить подзадачу с null.")
    public void createSubTaskWithNullShouldReturnErrorCode() {
        int actual = sut.createSubTask(null);

        assertEquals(-1, actual);
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

        compareSubTasks(expected, actual);
    }

    @Test
    @DisplayName("При удалении подзадачи, она должна удаляться из памяти.")
    public void deleteSubTaskShouldDeleteSavedSubTask() {
        Epic epic = getRandomEpic();
        SubTask saved = getRandomSubTask(epic.getId());

        sut.deleteSubTask(saved.getId());
        SubTask actual = sut.getSubTask(saved.getId());

        assertNull(actual, "Should not be found");
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

        assertTrue(compareListOfTasks(expected, actual), "Should be the same list");
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

        assertTrue(compareListOfTasks(expected, actual), "Should be the same list");
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
        sut.deleteTask(expected.getId());

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
        sut.getTask(random.nextInt());

        List<Task> actual = sut.getHistory();

        assertTrue(actual.isEmpty());
    }
}
