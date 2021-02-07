package mcjty.tools.rules;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public interface IModRuleCompatibilityLayer {

    // --------------------
    // Baubles
    // --------------------
    boolean hasBaubles();

    int[] getAmuletSlots();

    int[] getBeltSlots();

    int[] getBodySlots();

    int[] getCharmSlots();

    int[] getHeadSlots();

    int[] getRingSlots();

    int[] getTrinketSlots();

    ItemStack getBaubleStack(PlayerEntity player, int slot);


    // --------------------
    // Game Stages
    // --------------------

    boolean hasGameStages();

    boolean hasGameStage(PlayerEntity player, String stage);

    void addGameStage(PlayerEntity player, String stage);

    void removeGameStage(PlayerEntity player, String stage);

    // --------------------
    // Lost Cities
    // --------------------

    boolean hasLostCities();

    <T> boolean isCity(IEventQuery<T> query, T event);

    <T> boolean isStreet(IEventQuery<T> query, T event);

    <T> boolean inSphere(IEventQuery<T> query, T event);

    <T> boolean isBuilding(IEventQuery<T> query, T event);

    // --------------------
    // Serene Seasons
    // --------------------

    boolean hasSereneSeasons();

    boolean isSpring(IWorld world);

    boolean isSummer(IWorld world);

    boolean isWinter(IWorld world);

    boolean isAutumn(IWorld world);

    // --------------------
    // EnigmaScript
    // --------------------

    boolean hasEnigmaScript();

    void setPlayerState(PlayerEntity player, String statename, String statevalue);

    String getPlayerState(PlayerEntity player, String statename);

    void setState(IWorld world, String statename, String statevalue);

    String getState(IWorld world, String statename);

    // --------------------
    // Specific methods to avoid AT issues in McJtyTools
    // --------------------
    String getBiomeName(Biome biome);
}
