package io.openio.sds.common;

import static io.openio.sds.common.OioConstants.OIO_CHARSET;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import io.openio.sds.models.Position;

public class JsonUtils {

    private static final GsonBuilder builder = new GsonBuilder()
            .registerTypeAdapter(Position.class, new PositionAdapter());

    private static final Type MAP_TYPE = new TypeToken<Map<String, String>>() {
    }.getType();

    public static Gson gson() {
        return builder.create();
    }

    public static Map<String, String> jsonToMap(String map) {
        return gson().fromJson(map, MAP_TYPE);
    }

    public static Map<String, String> jsonToMap(InputStream in) {
        return gson().fromJson(new JsonReader(
                new InputStreamReader(in, OIO_CHARSET)), MAP_TYPE);
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
