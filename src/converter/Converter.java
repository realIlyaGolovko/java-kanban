package converter;

import model.Task;

public interface Converter<T extends Task> {
    String toString(T task);

    T fromString(String line);
}
