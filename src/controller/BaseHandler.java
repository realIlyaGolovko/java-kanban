package controller;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import exception.NotFoundException;
import exception.ValidationException;
import server.BasePath;
import service.TaskManager;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static java.net.HttpURLConnection.HTTP_NOT_ACCEPTABLE;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;

public abstract class BaseHandler implements HttpHandler {
    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    protected final TaskManager taskManager;
    protected final Gson gson;

    public BaseHandler(TaskManager taskManager, Gson gson) {
        this.taskManager = taskManager;
        this.gson = gson;
    }

    abstract String prepareResponse(HttpExchange exchange) throws IOException;

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String response = prepareResponse(exchange);
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=" + DEFAULT_CHARSET);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        } catch (Exception e) {
            errorHandle(exchange, e);
        } finally {
            exchange.close();
        }
    }

    protected void errorHandle(HttpExchange exchange, Exception exception) throws IOException {
        int responseCode = (exception instanceof ValidationException) ? HTTP_NOT_ACCEPTABLE :
                (exception instanceof NotFoundException) ? HTTP_NOT_FOUND : HTTP_INTERNAL_ERROR;
        writeResponse(exchange, responseCode, gson.toJson(exception.getMessage()));
    }

    protected List<String> getPath(HttpExchange exchange) {
        return Arrays.stream(exchange
                        .getRequestURI()
                        .getPath()
                        .trim()
                        .toUpperCase()
                        .split("/"))
                .filter(text -> !text.isBlank())
                .toList();
    }

    protected int getId(List<String> path) {
        return Integer.parseInt(path.get(1));
    }

    protected boolean isValidBasePath(List<String> path, BasePath base) {
        return (path.size() == 1) && (path.getLast().equals(base.getValue()));
    }

    protected boolean isValidIdPath(List<String> path, BasePath base) {
        return (path.size()) == 2 && (path.getFirst().equals(base.getValue()));
    }

    private void writeResponse(HttpExchange exchange, int responseCode, String responseText) throws IOException {
        exchange.sendResponseHeaders(responseCode, responseText.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseText.getBytes(DEFAULT_CHARSET));
        }
    }
}
