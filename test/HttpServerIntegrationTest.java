import com.google.gson.Gson;
import model.Epic;
import model.SubTask;
import model.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import server.HttpTaskServer;
import service.Managers;
import service.TaskManager;
import util.client.TestHttpClient;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_NOT_ACCEPTABLE;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static util.check.TaskComparator.compareListOfTasks;
import static util.check.TaskComparator.compareTasks;
import static util.check.TaskComparator.compareTasksWithoutId;
import static util.testdata.RandomTask.initRandomEpic;
import static util.testdata.RandomTask.initRandomSubTask;
import static util.testdata.RandomTask.initRandomTask;

@DisplayName("Интеграционные тесты Http сервера.")
public class HttpServerIntegrationTest {
    HttpTaskServer sut;
    TaskManager manager;
    Gson gson;


    @BeforeEach
    void setUp() throws IOException {
        manager = Managers.getDefault();
        sut = new HttpTaskServer(manager);
        sut.start();
        gson = HttpTaskServer.getGson();
    }

    @AfterEach
    void tearDown() {
        sut.stop();
    }

    @Test
    @DisplayName("Должен вернуть список отсортированных по приоритету задач.")
    void shouldReturnPrioritizedTasks() throws IOException, InterruptedException {
        LocalDateTime startTime = LocalDateTime.now();
        Duration duration = Duration.ofMinutes(10);
        int thirdEpicId = manager.createTask(initRandomTask(duration, startTime.plus(Duration.ofMinutes(40))));
        Task thirdTask = manager.getTask(thirdEpicId);
        int firstTaskId = manager.createTask(initRandomTask(duration, startTime));
        Task firstTask = manager.getTask(firstTaskId);
        int secondTaskId = manager.createTask(initRandomTask(duration, startTime.plus(Duration.ofMinutes(20))));
        Task secondTask = manager.getTask(secondTaskId);

        var response = TestHttpClient.getPrioritizedTasks();
        List<Task> actual = TestHttpClient.parseJsonToListOfTask(response);

        assertEquals(HTTP_OK, response.statusCode());
        compareTasks(firstTask, actual.getFirst());
        compareTasks(secondTask, actual.get(1));
        compareTasks(thirdTask, actual.getLast());

    }

    @Test
    @DisplayName("Должен вернуть список просмотренных задач.")
    void shouldReturnHistory() throws IOException, InterruptedException {
        manager.createEpic(initRandomEpic());
        int id = manager.createTask(initRandomTask());
        Task expectedTask = manager.getTask(id);

        var response = TestHttpClient.getHistory();
        List<Task> actual = TestHttpClient.parseJsonToListOfTask(response);

        assertEquals(HTTP_OK, response.statusCode());
        assertEquals(1, actual.size());
        compareTasks(expectedTask, actual.getFirst());
    }

    @Test
    @DisplayName("Должен вернуть список всех эпиков.")
    void shouldReturnAllEpics() throws IOException, InterruptedException {
        List<Epic> expected = IntStream.range(0, 3)
                .mapToObj(i -> initRandomEpic())
                .toList();
        expected.forEach(epic -> manager.createEpic(epic));

        var response = TestHttpClient.getEpic();
        List<Epic> actual = TestHttpClient.parseJsonToListOfEpic(response);


        assertEquals(HTTP_OK, response.statusCode());
        compareListOfTasks(expected, actual);
    }

    @Test
    @DisplayName("Должен вернуть список всех задач.")
    void shouldReturnAllTasks() throws IOException, InterruptedException {
        List<Task> expected = IntStream.range(0, 3)
                .mapToObj(i -> initRandomTask())
                .toList();
        expected.forEach(task -> manager.createTask(task));

        var response = TestHttpClient.getTasks();
        List<Task> actual = TestHttpClient.parseJsonToListOfTask(response);


        assertEquals(HTTP_OK, response.statusCode());
        compareListOfTasks(expected, actual);
    }

    @Test
    @DisplayName("Должен вернуть список всех подзадач.")
    void shouldReturnAllSubTasks() throws IOException, InterruptedException {
        int epicId = manager.createEpic(initRandomEpic());
        List<SubTask> expected = IntStream.range(0, 3)
                .mapToObj(i -> initRandomSubTask(epicId))
                .toList();
        expected.forEach(subTask -> manager.createSubTask(subTask));

        var response = TestHttpClient.getSubTasks();
        List<SubTask> actual = TestHttpClient.parseJsonToListOfSubTask(response);


        assertEquals(HTTP_OK, response.statusCode());
        compareListOfTasks(expected, actual);
    }

