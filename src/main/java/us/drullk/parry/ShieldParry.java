package us.drullk.parry;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.DamagingProjectileEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ITag;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.BiConsumer;

@Mod(ShieldParry.MODID)
@Mod.EventBusSubscriber(modid = ShieldParry.MODID)
public class ShieldParry {
    public static final String MODID = "parry";

    public static final ITag.INamedTag<EntityType<?>> BYPASSES = EntityTypeTags.bind(MODID + ":projectiles_parrying_disabled");

    private static Logger LOGGER = LogManager.getLogger(ShieldParry.MODID);

    public ShieldParry() {
        Pair<ParryConfig, ForgeConfigSpec> pairConfigSpec = new ForgeConfigSpec.Builder().configure(ParryConfig::new);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, pairConfigSpec.getRight());
        ParryConfig.INSTANCE = pairConfigSpec.getLeft();
    }

    private static <T extends ProjectileEntity> boolean parryProjectile(T projectile, LivingEntity entityBlocking, boolean takeOwnership, BiConsumer<Vector3d, T> trajectoryChange) {
        if (!ShieldParry.BYPASSES.contains(projectile.getType()) && entityBlocking.isBlocking() && entityBlocking.getUseItem().getUseDuration() - entityBlocking.getUseItemRemainingTicks() <= applyTimerBonus(ParryConfig.INSTANCE.shieldParryTicks.get(), entityBlocking.getUseItem(), ParryConfig.INSTANCE.shieldEnchantmentMultiplier.get())) {
            if (takeOwnership) {
                projectile.setOwner(entityBlocking);
                projectile.leftOwner = true;
            }

            trajectoryChange.accept(entityBlocking.getLookAngle(), projectile);

            return true;
        }

        return false;
    }

    // FIXME Curse your lack of instanceof patterns, J8!
    private static boolean dispatchShieldParry(Entity possibleProjectile, RayTraceResult rayTraceResult, boolean takeOwnership, BiConsumer<Vector3d, ProjectileEntity> trajectoryChange) {
        if (possibleProjectile.level.isClientSide() || !(possibleProjectile instanceof ProjectileEntity) || !(rayTraceResult instanceof EntityRayTraceResult))
            return false;

        Entity entity = ((EntityRayTraceResult) rayTraceResult).getEntity();

        return entity instanceof LivingEntity && parryProjectile((ProjectileEntity) possibleProjectile, (LivingEntity) entity, takeOwnership, trajectoryChange);
    }

    private static <T extends ProjectileEntity> boolean dispatchProjectileParry(T projectile, RayTraceResult rayTraceResult, boolean takeOwnership, BiConsumer<Vector3d, T> trajectoryChange) {
        if (projectile.level.isClientSide() || !(rayTraceResult instanceof EntityRayTraceResult))
            return false;

        Entity entity = ((EntityRayTraceResult) rayTraceResult).getEntity();

        return entity instanceof LivingEntity && parryProjectile(projectile, (LivingEntity) entity, takeOwnership, trajectoryChange);
    }

    @SubscribeEvent
    public static void parryThisCasual(ProjectileImpactEvent event) {
        if (dispatchShieldParry(event.getEntity(), event.getRayTraceResult(), !(event.getEntity() instanceof FishingBobberEntity), (reboundAngle, projectile) -> {
            projectile.shoot(reboundAngle.x, reboundAngle.y, reboundAngle.z, 1.1F, 0.1F);  // reflect faster and more accurately

            if (projectile instanceof DamagingProjectileEntity) {
                DamagingProjectileEntity damagingProjectile = (DamagingProjectileEntity) projectile;

                damagingProjectile.xPower = reboundAngle.x * 0.1D;
                damagingProjectile.yPower = reboundAngle.y * 0.1D;
                damagingProjectile.zPower = reboundAngle.z * 0.1D;
            }
        })) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void arrowParry(ProjectileImpactEvent.Arrow event) {
        if (dispatchProjectileParry(event.getArrow(), event.getRayTraceResult(), true, (reboundAngle, projectile) -> {
            projectile.shoot(reboundAngle.x, reboundAngle.y, reboundAngle.z, 1.1F, 0.1F);  // reflect faster and more accurately
        }))
            event.setCanceled(true);
    }

    @SubscribeEvent
    public static void fireballParry(ProjectileImpactEvent.Fireball event) {
        if (dispatchProjectileParry(event.getFireball(), event.getRayTraceResult(), true, (reboundAngle, projectile) -> {
            projectile.shoot(reboundAngle.x, reboundAngle.y, reboundAngle.z, 1.1F, 0.1F);  // reflect faster and more accurately

            projectile.xPower = reboundAngle.x * 0.1D;
            projectile.yPower = reboundAngle.y * 0.1D;
            projectile.zPower = reboundAngle.z * 0.1D;
        }))
            event.setCanceled(true);
    }

    @SubscribeEvent
    public static void throwableParry(ProjectileImpactEvent.Throwable event) {
        if (dispatchProjectileParry(event.getThrowable(), event.getRayTraceResult(), true, (reboundAngle, projectile) -> {
            projectile.shoot(reboundAngle.x, reboundAngle.y, reboundAngle.z, 1.1F, 0.1F);  // reflect faster and more accurately
        }))
            event.setCanceled(true);
    }

    @SubscribeEvent
    public static void fireworkParry(ProjectileImpactEvent.FireworkRocket event) {
        if (dispatchProjectileParry(event.getFireworkRocket(), event.getRayTraceResult(), true, (reboundAngle, projectile) -> {
            projectile.shoot(reboundAngle.x, reboundAngle.y, reboundAngle.z, 1.1F, 0.1F);  // reflect faster and more accurately
        }))
            event.setCanceled(true);
    }

    @SubscribeEvent
    public static void fishhookParry(ProjectileImpactEvent.FishingBobber event) {
        if (dispatchProjectileParry(event.getFishingBobber(), event.getRayTraceResult(), false, (reboundAngle, projectile) -> {
            projectile.shoot(reboundAngle.x, reboundAngle.y, reboundAngle.z, 1.1F, 0.1F);  // reflect faster and more accurately
        }))
            event.setCanceled(true);
    }

    // TODO What was this even for???
    /*@SubscribeEvent // Not feeling bothered writing a forge patch for another event related to an item's enchantability
    public static void enchantShieldLevels(EnchantmentLevelSetEvent event) {
        ItemStack stack = event.getItem();
        Item item = stack.getItem();

        if (item instanceof ItemShield || item.isShield(stack, null))
            event.setLevel(1);
    }*/

    private static int applyTimerBonus(int base, ItemStack stack, double multiplier) {
        //LOGGER.info(base + base * getEnchantedLevel(stack) * multiplier);

        return (int) (base + base * EnchantmentHelper.getItemEnchantmentLevel(ParryEnchantment.reboundEnchantment, stack) * multiplier);
    }
}
