package mcjty.incontrol;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.HashSet;
import java.util.Set;

public class ErrorHandler {

    private static final Set<String> errors = new HashSet<>();

    public static void clearErrors() {
        errors.clear();
    }

    // Publish an error and notify all players of that error
    public static void error(String message) {
        errors.add(message);
        InControl.setup.getLogger().error(message);
        // Notify all logged in players
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                player.sendSystemMessage(Component.literal(ChatFormatting.RED + "InControl Error: " + ChatFormatting.GOLD + message));
            }
        }
    }

    public static void onPlayerJoinWorld(PlayerEvent.PlayerLoggedInEvent event) {
        for (String error : errors) {
            event.getEntity().sendSystemMessage(Component.literal(ChatFormatting.RED + "InControl Error: " + ChatFormatting.GOLD + error));
        }
    }
}
