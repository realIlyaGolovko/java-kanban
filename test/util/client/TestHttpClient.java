package util.client;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import model.Epic;
import model.SubTask;
import model.Task;
import server.BasePath;
import server.HttpTaskServer;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class TestHttpClient {
    public static final String HOST = "http://localhost:8080";
    private static final Gson gson = HttpTaskServer.getGson();

    public static HttpResponse<String> get(String path) throws IOException, InterruptedException {
        URI uri = URI.create(HOST + path);
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest requestBuilder = HttpRequest
                    .newBuilder()
                    .header("content-type", "application/json")
                    .uri(uri)
                    .GET()
                    .build();
            HttpResponse.BodyHandler<String> handler = HttpResponse.BodyHandlers.ofString();
            return client.send(requestBuilder, handler);
        }
    }

    public static HttpResponse<String> getTasks() throws IOException, InterruptedException {
        return get(BasePath.TASK.getRoot());
    }

    public static HttpResponse<String> getTask(int taskId) throws IOException, InterruptedException {
        return get(String.format("%s/%s", BasePath.TASK.getRoot(), taskId));
    }

    public static HttpResponse<String> getPrioritizedTasks() throws IOException, InterruptedException {
        return get(BasePath.PRIORITY.getRoot());
    }

    public static HttpResponse<String> getHistory() throws IOException, InterruptedException {
        return get(BasePath.HISTORY.getRoot());
    }

    public static HttpResponse<String> getEpic() throws IOException, InterruptedException {
        return get(BasePath.EPIC.getRoot());
    }

    public static HttpResponse<String> getEpic(int epicId) throws IOException, InterruptedException {
        return get(String.format("%s/%s", BasePath.EPIC.getRoot(), epicId));
    }

    public static HttpResponse<String> getSubTasks() throws IOException, InterruptedException {
        return get(BasePath.SUBTASK.getRoot());
    }

    public static HttpResponse<String> getSubTask(int subTaskId) throws IOException, InterruptedException {
        return get((String.format("%s/%s", BasePath.SUBTASK.getRoot(), subTaskId)));
    }

    public static HttpResponse<String> getSubTaskByEpic(int epicId) throws IOException, InterruptedException {
        return get((String.format("%s/%s%s", BasePath.EPIC.getRoot(), epicId, BasePath.SUBTASK.getRoot())));
    }

    public static HttpResponse<String> post(String path, Task task) throws IOException, InterruptedException {
        URI uri = URI.create(HOST + path);
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest requestBuilder = HttpRequest
                    .newBuilder()
                    .header("content-type", "application/json")
                    .uri(uri)
                    .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(task)))
                    .build();
            HttpResponse.BodyHandler<String> handler = HttpResponse.BodyHandlers.ofString();
            return client.send(requestBuilder, handler);
        }
    }

    public static HttpResponse<String> postTask(Task task) throws IOException, InterruptedException {
        return post(BasePath.TASK.getRoot(), task);
    }

    public static HttpResponse<String> postEpic(Epic epic) throws IOException, InterruptedException {
        return post(BasePath.EPIC.getRoot(), epic);
    }

    public static HttpResponse<String> postSubTask(SubTask subTask) throws IOException, InterruptedException {
        return post(BasePath.SUBTASK.getRoot(), subTask);
    }

    public static HttpResponse<String> delete(String path) throws IOException, InterruptedException {
        URI uri = URI.create(HOST + path);
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest requestBuilder = HttpRequest
                    .newBuilder()
                    .header("content-type", "application/json")
                    .uri(uri)
                    .DELETE()
                    .build();
            HttpResponse.BodyHandler<String> handler = HttpResponse.BodyHandlers.ofString();
            return client.send(requestBuilder, handler);
        }
    }

    public static HttpResponse<String> deleteTask(int taskId) throws IOException, InterruptedException {
        return delete(String.format("%s/%s", BasePath.TASK.getRoot(), taskId));
    }

    public static HttpResponse<String> deleteSubTask(int subTaskId) throws IOException, InterruptedException {
        return delete(String.format("%s/%s", BasePath.SUBTASK.getRoot(), subTaskId));
    }

    public static HttpResponse<String> deleteEpic(int epicId) throws IOException, InterruptedException {
        return delete(String.format("%s/%s", BasePath.EPIC.getRoot(), epicId));
    }

    private static String parseJson(HttpResponse<String> response) {
        String rawJson = response.body();
        String json = rawJson.startsWith("\"") ? rawJson.substring(1, rawJson.length() - 1) : rawJson;
        return json.replace("\\\"", "\"");
    }

    public static List<Task> parseJsonToListOfTask(HttpResponse<String> response) {
        String json = parseJson(response);
        return gson.fromJson(json, new TaskListTypeToken().getType());
    }

    public static List<Epic> parseJsonToListOfEpic(HttpResponse<String> response) {
        String json = parseJson(response);
        return gson.fromJson(json, new EpicListTypeToken().getType());
    }

    public static List<SubTask> parseJsonToListOfSubTask(HttpResponse<String> response) {
        String json = parseJson(response);
        return gson.fromJson(json, new SubTaskListTypeToken().getType());
    }

    private static class TaskListTypeToken extends TypeToken<List<Task>> {
    }

    private static class SubTaskListTypeToken extends TypeToken<List<SubTask>> {
    }

    private static class EpicListTypeToken extends TypeToken<List<Epic>> {
    }


}
