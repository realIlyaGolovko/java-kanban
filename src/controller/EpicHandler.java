package controller;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import model.Epic;
import model.SubTask;
import service.TaskManager;

import java.io.IOException;
import java.util.List;

import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static java.net.HttpURLConnection.HTTP_OK;
import static server.BasePath.EPIC;
import static server.BasePath.SUBTASK;

public class EpicHandler extends BaseHandler {

    public EpicHandler(TaskManager taskManager, Gson gson) {
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
                if (isValidBasePath(path, EPIC)) {
                    response = handlePostRequest(exchange);
                    statusCode = HTTP_CREATED;
                }
            }
            case DELETE -> {
                if (isValidIdPath(path, EPIC)) {
                    handleDeleteRequest(path);
                    statusCode = HTTP_OK;
                }
            }
        }
        exchange.sendResponseHeaders(statusCode, response.length());
        return response;
    }

    private String handleGetRequest(List<String> path) {
        String result = "";
        if (isValidBasePath(path, EPIC)) {
            String jsonString = gson.toJson(taskManager.getEpics());
            result = gson.toJson(jsonString);
        } else if (isValidIdPath(path, EPIC)) {
            int id = getId(path);
            Epic epic = taskManager.getEpic(id);
            result = gson.toJson(epic);
        } else if (isValidSubTaskPath(path)) {
            int id = getId(path);
            List<SubTask> subtasksId = taskManager.getSubtasksOfEpic(id);
            result = gson.toJson(subtasksId);
        }
        return result;
    }

    private String handlePostRequest(HttpExchange exchange) throws IOException {
        String bodyRequest = new String(exchange.getRequestBody().readAllBytes(), DEFAULT_CHARSET);
        Epic epic = gson.fromJson(bodyRequest, Epic.class);
        int epicId = taskManager.createEpic(epic);
        epic.setId(epicId);
        return gson.toJson(epic);
    }

    private void handleDeleteRequest(List<String> path) {
        int id = getId(path);
        taskManager.deleteEpic(id);
    }

    private boolean isValidSubTaskPath(List<String> path) {
        return (path.size() == 3) && (path.getFirst().equals(EPIC.getValue())) &&
                path.getLast().equals(SUBTASK.getValue());
    }
}
