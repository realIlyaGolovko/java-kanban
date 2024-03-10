package converter;

import model.Task;
import model.TaskStatus;

public class TaskConverter implements Converter<Task> {

    @Override
    public String toString(Task task) {
        return String.format("%s,%s,%s,%s,%s",
                task.getId(),
                task.getTaskType(),
                task.getName(),
                task.getStatus(),
                task.getDescription());
    }

    @Override
    public Task fromString(String line) {
        String[] columns = line.split(",");
        int id = Integer.parseInt(columns[0]);
        String name = columns[2];
        TaskStatus status = TaskStatus.valueOf(columns[3]);
        String description = columns[4];
        return new Task(name, description, id, status);
    }
}
