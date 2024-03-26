package converter;

import model.SubTask;
import model.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;

public class SubTaskConverter implements Converter<SubTask> {

    @Override
    public String toString(SubTask subTask) {
        return String.format("%s,%s,%s,%s,%s,%s,%s,%s",
                subTask.getId(),
                subTask.getTaskType(),
                subTask.getName(),
                subTask.getStatus(),
                subTask.getDescription(),
                subTask.getEpicId(),
                subTask.getDuration(),
                subTask.getStartTime()
        );
    }

    @Override
    public SubTask fromString(String line) {
        String[] columns = line.split(",");
        int id = Integer.parseInt(columns[0]);
        String name = columns[2];
        TaskStatus status = TaskStatus.valueOf(columns[3]);
        String description = columns[4];
        int epicId = Integer.parseInt(columns[5]);
        Duration duration = Duration.parse(columns[6]);
        LocalDateTime startTime = LocalDateTime.parse(columns[7]);
        return new SubTask(name, description, id, status, epicId, duration, startTime);
    }
}

