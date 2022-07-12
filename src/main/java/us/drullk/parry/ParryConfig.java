package us.drullk.parry;

import net.minecraftforge.common.ForgeConfigSpec;

public class ParryConfig {
    public static ParryConfig INSTANCE;

    public final ForgeConfigSpec.IntValue shieldParryTicks;
    public final ForgeConfigSpec.DoubleValue shieldEnchantmentMultiplier;
    public final ForgeConfigSpec.IntValue parryEnchantmentMaxLevel;
    public final ForgeConfigSpec.BooleanValue treasureEnchantment;

    public ParryConfig(ForgeConfigSpec.Builder builder) {
        builder.push("shield_parry_config");
        this.shieldParryTicks = builder.comment("Measured in Minecraft Ticks. 20 ticks equals 1 second.").defineInRange("parry_interval_ticks", 40, 10, 200);
        this.shieldEnchantmentMultiplier = builder.comment("Multiplier bonus for Rebound Enchantment, to extend the parry timing").defineInRange("enchantment_parry_multiplier", 0.25f, 0d, 4d);
        this.parryEnchantmentMaxLevel = builder.comment("Maximum enchantment level for Rebound Enchantment.").defineInRange("max_enchant_level", 5, 1, 10);
        this.treasureEnchantment = builder.comment("Makes Rebound enchantment treasure-only, and prevents it from being enchanted onto books in the Enchanting Table.").define("rebound_enchantment_treasure", false);
        builder.pop();
    }
}
