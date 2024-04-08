package adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.Duration;

public class DurationAdapter extends TypeAdapter<Duration> {
    @Override
    public Duration read(JsonReader reader) throws IOException {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull();
            return null;
        }
        long duration = Long.parseLong(reader.nextString());
        return Duration.ofMinutes(duration);
    }

    @Override
    public void write(JsonWriter writer, Duration value) throws IOException {
        if (value == null) {
            writer.nullValue();
            return;
        }
        String duration = String.format("%d", value.toMinutes());
        writer.value(duration);
    }
}
