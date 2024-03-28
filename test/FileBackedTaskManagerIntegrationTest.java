import model.Epic;
import model.SubTask;
import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import service.FileBackedTaskManager;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static util.check.TaskComparator.compareListOfTasks;
import static util.check.TaskComparator.compareTasks;

@DisplayName("Тесты менеджера задач из файла")
public class FileBackedTaskManagerIntegrationTest extends TaskManagerTest<FileBackedTaskManager> {
    private File file;

    @BeforeEach
    public void setUp() {
        try {
            file = File.createTempFile("testData" + random.nextInt(), "csv");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        sut = new FileBackedTaskManager(file);
    }

    @Test
    @DisplayName("Создание задач должно сохранять их в файл.")
    public void createShouldSaveTaskToFile() {
        List<Task> expected = getListOfRandomDifferentTasks();
        List<Task> actualTasks = new ArrayList<>();

        FileBackedTaskManager actual = FileBackedTaskManager.loadFromFile(file);
        actualTasks.addAll(actual.getTasks());
        actualTasks.addAll(actual.getEpics());
        actualTasks.addAll(actual.getSubTasks());

        compareListOfTasks(expected, actualTasks);
    }

    @Test
    @DisplayName("Изменение задачи должно сохранять состояние в файл.")
    public void updateTaskShouldSaveToFile() {
        Task defaultTask = getRandomTask();
        Task expectedTask = new Task("name", "desc", defaultTask.getId(), TaskStatus.DONE);
        sut.updateTask(expectedTask);

        Task actual = FileBackedTaskManager.loadFromFile(file).getTask(expectedTask.getId());

        compareTasks(expectedTask, actual);
    }

    @Test
    @DisplayName("Эпик должен быть завершен при загрузке, если все сабтаски завершены.")
    public void epicShouldBeDoneOnLoadIfAllSubTasksAreDone() {
        Epic expectedEpic = getRandomEpic();
        SubTask expectedSubTask = getRandomSubTask(expectedEpic.getId());
        expectedSubTask.setStatus(TaskStatus.DONE);
        sut.updateSubTask(expectedSubTask);
        SubTask secondExpectedSubTask = getRandomSubTask(expectedEpic.getId());
        secondExpectedSubTask.setStatus(TaskStatus.DONE);
        sut.updateSubTask(secondExpectedSubTask);


        FileBackedTaskManager actual = FileBackedTaskManager.loadFromFile(file);
        TaskStatus actualEpicStatus = actual.getEpic(expectedEpic.getId()).getStatus();
        TaskStatus actualSubTaskStatus = actual.getSubTask(expectedSubTask.getId()).getStatus();
        TaskStatus actualSecondStatus = actual.getSubTask(secondExpectedSubTask.getId()).getStatus();

        assertEquals(TaskStatus.DONE, actualEpicStatus, "Status of epic should be actual");
        assertEquals(TaskStatus.DONE, actualSubTaskStatus, "Status of subTask should be actual");
        assertEquals(TaskStatus.DONE, actualSecondStatus, "Status of subTask should be actual");
    }

    @Test
    @DisplayName("Эпик должен быть NEW при загрузке, если все сабтаски NEW.")
    public void epicShouldBeNewOnLoadIfAllSubTasksAreNew() {
        Epic expectedEpic = getRandomEpic();
        SubTask expectedSubTask = getRandomSubTask(expectedEpic.getId());
        SubTask secondExpectedSubTask = getRandomSubTask(expectedEpic.getId());

        FileBackedTaskManager actual = FileBackedTaskManager.loadFromFile(file);
        TaskStatus actualEpicStatus = actual.getEpic(expectedEpic.getId()).getStatus();
        TaskStatus actualSubTaskStatus = actual.getSubTask(expectedSubTask.getId()).getStatus();
        TaskStatus actualSecondStatus = actual.getSubTask(secondExpectedSubTask.getId()).getStatus();

        assertEquals(TaskStatus.NEW, actualEpicStatus, "Status of epic should be actual");
        assertEquals(TaskStatus.NEW, actualSubTaskStatus, "Status of epic should be actual");
        assertEquals(TaskStatus.NEW, actualSecondStatus, "Status of epic should be actual");
    }

    @Test
    @DisplayName("Эпик должен быть IN_PROGRESS при загрузке, если все сабтаски в промежуточом статусе.")
    public void epicShouldBeInProgressOnLoadIfAllSubTasksAreIntermediate() {
        Epic expectedEpic = getRandomEpic();
        SubTask expectedSubTask = getRandomSubTask(expectedEpic.getId());
        expectedSubTask.setStatus(TaskStatus.DONE);
        sut.updateSubTask(expectedSubTask);
        SubTask secondExpectedSubTask = getRandomSubTask(expectedEpic.getId());

        FileBackedTaskManager actual = FileBackedTaskManager.loadFromFile(file);
        TaskStatus actualEpicStatus = actual.getEpic(expectedEpic.getId()).getStatus();
        TaskStatus actualSubTaskStatus = actual.getSubTask(expectedSubTask.getId()).getStatus();
        TaskStatus actualSecondStatus = actual.getSubTask(secondExpectedSubTask.getId()).getStatus();

        assertEquals(TaskStatus.IN_PROGRESS, actualEpicStatus, "Status of epic should be actual");
        assertEquals(TaskStatus.DONE, actualSubTaskStatus, "Status of SubTask should be actual");
        assertEquals(TaskStatus.NEW, actualSecondStatus, "Status of SubTask should be actual");
    }

    @Test
    @DisplayName("Эпик должен быть IN_PROGRESS при загрузке, если все сабтаски IN_PROGRESS")
    public void epicShouldBeInProgressOnLoadIfAllSubTasksAreInProgress() {
        Epic expectedEpic = getRandomEpic();
        SubTask expectedSubTask = getRandomSubTask(expectedEpic.getId());
        expectedSubTask.setStatus(TaskStatus.IN_PROGRESS);
        sut.updateSubTask(expectedSubTask);
        SubTask secondExpectedSubTask = getRandomSubTask(expectedEpic.getId());
        secondExpectedSubTask.setStatus(TaskStatus.IN_PROGRESS);
        sut.updateSubTask(secondExpectedSubTask);

        FileBackedTaskManager actual = FileBackedTaskManager.loadFromFile(file);
        TaskStatus actualEpicStatus = actual.getEpic(expectedEpic.getId()).getStatus();
        TaskStatus actualSubTaskStatus = actual.getSubTask(expectedSubTask.getId()).getStatus();
        TaskStatus actualSecondStatus = actual.getSubTask(secondExpectedSubTask.getId()).getStatus();

        assertEquals(TaskStatus.IN_PROGRESS, actualEpicStatus, "Status of epic should be actual");
        assertEquals(TaskStatus.IN_PROGRESS, actualSubTaskStatus, "Status of SubTask should be actual");
        assertEquals(TaskStatus.IN_PROGRESS, actualSecondStatus, "Status of SubTask should be actual");
    }


    @Test
    @DisplayName("Удаленные задачи должны быть удалены из файла.")
    public void deleteShouldDeleteTaskFromFile() {
        Task taskForDelete = getRandomTask();
        Task expectedTask = getRandomTask();
        sut.deleteTask(taskForDelete.getId());


        List<Task> actual = FileBackedTaskManager.loadFromFile(file).getTasks();

        compareTasks(expectedTask, actual.getFirst());
        assertEquals(1, actual.size(), "Should be one task in list");
    }

    @Test
    @DisplayName("История должна сохраняться в файл.")
    public void historyShouldSaveInFile() {
        Task expectedTask = getRandomTask();
        markTaskAsWatched(expectedTask);
        Epic expectedEpic = getRandomEpic();
        markEpicAsWatched(expectedEpic);
        SubTask expectedSubTask = getRandomSubTask(expectedEpic.getId());
        markSubTaskAsWatched(expectedSubTask);

        List<Task> actual = FileBackedTaskManager.loadFromFile(file).getHistory();

        compareTasks(expectedTask, actual.getFirst());
        compareTasks(expectedEpic, actual.get(1));
        compareTasks(expectedSubTask, actual.getLast());
    }

    @Test
    @DisplayName("Сохранение и загрузка задач из пустого файла не должна вызывать ошибок.")
    public void loadFromEmptyFileShouldNotThrowExceptions() {
        int taskId = getRandomTask().getId();
        sut.deleteTask(taskId);

        List<Task> actual = FileBackedTaskManager.loadFromFile(file).getTasks();

        assertEquals(0, actual.size(), "Should be 0");
    }

    @Test
    @DisplayName("Загрузка из файла должна восстанавливать приоритет задач.")
    public void loadShouldRecoverPriorityOfTasks() {
        int epicId = getRandomEpic().getId();
        SubTask expectedSecondTask = getRandomSubTask(epicId, duration, startTime.plusHours(1));
        Task expectedFirstTask = getRandomTask(duration, startTime);
        SubTask expectedThirdTask = getRandomSubTask(epicId, duration, startTime.plusDays(1));

        List<Task> actual = FileBackedTaskManager.loadFromFile(file).getPrioritizedTasks();

        compareTasks(expectedFirstTask, actual.getFirst());
        compareTasks(expectedSecondTask, actual.get(1));
        compareTasks(expectedThirdTask, actual.getLast());
    }

    @Test
    @DisplayName("Загрузка из файла должна восстанавливать временной интервал задачи.")
    public void loadShouldRecoverTimeIntervalOfTask() {
        Task expectedTask = getRandomTask(duration, startTime);

        Task actual = FileBackedTaskManager.loadFromFile(file).getTask(expectedTask.getId());

        assertEquals(expectedTask.getDuration(), actual.getDuration(), "Duration should be actual");
        assertEquals(expectedTask.getStartTime(), actual.getStartTime(), "StartTime should be actual");
        assertEquals(expectedTask.getEndTime(), actual.getEndTime(), "EndTime should be actual");
    }

    @Test
    @DisplayName("Загрузка из файла должна восстановить временной интервал эпика.")
    public void loadShouldRecoverTimeIntervalOfEpic() {
        Epic expectedEpic = getRandomEpic();
        SubTask secondTask = getRandomSubTask(expectedEpic.getId(), duration, startTime.plusHours(1));
        SubTask thirdTask = getRandomSubTask(expectedEpic.getId(), duration, startTime.plusDays(1));
        SubTask firstTask = getRandomSubTask(expectedEpic.getId(), duration, startTime);
        Duration expectedDuration = firstTask.getDuration().plus(secondTask.getDuration()
                .plus(thirdTask.getDuration()));
        LocalDateTime expectedStartTime = firstTask.getStartTime();
        LocalDateTime expectedEndTime = thirdTask.getEndTime();

        Epic actualEpic = FileBackedTaskManager.loadFromFile(file).getEpic(expectedEpic.getId());

        assertEquals(expectedDuration, actualEpic.getDuration(), "Duration should be actual");
        assertEquals(expectedStartTime, actualEpic.getStartTime(), "StartTime should be actual");
        assertEquals(expectedEndTime, actualEpic.getEndTime(), "EndTime should be actual");
    }
}
