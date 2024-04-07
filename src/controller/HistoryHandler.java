package controller;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import server.BasePath;
import service.TaskManager;

import java.io.IOException;
import java.util.List;

import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static java.net.HttpURLConnection.HTTP_OK;

public class HistoryHandler extends BaseHandler {
    public HistoryHandler(TaskManager taskManager, Gson gson) {
        super(taskManager, gson);
    }

    @Override
    String prepareResponse(HttpExchange exchange) throws IOException {
        HttpMethod method = HttpMethod.valueOf(exchange.getRequestMethod());
        String response = "";
        int statusCode = HTTP_INTERNAL_ERROR;
        List<String> path = getPath(exchange);
        if (method == HttpMethod.GET) {
            if (isValidBasePath(path, BasePath.HISTORY)) {
                String jsonString = gson.toJson(taskManager.getHistory());
                response = gson.toJson(jsonString);
            }
            statusCode = HTTP_OK;
        }
        exchange.sendResponseHeaders(statusCode, response.length());
        return response;
    }
}
