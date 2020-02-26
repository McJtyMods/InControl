package mcjty.tools.typed;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import mcjty.tools.varia.JSonTools;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.dimension.DimensionType;

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
                } else if (type == Type.JSON) {
                    transformer = JsonElement::toString;
                } else if (type == Type.DIMENSION_TYPE) {
                    transformer = jsonElement -> {
                        return DimensionType.byName(new ResourceLocation(jsonElement.getAsString()));
                    };
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
                } else if (type == Type.DIMENSION_TYPE) {
                    if (jsonObject.has(key.getName())) {
                        String str = jsonObject.get(key.getName()).getAsString();
                        map.setNonnull(key, DimensionType.byName(new ResourceLocation(str)));
                    }
                } else if (type == Type.JSON) {
                    if (jsonObject.has(key.getName())) {
                        JsonElement el = jsonObject.get(key.getName());
                        if (el.isJsonObject()) {
                            JsonObject obj = el.getAsJsonObject();
                            map.setNonnull(key, obj.toString());
                        } else {
                            if (el.isJsonPrimitive()) {
                                JsonPrimitive prim = el.getAsJsonPrimitive();
                                if (prim.isString()) {
                                    map.setNonnull(key, prim.getAsString());
                                } else if (prim.isNumber()) {
                                    map.setNonnull(key, "" + prim.getAsInt());
                                } else {
                                    throw new RuntimeException("Bad type for key '" + key.getName() + "'!");
                                }
                            }
                        }
                    }
                }
            }
        }

        return map;
    }
}
