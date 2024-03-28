package model;

import java.time.Duration;
import java.time.LocalDateTime;

public class SubTask extends Task {

    private final int epicId;

    public SubTask(String name, String description, int id, int epicId) {
        super(name, description, id);
        this.epicId = epicId;
    }

    public SubTask(String name, String description, int id, TaskStatus status, int epicId,
                   Duration duration, LocalDateTime startTime) {
        super(name, description, status, id, duration, startTime);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public TaskType getTaskType() {
        return TaskType.SUBTASK;
    }

    @Override
    public String toString() {
        return "SubTask{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", id=" + id +
                ", type=" + getTaskType() +
                ", epicId=" + epicId +
                ", duration=" + duration +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }

}
