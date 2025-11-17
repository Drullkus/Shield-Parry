package us.drullk.parry.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import us.drullk.parry.ShieldParry;

import java.util.concurrent.CompletableFuture;

class ParryEntityTags extends EntityTypeTagsProvider {

	public ParryEntityTags(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider, GatherDataEvent event) {
		super(packOutput, lookupProvider, ShieldParry.MODID, event.getExistingFileHelper());
	}

	@Override
	protected void addTags(HolderLookup.Provider provider) {
		this.tag(ShieldParry.PROJECTILES_DISABLED_FOR_PARRYING).add(EntityType.FISHING_BOBBER);
	}

}
