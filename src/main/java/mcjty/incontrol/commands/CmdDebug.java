package mcjty.incontrol.commands;

import mcjty.incontrol.ForgeEventHandlers;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

import javax.annotation.Nonnull;

public class CmdDebug extends CommandBase {
    @Nonnull
    @Override
    public String getName() {
        return "ctrldebug";
    }

    @Nonnull
    @Override
    public String getUsage(@Nonnull ICommandSender sender) {
        return "ctrldebug";
    }

    @Override
    public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, String[] args) {
        int debugtype = 0;
        int arglen = args.length;

        if (arglen > 0) {
            debugtype = Integer.parseInt(args[0]);
        } else switch (ForgeEventHandlers.debugtype) {
            case 0: {
                debugtype = 1;
                break;
            }
            case 1: {
                debugtype = 2;
                break;
            }
            case 2: {
                debugtype = 3;
                break;
            }
            default: {
                break;
            }
        }

        if (debugtype < 0) {
            debugtype = 0;
        }

        if (debugtype > 3) {
            debugtype = 3;
        }

        ForgeEventHandlers.debugtype = debugtype;

        switch (ForgeEventHandlers.debugtype) {
            case 0: {
                sender.sendMessage(new TextComponentString("Disabled InControl debug"));
                break;
            }
            case 1: {
                sender.sendMessage(new TextComponentString("Enabled InControl spawn only debug"));
                break;
            }
            case 2: {
                sender.sendMessage(new TextComponentString("Enabled InControl potentialspawn only debug"));
                break;
            }
            case 3: {
                sender.sendMessage(new TextComponentString("Enabled InControl full debug"));
                break;
            }
        }
    }
}
