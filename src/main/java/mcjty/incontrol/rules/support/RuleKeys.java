package mcjty.incontrol.rules.support;

import mcjty.tools.rules.CommonRuleKeys;
import mcjty.tools.typed.AttributeMap;
import mcjty.tools.typed.Key;
import mcjty.tools.typed.Type;

public interface RuleKeys extends CommonRuleKeys {

    // Inputs
    Key<String> MINCOUNT = Key.create(Type.STRING, "mincount");
    Key<String> MAXCOUNT = Key.create(Type.STRING, "maxcount");
    Key<Boolean> CANSPAWNHERE = Key.create(Type.BOOLEAN, "canspawnhere");
    Key<Boolean> NOTCOLLIDING = Key.create(Type.BOOLEAN, "notcolliding");
    Key<Boolean> PASSIVE = Key.create(Type.BOOLEAN, "passive");
    Key<Boolean> HOSTILE = Key.create(Type.BOOLEAN, "hostile");
    Key<String> MOB = Key.create(Type.STRING, "mob");
    Key<String> MOD = Key.create(Type.STRING, "mod");

    Key<Boolean> INCITY = Key.create(Type.BOOLEAN, "incity");
    Key<Boolean> INBUILDING = Key.create(Type.BOOLEAN, "inbuilding");
    Key<Boolean> INSTREET = Key.create(Type.BOOLEAN, "instreet");
    Key<Boolean> INSPHERE = Key.create(Type.BOOLEAN, "insphere");
    Key<Boolean> SPAWNER = Key.create(Type.BOOLEAN, "spawner");
    Key<Boolean> SUMMER = Key.create(Type.BOOLEAN, "summer");
    Key<Boolean> WINTER = Key.create(Type.BOOLEAN, "winter");
    Key<Boolean> SPRING = Key.create(Type.BOOLEAN, "spring");
    Key<Boolean> AUTUMN = Key.create(Type.BOOLEAN, "autumn");

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
    Key<Boolean> REALPLAYER = Key.create(Type.BOOLEAN, "realplayer");
    Key<Boolean> FAKEPLAYER = Key.create(Type.BOOLEAN, "fakeplayer");
    Key<Boolean> PROJECTILE = Key.create(Type.BOOLEAN, "projectile");
    Key<Boolean> EXPLOSION = Key.create(Type.BOOLEAN, "explosion");
    Key<Boolean> FIRE = Key.create(Type.BOOLEAN, "fire");
    Key<Boolean> MAGIC = Key.create(Type.BOOLEAN, "magic");
    Key<String> GAMESTAGE = Key.create(Type.STRING, "gamestage");

    Key<String> ACTION_ITEMNBT = Key.create(Type.JSON, "nbt");
    Key<String> ACTION_ITEM = Key.create(Type.STRING, "item");
    Key<String> ACTION_ITEMCOUNT = Key.create(Type.STRING, "itemcount");
    Key<Boolean> ACTION_REMOVEALL = Key.create(Type.BOOLEAN, "removeall");

    Key<Integer> ACTION_SETXP = Key.create(Type.INTEGER, "setxp");
    Key<Float> ACTION_MULTXP = Key.create(Type.FLOAT, "multxp");
    Key<Float> ACTION_ADDXP = Key.create(Type.FLOAT, "addxp");

    // Mob spawn entry
    Key<AttributeMap> ACTION_MOBS = Key.create(Type.MAP, "mobs");
    Key<String> ACTION_REMOVE = Key.create(Type.STRING, "remove");
    Key<String> ACTION_MOBNBT = Key.create(Type.JSON, "nbt");
    Key<String> MOB_NAME = Key.create(Type.STRING, "mob");
    Key<Integer> MOB_WEIGHT = Key.create(Type.INTEGER, "weight");
    Key<Integer> MOB_GROUPCOUNTMIN = Key.create(Type.INTEGER, "groupcountmin");
    Key<Integer> MOB_GROUPCOUNTMAX = Key.create(Type.INTEGER, "groupcountmax");

}
