package us.drullk.parry.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EnchantmentTagsProvider;
import net.minecraft.tags.EnchantmentTags;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import us.drullk.parry.ParryEnchantments;
import us.drullk.parry.ShieldParry;

import java.util.concurrent.CompletableFuture;

class ParryEnchantmentTags extends EnchantmentTagsProvider {

	public ParryEnchantmentTags(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider, GatherDataEvent event) {
		super(packOutput, lookupProvider, ShieldParry.MODID, event.getExistingFileHelper());
	}

	@Override
	protected void addTags(HolderLookup.Provider provider) {
		this.tag(EnchantmentTags.TREASURE).add(ParryEnchantments.REBOUND);
	}

}
