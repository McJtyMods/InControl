package mcjty.incontrol.compat;

import mcjty.incontrol.InControl;
import mcjty.tools.rules.IEventQuery;
import mcjty.tools.rules.IModRuleCompatibilityLayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public class ModRuleCompatibilityLayer implements IModRuleCompatibilityLayer {

    @Override
    public boolean hasBaubles() {
        return InControl.baubles;
    }

    @Override
    public int[] getAmuletSlots() {
        return BaublesSupport.getAmuletSlots();
    }

    @Override
    public int[] getBeltSlots() {
        return BaublesSupport.getBeltSlots();
    }

    @Override
    public int[] getBodySlots() {
        return BaublesSupport.getBodySlots();
    }

    @Override
    public int[] getCharmSlots() {
        return BaublesSupport.getCharmSlots();
    }

    @Override
    public int[] getHeadSlots() {
        return BaublesSupport.getHeadSlots();
    }

    @Override
    public int[] getRingSlots() {
        return BaublesSupport.getRingSlots();
    }

    @Override
    public int[] getTrinketSlots() {
        return BaublesSupport.getTrinketSlots();
    }

    @Override
    public ItemStack getBaubleStack(EntityPlayer player, int slot) {
        return BaublesSupport.getStack(player, slot);
    }

    @Override
    public boolean hasGameStages() {
        return InControl.gamestages;
    }

    @Override
    public boolean hasGameStage(EntityPlayer player, String stage) {
        return GameStageSupport.hasGameStage(player, stage);
    }

    @Override
    public boolean hasLostCities() {
        return InControl.lostcities;
    }

    @Override
    public <T> boolean isCity(IEventQuery<T> query, T event) {
        return LostCitySupport.isCity(query, event);
    }

    @Override
    public <T> boolean isStreet(IEventQuery<T> query, T event) {
        return LostCitySupport.isStreet(query, event);
    }

    @Override
    public <T> boolean inSphere(IEventQuery<T> query, T event) {
        return LostCitySupport.inSphere(query, event);
    }

    @Override
    public <T> boolean isBuilding(IEventQuery<T> query, T event) {
        return LostCitySupport.isBuilding(query, event);
    }

    @Override
    public boolean hasSereneSeasons() {
        return InControl.sereneSeasons;
    }

    @Override
    public boolean isSpring(World world) {
        return SereneSeasonsSupport.isSpring(world);
    }

    @Override
    public boolean isSummer(World world) {
        return SereneSeasonsSupport.isSummer(world);
    }

    @Override
    public boolean isWinter(World world) {
        return SereneSeasonsSupport.isWinter(world);
    }

    @Override
    public boolean isAutumn(World world) {
        return SereneSeasonsSupport.isAutumn(world);
    }

    @Override
    public boolean hasEnigmaScript() {
        return false;
    }

    @Override
    public String getPlayerState(EntityPlayer player, String statename) {
        return null;
    }

    @Override
    public String getState(World world, String statename) {
        return null;
    }

    @Override
    public void setPlayerState(EntityPlayer player, String statename, String statevalue) {
        // Not supported by In Control
    }

    @Override
    public void setState(World world, String statename, String statevalue) {
        // Not supported by In Control
    }

    @Override
    public String getBiomeName(Biome biome) {
        return biome.biomeName;
    }
}
