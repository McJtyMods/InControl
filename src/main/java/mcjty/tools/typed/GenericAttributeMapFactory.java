package mcjty.tools.typed;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import mcjty.incontrol.ErrorHandler;
import mcjty.tools.varia.JSonTools;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
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
        Set<String> validKeys = attributes.stream().map(a -> a.getKey().getName()).collect(Collectors.toSet());
        Set<String> errors = new HashSet<>();
        object.entrySet().forEach(entry -> {
            String attr = entry.getKey();
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
                        RegistryKey<World> worldkey = RegistryKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(jsonElement.getAsString()));
                        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
                        if (server != null) {
                            if (!server.levelKeys().contains(worldkey)) {
                                ErrorHandler.error("Dimension '" + jsonElement.getAsString() + "' not found!");
                            }
                        }
                        return worldkey;
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
                        JsonElement jsonElement = jsonObject.get(key.getName());
                        map.setNonnull(key, RegistryKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(jsonElement.getAsString())));
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
