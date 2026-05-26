package net.hellomouse.createrailgun.registry;

import net.hellomouse.createrailgun.CreateRailgun;
import net.hellomouse.createrailgun.entity.RailgunSlugEntity;
import net.hellomouse.createrailgun.munitions.big_cannon.BurstShellProjectile;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import rbasamoyai.createbigcannons.index.CBCMunitionPropertiesHandlers;
import rbasamoyai.createbigcannons.munitions.config.MunitionPropertiesHandler;

import net.minecraft.world.entity.EntityType.Builder;
import rbasamoyai.createbigcannons.munitions.big_cannon.AbstractBigCannonProjectile;


public class CREntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, CreateRailgun.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<RailgunSlugEntity>> RAILGUN_PROJECTILE =
            ENTITY_TYPES.register("railgun_projectile", () ->
                    EntityType.Builder.<RailgunSlugEntity>of(RailgunSlugEntity::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(10) // Render distance
                            .updateInterval(1)
                            .build("railgun_shell")
            );
    public static final DeferredHolder<EntityType<?>, EntityType<BurstShellProjectile>> BURST_SHELL = projectile("burst_shell", BurstShellProjectile::new);

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }

    private static <T extends AbstractBigCannonProjectile> DeferredHolder<EntityType<?>, EntityType<T>> projectile(String name, EntityType.EntityFactory<T> factory) {
        return ENTITY_TYPES.register(name, () -> Builder.of(factory, MobCategory.MISC).sized(0.8F, 0.8F).fireImmune().clientTrackingRange(16).updateInterval(1).build(name));
    }

    public static void registerHandlers() {
        MunitionPropertiesHandler.registerProjectileHandler(BURST_SHELL.get(), CBCMunitionPropertiesHandlers.COMMON_SHELL_BIG_CANNON_PROJECTILE);
    }
}
