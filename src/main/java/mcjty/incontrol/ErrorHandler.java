package mcjty.incontrol;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.util.HashSet;
import java.util.Set;

public class ErrorHandler {

    private static Set<String> errors = new HashSet<>();

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
            for (ServerPlayerEntity player : server.getPlayerList().getPlayers()) {
                player.sendMessage(new StringTextComponent(TextFormatting.RED + "InControl Error: " + TextFormatting.GOLD + message), Util.NIL_UUID);
            }
        }
    }

    public static void onPlayerJoinWorld(PlayerEvent.PlayerLoggedInEvent event) {
        for (String error : errors) {
            event.getPlayer().sendMessage(new StringTextComponent(TextFormatting.RED + "InControl Error: " + TextFormatting.GOLD + error), Util.NIL_UUID);
        }
    }
}
