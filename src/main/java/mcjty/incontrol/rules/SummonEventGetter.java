package mcjty.incontrol.rules;

import mcjty.tools.rules.RuleBase;
import net.minecraft.entity.monster.ZombieEntity;

public interface SummonEventGetter extends RuleBase.EventGetter {
    ZombieEntity getZombieHelper();
}
