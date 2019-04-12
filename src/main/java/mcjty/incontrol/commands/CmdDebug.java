package mcjty.incontrol.commands;

import mcjty.incontrol.ForgeEventHandlers;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class CmdDebug extends CommandBase {
    @Override
    public String getName() {
        return "ctrldebug";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "ctrldebug";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
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
                debugtype = 0;
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
