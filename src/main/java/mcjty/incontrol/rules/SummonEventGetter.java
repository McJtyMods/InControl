package mcjty.incontrol.rules;

import mcjty.tools.rules.RuleBase;
import net.minecraft.entity.monster.EntityZombie;

interface SummonEventGetter extends RuleBase.EventGetter {
    EntityZombie getZombieHelper();
}
