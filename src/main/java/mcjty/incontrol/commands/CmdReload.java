package mcjty.incontrol.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mcjty.incontrol.ErrorHandler;
import mcjty.incontrol.InControl;
import mcjty.incontrol.events.EventsSystem;
import mcjty.incontrol.rules.RulesManager;
import mcjty.incontrol.spawner.SpawnerSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class CmdReload implements Command<CommandSourceStack> {

    private static final CmdReload CMD = new CmdReload();

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher) {
        return Commands.literal("reload")
                .requires(cs -> cs.hasPermission(1))
                .executes(CMD);
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ErrorHandler.clearErrors();
        if (player != null) {
            player.sendSystemMessage(Component.literal("Reloaded InControl rules"));
            try {
                RulesManager.reloadRules();
                SpawnerSystem.reloadRules();
                EventsSystem.reloadRules();
            } catch (Exception e) {
                InControl.setup.getLogger().error("Error reloading rules!", e);
                player.sendSystemMessage(Component.literal(ChatFormatting.RED + "Error: " + e.getLocalizedMessage()));
            }
        }
        return 0;
    }
}
