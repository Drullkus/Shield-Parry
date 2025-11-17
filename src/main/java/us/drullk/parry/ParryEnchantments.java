package us.drullk.parry;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.registries.holdersets.AndHolderSet;
import net.neoforged.neoforge.registries.holdersets.NotHolderSet;

import java.util.List;
import java.util.Optional;

public class ParryEnchantments {
    public static final ResourceKey<Enchantment> REBOUND = ResourceKey.create(Registries.ENCHANTMENT, ShieldParry.modId("rebound"));


    public static void bootstrap(BootstrapContext<Enchantment> context) {
		HolderGetter<Item> items = context.lookup(Registries.ITEM);

		context.register(REBOUND, Enchantment.enchantment(
				new Enchantment.EnchantmentDefinition(
						new AndHolderSet<>(items.getOrThrow(Tags.Items.TOOLS_SHIELD), new NotHolderSet<>(context.registryLookup(Registries.ITEM).orElseThrow(), items.getOrThrow(ShieldParry.EXCLUDED_SHIELDS))),
                        Optional.empty(),
						10,
						5,
						Enchantment.dynamicCost(1, 10),
						Enchantment.dynamicCost(6, 10),
						1,
						List.of(EquipmentSlotGroup.HAND)
				)
		)
				.build(REBOUND.location()));
    }
}
