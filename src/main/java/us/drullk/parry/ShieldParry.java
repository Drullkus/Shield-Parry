package us.drullk.parry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

@Mod(ShieldParry.MODID)
@Mod.EventBusSubscriber(modid = ShieldParry.MODID)
public class ShieldParry {
    public static final String MODID = "parry";
    public static final TagKey<EntityType<?>> PROJECTILES_DISABLED_FOR_PARRYING = TagKey.create(Registries.ENTITY_TYPE, ShieldParry.modId("projectiles_parrying_disabled"));
    public static final TagKey<Item> EXCLUDED_SHIELDS = TagKey.create(Registries.ITEM, ShieldParry.modId("excluded_shields"));

    public ShieldParry() {
        Pair<ParryConfig, ForgeConfigSpec> pairConfigSpec = new ForgeConfigSpec.Builder().configure(ParryConfig::new);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, pairConfigSpec.getRight());
        ParryConfig.INSTANCE = pairConfigSpec.getLeft();
    }

    private static <T extends Projectile> boolean tryParry(T projectile, LivingEntity entityBlocking, boolean takeOwnership) {
        if (!projectile.getType().is(PROJECTILES_DISABLED_FOR_PARRYING) && entityBlocking.isBlocking() && projectile.getOwner() != entityBlocking) {
            ItemStack itemUsed = entityBlocking.getUseItem();
            if (!itemUsed.is(EXCLUDED_SHIELDS) && itemUsed.getUseDuration() - entityBlocking.getUseItemRemainingTicks() <= applyTimerBonus(ParryConfig.INSTANCE.shieldParryTicks.get(), itemUsed, ParryConfig.INSTANCE.shieldEnchantmentMultiplier.get())) {
                return parryProjectile(projectile, entityBlocking, takeOwnership);
            }
        }

        return false;
    }

    private static <T extends Projectile> boolean parryProjectile(T projectile, LivingEntity entityBlocking, boolean takeOwnership) {
        if (takeOwnership) transferOwnership(projectile, entityBlocking);

        Vec3 reboundAngle = entityBlocking.getLookAngle();

        projectile.shoot(reboundAngle.x, reboundAngle.y, reboundAngle.z, 1.1F, 0.1F);  // reflect faster and more accurately

        if (projectile instanceof AbstractHurtingProjectile damagingProjectile) {
            damagingProjectile.xPower = reboundAngle.x * 0.1D;
            damagingProjectile.yPower = reboundAngle.y * 0.1D;
            damagingProjectile.zPower = reboundAngle.z * 0.1D;
        }

        // Also used in parrying Ghast Fireball
        projectile.hurtMarked = true;

        return true;
    }

    private static <T extends Projectile> void transferOwnership(T projectile, LivingEntity entityBlocking) {
        if (projectile instanceof AbstractArrow arrow) {
            // AbstractArrow overrides setOwner, override changes state for its pickup based on owner
            AbstractArrow.Pickup priorPickupState = arrow.pickup;

            arrow.setOwner(entityBlocking);
            arrow.leftOwner = true;

            // Forge's event for rebounding projectiles has an issue that causes the server-side to stall if piercing arrows are involved
            // Though this is a reasonable long-term nerf anyway: Loss of 1 pierce level upon rebound
            byte pierceLevel = arrow.getPierceLevel();
            if (pierceLevel > 0) {
                arrow.setPierceLevel((byte) (pierceLevel - 1));
            }

            // Re-set the pre-fetched value
            arrow.pickup = priorPickupState;
        } else {
            projectile.setOwner(entityBlocking);
            projectile.leftOwner = true;
        }
    }

    @SubscribeEvent
    public static void parryThisCasual(ProjectileImpactEvent event) {
        if (!event.getEntity().getLevel().isClientSide()
                && event.getEntity() instanceof Projectile projectile
                && event.getRayTraceResult() instanceof EntityHitResult entityHitResult
                && entityHitResult.getEntity() instanceof LivingEntity livingEntity
                && tryParry(projectile, livingEntity, !(projectile instanceof FishingHook))
        ) {
            event.setCanceled(true);
        }
    }

    private static int applyTimerBonus(int base, ItemStack stack, double multiplier) {
        //LOGGER.info(base + base * getEnchantedLevel(stack) * multiplier);

        return (int) (base + base * EnchantmentHelper.getItemEnchantmentLevel(ParryEnchantment.reboundEnchantment, stack) * multiplier);
    }

    public static ResourceLocation modId(String name) {
        return new ResourceLocation(MODID, name);
    }
}
