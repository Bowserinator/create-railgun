package net.hellomouse.createrailgun;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER_SERVER = new ModConfigSpec.Builder();
    private static final ModConfigSpec.Builder BUILDER_CLIENT = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue EXPLOSION_POWER = BUILDER_SERVER.defineInRange("explosionPower", 15, 0, Integer.MAX_VALUE);
    public static final ModConfigSpec.IntValue POWER_BUFFER_REQUIRED = BUILDER_SERVER.defineInRange("powerBufferRequired", 5000, 0, Integer.MAX_VALUE);
    public static final ModConfigSpec.IntValue COOLDOWN_TICKS = BUILDER_SERVER.defineInRange("cooldownTicks", 120, 0, Integer.MAX_VALUE);
    public static final ModConfigSpec.IntValue CHARGE_TIME_TICKS = BUILDER_SERVER.defineInRange("chargeTimeTicks", 20, 0, Integer.MAX_VALUE);
    public static final ModConfigSpec.IntValue MAX_RAILS = BUILDER_SERVER.defineInRange("maxRails", 7, 1, 64);
    public static final ModConfigSpec.DoubleValue VELOCITY_SCALE = BUILDER_SERVER.defineInRange("muzzleVelScale", 3.0, 0.1, 16.0);

    // ----- Client -------
    public static final ModConfigSpec.DoubleValue TRAIL_OPACITY = BUILDER_CLIENT.defineInRange("trailOpacity", 0.6, 0.0, 1.0);

    public static final ModConfigSpec SPEC_SERVER = BUILDER_SERVER.build();
    public static final ModConfigSpec SPEC_CLIENT = BUILDER_CLIENT.build();

    private Config() {
    }
}