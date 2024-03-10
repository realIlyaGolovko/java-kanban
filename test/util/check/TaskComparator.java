package util.check;

import model.Epic;
import model.SubTask;
import model.Task;
import model.TaskType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TaskComparator {
    public static <T extends Task> void compareTasks(T expected, T actual) {
        assertEquals(expected.getId(), actual.getId(), "Should be the same Ids");
        assertEquals(expected.getName(), actual.getName(), "Should be the same names");
        assertEquals(expected.getDescription(), actual.getDescription(), "Should be the same descriptions");
        assertEquals(expected.getStatus(), actual.getStatus(), "Should be the same statuses");
        assertEquals(expected.getTaskType(), actual.getTaskType(), "Should be the same types");
        if (expected.getTaskType() == TaskType.EPIC) {
            Epic expectedEpic = (Epic) expected;
            Epic actualEpic = (Epic) actual;
            assertEquals(expectedEpic.getSubTaskIds(), actualEpic.getSubTaskIds(),
                    "Should be the same subTaskIds");
        } else if (expected.getTaskType() == TaskType.SUBTASK) {
            SubTask expectedSubTask = (SubTask) expected;
            SubTask actualSubTask = (SubTask) actual;
            assertEquals(expectedSubTask.getEpicId(), actualSubTask.getEpicId(), "Should be the same epicIds");

        }
    }

    public static <T extends Task> void compareListOfTasks(List<T> expected, List<T> actual) {
        assertEquals(expected.size(), actual.size(), "Should be the same size");
        for (int i = 0; i < expected.size(); i++) {
            compareTasks(expected.get(i), actual.get(i));
        }
    }
}
