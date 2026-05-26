package net.hellomouse.createrailgun.registry;

import net.hellomouse.createrailgun.CreateRailgun;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public class CRSounds {
    public static final SoundEvent RAILGUN_CHARGE = SoundEvent.createFixedRangeEvent(ResourceLocation.fromNamespaceAndPath(CreateRailgun.MODID, "railgun_charge"), 16.0F);
    public static final SoundEvent LOW_POWER = SoundEvent.createFixedRangeEvent(ResourceLocation.fromNamespaceAndPath(CreateRailgun.MODID, "low_power"), 8.0F);
}
