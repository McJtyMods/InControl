package mcjty.tools.varia;

import com.google.gson.*;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.*;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class JSonTools {

    public static JsonElement getRootElement(String path, String filename, Logger logger) {
        File file;
        if (path == null) {
            file = new File(filename);
        } else {
            file = new File(path + File.separator + "incontrol", filename);
        }
        if (!file.exists()) {
            // Create an empty rule file
            makeEmptyRuleFile(file, logger);
            return null;
        }

        logger.log(Level.INFO, "Reading rules from " + filename);
        InputStream inputstream = null;
        try {
            inputstream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            logger.log(Level.ERROR, "Error reading " + filename + "!");
            return null;
        }

        BufferedReader br;
        try {
            br = new BufferedReader(new InputStreamReader(inputstream, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            logger.log(Level.ERROR, "Error reading " + filename + "!");
            return null;
        }

        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(br);

        return element;
    }

    private static void makeEmptyRuleFile(File file, Logger logger) {
        PrintWriter writer;
        try {
            writer = new PrintWriter(file);
        } catch (FileNotFoundException e) {
            logger.log(Level.ERROR, "Error writing " + file.getName() + "!");
            return;
        }
        JsonArray array = new JsonArray();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        writer.print(gson.toJson(array));
        writer.close();
    }


    public static Optional<JsonElement> getElement(JsonObject element, String name) {
        JsonElement el = element.get(name);
        if (el != null) {
            return Optional.of(el);
        } else {
            return Optional.empty();
        }
    }

    @Nullable
    public static Float parseFloat(JsonObject jsonObject, String name) {
        if (jsonObject.has(name)) {
            return jsonObject.get(name).getAsFloat();
        } else {
            return null;
        }
    }

    @Nullable
    public static Integer parseInt(JsonObject jsonObject, String name) {
        if (jsonObject.has(name)) {
            return jsonObject.get(name).getAsInt();
        } else {
            return null;
        }
    }

    @Nullable
    public static Boolean parseBool(JsonObject jsonObject, String name) {
        if (jsonObject.has(name)) {
            return jsonObject.get(name).getAsBoolean();
        } else {
            return null;
        }
    }

    public static Stream<Pair<String,String>> asPairs(JsonElement element) {
        Stream.Builder<Pair<String, String>> builder = Stream.builder();
        for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
            builder.add(Pair.of(entry.getKey(), entry.getValue().getAsString()));
        }
        return builder.build();
    }

    public static Stream<JsonElement> asArrayOrSingle(JsonElement element) {
        if (element.isJsonArray()) {
            Stream.Builder<JsonElement> builder = Stream.builder();
            for (JsonElement el : element.getAsJsonArray()) {
                builder.add(el);
            }
            return builder.build();
        } else {
            return Stream.of(element);
        }
    }

    public static void addPairs(JsonObject parent, String name, Map<String, String> pairs) {
        if (pairs != null) {
            JsonObject object = new JsonObject();
            for (Map.Entry<String, String> entry : pairs.entrySet()) {
                object.add(entry.getKey(), new JsonPrimitive(entry.getValue()));
            }
            parent.add(name, object);
        }
    }

    public static void addArrayOrSingle(JsonObject parent, String name, Collection<String> strings) {
        if (strings != null) {
            if (strings.size() == 1) {
                parent.add(name, new JsonPrimitive(strings.iterator().next()));
            } else {
                JsonArray array = new JsonArray();
                for (String value : strings) {
                    array.add(new JsonPrimitive(value));
                }
                parent.add(name, array);
            }
        }
    }

    public static void addIntArrayOrSingle(JsonObject parent, String name, Collection<Integer> integers) {
        if (integers != null) {
            if (integers.size() == 1) {
                parent.add(name, new JsonPrimitive(integers.iterator().next()));
            } else {
                JsonArray array = new JsonArray();
                for (Integer value : integers) {
                    array.add(new JsonPrimitive(value));
                }
                parent.add(name, array);
            }
        }
    }
}
