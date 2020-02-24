package mcjty.enigma.api;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

public interface IEnigmaScript {

    void setPlayerState(PlayerEntity player, String statename, String statevalue);

    String getPlayerState(PlayerEntity player, String statename);

    void setState(World world, String statename, String statevalue);

    String getState(World world, String statename);
}
