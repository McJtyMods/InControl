package mcjty.incontrol.rules;

import mcjty.incontrol.tools.rules.RuleBase;
import net.minecraft.entity.monster.ZombieEntity;

public interface SummonEventGetter extends RuleBase.EventGetter {
    ZombieEntity getZombieHelper();
}
