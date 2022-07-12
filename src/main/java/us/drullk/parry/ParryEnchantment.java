package us.drullk.parry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ParryEnchantment {
    static Enchantment reboundEnchantment;
    private static final String enchantmentName = "rebound";
    private static EnchantmentCategory enchantmentTypeShield;

    @SubscribeEvent
    public static void registerEnchantment(RegistryEvent.Register<Enchantment> event) {
        enchantmentTypeShield = EnchantmentCategory.create("shield", input -> input instanceof ShieldItem || input != null && input.canPerformAction(new ItemStack(input, 1), ToolActions.SHIELD_BLOCK));

        reboundEnchantment = new EnchantmentRebound(Enchantment.Rarity.COMMON).setRegistryName(new ResourceLocation(ShieldParry.MODID, enchantmentName));
        event.getRegistry().register(reboundEnchantment);
    }

    private static class EnchantmentRebound extends Enchantment {
        EnchantmentRebound(Rarity rarityIn) {
            super(rarityIn, enchantmentTypeShield, new EquipmentSlot[]{EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND});
        }

        @Override
        public boolean canApplyAtEnchantingTable(ItemStack stack) {
            return stack.getItem() instanceof ShieldItem || stack.isShield(null);
        }

        @Override
        public int getMaxLevel() {
            return ParryConfig.INSTANCE.parryEnchantmentMaxLevel.get();
        }

        @Override
        public boolean isTreasureOnly() {
            return true;
        }
    }
}
