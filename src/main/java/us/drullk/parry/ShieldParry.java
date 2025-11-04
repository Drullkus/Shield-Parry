package us.drullk.parry;

import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Optional;

@Mod(ShieldParry.MODID)
public class ShieldParry {
    public static final String MODID = "parry";
    public static final TagKey<EntityType<?>> PROJECTILES_DISABLED_FOR_PARRYING = TagKey.create(Registries.ENTITY_TYPE, ShieldParry.modId("projectiles_parrying_disabled"));
    public static final TagKey<Item> EXCLUDED_SHIELDS = TagKey.create(Registries.ITEM, ShieldParry.modId("excluded_shields"));
    private final ParryConfig config;

    public ShieldParry(IEventBus modEventBus, ModContainer modContainer) {
        Pair<ParryConfig, ModConfigSpec> pairConfigSpec = new ModConfigSpec.Builder().configure(ParryConfig::new);
		this.config = pairConfigSpec.getLeft();
        modContainer.registerConfig(ModConfig.Type.COMMON, pairConfigSpec.getRight());

        NeoForge.EVENT_BUS.addListener(this::parryThisCasual);
        modEventBus.addListener(ParryDatagen::generateData);
    }

    private <T extends Projectile> boolean tryParry(T projectile, LivingEntity entityBlocking) {
        if (!projectile.getType().is(PROJECTILES_DISABLED_FOR_PARRYING) && entityBlocking.isBlocking() && projectile.getOwner() != entityBlocking) {
            ItemStack itemUsed = entityBlocking.getUseItem();
            if (!itemUsed.is(EXCLUDED_SHIELDS) && itemUsed.getUseDuration(entityBlocking) - entityBlocking.getUseItemRemainingTicks() <= getTimerBonus(entityBlocking.registryAccess(), this.config.shieldParryTicks.get(), itemUsed, this.config.shieldEnchantmentMultiplier.get())) {
                return projectile.deflect(ProjectileDeflection.AIM_DEFLECT, entityBlocking, projectile.getOwner(), true);
            }
        }

        return false;
    }

    private void parryThisCasual(ProjectileImpactEvent event) {
        if (!event.getEntity().level().isClientSide()
                && event.getEntity() instanceof Projectile projectile
                && event.getRayTraceResult() instanceof EntityHitResult entityHitResult
                && entityHitResult.getEntity() instanceof LivingEntity livingEntity
                && this.tryParry(projectile, livingEntity)
        ) {
            event.setCanceled(true);
        }
    }

    @SuppressWarnings("OptionalIsPresent")
	private static int getTimerBonus(RegistryAccess registryAccess, int base, ItemStack stack, double multiplier) {
        //LOGGER.info(base + base * getEnchantedLevel(stack) * multiplier);

        // TODO Switch to checking attribute

        Optional<Holder.Reference<Enchantment>> reboundEnchantment = registryAccess.registry(Registries.ENCHANTMENT).flatMap(r -> r.getHolder(ParryEnchantments.REBOUND));

        if (reboundEnchantment.isEmpty()) {
            return 0;
        }

        return (int) (base + base * EnchantmentHelper.getItemEnchantmentLevel(reboundEnchantment.get(), stack) * multiplier);
    }

    public static ResourceLocation modId(String name) {
        return ResourceLocation.fromNamespaceAndPath(MODID, name);
    }
}
