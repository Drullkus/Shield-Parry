package us.drullk.parry;

import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import org.apache.commons.lang3.tuple.Pair;
import us.drullk.parry.data.ParryDatagen;

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

    private <T extends Projectile> boolean tryParry(T projectile, LivingEntity entityBlocking) {
		if (projectile.getType().is(PROJECTILES_DISABLED_FOR_PARRYING) || !entityBlocking.isBlocking() || projectile.getOwner() == entityBlocking) {
			return false;
		}

        ItemStack itemUsed = entityBlocking.getUseItem();
		if (itemUsed.is(EXCLUDED_SHIELDS))
			return false;

		int timeUp = itemUsed.getUseDuration(entityBlocking) - entityBlocking.getUseItemRemainingTicks();
		int baseLimit = this.config.shieldParryTicks.get();
		int limitToCrit = getReboundDuration(entityBlocking.registryAccess(), baseLimit, itemUsed, this.config.shieldEnchantmentMultiplier.get());
		// Cannot rebound if shield up longer than limit
		if (timeUp > limitToCrit) {
			return false;
		}

		if (entityBlocking.level() instanceof ServerLevel serverLevel) {
			boolean isEnchantBoosted = timeUp <= baseLimit;
			this.sendEffects(entityBlocking, serverLevel, isEnchantBoosted, projectile.getX(), projectile.getY(), projectile.getZ());
		}

		return projectile.deflect(ProjectileDeflection.AIM_DEFLECT, entityBlocking, projectile.getOwner(), true);
	}

	private void sendEffects(LivingEntity entityBlocking, ServerLevel serverLevel, boolean isEnchantBoosted, double effectX, double effectY, double effectZ) {
        serverLevel.playSeededSound(null, effectX, effectY, effectZ, isEnchantBoosted ? SoundEvents.PLAYER_ATTACK_SWEEP : SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1f, 1f, entityBlocking.getRandom().nextInt());
		ParticleOptions particleType = isEnchantBoosted ? ParticleTypes.ENCHANTED_HIT : ParticleTypes.CRIT;
		if (entityBlocking instanceof ServerPlayer playerBlocking) {
            serverLevel.sendParticles(playerBlocking, particleType, false, effectX, effectY, effectZ, 3, 0.1, 0.1, 0.1, 0.1);
        } else {
            serverLevel.sendParticles(particleType, effectX, effectY, effectZ, 3, 0.1, 0.1, 0.1, 0.1);
        }
    }

    @SuppressWarnings("OptionalIsPresent")
	private static int getReboundDuration(RegistryAccess registryAccess, int base, ItemStack stack, double multiplier) {
        Optional<Holder.Reference<Enchantment>> reboundEnchantment = registryAccess.registry(Registries.ENCHANTMENT).flatMap(r -> r.getHolder(ParryEnchantments.REBOUND));

        if (reboundEnchantment.isEmpty()) {
            return 0;
        }

		return Mth.ceil(base + base * stack.getEnchantmentLevel(reboundEnchantment.get()) * multiplier);
    }

    public static ResourceLocation modId(String name) {
        return ResourceLocation.fromNamespaceAndPath(MODID, name);
    }
}
