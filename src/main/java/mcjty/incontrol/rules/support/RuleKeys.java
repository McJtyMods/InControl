package mcjty.incontrol.rules.support;

import mcjty.tools.rules.CommonRuleKeys;
import mcjty.tools.typed.AttributeMap;
import mcjty.tools.typed.Key;
import mcjty.tools.typed.Type;

public interface RuleKeys extends CommonRuleKeys {

    // Inputs
    Key<String> MINCOUNT = Key.create(Type.JSON, "mincount");
    Key<String> MAXCOUNT = Key.create(Type.JSON, "maxcount");
    Key<Boolean> CANSPAWNHERE = Key.create(Type.BOOLEAN, "canspawnhere");
    Key<Boolean> NOTCOLLIDING = Key.create(Type.BOOLEAN, "notcolliding");
    Key<Boolean> PASSIVE = Key.create(Type.BOOLEAN, "passive");
    Key<Boolean> HOSTILE = Key.create(Type.BOOLEAN, "hostile");
    Key<String> MOB = Key.create(Type.STRING, "mob");
    Key<String> MOD = Key.create(Type.STRING, "mod");

    Key<Boolean> SPAWNER = Key.create(Type.BOOLEAN, "spawner");

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
    Key<AttributeMap> ACTION_MOBS = Key.create(Type.MAP, "mobs");
    Key<String> ACTION_REMOVE = Key.create(Type.JSON, "remove");
    Key<String> ACTION_REMOVE_MOBS = Key.create(Type.STRING, "remove");
    Key<String> MOB_NAME = Key.create(Type.STRING, "mob");
    Key<Integer> MOB_WEIGHT = Key.create(Type.INTEGER, "weight");
    Key<Integer> MOB_GROUPCOUNTMIN = Key.create(Type.INTEGER, "groupcountmin");
    Key<Integer> MOB_GROUPCOUNTMAX = Key.create(Type.INTEGER, "groupcountmax");

}
