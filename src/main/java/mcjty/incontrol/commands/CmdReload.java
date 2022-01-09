package mcjty.incontrol.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mcjty.incontrol.ErrorHandler;
import mcjty.incontrol.InControl;
import mcjty.incontrol.rules.RulesManager;
import mcjty.incontrol.spawner.SpawnerSystem;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.Util;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.ChatFormatting;

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
            player.sendMessage(new TextComponent("Reloaded InControl rules"), Util.NIL_UUID);
            try {
                RulesManager.reloadRules();
                SpawnerSystem.reloadRules();
            } catch (Exception e) {
                InControl.setup.getLogger().error("Error reloading rules!", e);
                player.sendMessage(new TextComponent(ChatFormatting.RED + "Error: " + e.getLocalizedMessage()), Util.NIL_UUID);
            }
        }
        return 0;
    }
}
