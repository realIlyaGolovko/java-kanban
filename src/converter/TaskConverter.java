package converter;

import model.Task;
import model.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;

public class TaskConverter implements Converter<Task> {

    @Override
    public String toString(Task task) {
        return String.format("%s,%s,%s,%s,%s,%s,%s",
                task.getId(),
                task.getTaskType(),
                task.getName(),
                task.getStatus(),
                task.getDescription(),
                task.getDuration(),
                task.getStartTime()
        );
    }

    @Override
    public Task fromString(String line) {
        String[] columns = line.split(",");
        int id = Integer.parseInt(columns[0]);
        String name = columns[2];
        TaskStatus status = TaskStatus.valueOf(columns[3]);
        String description = columns[4];
        Duration duration = Duration.parse(columns[5]);
        LocalDateTime startTime = LocalDateTime.parse(columns[6]);
        return new Task(name, description, status, id, duration, startTime);
    }
}
