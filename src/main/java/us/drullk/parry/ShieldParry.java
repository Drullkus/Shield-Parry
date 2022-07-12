package us.drullk.parry;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.Projectile;
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

import java.util.function.BiConsumer;

@Mod(ShieldParry.MODID)
@Mod.EventBusSubscriber(modid = ShieldParry.MODID)
public class ShieldParry {
    public static final String MODID = "parry";
    public static final TagKey<EntityType<?>> PROJECTILES_DISABLED_FOR_PARRYING = TagKey.create(Registry.ENTITY_TYPE_REGISTRY, ShieldParry.modId("projectiles_parrying_disabled"));
    //private static Logger LOGGER = LogManager.getLogger(ShieldParry.MODID);

    public ShieldParry() {
        Pair<ParryConfig, ForgeConfigSpec> pairConfigSpec = new ForgeConfigSpec.Builder().configure(ParryConfig::new);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, pairConfigSpec.getRight());
        ParryConfig.INSTANCE = pairConfigSpec.getLeft();
    }

    private static <T extends Projectile> boolean parryProjectile(T projectile, LivingEntity entityBlocking, boolean takeOwnership, BiConsumer<Vec3, T> trajectoryChange) {
        if (!projectile.getType().is(PROJECTILES_DISABLED_FOR_PARRYING) && entityBlocking.isBlocking() && entityBlocking.getUseItem().getUseDuration() - entityBlocking.getUseItemRemainingTicks() <= applyTimerBonus(ParryConfig.INSTANCE.shieldParryTicks.get(), entityBlocking.getUseItem(), ParryConfig.INSTANCE.shieldEnchantmentMultiplier.get())) {
            if (takeOwnership) {
                projectile.setOwner(entityBlocking);
                projectile.leftOwner = true;
            }

            trajectoryChange.accept(entityBlocking.getLookAngle(), projectile);

            return true;
        }

        return false;
    }

    private static boolean dispatchShieldParry(Projectile projectile, EntityHitResult entityHitResult, boolean takeOwnership, BiConsumer<Vec3, Projectile> trajectoryChange) {
        return !projectile.level.isClientSide()
                && entityHitResult.getEntity() instanceof LivingEntity livingEntity
                && parryProjectile(projectile, livingEntity, takeOwnership, trajectoryChange);
    }

    @SubscribeEvent
    public static void parryThisCasual(ProjectileImpactEvent event) {
        if (event.getEntity() instanceof Projectile projectile && event.getRayTraceResult() instanceof EntityHitResult entityHitResult && dispatchShieldParry(projectile, entityHitResult, !(event.getEntity() instanceof FishingHook), (reboundAngle, rebounding) -> {
            rebounding.shoot(reboundAngle.x, reboundAngle.y, reboundAngle.z, 1.1F, 0.1F);  // reflect faster and more accurately

            if (rebounding instanceof AbstractHurtingProjectile damagingProjectile) {
                damagingProjectile.xPower = reboundAngle.x * 0.1D;
                damagingProjectile.yPower = reboundAngle.y * 0.1D;
                damagingProjectile.zPower = reboundAngle.z * 0.1D;
            }
        })) event.setCanceled(true);
    }

    private static int applyTimerBonus(int base, ItemStack stack, double multiplier) {
        //LOGGER.info(base + base * getEnchantedLevel(stack) * multiplier);

        return (int) (base + base * EnchantmentHelper.getItemEnchantmentLevel(ParryEnchantment.reboundEnchantment, stack) * multiplier);
    }

    public static ResourceLocation modId(String name) {
        return new ResourceLocation(MODID, name);
    }
}
