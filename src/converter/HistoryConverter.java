package converter;

import model.Task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HistoryConverter {

    public static String toString(List<Task> history) {
        if (history.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        history.forEach(task -> {
            sb.append(task.getId());
            sb.append(",");
        });
        return sb.deleteCharAt(sb.lastIndexOf(",")).toString();
    }

    public static List<Integer> fromString(String text) {
        List<Integer> history = new ArrayList<>();
        if (text != null && !text.isEmpty()) {
            Arrays.stream(text.split(","))
                    .forEach(number -> history.add(Integer.parseInt(number)));
        }
        return history;
    }
}