    @Test
    @DisplayName("Должен вернуть список дочерних подзадач по id эпика.")
    void shouildReturnListOfSubTasksByEpic() throws IOException, InterruptedException {
        int epicId = manager.createEpic(initRandomEpic());
        List<SubTask> expected = IntStream.range(0, 3)
                .mapToObj(i -> initRandomSubTask(epicId))
                .toList();
        expected.forEach(subTask -> manager.createSubTask(subTask));

        var response = TestHttpClient.getSubTaskByEpic(epicId);
        List<SubTask> actual = TestHttpClient.parseJsonToListOfSubTask(response);


        assertEquals(HTTP_OK, response.statusCode());
        compareListOfTasks(expected, actual);
    }

    @Test
    @DisplayName("Должен вернуть эпик по его id.")
    void shouldReturnEpicById() throws IOException, InterruptedException {
        Epic expected = manager.getEpic(manager.createEpic(initRandomEpic()));

        var response = TestHttpClient.getEpic(expected.getId());
        Epic actual = gson.fromJson(response.body(), Epic.class);

        assertEquals(HTTP_OK, response.statusCode());
        compareTasks(expected, actual);
    }

    @Test
    @DisplayName("Должен вернуть задачу по ее id.")
    public void shouldReturnTaskById() throws IOException, InterruptedException {
        Task expected = manager.getTask(manager.createTask(initRandomTask()));

        var response = TestHttpClient.getTask(expected.getId());
        Task actual = gson.fromJson(response.body(), Task.class);

        assertEquals(HTTP_OK, response.statusCode());
        compareTasks(expected, actual);
    }

    @Test
    @DisplayName("Должен вернуть подзадачу по ее id.")
    void shouldReturnSubTaskById() throws IOException, InterruptedException {
        int epicId = manager.createEpic(initRandomEpic());
        SubTask expected = manager.getSubTask(manager.createSubTask(initRandomSubTask(epicId)));

        var response = TestHttpClient.getSubTask(expected.getId());
        SubTask actual = gson.fromJson(response.body(), SubTask.class);

        assertEquals(HTTP_OK, response.statusCode());
        compareTasks(expected, actual);
    }

    @Test
    @DisplayName("Должен вернуть ошибку, если эпик не найден.")
    void shouldReturnErrorWhenEpicIsNotFound() throws IOException, InterruptedException {
        var actual = TestHttpClient.getEpic(-1);

        assertEquals(HTTP_NOT_FOUND, actual.statusCode());
    }

    @Test
    @DisplayName("Должен вернуть ошибку, если эпик не найден.")
    void getSubTasksByEpicShouldThrowsExceptionWhenEpicNotFound() throws IOException, InterruptedException {
        var response = TestHttpClient.getSubTaskByEpic(-1);

        assertEquals(HTTP_NOT_FOUND, response.statusCode());
    }

    @Test
    @DisplayName("Должен вернуть ошибку, если задача не найден.")
    void shouldReturnErrorWhenTaskIsNotFound() throws IOException, InterruptedException {
        var response = TestHttpClient.getTask(-1);

        assertEquals(HTTP_NOT_FOUND, response.statusCode());
    }

    @Test
    @DisplayName("Должен вернуть ошибку, если подзадача не найдена.")
    void shouldReturnErrorWhenSubTaskIsNotFound() throws IOException, InterruptedException {
        var response = TestHttpClient.getSubTask(-1);

        assertEquals(HTTP_NOT_FOUND, response.statusCode());
    }

    @Test
    @DisplayName("Должен вернуть ошибку, если есть пересечение интервалов у задач.")
    void shouldReturnErrorWhenTaskIntervalIncludes() throws IOException, InterruptedException {
        LocalDateTime startTime = LocalDateTime.now();
        Duration duration = Duration.ofMinutes(10);
        manager.createTask(initRandomTask(duration, startTime));
        Task badIntervalTask = initRandomTask(duration, startTime);

        var response = TestHttpClient.postTask(badIntervalTask);

        assertEquals(HTTP_NOT_ACCEPTABLE, response.statusCode());
        assertEquals(1, manager.getTasks().size());
    }

    @Test
    @DisplayName("Должен вернуть ошибку, если есть пересечение интервалов у подзадач.")
    void shouldReturnErrorWhenSubTaskIntervalIncludes() throws IOException, InterruptedException {
        LocalDateTime startTime = LocalDateTime.now();
        Duration duration = Duration.ofMinutes(10);
        manager.createTask(initRandomTask(duration, startTime));
        int epicId = manager.createEpic(initRandomEpic());
        SubTask badIntervalSubTask = initRandomSubTask(epicId, duration, startTime);

        var response = TestHttpClient.postSubTask(badIntervalSubTask);

        assertEquals(HTTP_NOT_ACCEPTABLE, response.statusCode());
        assertEquals(1, manager.getTasks().size());
    }

