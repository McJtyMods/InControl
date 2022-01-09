package mcjty.incontrol;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.server.ServerLifecycleHooks;

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
                player.sendMessage(new TextComponent(ChatFormatting.RED + "InControl Error: " + ChatFormatting.GOLD + message), Util.NIL_UUID);
            }
        }
    }

    public static void onPlayerJoinWorld(PlayerEvent.PlayerLoggedInEvent event) {
        for (String error : errors) {
            event.getPlayer().sendMessage(new TextComponent(ChatFormatting.RED + "InControl Error: " + ChatFormatting.GOLD + error), Util.NIL_UUID);
        }
    }
}
