package mcjty.incontrol;

import mcjty.incontrol.commands.ModCommands;
import mcjty.incontrol.events.EventsSystem;
import mcjty.incontrol.platform.RuleEvents.*;
import mcjty.incontrol.platform.ServerLifecycleHooks;
import mcjty.incontrol.setup.Config;
import mcjty.incontrol.setup.ModSetup;
import mcjty.incontrol.tools.cache.StructureCache;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.*;
import net.fabricmc.fabric.api.event.player.*;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class InControl implements ModInitializer {
    public static final String MODID = "incontrol";
    public static final ModSetup setup = new ModSetup();
    public static final FabricEventHandlers HANDLERS = new FabricEventHandlers();

    @Override
    public void onInitialize() {
        setup.init();
        Config.load();
        CommandRegistrationCallback.EVENT.register((dispatcher, access, environment) -> ModCommands.register(dispatcher));
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            ServerLifecycleHooks.setCurrentServer(server);
            Config.load();
            FabricEventHandlers.tryLoadRules();
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            HANDLERS.onServerStopping();
            FabricEventHandlers.tickCounters.clear();
            EventsSystem.cleanup();
            StructureCache.CACHE.clean();
            setup.cache = new mcjty.incontrol.rules.support.RuleCache();
            ServerLifecycleHooks.setCurrentServer(null);
        });
        ServerTickEvents.START_WORLD_TICK.register(level -> HANDLERS.onWorldTick(new LevelTickEvent.Pre(level)));
        ServerTickEvents.END_SERVER_TICK.register(server -> server.getPlayerList().getPlayers().forEach(player ->
                HANDLERS.onPlayerTick(new PlayerTickEvent.Post(player))));
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            if (entity instanceof LivingEntity && !(entity instanceof Player)) setup.cache.addMob(level, entity);
        });
        ServerEntityEvents.ENTITY_UNLOAD.register((entity, level) -> HANDLERS.onEntityLeavesWorld(new EntityLeaveLevelEvent(entity, level)));
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> ErrorHandler.onPlayerJoinWorld(handler.player));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> FabricEventHandlers.tickCounters.remove(handler.player.getId()));
        PlayerBlockBreakEvents.BEFORE.register((level, player, pos, state, blockEntity) -> {
            BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(level, pos, player);
            HANDLERS.onBlockBreakEvent(event);
            return !event.isCanceled();
        });
        AttackBlockCallback.EVENT.register((player, level, hand, pos, direction) -> {
            if (level.isClientSide() || player.isSpectator()) return InteractionResult.PASS;
            PlayerInteractEvent.LeftClickBlock event = new PlayerInteractEvent.LeftClickBlock(player, pos);
            HANDLERS.onLeftClickEvent(event);
            return event.isCanceled() ? InteractionResult.FAIL : InteractionResult.PASS;
        });
    }
}
