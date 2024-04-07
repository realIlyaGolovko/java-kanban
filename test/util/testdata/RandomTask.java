package util.testdata;

import model.Epic;
import model.SubTask;
import model.Task;
import model.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Random;

public class RandomTask {
    public static final Random random = new Random();

    public static Task initRandomTask() {
        return new Task("taskName" + random.nextInt(), "taskDescription" + random.nextInt(),
                random.nextInt());
    }

    public static Task initRandomTask(Duration duration, LocalDateTime startTime) {
        return new Task("taskName" + random.nextInt(), "taskDescription" + random.nextInt(),
                TaskStatus.NEW, random.nextInt(), duration, startTime);
    }

    public static Epic initRandomEpic() {
        return new Epic("epicName" + random.nextInt(), "epicDescription" + random.nextInt(),
                random.nextInt());
    }

    public static SubTask initRandomSubTask(int epicId) {
        return new SubTask("subTaskName" + random.nextInt(), "subTaskDescription" + random.nextInt(),
                random.nextInt(), epicId);
    }

    public static SubTask initRandomSubTask(int epicId, Duration duration, LocalDateTime startTime) {
        return new SubTask("subTaskName" + random.nextInt(), "subTaskDescription" + random.nextInt(),
                random.nextInt(), TaskStatus.NEW, epicId, duration, startTime);
    }

    public static Duration getRandomDuration() {
        return Duration.ofMinutes(random.nextInt());
    }
}
