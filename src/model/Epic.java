package model;

import java.util.ArrayList;

public class Epic extends Task {

    protected ArrayList<Integer> subTaskIds;

    public Epic(String name, String description, int id) {
        super(name, description, id);
        subTaskIds = new ArrayList<>();
    }

    public void addSubTaskId(Integer subTaskId) {
        subTaskIds.add(subTaskId);
    }

    public ArrayList<Integer> getSubTaskIds() {
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
    public String toString() {
        return "Epic{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", id=" + id +
                ", type=" + getTaskType() +
                ", subTasksId=" + subTaskIds +
                '}';
    }
}
