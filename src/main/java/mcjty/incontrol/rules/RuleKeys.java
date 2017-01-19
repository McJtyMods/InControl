package mcjty.incontrol.rules;

import mcjty.incontrol.typed.AttributeMap;
import mcjty.incontrol.typed.Key;
import mcjty.incontrol.typed.Type;

public interface RuleKeys {

    // Inputs
    Key<Integer> MINTIME = Key.create(Type.INTEGER, "mintime");
    Key<Integer> MAXTIME = Key.create(Type.INTEGER, "maxtime");
    Key<String> MINCOUNT = Key.create(Type.STRING, "mincount");
    Key<String> MAXCOUNT = Key.create(Type.STRING, "maxcount");
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
    Key<String> STRUCTURE = Key.create(Type.STRING, "structure");
    Key<Integer> DIMENSION = Key.create(Type.INTEGER, "dimension");

    // Outputs
    Key<String> ACTION_RESULT = Key.create(Type.STRING, "result");
    Key<Float> ACTION_HEALTHMULTIPLY = Key.create(Type.FLOAT, "healthmultiply");
    Key<Float> ACTION_HEALTHADD = Key.create(Type.FLOAT, "healthadd");
    Key<Float> ACTION_SPEEDMULTIPLY = Key.create(Type.FLOAT, "speedmultiply");
    Key<Float> ACTION_SPEEDADD = Key.create(Type.FLOAT, "speedadd");
    Key<Float> ACTION_DAMAGEMULTIPLY = Key.create(Type.FLOAT, "damagemultiply");
    Key<Float> ACTION_DAMAGEADD = Key.create(Type.FLOAT, "damageadd");
    Key<Float> ACTION_SIZEMULTIPLY = Key.create(Type.FLOAT, "sizemultiply");
    Key<Float> ACTION_SIZEADD = Key.create(Type.FLOAT, "sizeadd");
    Key<Boolean> ACTION_ANGRY = Key.create(Type.BOOLEAN, "angry");
    Key<String> ACTION_POTION = Key.create(Type.STRING, "potion");
    Key<String> ACTION_HELDITEM = Key.create(Type.STRING, "helditem");
    Key<String> ACTION_ARMORCHEST = Key.create(Type.STRING, "armorchest");
    Key<String> ACTION_ARMORHELMET = Key.create(Type.STRING, "armorhelmet");
    Key<String> ACTION_ARMORLEGS = Key.create(Type.STRING, "armorlegs");
    Key<String> ACTION_ARMORBOOTS = Key.create(Type.STRING, "armorboots");

    // Foor loot rules
    Key<String> SOURCE = Key.create(Type.STRING, "source");
    Key<Boolean> PLAYER = Key.create(Type.BOOLEAN, "player");
    Key<Boolean> PROJECTILE = Key.create(Type.BOOLEAN, "projectile");
    Key<Boolean> EXPLOSION = Key.create(Type.BOOLEAN, "explosion");
    Key<Boolean> FIRE = Key.create(Type.BOOLEAN, "fire");
    Key<Boolean> MAGIC = Key.create(Type.BOOLEAN, "magic");
    Key<String> HELDITEM = Key.create(Type.STRING, "helditem");

    Key<String> ACTION_ITEM = Key.create(Type.STRING, "item");
    Key<Boolean> ACTION_REMOVEALL = Key.create(Type.BOOLEAN, "removeall");

    // Mob spawn entry
    Key<AttributeMap> ACTION_MOBS = Key.create(Type.MAP, "mobs");
    Key<String> ACTION_REMOVE = Key.create(Type.STRING, "remove");
    Key<String> MOB_NAME = Key.create(Type.STRING, "mob");
    Key<Integer> MOB_WEIGHT = Key.create(Type.INTEGER, "weight");
    Key<Integer> MOB_GROUPCOUNTMIN = Key.create(Type.INTEGER, "groupcountmin");
    Key<Integer> MOB_GROUPCOUNTMAX = Key.create(Type.INTEGER, "groupcountmax");

}
