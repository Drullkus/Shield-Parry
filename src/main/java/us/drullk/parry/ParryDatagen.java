package us.drullk.parry;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

public class ParryDatagen {
	public static void generateData(GatherDataEvent event) {
		PackOutput packOutput = event.getGenerator().getPackOutput();
		CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
		event.addProvider(new DatapackBuiltinEntriesProvider(packOutput, lookupProvider, new RegistrySetBuilder().add(Registries.ENCHANTMENT, ParryEnchantments::bootstrap), Collections.singleton(ShieldParry.MODID)));

		event.addProvider(new EntityTypeTagsProvider(packOutput, lookupProvider, ShieldParry.MODID, event.getExistingFileHelper()) {
			@Override
			protected void addTags(HolderLookup.Provider provider) {
				this.tag(ShieldParry.PROJECTILES_DISABLED_FOR_PARRYING).add(EntityType.FISHING_BOBBER);
			}
		});
	}
}
