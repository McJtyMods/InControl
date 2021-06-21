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
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class CmdReload implements Command<CommandSource> {

    private static final CmdReload CMD = new CmdReload();

    public static ArgumentBuilder<CommandSource, ?> register(CommandDispatcher<CommandSource> dispatcher) {
        return Commands.literal("reload")
                .requires(cs -> cs.hasPermissionLevel(1))
                .executes(CMD);
    }

    @Override
    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().asPlayer();
        ErrorHandler.clearErrors();
        if (player != null) {
            player.sendMessage(new StringTextComponent("Reloaded InControl rules"), Util.DUMMY_UUID);
            try {
                RulesManager.reloadRules();
                SpawnerSystem.reloadRules();
            } catch (Exception e) {
                InControl.setup.getLogger().error("Error reloading rules!", e);
                player.sendMessage(new StringTextComponent(TextFormatting.RED + "Error: " + e.getLocalizedMessage()), Util.DUMMY_UUID);
            }
        }
        return 0;
    }
}
