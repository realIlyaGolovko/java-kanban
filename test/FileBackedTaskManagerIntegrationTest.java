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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static util.check.TaskComparator.compareListOfTasks;
import static util.check.TaskComparator.compareTasks;

@DisplayName("Тесты менеджера задач из файла")
public class FileBackedTaskManagerIntegrationTest extends TaskManagerTest<FileBackedTaskManager> {
    private File file;

    @Override
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
        List<Task> expectedTasks = getListOfRandomDifferentTasks();

        List<Task> actual = FileBackedTaskManager.loadFromFile(file).getAllTasks();

        compareListOfTasks(expectedTasks, actual);
    }

    @Test
    @DisplayName("Изменение задач должно сохранять состояние в файл.")
    public void updateShouldSaveTaskToFile() {
        Epic expectedEpic = getRandomEpic();
        SubTask expectedSubTask = getRandomSubTask(expectedEpic.getId());
        expectedSubTask.setStatus(TaskStatus.DONE);
        sut.updateSubTask(expectedSubTask);

        List<Task> actual = FileBackedTaskManager.loadFromFile(file).getAllTasks();

        assertEquals(TaskStatus.DONE, actual.getFirst().getStatus(), "Status of epic should be actual");
        assertEquals(TaskStatus.DONE, actual.getLast().getStatus(), "Status of epic should be actual");
    }

    @Test
    @DisplayName("Удаленные задачи должны быть удалены из файла.")
    public void deleteShouldDeleteTaskFromFile() {
        Task taskForDelete = getRandomTask();
        Task expectedTask = getRandomTask();
        sut.deleteTask(taskForDelete.getId());


        List<Task> actual = FileBackedTaskManager.loadFromFile(file).getAllTasks();

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

        List<Task> actual = FileBackedTaskManager.loadFromFile(file).getHistory();

        compareTasks(expectedTask, actual.getFirst());
        compareTasks(expectedEpic, actual.getLast());
    }

    @Test
    @DisplayName("Сохранение и загрузка задач из пустого файла не должна вызывать ошибок.")
    public void loadFromEmptyFileShouldNotThrowExceptions() {
        int taskId = getRandomTask().getId();
        sut.deleteTask(taskId);

        List<Task> actual = FileBackedTaskManager.loadFromFile(file).getTasks();

        assertEquals(0, actual.size(), "Should be 0");
    }
}
