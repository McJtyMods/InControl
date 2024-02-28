package mcjty.incontrol.tools.typed;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import mcjty.incontrol.ErrorHandler;
import mcjty.incontrol.tools.varia.JSonTools;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GenericAttributeMapFactory {

    private final List<Attribute> attributes = new ArrayList<>();

    public GenericAttributeMapFactory attribute(@Nonnull Attribute a) {
        attributes.add(a);
        return this;
    }

    private boolean validate(JsonObject object, String file) {
        Set<String> validKeys = attributes.stream().map(a -> a.key().name()).collect(Collectors.toSet());
        Set<String> errors = new HashSet<>();
        object.keySet().forEach(attr -> {
            if (!validKeys.contains(attr)) {
                errors.add(attr);
            }
        });
        if (!errors.isEmpty()) {
            ErrorHandler.error("Invalid keywords for " + file + ": " + StringUtils.join(errors, ' '));
            return false;
        }
        return true;
    }

    @Nonnull
    public AttributeMap parse(@Nonnull JsonElement element, String file) {
        JsonObject jsonObject = element.getAsJsonObject();
        AttributeMap map = new AttributeMap();

        if (!validate(jsonObject, file)) {
            return map;
        }

        for (Attribute attribute : attributes) {
            Key key = attribute.key();
            Type type = key.type();

            if (attribute.multi()) {
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
                } else if (type == Type.OBJECT) {
                    if (jsonObject.isJsonPrimitive()) {
                        if (jsonObject.getAsJsonPrimitive().isNumber()) {
                            transformer = JsonElement::getAsInt;
                        } else if (jsonObject.getAsJsonPrimitive().isBoolean()) {
                            transformer = JsonElement::getAsBoolean;
                        } else {
                            transformer = JsonElement::getAsString;
                        }
                    } else {
                        ErrorHandler.error("Expected a primitive for " + key.name() + "!");
                        return map;
                    }
                } else if (type == Type.DIMENSION_TYPE) {
                    transformer = jsonElement -> ResourceKey.create(Registries.DIMENSION, new ResourceLocation(jsonElement.getAsString()));
                } else {
                    transformer = e -> "INVALID";
                }

                JSonTools.getElement(jsonObject, key.name())
                        .ifPresent(e -> {
                            JSonTools.asArrayOrSingle(e)
                                    .map(transformer)
                                    .forEach(s -> {
                                        map.addListNonnull(key, s);
                                    });
                        });
            } else {
                if (type == Type.INTEGER) {
                    map.setNonnull(key, JSonTools.parseInt(jsonObject, key.name()));
                } else if (type == Type.FLOAT) {
                    map.setNonnull(key, JSonTools.parseFloat(jsonObject, key.name()));
                } else if (type == Type.BOOLEAN) {
                    map.setNonnull(key, JSonTools.parseBool(jsonObject, key.name()));
                } else if (type == Type.STRING) {
                    if (jsonObject.has(key.name())) {
                        map.setNonnull(key, jsonObject.get(key.name()).getAsString());
                    }
                } else if (type == Type.OBJECT) {
                    if (jsonObject.has(key.name())) {
                        JsonElement el = jsonObject.get(key.name());
                        if (el.isJsonObject()) {
                            map.setNonnull(key, el.getAsJsonObject().toString());
                        } else {
                            if (el.isJsonPrimitive()) {
                                JsonPrimitive prim = el.getAsJsonPrimitive();
                                if (prim.isString()) {
                                    map.setNonnull(key, prim.getAsString());
                                } else if (prim.isNumber()) {
                                    map.setNonnull(key, "" + prim.getAsInt());
                                } else {
                                    throw new RuntimeException("Bad type for key '" + key.name() + "'!");
                                }
                            }
                        }
                    }
                } else if (type == Type.DIMENSION_TYPE) {
                    if (jsonObject.has(key.name())) {
                        JsonElement jsonElement = jsonObject.get(key.name());
                        map.setNonnull(key, ResourceKey.create(Registries.DIMENSION, new ResourceLocation(jsonElement.getAsString())));
                    }
                } else if (type == Type.JSON) {
                    if (jsonObject.has(key.name())) {
                        JsonElement el = jsonObject.get(key.name());
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
                                    throw new RuntimeException("Bad type for key '" + key.name() + "'!");
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
