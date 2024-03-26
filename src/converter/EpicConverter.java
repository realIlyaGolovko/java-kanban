package converter;

import model.Epic;

public class EpicConverter implements Converter<Epic> {

    @Override
    public String toString(Epic epic) {
        return String.format("%s,%s,%s,%s,%s,%s,%s",
                epic.getId(),
                epic.getTaskType(),
                epic.getName(),
                epic.getStatus(),
                epic.getDescription(),
                epic.getDuration(),
                epic.getStartTime()
        );
    }

    @Override
    public Epic fromString(String line) {
        String[] columns = line.split(",");
        int id = Integer.parseInt(columns[0]);
        String name = columns[2];
        String description = columns[4];
        return new Epic(name, description, id);
    }
}
