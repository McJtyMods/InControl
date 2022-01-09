package mcjty.incontrol.rules;

import mcjty.incontrol.tools.rules.RuleBase;
import net.minecraft.world.entity.monster.Zombie;

public interface SummonEventGetter extends RuleBase.EventGetter {
    Zombie getZombieHelper();
}
