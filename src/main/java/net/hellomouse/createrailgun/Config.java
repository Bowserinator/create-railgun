package net.hellomouse.createrailgun;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue EXPLOSION_POWER = BUILDER.defineInRange("explosionPower", 5, 0, Integer.MAX_VALUE);
    public static final ModConfigSpec.IntValue POWER_BUFFER_REQUIRED = BUILDER.defineInRange("powerBufferRequired", 5000, 0, Integer.MAX_VALUE);
    public static final ModConfigSpec.IntValue COOLDOWN_TICKS = BUILDER.defineInRange("cooldownTicks", 120, 0, Integer.MAX_VALUE);
    public static final ModConfigSpec.IntValue CHARGE_TIME_TICKS = BUILDER.defineInRange("chargeTimeTicks", 20, 0, Integer.MAX_VALUE);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private Config() {
    }
}