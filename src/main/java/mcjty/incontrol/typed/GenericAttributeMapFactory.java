package mcjty.incontrol.typed;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mcjty.incontrol.varia.JSonTools;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class GenericAttributeMapFactory {

    private final List<Attribute> attributes = new ArrayList<>();

    public GenericAttributeMapFactory attribute(@Nonnull Attribute a) {
        attributes.add(a);
        return this;
    }

    @Nonnull
    public AttributeMap parse(@Nonnull JsonElement element) {
        JsonObject jsonObject = element.getAsJsonObject();
        AttributeMap map = new AttributeMap();

        for (Attribute attribute : attributes) {
            Key key = attribute.getKey();
            Type type = key.getType();

            if (attribute.isMulti()) {
                Function<JsonElement, Object> transformer;
                if (type == Type.INTEGER) {
                    transformer = JsonElement::getAsInt;
                } else if (type == Type.FLOAT) {
                    transformer = JsonElement::getAsFloat;
                } else if (type == Type.BOOLEAN) {
                    transformer = JsonElement::getAsBoolean;
                } else if (type == Type.STRING) {
                    transformer = JsonElement::getAsString;
                } else {
                    transformer = e -> "INVALID";
                }

                JSonTools.getElement(jsonObject, key.getName())
                        .ifPresent(e -> {
                            JSonTools.asArrayOrSingle(e)
                                    .map(transformer)
                                    .forEach(s -> {
                                        map.addListNonnull(key, s);
                                    });
                        });
            } else {
                if (type == Type.INTEGER) {
                    map.setNonnull(key, JSonTools.parseInt(jsonObject, key.getName()));
                } else if (type == Type.FLOAT) {
                    map.setNonnull(key, JSonTools.parseFloat(jsonObject, key.getName()));
                } else if (type == Type.BOOLEAN) {
                    map.setNonnull(key, JSonTools.parseBool(jsonObject, key.getName()));
                } else if (type == Type.STRING) {
                    if (jsonObject.has(key.getName())) {
                        map.setNonnull(key, jsonObject.get(key.getName()).getAsString());
                    }
                }
            }
        }

        return map;
    }
}
