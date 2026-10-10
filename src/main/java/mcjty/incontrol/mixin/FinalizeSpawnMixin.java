package mcjty.incontrol.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import mcjty.incontrol.platform.SpawnHooks;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;

/** Wrap each vanilla override so allow/deny skips the entire finalization, including subclass work. */
@Mixin(value = {
    net.minecraft.world.entity.animal.AbstractSchoolingFish.class,
    net.minecraft.world.entity.animal.Dolphin.class,
    net.minecraft.world.entity.animal.Ocelot.class,
    net.minecraft.world.entity.animal.PolarBear.class,
    net.minecraft.world.entity.animal.Parrot.class,
    net.minecraft.world.entity.animal.Panda.class,
    net.minecraft.world.entity.animal.Rabbit.class,
    net.minecraft.world.entity.animal.TropicalFish.class,
    net.minecraft.world.entity.animal.Fox.class,
    net.minecraft.world.entity.animal.Turtle.class,
    net.minecraft.world.entity.animal.Cat.class,
    net.minecraft.world.entity.animal.Sheep.class,
    net.minecraft.world.entity.animal.Wolf.class,
    net.minecraft.world.entity.animal.axolotl.Axolotl.class,
    net.minecraft.world.entity.animal.camel.Camel.class,
    net.minecraft.world.entity.animal.goat.Goat.class,
    net.minecraft.world.entity.animal.horse.Llama.class,
    net.minecraft.world.entity.animal.horse.Horse.class,
    net.minecraft.world.entity.animal.frog.Frog.class,
    net.minecraft.world.entity.animal.horse.TraderLlama.class,
    net.minecraft.world.entity.animal.horse.AbstractHorse.class,
    net.minecraft.world.entity.monster.AbstractSkeleton.class,
    net.minecraft.world.entity.monster.PatrollingMonster.class,
    net.minecraft.world.entity.monster.Illusioner.class,
    net.minecraft.world.entity.monster.Phantom.class,
    net.minecraft.world.entity.monster.CaveSpider.class,
    net.minecraft.world.entity.monster.Drowned.class,
    net.minecraft.world.entity.monster.Pillager.class,
    net.minecraft.world.entity.monster.Slime.class,
    net.minecraft.world.entity.monster.WitherSkeleton.class,
    net.minecraft.world.entity.monster.Vindicator.class,
    net.minecraft.world.entity.monster.Vex.class,
    net.minecraft.world.entity.monster.Shulker.class,
    net.minecraft.world.entity.monster.Zombie.class,
    net.minecraft.world.entity.monster.ZombieVillager.class,
    net.minecraft.world.entity.monster.Spider.class,
    net.minecraft.world.entity.monster.piglin.PiglinBrute.class,
    net.minecraft.world.entity.monster.hoglin.Hoglin.class,
    net.minecraft.world.entity.monster.piglin.Piglin.class,
    net.minecraft.world.entity.monster.Strider.class,
    net.minecraft.world.entity.npc.AbstractVillager.class,
    net.minecraft.world.entity.monster.warden.Warden.class,
    net.minecraft.world.entity.npc.Villager.class,
    net.minecraft.world.entity.raid.Raider.class,
    net.minecraft.world.entity.AgeableMob.class,
    net.minecraft.world.entity.Mob.class
})
public abstract class FinalizeSpawnMixin {
    @WrapMethod(method = "finalizeSpawn")
    private SpawnGroupData incontrol$finalize(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType type, SpawnGroupData data, Operation<SpawnGroupData> original) {
        return SpawnHooks.finalize((Mob) (Object) this, level, type, data, () -> original.call(level, difficulty, type, data));
    }
}
