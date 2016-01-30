package io.openio.sds.common;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import io.openio.sds.models.Position;

public class JsonUtils {

    private static final GsonBuilder builder = new GsonBuilder()
            .registerTypeAdapter(Position.class, new PositionAdapter());

    public static Gson gson() {
        return builder.create();
    }

    private static final class PositionAdapter
            implements JsonSerializer<Position>, JsonDeserializer<Position> {

        @Override
        public Position deserialize(JsonElement json, Type typeOfT,
                JsonDeserializationContext context) throws JsonParseException {
            return Position.parse(json.getAsString());
        }

        @Override
        public JsonElement serialize(Position src, Type typeOfSrc,
                JsonSerializationContext context) {
            return new JsonPrimitive(src.toString());
        }

    }
}
