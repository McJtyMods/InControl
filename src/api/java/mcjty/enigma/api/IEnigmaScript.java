package mcjty.enigma.api;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public interface IEnigmaScript {

    void setPlayerState(EntityPlayer player, String statename, String statevalue);

    String getPlayerState(EntityPlayer player, String statename);

    void setState(World world, String statename, String statevalue);

    String getState(World world, String statename);
}
