import model.Epic;
import model.SubTask;
import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.InMemoryHistoryManager;
import service.Managers;
import service.TaskManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

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
        sut.createTask(task);
        return task;
    }

    private List<Task> getRandomTasks() {
        List<Task> tasks = new ArrayList<>();
        for (int i = 0; i < random.nextInt(5); i++) {
            tasks.add(getRandomTask());
        }
        return tasks;
    }

    private List<Task> getRandomTasks(int countOfTasks) {
        List<Task> tasks = new ArrayList<>(countOfTasks);
        for (int i = 0; i < countOfTasks; i++) {
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
        sut.createEpic(epic);
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
        sut.createSubTask(subTask);
        return subTask;
    }

    private List<SubTask> getRandomSubTasksByEpic(int epicId, int countOfSubTasks) {
        List<SubTask> subTasks = new ArrayList<>();
        for (int i = 0; i < countOfSubTasks; i++) {
            subTasks.add(getRandomSubTask(epicId));
        }
        return subTasks;
    }

    private void markAsWatched(List<Task> tasks) {
        for (Task task : tasks) {
            sut.getTask(task.getId());
        }
    }

    private void markAsWatched(Task task) {
        sut.getTask(task.getId());
    }

    private <T extends Task> boolean compareTasks(List<T> expected, List<T> actual) {
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
    public void createTaskShouldSaveNewTask() {
        Task expected = initRandomTask();

        int savedTask = sut.createTask(expected);
        Task actual = sut.getTask(savedTask);

        assertEquals(expected, actual, "Should be the same task");
    }

    @Test
    public void createTaskWithNullShouldReturnErrorCode() {
        int actual = sut.createTask(null);

        assertEquals(-1, actual);
    }

    @Test
    public void updateTaskShouldUpdateSavedTask() {
        Task saved = getRandomTask();
        Task expected = new Task("newTaskName", "newTaskDescription", saved.getId());
        expected.setStatus(TaskStatus.DONE);

        sut.updateTask(expected);
        Task actual = sut.getTask(saved.getId());

        assertEquals(expected, actual, "Should be the same task");
    }

    @Test
    public void getInvalidTaskShouldReturnNull() {
        Task actual = sut.getTask(random.nextInt());

        assertNull(actual, "Should not be found");
    }

    @Test
    public void deleteTaskShouldDeleteSavedTask() {
        Task saved = getRandomTask();

        sut.deleteTask(saved.getId());
        Task actual = sut.getTask(saved.getId());

        assertNull(actual, "Should not be found");
    }

    @Test
    public void getAllTasksReturnEmptyListWhenNoTasks() {
        List<Task> result = sut.getTasks();
        assertTrue(result.isEmpty(), "Should be empty");
    }

    @Test
    public void getAllTasksReturnOnlyAllSavedTasks() {
        List<Task> expected = getRandomTasks();
        getRandomEpics();

        List<Task> actual = sut.getTasks();

        assertTrue(compareTasks(expected, actual), "Should be same");
    }

    @Test
    public void deleteAllTasksShouldDeleteAllSavedTasks() {
        getRandomTasks();

        sut.deleteTasks();
        List<Task> actual = sut.getTasks();

        assertTrue(actual.isEmpty(), "Should be empty");
    }

    @Test
    public void createEpicShouldSaveNewEpic() {
        Epic expected = initRandomEpic();

        int savedEpic = sut.createEpic(expected);
        Epic actual = sut.getEpic(savedEpic);

        assertEquals(expected, actual, "Should be the same epic");
        assertEquals(TaskStatus.NEW, actual.getStatus(), "Should be new");
    }

    @Test
    public void createEpicWithNullShouldReturnErrorCode() {
        int actual = sut.createEpic(null);

        assertEquals(-1, actual);
    }

    @Test
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
    public void updateEpicShouldUpdateSavedEpic() {
        Epic saved = getRandomEpic();
        Epic expected = new Epic("newEpicName", "newEpicDescription", saved.getId());

        sut.updateEpic(expected);
        Epic actual = sut.getEpic(saved.getId());

        assertEquals(expected, actual, "Should be the same epic");
    }

    @Test
    public void getInvalidEpicShouldReturnNull() {
        Epic actual = sut.getEpic(random.nextInt());

        assertNull(actual, "Should not be found");
    }

    @Test
    public void deleteEpicWithoutSubTasksShouldDeleteSavedEpic() {
        Epic saved = getRandomEpic();

        sut.deleteEpic(saved.getId());
        Epic actual = sut.getEpic(saved.getId());

        assertNull(actual, "Should not be found");
    }

    @Test
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
    public void getAllEpicsReturnOnlyAllSavedEpics() {
        List<Epic> expected = getRandomEpics();

        List<Epic> actual = sut.getEpics();

        assertTrue(compareTasks(expected, actual), "Should be the same list");
    }

    @Test
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
    public void createSubtaskShouldSaveNewSubtaskWithParent() {
        Epic epic = initRandomEpic();
        int createdEpic = sut.createEpic(epic);
        SubTask expected = initRandomSubTask(createdEpic);

        int savedSubTask = sut.createSubTask(expected);
        SubTask actual = sut.getSubTask(savedSubTask);

        assertEquals(expected, actual, "Should be the same subtask");
    }

    @Test
    public void createSubTaskWithInvalidEpicShouldNotSave() {
        SubTask expected = initRandomSubTask(random.nextInt());

        int savedSubTask = sut.createSubTask(expected);
        SubTask actual = sut.getSubTask(savedSubTask);

        assertNull(actual, "Should not be saved");
    }

    @Test
    public void createSubTaskWithNullShouldReturnErrorCode() {
        int actual = sut.createSubTask(null);

        assertEquals(-1, actual);
    }

    @Test
    public void updateSubTaskShouldUpdateSavedSubTask() {
        Epic epic = getRandomEpic();
        SubTask saved = getRandomSubTask(epic.getId());
        SubTask expected = new SubTask("newSubTaskName", "newSubTaskDescription", saved.getId(),
                epic.getId());

        sut.updateSubTask(expected);
        SubTask actual = sut.getSubTask(saved.getId());

        assertEquals(expected, actual, "Should be the same subtask");
    }

    @Test
    public void deleteSubTaskShouldDeleteSavedSubTask() {
        Epic epic = getRandomEpic();
        SubTask saved = getRandomSubTask(epic.getId());

        sut.deleteSubTask(saved.getId());
        SubTask actual = sut.getSubTask(saved.getId());

        assertNull(actual, "Should not be found");
    }

    @Test
    public void getSubTasksShouldReturnEmptyListWhenNoSubTasks() {
        List<SubTask> actual = sut.getSubTasks();

        assertTrue(actual.isEmpty(), "Should be empty");
    }

    @Test
    public void getSubTasksShouldReturnOnlyAllSavedSubTasks() {
        Epic epic = getRandomEpic();
        List<SubTask> expected = getRandomSubTasksByEpic(epic.getId(), 2);

        List<SubTask> actual = sut.getSubTasks();

        assertTrue(compareTasks(expected, actual), "Should be the same list");
    }

    @Test
    public void deleteSubTasksShouldDeleteAllSavedSubTasks() {
        Epic epic = getRandomEpic();
        getRandomSubTasksByEpic(epic.getId(), 2);

        sut.deleteSubTasks();
        List<SubTask> actual = sut.getSubTasks();

        assertTrue(actual.isEmpty(), "Should be empty");
    }

    @Test
    public void getSubTasksOfEpicShouldReturnEmptyListWhenNoSubTasks() {
        Epic epic = getRandomEpic();

        List<SubTask> actual = sut.getSubtasksOfEpic(epic.getId());

        assertTrue(actual.isEmpty(), "Should be empty");
    }

    @Test
    public void getSubTasksOfEpicShouldReturnOnlyChildSubTasks() {
        Epic epic = getRandomEpic();
        List<SubTask> expected = getRandomSubTasksByEpic(epic.getId(), 2);

        List<SubTask> actual = sut.getSubtasksOfEpic(epic.getId());

        assertTrue(compareTasks(expected, actual), "Should be the same list");
    }

    @Test
    public void getHistoryShouldReturnEmptyListWhenNoHistory() {
        List<Task> actual = sut.getHistory();

        assertTrue(actual.isEmpty(), "Should be empty");
    }

    @Test
    public void getHistoryShouldReturnOnlyWatchedTasks() {
        getRandomTask();
        Task expected = getRandomTask();
        sut.getTask(expected.getId());

        Task actual = sut.getHistory().getFirst();

        assertEquals(expected, actual, "Should be the same task");
    }

    @Test
    public void historyCanWorkWithMultipleTasks() {
        Task task = getRandomTask();
        sut.getTask(task.getId());
        Epic epic = getRandomEpic();
        sut.getEpic(epic.getId());
        SubTask subTask = getRandomSubTask(epic.getId());
        sut.getSubTask(subTask.getId());

        List<Task> actual = sut.getHistory();
        assertEquals(3, actual.size(), "Should be 3");
    }

    @Test
    public void historyCanContainTasksWithSameId() {
        Task expect = getRandomTask();
        markAsWatched(expect);
        markAsWatched(expect);

        List<Task> actual = sut.getHistory();

        assertEquals(actual.getFirst(), actual.getLast(), "Should be the same task");
    }

    @Test
    public void historyShouldContainDeletedTasks() {
        Task expected = getRandomTask();
        markAsWatched(expected);
        sut.deleteTask(expected.getId());

        Task actual = sut.getHistory().getFirst();

        assertEquals(expected, actual, "Should be the same task");
    }

    @Test
    public void historyShouldNotContainNull() {
        sut.getTask(random.nextInt());

        List<Task> actual = sut.getHistory();

        assertTrue(actual.isEmpty());
    }

    @Test
    public void historyIsNotClearedWhenSizeLessMaxByOne() {
        List<Task> expected = getRandomTasks(InMemoryHistoryManager.MAX_HISTORY_SIZE - 1);
        markAsWatched(expected);

        List<Task> actual = sut.getHistory();

        assertTrue(compareTasks(expected, actual), "Should be the same list");

    }

    @Test
    public void historyIsNotClearedWhenSizeEqualsMax() {
        List<Task> expected = getRandomTasks(InMemoryHistoryManager.MAX_HISTORY_SIZE);
        markAsWatched(expected);

        List<Task> actual = sut.getHistory();

        assertTrue(compareTasks(expected, actual), "Should be the same list");
    }

    @Test
    public void historyIsClearedWhenSizeLargerMaxByOne() {
        List<Task> tasks = getRandomTasks(InMemoryHistoryManager.MAX_HISTORY_SIZE + 1);
        markAsWatched(tasks);
        List<Task> expected = tasks.subList(1, tasks.size());

        List<Task> actual = sut.getHistory();

        assertTrue(compareTasks(expected, actual), "Should be the same list");
    }

}
