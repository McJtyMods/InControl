package mcjty.incontrol.setup;


import com.mojang.serialization.MapCodec;
import mcjty.incontrol.events.InControlLootModifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

import static mcjty.incontrol.InControl.MODID;

public class Registration {

    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> LOOT_MODIFIER_SERIALIZERS = DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, MODID);

    public static final Supplier<MapCodec<? extends IGlobalLootModifier>> INCONTROL_GLM = LOOT_MODIFIER_SERIALIZERS.register("incontrol_glm", () -> InControlLootModifier.CODEC);

    public static void register(IEventBus bus) {
        LOOT_MODIFIER_SERIALIZERS.register(bus);
    }
}
