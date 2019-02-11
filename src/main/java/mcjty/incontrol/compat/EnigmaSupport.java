package mcjty.incontrol.compat;

import mcjty.enigma.api.IEnigmaScript;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.event.FMLInterModComms;

import javax.annotation.Nullable;
import java.util.function.Function;

public class EnigmaSupport {

    private static IEnigmaScript enigmaScript;

    public static void register() {
        FMLInterModComms.sendFunctionMessage("enigma", "getEnigmaScript", "mcjty.fxcontrol.compat.EnigmaSupport$GetEnigmaScript");
    }

    public static void setPlayerState(EntityPlayer player, String statename, String statevalue) {
        enigmaScript.setPlayerState(player, statename, statevalue);
    }

    public static void setState(World world, String statename, String statevalue) {
        enigmaScript.setState(world, statename, statevalue);
    }

    public static String getPlayerState(EntityPlayer player, String statename) {
        return enigmaScript.getPlayerState(player, statename);
    }

    public static String getState(World world, String statename) {
        return enigmaScript.getState(world, statename);
    }

    public static class GetEnigmaScript implements Function<IEnigmaScript, Void> {
        @Nullable
        @Override
        public Void apply(IEnigmaScript lc) {
            enigmaScript = lc;
            return null;
        }
    }

}

