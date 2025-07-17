package mcjty.incontrol;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.registries.ForgeRegistries;

public class ModBusHandler {

    public static void addEntityAttributes(EntityAttributeModificationEvent event) {
        Attribute attackDamage = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation("minecraft", "generic.attack_damage"));
        Attribute attackKnockback = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation("minecraft", "generic.attack_knockback"));
        event.add(EntityType.COW, attackDamage, 1.0);
        event.add(EntityType.COW, attackKnockback, 0.1);
    }
}
