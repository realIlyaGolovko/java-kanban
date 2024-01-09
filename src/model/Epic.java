package model;

import java.util.ArrayList;
import java.util.Objects;

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Epic epic = (Epic) o;
        return Objects.equals(subTaskIds, epic.subTaskIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), subTaskIds);
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