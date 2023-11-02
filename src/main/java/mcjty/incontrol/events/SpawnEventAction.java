package mcjty.incontrol.events;

import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record SpawnEventAction(List<ResourceLocation> mobid, int attempts,
                               float mindistance, float maxdistance, int minamount, int maxamount,
                               boolean norestrictions) {
}
