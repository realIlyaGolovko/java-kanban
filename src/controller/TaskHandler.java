package controller;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import exception.NotFoundException;
import model.Task;
import service.TaskManager;

import java.io.IOException;
import java.util.List;

import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static java.net.HttpURLConnection.HTTP_OK;
import static server.BasePath.TASK;

public class TaskHandler extends BaseHandler {
    public TaskHandler(TaskManager taskManager, Gson gson) {
        super(taskManager, gson);
    }

    @Override
    String prepareResponse(HttpExchange exchange) throws IOException {
        HttpMethod method = HttpMethod.valueOf(exchange.getRequestMethod());
        String response = "";
        int statusCode = HTTP_INTERNAL_ERROR;
        List<String> path = getPath(exchange);
        switch (method) {
            case GET -> {
                response = handleGetRequest(path);
                statusCode = HTTP_OK;
            }
            case POST -> {
                if (isValidBasePath(path, TASK)) {
                    response = handlePostRequest(exchange);
                    statusCode = HTTP_CREATED;
                }
            }
            case DELETE -> {
                if (isValidIdPath(path, TASK)) {
                    handleDeleteRequest(path);
                    statusCode = HTTP_OK;
                }
            }
        }
        exchange.sendResponseHeaders(statusCode, response.length());
        return response;
    }

    private void handleDeleteRequest(List<String> path) {
        int id = getId(path);
        taskManager.deleteTask(id);
    }

    private String handlePostRequest(HttpExchange exchange) throws IOException {
        String bodyRequest = new String(exchange.getRequestBody().readAllBytes(), DEFAULT_CHARSET);
        Task task = gson.fromJson(bodyRequest, Task.class);
        try {
            taskManager.getTask(task.getId());
            taskManager.updateTask(task);
        } catch (NotFoundException e) {
            int taskId = taskManager.createTask(task);
            task.setId(taskId);
        }
        return gson.toJson(task);
    }

    private String handleGetRequest(List<String> path) {
        String result = "";
        if (isValidBasePath(path, TASK)) {
            String jsonString = gson.toJson(taskManager.getTasks());
            result = gson.toJson(jsonString);
        } else if (isValidIdPath(path, TASK)) {
            int id = getId(path);
            Task task = taskManager.getTask(id);
            result = gson.toJson(task);
        }
        return result;
    }
}
