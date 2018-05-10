package us.drullk.parry;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemShield;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.enchanting.EnchantmentLevelSetEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@Mod(modid = ShieldParry.MODID, name = ShieldParry.NAME, version = ShieldParry.VERSION)
@Mod.EventBusSubscriber(modid = ShieldParry.MODID)
public class ShieldParry {
    public static final String MODID = "parry";
    public static final String NAME = "Shield Parrying";
    public static final String VERSION = "1.0";

    private static Logger LOGGER = LogManager.getLogger(ShieldParry.MODID);

    private static Enchantment enchantment;
    private static final String enchantmentName = "parry";
    private static EnumEnchantmentType enchantmentTypeShield;

    @SubscribeEvent
    public static void registerEnchantment(RegistryEvent.Register<Enchantment> event) {
        enchantmentTypeShield = EnumHelper.addEnchantmentType("shield", input -> input instanceof ItemShield || input != null && input.isShield(new ItemStack(input, 1, 0), null));

        enchantment = new EnchantmentPrecision(Enchantment.Rarity.COMMON).setRegistryName(new ResourceLocation(MODID, enchantmentName)).setName(enchantmentName);
        event.getRegistry().register(enchantment);
    }

    @SubscribeEvent
    public static void arrowParry(ProjectileImpactEvent.Arrow event) {
        final EntityArrow projectile = event.getArrow();

        if (!projectile.getEntityWorld().isRemote) {
            Entity entity = event.getRayTraceResult().entityHit;

            if (event.getEntity() != null) {
                if (entity instanceof EntityLivingBase) {
                    EntityLivingBase entityBlocking = (EntityLivingBase) entity;

                    if (entityBlocking.canBlockDamageSource(new DamageSource("parry_this") {
                        public Vec3d getDamageLocation() { return projectile.getPositionVector(); }
                    }) && (entityBlocking.getActiveItemStack().getItem().getMaxItemUseDuration(entityBlocking.getActiveItemStack()) - entityBlocking.getItemInUseCount()) <= applyTimerBonus(ModConfig.shieldParryTicksArrow, entityBlocking.getActiveItemStack(), ModConfig.shieldEnchantmentMultiplierArrow)) {
                        Vec3d playerVec3 = entityBlocking.getLookVec();

                        projectile.shoot(playerVec3.x, playerVec3.y, playerVec3.z, 1.1F, 0.1F);  // reflect faster and more accurately

                        projectile.shootingEntity = entityBlocking;

                        event.setCanceled(true);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void fireballParry(ProjectileImpactEvent.Fireball event) {
        final EntityFireball projectile = event.getFireball();

        if (!projectile.getEntityWorld().isRemote) {
            Entity entity = event.getRayTraceResult().entityHit;

            if (event.getEntity() != null) {
                if (entity instanceof EntityLivingBase) {
                    EntityLivingBase entityBlocking = (EntityLivingBase) entity;

                    if (entityBlocking.canBlockDamageSource(new DamageSource("parry_this") {
                        public Vec3d getDamageLocation() { return projectile.getPositionVector(); }
                    }) && (entityBlocking.getActiveItemStack().getItem().getMaxItemUseDuration(entityBlocking.getActiveItemStack()) - entityBlocking.getItemInUseCount()) <= applyTimerBonus(ModConfig.shieldParryTicksFireball, entityBlocking.getActiveItemStack(), ModConfig.shieldEnchantmentMultiplierFireball)) {
                        Vec3d playerVec3 = entityBlocking.getLookVec();

                        projectile.motionX = playerVec3.x;
                        projectile.motionY = playerVec3.y;
                        projectile.motionZ = playerVec3.z;
                        projectile.accelerationX = projectile.motionX * 0.1D;
                        projectile.accelerationY = projectile.motionY * 0.1D;
                        projectile.accelerationZ = projectile.motionZ * 0.1D;

                        projectile.shootingEntity = entityBlocking;

                        event.setCanceled(true);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void throwableParry(ProjectileImpactEvent.Throwable event) {
        final EntityThrowable projectile = event.getThrowable();

        if (!projectile.getEntityWorld().isRemote) {
            Entity entity = event.getRayTraceResult().entityHit;

            if (event.getEntity() != null && entity instanceof EntityLivingBase) {
                EntityLivingBase entityBlocking = (EntityLivingBase) entity;

                if (entityBlocking.canBlockDamageSource(new DamageSource("parry_this") {
                    public Vec3d getDamageLocation() { return projectile.getPositionVector(); }
                }) && (entityBlocking.getActiveItemStack().getItem().getMaxItemUseDuration(entityBlocking.getActiveItemStack()) - entityBlocking.getItemInUseCount()) <= applyTimerBonus(ModConfig.shieldParryTicksThrowable, entityBlocking.getActiveItemStack(), ModConfig.shieldEnchantmentMultiplierThrowable)) {
                    Vec3d playerVec3 = entityBlocking.getLookVec();

                    projectile.shoot(playerVec3.x, playerVec3.y, playerVec3.z, 1.1F, 0.1F);  // reflect faster and more accurately

                    projectile.thrower = entityBlocking;

                    event.setCanceled(true);
                }
            }
        }
    }

    /*@SubscribeEvent // Not feeling bothered writing a forge patch for another event related to an item's enchantability
    public static void enchantShieldLevels(EnchantmentLevelSetEvent event) {
        ItemStack stack = event.getItem();
        Item item = stack.getItem();

        if (item instanceof ItemShield || item.isShield(stack, null))
            event.setLevel(1);
    }*/

    private static int applyTimerBonus(int base, ItemStack stack, float multiplier) {
        LOGGER.info(base + base * getEnchantedLevel(stack) * multiplier);

        return (int) (base + base * getEnchantedLevel(stack) * multiplier);
    }

    private static int getEnchantedLevel(ItemStack stack) {
        int level = 0;

        for (NBTBase nbt : stack.getEnchantmentTagList()) {
            if (nbt instanceof NBTTagCompound) {
                NBTTagCompound compound = (NBTTagCompound) nbt;

                if (Enchantment.getEnchantmentByID(compound.getShort("id")) == enchantment)
                    level += compound.getShort("lvl");
            }
        }

        return level;
    }

    private static class EnchantmentPrecision extends Enchantment {
        EnchantmentPrecision(Rarity rarityIn) {
            super(rarityIn, enchantmentTypeShield, new EntityEquipmentSlot[]{ EntityEquipmentSlot.MAINHAND, EntityEquipmentSlot.OFFHAND});
        }

        @Override
        public boolean canApplyAtEnchantingTable(ItemStack stack) {
            return stack.getItem() instanceof ItemShield || stack.getItem().isShield(stack, null);
        }

        @Override
        public int getMaxLevel() {
            return ModConfig.parryEnchantmentMaxLevel;
        }

        @Override
        public boolean isTreasureEnchantment() {
            return true;
        }
    }

    @Config(modid = ShieldParry.MODID)
    public static class ModConfig {
        @Config.Ignore
        private static final String config = "parry.";

        @Config.LangKey(config + "parry_window_arrow")
        @Config.RangeInt(min = 0)
        public static int shieldParryTicksArrow = 40;
        @Config.LangKey(config + "parry_enchantment_bonus_arrow")
        @Config.RangeDouble(min = 0)
        public static float shieldEnchantmentMultiplierArrow =  0.25f;

        @Config.LangKey(config + "parry_window_fireball")
        @Config.RangeInt(min = 0)
        public static int shieldParryTicksFireball = 40;
        @Config.LangKey(config + "parry_enchantment_bonus_fireball")
        @Config.RangeDouble(min = 0)
        public static float shieldEnchantmentMultiplierFireball =  0.25f;

        @Config.LangKey(config + "parry_window_throwable")
        @Config.RangeInt(min = 0)
        public static int shieldParryTicksThrowable = 40;
        @Config.LangKey(config + "parry_enchantment_bonus_throwable")
        @Config.RangeDouble(min = 0)
        public static float shieldEnchantmentMultiplierThrowable =  0.25f;

        @Config.LangKey(config + "parry_enchantment_max_level")
        @Config.RangeInt(min = 0)
        public static int parryEnchantmentMaxLevel = 5;
    }
}
