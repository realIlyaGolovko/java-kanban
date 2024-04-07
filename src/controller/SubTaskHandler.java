package controller;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import exception.NotFoundException;
import model.SubTask;
import service.TaskManager;

import java.io.IOException;
import java.util.List;

import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static java.net.HttpURLConnection.HTTP_OK;
import static server.BasePath.SUBTASK;

public class SubTaskHandler extends BaseHandler {
    public SubTaskHandler(TaskManager taskManager, Gson gson) {
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
                if (isValidBasePath(path, SUBTASK)) {
                    response = handlePostRequest(exchange);
                    statusCode = HTTP_CREATED;
                }
            }
            case DELETE -> {
                if (isValidIdPath(path, SUBTASK)) {
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
        taskManager.deleteSubTask(id);
    }

    private String handlePostRequest(HttpExchange exchange) throws IOException {
        String bodyRequest = new String(exchange.getRequestBody().readAllBytes(), DEFAULT_CHARSET);
        SubTask subTask = gson.fromJson(bodyRequest, SubTask.class);
        try {
            taskManager.getSubTask(subTask.getId());
            taskManager.updateSubTask(subTask);
        } catch (NotFoundException e) {
            int subTaskId = taskManager.createSubTask(subTask);
            subTask.setId(subTaskId);
        }
        return gson.toJson(subTask);
    }

    private String handleGetRequest(List<String> path) {
        String result = "";
        if (isValidBasePath(path, SUBTASK)) {
            String jsonString = gson.toJson(taskManager.getSubTasks());
            result = gson.toJson(jsonString);
        } else if (isValidIdPath(path, SUBTASK)) {
            int id = getId(path);
            SubTask subTask = taskManager.getSubTask(id);
            result = gson.toJson(subTask);
        }
        return result;
    }
}
