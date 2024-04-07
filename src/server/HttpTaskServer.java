package server;

import adapter.DurationAdapter;
import adapter.LocalDateTimeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;
import controller.EpicHandler;
import controller.HistoryHandler;
import controller.PriorityHandler;
import controller.SubTaskHandler;
import controller.TaskHandler;
import service.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;

public class HttpTaskServer {
    private static final int PORT = 8080;
    private final HttpServer httpServer;
    private final TaskManager manager;
    private final Gson gson;

    public HttpTaskServer(TaskManager manager) throws IOException {
        this.manager = manager;
        this.gson = getGson();
        this.httpServer = HttpServer.create(new InetSocketAddress("localhost", PORT), 0);
        httpServer.createContext(BasePath.TASK.getRoot(), new TaskHandler(this.manager, this.gson));
        httpServer.createContext(BasePath.SUBTASK.getRoot(), new SubTaskHandler(this.manager, this.gson));
        httpServer.createContext(BasePath.EPIC.getRoot(), new EpicHandler(this.manager, this.gson));
        httpServer.createContext(BasePath.HISTORY.getRoot(), new HistoryHandler(this.manager, this.gson));
        httpServer.createContext(BasePath.PRIORITY.getRoot(), new PriorityHandler(this.manager, this.gson));
    }

    public static Gson getGson() {
        return new GsonBuilder()
                .serializeNulls()
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
    }

    public void start() {
        httpServer.start();
    }

    public void stop() {
        httpServer.stop(0);
    }

}
