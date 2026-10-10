package mcjty.incontrol.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import mcjty.incontrol.platform.SpawnHooks;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;

/** Wrap each vanilla override so allow/deny skips the entire finalization, including subclass work. */
@Mixin(value = {
    net.minecraft.world.entity.AgeableMob.class,
    net.minecraft.world.entity.Mob.class,
    net.minecraft.world.entity.animal.axolotl.Axolotl.class,
    net.minecraft.world.entity.animal.camel.Camel.class,
    net.minecraft.world.entity.animal.chicken.Chicken.class,
    net.minecraft.world.entity.animal.cow.Cow.class,
    net.minecraft.world.entity.animal.dolphin.Dolphin.class,
    net.minecraft.world.entity.animal.equine.AbstractHorse.class,
    net.minecraft.world.entity.animal.equine.Horse.class,
    net.minecraft.world.entity.animal.equine.Llama.class,
    net.minecraft.world.entity.animal.equine.TraderLlama.class,
    net.minecraft.world.entity.animal.equine.ZombieHorse.class,
    net.minecraft.world.entity.animal.feline.Cat.class,
    net.minecraft.world.entity.animal.feline.Ocelot.class,
    net.minecraft.world.entity.animal.fish.AbstractSchoolingFish.class,
    net.minecraft.world.entity.animal.fish.Salmon.class,
    net.minecraft.world.entity.animal.fish.TropicalFish.class,
    net.minecraft.world.entity.animal.fox.Fox.class,
    net.minecraft.world.entity.animal.frog.Frog.class,
    net.minecraft.world.entity.animal.goat.Goat.class,
    net.minecraft.world.entity.animal.golem.CopperGolem.class,
    net.minecraft.world.entity.animal.nautilus.AbstractNautilus.class,
    net.minecraft.world.entity.animal.nautilus.ZombieNautilus.class,
    net.minecraft.world.entity.animal.panda.Panda.class,
    net.minecraft.world.entity.animal.parrot.Parrot.class,
    net.minecraft.world.entity.animal.pig.Pig.class,
    net.minecraft.world.entity.animal.polarbear.PolarBear.class,
    net.minecraft.world.entity.animal.rabbit.Rabbit.class,
    net.minecraft.world.entity.animal.sheep.Sheep.class,
    net.minecraft.world.entity.animal.squid.Squid.class,
    net.minecraft.world.entity.animal.turtle.Turtle.class,
    net.minecraft.world.entity.animal.wolf.Wolf.class,
    net.minecraft.world.entity.monster.PatrollingMonster.class,
    net.minecraft.world.entity.monster.Phantom.class,
    net.minecraft.world.entity.monster.Shulker.class,
    net.minecraft.world.entity.monster.Strider.class,
    net.minecraft.world.entity.monster.Vex.class,
    net.minecraft.world.entity.monster.Zoglin.class,
    net.minecraft.world.entity.monster.cubemob.AbstractCubeMob.class,
    net.minecraft.world.entity.monster.cubemob.MagmaCube.class,
    net.minecraft.world.entity.monster.cubemob.Slime.class,
    net.minecraft.world.entity.monster.hoglin.Hoglin.class,
    net.minecraft.world.entity.monster.illager.Illusioner.class,
    net.minecraft.world.entity.monster.illager.Pillager.class,
    net.minecraft.world.entity.monster.illager.Vindicator.class,
    net.minecraft.world.entity.monster.piglin.Piglin.class,
    net.minecraft.world.entity.monster.piglin.PiglinBrute.class,
    net.minecraft.world.entity.monster.skeleton.AbstractSkeleton.class,
    net.minecraft.world.entity.monster.skeleton.WitherSkeleton.class,
    net.minecraft.world.entity.monster.spider.CaveSpider.class,
    net.minecraft.world.entity.monster.spider.Spider.class,
    net.minecraft.world.entity.monster.warden.Warden.class,
    net.minecraft.world.entity.monster.zombie.Drowned.class,
    net.minecraft.world.entity.monster.zombie.Husk.class,
    net.minecraft.world.entity.monster.zombie.Zombie.class,
    net.minecraft.world.entity.monster.zombie.ZombieVillager.class,
    net.minecraft.world.entity.npc.villager.AbstractVillager.class,
    net.minecraft.world.entity.npc.villager.Villager.class,
    net.minecraft.world.entity.raid.Raider.class
})
public abstract class FinalizeSpawnMixin {
    @WrapMethod(method = "finalizeSpawn")
    private SpawnGroupData incontrol$finalize(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason type, SpawnGroupData data, Operation<SpawnGroupData> original) {
        return SpawnHooks.finalize((Mob) (Object) this, level, type, data, () -> original.call(level, difficulty, type, data));
    }
}
