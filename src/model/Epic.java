package model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {

    protected ArrayList<Integer> subTaskIds;

    public Epic(String name, String description, int id) {
        super(name, description, id);
        subTaskIds = new ArrayList<>();
    }

    public void addSubTaskId(Integer subTaskId) {
        subTaskIds.add(subTaskId);
    }

    public List<Integer> getSubTaskIds() {
        return subTaskIds;
    }

    public void cleanSubTaskIds() {
        subTaskIds.clear();
    }

    public void removeSubTask(Integer subTaskId) {
        subTaskIds.remove(subTaskId);
    }

    @Override
    public TaskType getTaskType() {
        return TaskType.EPIC;
    }

    @Override
    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    @Override
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(LocalDateTime time) {
        this.endTime = time;
    }

    @Override
    public String toString() {
        return "Epic{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", id=" + id +
                ", type=" + getTaskType() +
                ", subTasksId=" + subTaskIds +
                ", duration=" + duration +
                ", startTime=" + startTime +
                ", endTime=" + getEndTime() +
                '}';
    }
}
