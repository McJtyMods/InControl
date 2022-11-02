package mcjty.incontrol.tools.rules;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;

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

    ItemStack getBaubleStack(Player player, int slot);


    // --------------------
    // Game Stages
    // --------------------

    boolean hasGameStages();

    boolean hasGameStage(Player player, String stage);

    void addGameStage(Player player, String stage);

    void removeGameStage(Player player, String stage);

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

    boolean isSpring(Level world);

    boolean isSummer(Level world);

    boolean isWinter(Level world);

    boolean isAutumn(Level world);

    // --------------------
    // EnigmaScript
    // --------------------

    boolean hasEnigmaScript();

    void setPlayerState(Player player, String statename, String statevalue);

    String getPlayerState(Player player, String statename);

    void setState(LevelAccessor world, String statename, String statevalue);

    String getState(LevelAccessor world, String statename);

    // --------------------
    // Specific methods to avoid AT issues in McJtyTools
    // --------------------
    String getBiomeName(Biome biome);
}
