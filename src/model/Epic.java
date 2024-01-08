package model;

public class Epic extends Task {

    public Epic(String name, String description, int id) {
        super(name, description, id);
    }

    @Override
    public TaskType getTaskType() {
        return TaskType.EPIC;
    }

    @Override
    public String toString() {
        return "Epic{" +
                "name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status=" + getStatus() +
                ", id=" + getId() +
                ", type=" + getTaskType() +
                '}';
    }
}