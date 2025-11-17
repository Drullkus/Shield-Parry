package us.drullk.parry.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import us.drullk.parry.ParryEnchantments;
import us.drullk.parry.ShieldParry;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

public class ParryDatagen {
	public static void generateData(GatherDataEvent event) {
		PackOutput packOutput = event.getGenerator().getPackOutput();
		CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

		DatapackBuiltinEntriesProvider datapackProvider = new DatapackBuiltinEntriesProvider(packOutput, lookupProvider, new RegistrySetBuilder().add(Registries.ENCHANTMENT, ParryEnchantments::bootstrap), Collections.singleton(ShieldParry.MODID));
		event.addProvider(datapackProvider);

		event.addProvider(new ParryEntityTags(packOutput, datapackProvider.getRegistryProvider(), event));
		event.addProvider(new ParryEnchantmentTags(packOutput, datapackProvider.getRegistryProvider(), event));
	}
}
