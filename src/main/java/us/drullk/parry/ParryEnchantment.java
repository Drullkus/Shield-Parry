package us.drullk.parry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegisterEvent;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ParryEnchantment {
    static Enchantment reboundEnchantment;
    private static EnchantmentCategory enchantmentTypeShield;

    @SubscribeEvent
    public static void registerEnchantment(RegisterEvent event) {
        enchantmentTypeShield = EnchantmentCategory.create("shield", input -> input instanceof ShieldItem || input != null && input.canPerformAction(new ItemStack(input, 1), ToolActions.SHIELD_BLOCK));

        reboundEnchantment = new EnchantmentRebound(Enchantment.Rarity.COMMON);
        event.register(Registries.ENCHANTMENT, ShieldParry.modId("rebound"), () -> reboundEnchantment);
    }

    private static class EnchantmentRebound extends Enchantment {
        EnchantmentRebound(Rarity rarityIn) {
            super(rarityIn, enchantmentTypeShield, new EquipmentSlot[]{EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND});
        }

        @Override
        public boolean canApplyAtEnchantingTable(ItemStack stack) {
            return (stack.getItem() instanceof ShieldItem || stack.canPerformAction(ToolActions.SHIELD_BLOCK)) && !stack.is(ShieldParry.EXCLUDED_SHIELDS);
        }

        @Override
        public int getMaxLevel() {
            return ParryConfig.INSTANCE.parryEnchantmentMaxLevel.get();
        }

        @Override
        public boolean isTreasureOnly() {
            return ParryConfig.INSTANCE.treasureEnchantment.get();
        }
    }
}