    @Test
    @DisplayName("Должен создать новый эпик.")
    void shouldCreateNewEpic() throws IOException, InterruptedException {
        Epic expected = initRandomEpic();

        var response = TestHttpClient.postEpic(expected);
        Epic actual = gson.fromJson(response.body(), Epic.class);

        assertEquals(HTTP_CREATED, response.statusCode());
        compareTasksWithoutId(expected, actual);
    }

    @Test
    @DisplayName("Должен создать новую задачу.")
    void shouldCreateNewTask() throws IOException, InterruptedException {
        Task expected = initRandomTask();

        var response = TestHttpClient.postTask(expected);
        Task actual = gson.fromJson(response.body(), Task.class);

        assertEquals(HTTP_CREATED, response.statusCode());
        compareTasksWithoutId(expected, actual);
    }

    @Test
    @DisplayName("Должен создать новую подзадачу.")
    void shouldCreateNewSubTask() throws IOException, InterruptedException {
        int epicId = manager.createEpic(initRandomEpic());
        SubTask expected = initRandomSubTask(epicId);

        var response = TestHttpClient.postSubTask(expected);
        SubTask actual = gson.fromJson(response.body(), SubTask.class);

        assertEquals(HTTP_CREATED, response.statusCode());
        compareTasksWithoutId(expected, actual);
    }

    @Test
    @DisplayName("Должен обновить задачу.")
    void shouldUpdateTask() throws IOException, InterruptedException {
        Task saved = initRandomTask();
        int taskId = manager.createTask(saved);
        Task expected = initRandomTask();
        expected.setId(taskId);

        var response = TestHttpClient.postTask(expected);
        Task actual = gson.fromJson(response.body(), Task.class);

        assertEquals(HTTP_CREATED, response.statusCode());
        compareTasksWithoutId(expected, actual);
    }

    @Test
    @DisplayName("Должен обновить подзадачу.")
    void shouldUpdateSubTask() throws IOException, InterruptedException {
        int epicId = manager.createEpic(initRandomEpic());
        SubTask saved = initRandomSubTask(epicId);
        int subTaskId = manager.createSubTask(saved);
        SubTask expected = initRandomSubTask(epicId);
        expected.setId(subTaskId);

        var response = TestHttpClient.postSubTask(expected);
        Task actual = gson.fromJson(response.body(), SubTask.class);

        assertEquals(HTTP_CREATED, response.statusCode());
        compareTasksWithoutId(expected, actual);
    }

    @Test
    @DisplayName("Должен выбросить исключение, если тело POST запроса для эпика пустое.")
    void shouldTrowsExceptionWhenEpicPostBodyIsEmpty() throws IOException, InterruptedException {
        var response = TestHttpClient.postEpic(null);

        assertEquals(HTTP_BAD_REQUEST, response.statusCode());
    }

    @Test
    @DisplayName("Должен выбросить исключение, если тело POST запроса для подзадачи пустое.")
    void shouldTrowsExceptionWhenSubTaskPostBodyIsEmpty() throws IOException, InterruptedException {
        var response = TestHttpClient.postSubTask(null);

        assertEquals(HTTP_BAD_REQUEST, response.statusCode());
    }

    @Test
    @DisplayName("Должен выбросить исключение, если тело POST запроса для задачи пустое.")
    void shouldTrowsExceptionWhenTaskPostBodyIsEmpty() throws IOException, InterruptedException {
        var response = TestHttpClient.postTask(null);

        assertEquals(HTTP_BAD_REQUEST, response.statusCode());
    }

    @Test
    @DisplayName("Должен удалить эпик.")
    void shouldDeleteEpic() throws IOException, InterruptedException {
        int savedEpicId = manager.createEpic(initRandomEpic());

        var response = TestHttpClient.deleteEpic(savedEpicId);
        var actual = manager.getEpics();

        assertEquals(HTTP_OK, response.statusCode());
        assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("Должен удалять задачу.")
    public void shouldDeleteTask() throws IOException, InterruptedException {
        int saved = manager.createTask(initRandomTask());

        var response = TestHttpClient.deleteTask(saved);
        var actual = manager.getTasks();

        assertEquals(HTTP_OK, response.statusCode());
        assertTrue(actual.isEmpty());
    }

    @Test
    @DisplayName("Должен удалять подзадачу.")
    public void shouldDeleteSubTask() throws IOException, InterruptedException {
        int epicId = manager.createEpic(initRandomEpic());
        int saved = manager.createSubTask(initRandomSubTask(epicId));

        var response = TestHttpClient.deleteSubTask(saved);
        var actual = manager.getSubTasks();

        assertEquals(HTTP_OK, response.statusCode());
        assertTrue(actual.isEmpty());
    }
}
