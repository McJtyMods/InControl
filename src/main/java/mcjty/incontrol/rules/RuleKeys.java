package mcjty.incontrol.rules;

import mcjty.incontrol.typed.AttributeMap;
import mcjty.incontrol.typed.Key;
import mcjty.incontrol.typed.Type;

public interface RuleKeys {

    // Inputs
    Key<Integer> MINTIME = Key.create(Type.INTEGER, "mintime");
    Key<Integer> MAXTIME = Key.create(Type.INTEGER, "maxtime");
    Key<Integer> MINLIGHT = Key.create(Type.INTEGER, "minlight");
    Key<Integer> MAXLIGHT = Key.create(Type.INTEGER, "maxlight");
    Key<Integer> MINHEIGHT = Key.create(Type.INTEGER, "minheight");
    Key<Integer> MAXHEIGHT = Key.create(Type.INTEGER, "maxheight");
    Key<Float> MINDIFFICULTY = Key.create(Type.FLOAT, "mindifficulty");
    Key<Float> MAXDIFFICULTY = Key.create(Type.FLOAT, "maxdifficulty");
    Key<Float> MINSPAWNDIST = Key.create(Type.FLOAT, "minspawndist");
    Key<Float> MAXSPAWNDIST = Key.create(Type.FLOAT, "maxspawndist");
    Key<Float> RANDOM = Key.create(Type.FLOAT, "random");
    Key<Boolean> PASSIVE = Key.create(Type.BOOLEAN, "passive");
    Key<Boolean> HOSTILE = Key.create(Type.BOOLEAN, "hostile");
    Key<Boolean> SEESKY = Key.create(Type.BOOLEAN, "seesky");
    Key<String> WEATHER = Key.create(Type.STRING, "weather");
    Key<String> TEMPCATEGORY = Key.create(Type.STRING, "tempcategory");
    Key<String> DIFFICULTY = Key.create(Type.STRING, "difficulty");
    Key<String> MOB = Key.create(Type.STRING, "mob");
    Key<String> MOD = Key.create(Type.STRING, "mod");
    Key<String> BLOCK = Key.create(Type.STRING, "block");
    Key<String> BIOME = Key.create(Type.STRING, "biome");
    Key<Integer> DIMENSION = Key.create(Type.INTEGER, "dimension");

    // Outputs
    Key<String> RESULT = Key.create(Type.STRING, "result");
    Key<Float> HEALTHMULTIPLY = Key.create(Type.FLOAT, "healthmultiply");
    Key<Float> HEALTHADD = Key.create(Type.FLOAT, "healthadd");
    Key<Float> SPEEDMULTIPLY = Key.create(Type.FLOAT, "speedmultiply");
    Key<Float> SPEEDADD = Key.create(Type.FLOAT, "speedadd");
    Key<Float> DAMAGEMULTIPLY = Key.create(Type.FLOAT, "damagemultiply");
    Key<Float> DAMAGEADD = Key.create(Type.FLOAT, "damageadd");
    Key<Float> SIZEMULTIPLY = Key.create(Type.FLOAT, "sizemultiply");
    Key<Float> SIZEADD = Key.create(Type.FLOAT, "sizeadd");
    Key<Boolean> ANGRY = Key.create(Type.BOOLEAN, "angry");
    Key<String> POTION = Key.create(Type.STRING, "potion");
    Key<String> HELDITEM = Key.create(Type.STRING, "helditem");
    Key<String> ARMORCHEST = Key.create(Type.STRING, "armorchest");
    Key<String> ARMORHELMET = Key.create(Type.STRING, "armorhelmet");
    Key<String> ARMORLEGS = Key.create(Type.STRING, "armorlegs");
    Key<String> ARMORBOOTS = Key.create(Type.STRING, "armorboots");

    // Mob spawn entry
    Key<AttributeMap> MOBS = Key.create(Type.MAP, "mobs");

    Key<Integer> WEIGHT = Key.create(Type.INTEGER, "weight");
    Key<Integer> GROUPCOUNTMIN = Key.create(Type.INTEGER, "groupcountmin");
    Key<Integer> GROUPCOUNTMAX = Key.create(Type.INTEGER, "groupcountmax");

}
