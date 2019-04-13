package baubles.api.cap;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;

class BaublesContainerProvider implements INBTSerializable<NBTTagCompound>, ICapabilityProvider {

    private final BaublesContainer container;

    public BaublesContainerProvider(BaublesContainer container) {
        this.container = container;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing facing) {
        return false;
    }

    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing facing) {
        return null;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        return this.container.serializeNBT();
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        this.container.deserializeNBT(nbt);
    }

}
