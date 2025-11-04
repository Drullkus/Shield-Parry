package us.drullk.parry;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ParryConfig {
    public final ModConfigSpec.IntValue shieldParryTicks;
    public final ModConfigSpec.DoubleValue shieldEnchantmentMultiplier;

    public ParryConfig(ModConfigSpec.Builder builder) {
        builder.push("shield_parry_config");
        this.shieldParryTicks = builder.comment("Measured in Minecraft Ticks. 20 ticks equals 1 second.").defineInRange("parry_interval_ticks", 40, 10, 200);
        this.shieldEnchantmentMultiplier = builder.comment("Multiplier bonus for Rebound Enchantment, to extend the parry timing").defineInRange("enchantment_parry_multiplier", 0.25f, 0d, 4d);
        builder.pop();
    }
}
