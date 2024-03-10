package converter;

import model.Task;

import java.util.ArrayList;
import java.util.List;

public class HistoryConverter {

    public static String toString(List<Task> history) {
        if (history.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Task task : history) {
            int id = task.getId();
            sb.append(id);
            sb.append(",");
        }
        return sb.deleteCharAt(sb.lastIndexOf(",")).toString();
    }

    public static List<Integer> fromString(String text) {
        List<Integer> history = new ArrayList<>();
        if (text != null && !text.isEmpty()) {
            for (String number : text.split(",")) {
                history.add(Integer.parseInt(number));
            }
        }
        return history;
    }
}
