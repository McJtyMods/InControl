package mcjty.incontrol.rules.support;

import mcjty.incontrol.tools.typed.Key;
import mcjty.incontrol.tools.typed.Type;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public interface RuleKeys {

    // Special
    Key<String> PHASE = Key.create(Type.STRING, "phase");
    Key<String> WHEN = Key.create(Type.STRING, "when");
    Key<Integer> TIMEOUT = Key.create(Type.INTEGER, "timeout");

    // Inputs
    Key<String> TIME = Key.create(Type.STRING, "time");
    Key<Integer> MINTIME = Key.create(Type.INTEGER, "mintime");
    Key<Integer> MAXTIME = Key.create(Type.INTEGER, "maxtime");

    Key<String> LIGHT = Key.create(Type.STRING, "light");
    Key<Integer> MINLIGHT = Key.create(Type.INTEGER, "minlight");
    Key<Integer> MAXLIGHT = Key.create(Type.INTEGER, "maxlight");
    Key<Integer> MINLIGHT_FULL = Key.create(Type.INTEGER, "minlight_full");
    Key<Integer> MAXLIGHT_FULL = Key.create(Type.INTEGER, "maxlight_full");

    Key<String> HEIGHT = Key.create(Type.STRING, "height");
    Key<Integer> MINHEIGHT = Key.create(Type.INTEGER, "minheight");
    Key<Integer> MAXHEIGHT = Key.create(Type.INTEGER, "maxheight");

    Key<Float> MINSPAWNDIST = Key.create(Type.FLOAT, "minspawndist");
    Key<Float> MAXSPAWNDIST = Key.create(Type.FLOAT, "maxspawndist");

    Key<Float> MINDIFFICULTY = Key.create(Type.FLOAT, "mindifficulty");
    Key<Float> MAXDIFFICULTY = Key.create(Type.FLOAT, "maxdifficulty");

    Key<Float> RANDOM = Key.create(Type.FLOAT, "random");
    Key<Boolean> SEESKY = Key.create(Type.BOOLEAN, "seesky");
    Key<Boolean> SLIME = Key.create(Type.BOOLEAN, "slime");
    Key<String> WEATHER = Key.create(Type.STRING, "weather");
    Key<String> BIOMETAGS = Key.create(Type.STRING, "biometags");
    Key<String> DIFFICULTY = Key.create(Type.STRING, "difficulty");
    Key<String> BLOCK = Key.create(Type.JSON, "block");
    Key<String> AREA = Key.create(Type.STRING, "area");
    Key<String> BLOCKOFFSET = Key.create(Type.JSON, "blockoffset");
    Key<String> BIOME = Key.create(Type.STRING, "biome");
    Key<String> BIOMETYPE = Key.create(Type.STRING, "biometype");
    Key<String> STRUCTURE = Key.create(Type.STRING, "structure");
    Key<ResourceKey<Level>> DIMENSION = Key.create(Type.DIMENSION_TYPE, "dimension");
    Key<String> DIMENSION_MOD = Key.create(Type.STRING, "dimensionmod");

    Key<String> SCOREBOARDTAGS_ALL = Key.create(Type.STRING, "scoreboardtags_all");
    Key<String> SCOREBOARDTAGS_ANY = Key.create(Type.STRING, "scoreboardtags_any");
    Key<String> HELMET = Key.create(Type.JSON, "helmet");
    Key<String> CHESTPLATE = Key.create(Type.JSON, "chestplate");
    Key<String> LEGGINGS = Key.create(Type.JSON, "leggings");
    Key<String> BOOTS = Key.create(Type.JSON, "boots");
    Key<String> LACKHELMET = Key.create(Type.JSON, "lackhelmet");
    Key<String> LACKCHESTPLATE = Key.create(Type.JSON, "lackchestplate");
    Key<String> LACKLEGGINGS = Key.create(Type.JSON, "lackleggings");
    Key<String> LACKBOOTS = Key.create(Type.JSON, "lackboots");
    Key<String> HELDITEM = Key.create(Type.JSON, "helditem");
    Key<String> PLAYER_HELDITEM = Key.create(Type.JSON, "playerhelditem");
    Key<String> LACKHELDITEM = Key.create(Type.JSON, "lackhelditem");
    Key<String> OFFHANDITEM = Key.create(Type.JSON, "offhanditem");
    Key<String> LACKOFFHANDITEM = Key.create(Type.JSON, "lackoffhanditem");
    Key<String> BOTHHANDSITEM = Key.create(Type.JSON, "bothhandsitem");

    Key<Boolean> INCITY = Key.create(Type.BOOLEAN, "incity");
    Key<Boolean> INBUILDING = Key.create(Type.BOOLEAN, "inbuilding");
    Key<Boolean> INSTREET = Key.create(Type.BOOLEAN, "instreet");
    Key<Boolean> INSPHERE = Key.create(Type.BOOLEAN, "insphere");
    Key<String> BUILDING = Key.create(Type.STRING, "building");

    Key<String> GAMESTAGE = Key.create(Type.STRING, "gamestage");

    Key<Boolean> SUMMER = Key.create(Type.BOOLEAN, "summer");
    Key<Boolean> WINTER = Key.create(Type.BOOLEAN, "winter");
    Key<Boolean> SPRING = Key.create(Type.BOOLEAN, "spring");
    Key<Boolean> AUTUMN = Key.create(Type.BOOLEAN, "autumn");

    Key<String> AMULET = Key.create(Type.JSON, "amulet");
    Key<String> RING = Key.create(Type.JSON, "ring");
    Key<String> BELT = Key.create(Type.JSON, "belt");
    Key<String> TRINKET = Key.create(Type.JSON, "trinket");
    Key<String> HEAD = Key.create(Type.JSON, "head");
    Key<String> BODY = Key.create(Type.JSON, "body");
    Key<String> CHARM = Key.create(Type.JSON, "charm");

    Key<String> STATE = Key.create(Type.STRING, "state");
    Key<String> PSTATE = Key.create(Type.STRING, "pstate");

    Key<String> MINCOUNT = Key.create(Type.JSON, "mincount");
    Key<String> MAXCOUNT = Key.create(Type.JSON, "maxcount");

    Key<Object> DAYCOUNT = Key.create(Type.OBJECT, "daycount");
    Key<Integer> MINDAYCOUNT = Key.create(Type.INTEGER, "mindaycount");
    Key<Integer> MAXDAYCOUNT = Key.create(Type.INTEGER, "maxdaycount");

    Key<Boolean> CANSPAWNHERE = Key.create(Type.BOOLEAN, "canspawnhere");
    Key<Boolean> NOTCOLLIDING = Key.create(Type.BOOLEAN, "notcolliding");
    Key<Boolean> PASSIVE = Key.create(Type.BOOLEAN, "passive");
    Key<Boolean> HOSTILE = Key.create(Type.BOOLEAN, "hostile");
    Key<Boolean> BABY = Key.create(Type.BOOLEAN, "baby");
    Key<String> MOB = Key.create(Type.STRING, "mob");
    Key<String> MOD = Key.create(Type.STRING, "mod");

    Key<Boolean> SPAWNER = Key.create(Type.BOOLEAN, "spawner");
    Key<Boolean> INCONTROL = Key.create(Type.BOOLEAN, "incontrol");
    Key<Boolean> EVENTSPAWN = Key.create(Type.BOOLEAN, "eventspawn");

    Key<String> ACTION_COMMAND = Key.create(Type.STRING, "command");
    Key<String> ACTION_ADDSTAGE = Key.create(Type.STRING, "addstage");
    Key<String> ACTION_REMOVESTAGE = Key.create(Type.STRING, "removestage");
    Key<String> ACTION_RESULT = Key.create(Type.STRING, "result");
    Key<Boolean> ACTION_CONTINUE = Key.create(Type.BOOLEAN, "continue");
    Key<String> ACTION_CUSTOMEVENT = Key.create(Type.STRING, "customevent");

    Key<Float> ACTION_HEALTHSET = Key.create(Type.FLOAT, "healthset");
    Key<Float> ACTION_HEALTHMULTIPLY = Key.create(Type.FLOAT, "healthmultiply");
    Key<Float> ACTION_HEALTHADD = Key.create(Type.FLOAT, "healthadd");
    Key<Float> ACTION_SPEEDSET = Key.create(Type.FLOAT, "speedset");
    Key<Float> ACTION_SPEEDMULTIPLY = Key.create(Type.FLOAT, "speedmultiply");
    Key<Float> ACTION_SPEEDADD = Key.create(Type.FLOAT, "speedadd");
    Key<Float> ACTION_DAMAGESET = Key.create(Type.FLOAT, "damageset");
    Key<Float> ACTION_DAMAGEMULTIPLY = Key.create(Type.FLOAT, "damagemultiply");
    Key<Float> ACTION_DAMAGEADD = Key.create(Type.FLOAT, "damageadd");
    Key<Float> ACTION_SIZEMULTIPLY = Key.create(Type.FLOAT, "sizemultiply");
    Key<Float> ACTION_SIZEADD = Key.create(Type.FLOAT, "sizeadd");
    Key<Boolean> ACTION_NODESPAWN = Key.create(Type.BOOLEAN, "nodespawn");

    Key<String> ACTION_POTION = Key.create(Type.STRING, "potion");
    Key<String> ACTION_HELDITEM = Key.create(Type.JSON, "helditem");
    Key<String> ACTION_ARMORCHEST = Key.create(Type.JSON, "armorchest");
    Key<String> ACTION_ARMORHELMET = Key.create(Type.JSON, "armorhelmet");
    Key<String> ACTION_ARMORLEGS = Key.create(Type.JSON, "armorlegs");
    Key<String> ACTION_ARMORBOOTS = Key.create(Type.JSON, "armorboots");
    Key<String> ACTION_MOBNBT = Key.create(Type.JSON, "nbt");
    Key<String> ACTION_CUSTOMNAME = Key.create(Type.STRING, "customname");
    Key<Boolean> ACTION_ANGRY = Key.create(Type.BOOLEAN, "angry");
    Key<String> ACTION_MESSAGE = Key.create(Type.STRING, "message");
    Key<String> ACTION_ADDSCOREBOARDTAGS = Key.create(Type.JSON, "addscoreboardtags");
    Key<String> ACTION_GIVE = Key.create(Type.JSON, "give");
    Key<String> ACTION_DROP = Key.create(Type.JSON, "drop");
    Key<String> ACTION_SETBLOCK = Key.create(Type.JSON, "setblock");
    Key<String> ACTION_SETHELDITEM = Key.create(Type.JSON, "sethelditem");
    Key<String> ACTION_SETHELDAMOUNT = Key.create(Type.STRING, "setheldamount");
    Key<String> ACTION_SETSTATE = Key.create(Type.STRING, "setstate");
    Key<String> ACTION_SETPSTATE = Key.create(Type.STRING, "setpstate");
    Key<String> ACTION_SETPHASE = Key.create(Type.STRING, "setphase");
    Key<String> ACTION_CLEARPHASE = Key.create(Type.STRING, "clearphase");
    Key<String> ACTION_TOGGLEPHASE = Key.create(Type.STRING, "togglephase");

    Key<String> ACTION_EXPLOSION = Key.create(Type.STRING, "explosion");
    Key<Integer> ACTION_FIRE = Key.create(Type.INTEGER, "fire");
    Key<Boolean> ACTION_CLEAR = Key.create(Type.BOOLEAN, "clear");
    Key<String> ACTION_DAMAGE = Key.create(Type.STRING, "damage");

    // Foor loot rules
    Key<String> SOURCE = Key.create(Type.STRING, "source");
    Key<Boolean> PLAYER = Key.create(Type.BOOLEAN, "player");
    Key<Boolean> REALPLAYER = Key.create(Type.BOOLEAN, "realplayer");
    Key<Boolean> FAKEPLAYER = Key.create(Type.BOOLEAN, "fakeplayer");
    Key<Boolean> PROJECTILE = Key.create(Type.BOOLEAN, "projectile");
    Key<Boolean> EXPLOSION = Key.create(Type.BOOLEAN, "explosion");
    Key<Boolean> FIRE = Key.create(Type.BOOLEAN, "fire");
    Key<Boolean> MAGIC = Key.create(Type.BOOLEAN, "magic");

    Key<String> ACTION_ITEMNBT = Key.create(Type.JSON, "nbt");
    Key<String> ACTION_ITEM = Key.create(Type.STRING, "item");
    Key<String> ACTION_ITEMCOUNT = Key.create(Type.STRING, "itemcount");
    Key<Boolean> ACTION_REMOVEALL = Key.create(Type.BOOLEAN, "removeall");

    Key<Integer> ACTION_SETXP = Key.create(Type.INTEGER, "setxp");
    Key<Float> ACTION_MULTXP = Key.create(Type.FLOAT, "multxp");
    Key<Float> ACTION_ADDXP = Key.create(Type.FLOAT, "addxp");

    // Mob spawn entry
    Key<String> ACTION_REMOVE = Key.create(Type.JSON, "remove");
}
