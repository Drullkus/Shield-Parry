package us.drullk.parry;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentType;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShieldItem;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ParryEnchantment {
    static Enchantment reboundEnchantment;
    private static final String enchantmentName = "rebound";
    private static EnchantmentType enchantmentTypeShield;

    @SubscribeEvent
    public static void registerEnchantment(RegistryEvent.Register<Enchantment> event) {
        enchantmentTypeShield = EnchantmentType.create("shield", input -> input instanceof ShieldItem || input != null && input.isShield(new ItemStack(input, 1), null));

        reboundEnchantment = new EnchantmentRebound(Enchantment.Rarity.COMMON).setRegistryName(new ResourceLocation(ShieldParry.MODID, enchantmentName));
        event.getRegistry().register(reboundEnchantment);
    }

    private static class EnchantmentRebound extends Enchantment {
        EnchantmentRebound(Rarity rarityIn) {
            super(rarityIn, enchantmentTypeShield, new EquipmentSlotType[]{EquipmentSlotType.MAINHAND, EquipmentSlotType.OFFHAND});
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
