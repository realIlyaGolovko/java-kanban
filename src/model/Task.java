package model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public class Task {

    protected String name;
    protected String description;
    protected TaskStatus status;
    protected int id;
    protected Duration duration;
    protected LocalDateTime startTime;
    protected LocalDateTime endTime;

    public Task(String name, String description, int id) {
        this.name = name;
        this.description = description;
        this.status = TaskStatus.NEW;
        this.id = id;
        this.duration = Duration.ZERO;
        this.startTime = LocalDateTime.now();
        this.endTime = this.startTime.plus(this.duration);
    }

    public Task(String name, String description, int id, TaskStatus status) {
        this.name = name;
        this.description = description;
        this.id = id;
        this.status = status;
        this.duration = Duration.ZERO;
        this.startTime = LocalDateTime.now();
        this.endTime = this.startTime.plus(this.duration);
    }

    public Task(String name, String description, TaskStatus status, int id, Duration duration,
                LocalDateTime startTime) {
        this.name = name;
        this.description = description;
        this.status = status;
        this.id = id;
        this.duration = duration;
        this.startTime = startTime;
        this.endTime = this.startTime.plus(this.duration);
    }

    public TaskType getTaskType() {
        return TaskType.TASK;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
        endTime = startTime.plus(this.duration);
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
        endTime = this.startTime.plus(duration);
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Task{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", id=" + id +
                ", duration=" + duration +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }
}
