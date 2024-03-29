package com.yanny.ytech.configuration;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class YTechConfigSpec {
    @NotNull private final ModConfigSpec.ConfigValue<Boolean> makeBlocksRequireValidTool;
    @NotNull private final ModConfigSpec.ConfigValue<List<? extends String>> makeBlocksRequireValidToolList;
    @NotNull private final ModConfigSpec.ConfigValue<Boolean> craftSharpFlintByRightClickingOnStone;

    @NotNull private final ModConfigSpec.ConfigValue<Boolean> noDryingDuringRain;
    @NotNull private final ModConfigSpec.ConfigValue<List<? extends String>> slowDryingBiomeTags;
    @NotNull private final ModConfigSpec.ConfigValue<List<? extends String>> fastDryingBiomeTags;

    @NotNull private final ModConfigSpec.ConfigValue<Integer> baseFluidStoragePerBlock;
    @NotNull private final ModConfigSpec.ConfigValue<Boolean> rainingFillAqueduct;
    @NotNull private final ModConfigSpec.ConfigValue<Integer> rainingFillAmount;
    @NotNull private final ModConfigSpec.ConfigValue<Integer> rainingFillPerNthTick;
    @NotNull private final ModConfigSpec.ConfigValue<Boolean> validBlockForRaining;
    @NotNull private final ModConfigSpec.ConfigValue<Integer> valveFillAmount;
    @NotNull private final ModConfigSpec.ConfigValue<Integer> valveFillPerNthTick;
    @NotNull private final ModConfigSpec.ConfigValue<Integer> hydratorDrainAmount;
    @NotNull private final ModConfigSpec.ConfigValue<Integer> hydratorDrainPerNthTick;
    @NotNull private final ModConfigSpec.ConfigValue<Integer> fertilizerDuration;
    @NotNull private final ModConfigSpec.ConfigValue<Integer> applyFertilizerChance;

    public YTechConfigSpec(@NotNull ModConfigSpec.Builder builder) {
        builder.push("general");
            makeBlocksRequireValidTool = builder.comment("If mod can change behaviour of specified blocks to require valid tool for harvesting")
                    .worldRestart().define("makeBlocksRequireValidTool", true);
            makeBlocksRequireValidToolList = builder.comment("List of blocks that will require valid tool for harvesting")
                    .worldRestart().defineListAllowEmpty("makeBlocksRequireValidToolList", YTechConfigSpec::getMinecraftBlocksRequiringValidTool, YTechConfigSpec::validateResourceLocation);
            craftSharpFlintByRightClickingOnStone = builder.comment("Enables crafting Sharp Flint by right-clicking on stone")
                    .worldRestart().define("craftSharpFlintByRightClickingOnStone", true);
        builder.pop();
        builder.push("dryingRack");
            noDryingDuringRain = builder.comment("If Drying Rack should stop working during rain")
                    .worldRestart().define("noDryingDuringRain", true);
            slowDryingBiomeTags = builder.comment("List of biome tags, where will be drying 2x slower")
                    .worldRestart().defineListAllowEmpty("slowDryingBiomeTags", YTechConfigSpec::getSlowDryingBiomeTags, YTechConfigSpec::validateResourceLocation);
            fastDryingBiomeTags = builder.comment("List of biome tags, where will be drying 2x faster")
                    .worldRestart().defineListAllowEmpty("fastDryingBiomeTags", YTechConfigSpec::getFastDryingBiomeTags, YTechConfigSpec::validateResourceLocation);
        builder.pop();
        builder.push("irrigation");
            builder.push("aqueduct");
                baseFluidStoragePerBlock = builder.comment("Base amount of fluid stored per block")
                        .worldRestart().defineInRange("baseFluidStoragePerBlock", 500, 1, Integer.MAX_VALUE);
                rainingFillAqueduct = builder.comment("If raining should fill aqueduct")
                        .worldRestart().define("rainingFillAqueduct", true);
                rainingFillAmount = builder.comment("Amount of which will be aqueduct filled per nth tick when raining")
                        .worldRestart().defineInRange("rainingFillAmount", 1, 1, Integer.MAX_VALUE);
                rainingFillPerNthTick = builder.comment("How often should be filled aqueduct when raining (1 - every tick, 20 - every second)")
                        .worldRestart().defineInRange("rainingFillPerNthTick", 10, 1, Integer.MAX_VALUE);
                validBlockForRaining = builder.comment("If aqueduct must see sky and must be in valid biome where is raining when raining for filling")
                        .worldRestart().define("validBlockForRaining", true);
            builder.pop();
            builder.push("valve");
                valveFillAmount = builder.comment("Amount of which will be aqueduct filled every nth tick thru valve")
                        .worldRestart().defineInRange("valveFillAmount", 1, 1, Integer.MAX_VALUE);
                valveFillPerNthTick = builder.comment("How often should be filled aqueduct thru valve (1 - every tick, 20 - every second)")
                        .worldRestart().defineInRange("valveFillPerNthTick", 10, 1, Integer.MAX_VALUE);
            builder.pop();
            builder.push("hydrator");
                hydratorDrainAmount = builder.comment("Amount of which will be aqueduct drained every nth tick thru hydrator")
                        .worldRestart().defineInRange("hydratorDrainAmount", 100, 1, Integer.MAX_VALUE);
                hydratorDrainPerNthTick = builder.comment("How often should be drained aqueduct thru hydrator (1 - every tick, 20 - every second)")
                        .worldRestart().defineInRange("hydratorDrainPerNthTick", 200, 1, Integer.MAX_VALUE);
            builder.pop();
            builder.push("fertilizer");
                fertilizerDuration = builder.comment("How long last single piece of fertilizer")
                        .worldRestart().defineInRange("fertilizerDuration", 600, 1, Integer.MAX_VALUE);
                applyFertilizerChance = builder.comment("How often should be applied bone meal effect (1 / n chance per tick)")
                        .worldRestart().defineInRange("applyFertilizerChance", 60, 1, Integer.MAX_VALUE);
            builder.pop();
        builder.pop();
    }

    public boolean shouldRequireValidTool() {
        return makeBlocksRequireValidTool.get();
    }

    @NotNull
    public Set<ResourceLocation> getBlocksRequiringValidTool() {
        return makeBlocksRequireValidToolList.get().stream().map(ResourceLocation::new).collect(Collectors.toSet());
    }

    public boolean enableCraftingSharpFlint() {
        return craftSharpFlintByRightClickingOnStone.get();
    }

    public boolean noDryingDuringRain() {
        return noDryingDuringRain.get();
    }

    public Set<TagKey<Biome>> getSlowDryingBiomes() {
        return slowDryingBiomeTags.get().stream().map(ResourceLocation::new).map((v) -> TagKey.create(Registries.BIOME, v)).collect(Collectors.toSet());
    }

    public Set<TagKey<Biome>> getFastDryingBiomes() {
        return fastDryingBiomeTags.get().stream().map(ResourceLocation::new).map((v) -> TagKey.create(Registries.BIOME, v)).collect(Collectors.toSet());
    }

    public int getBaseFluidStoragePerBlock() {
        return baseFluidStoragePerBlock.get();
    }

    public boolean shouldRainingFillAqueduct() {
        return rainingFillAqueduct.get();
    }

    public int getRainingFillAmount() {
        return rainingFillAmount.get();
    }

    public int getValveFillAmount() {
        return valveFillAmount.get();
    }

    public int getValveFillPerNthTick() {
        return valveFillPerNthTick.get();
    }

    public int getHydratorDrainAmount() {
        return hydratorDrainAmount.get();
    }

    public int getHydratorDrainPerNthTick() {
        return hydratorDrainPerNthTick.get();
    }

    public int getRainingFillPerNthTick() {
        return rainingFillPerNthTick.get();
    }

    public boolean isValidBlockForRaining() {
        return validBlockForRaining.get();
    }

    public int getFertilizerDuration() {
        return fertilizerDuration.get();
    }

    public int getApplyFertilizerChance() {
        return applyFertilizerChance.get();
    }

    @NotNull
    private static List<String> getMinecraftBlocksRequiringValidTool() {
        return Stream.of(
                // Logs & woods
                Blocks.ACACIA_LOG,
                Blocks.ACACIA_WOOD,
                Blocks.BIRCH_LOG,
                Blocks.BIRCH_WOOD,
                Blocks.CHERRY_LOG,
                Blocks.CHERRY_WOOD,
                Blocks.JUNGLE_LOG,
                Blocks.JUNGLE_WOOD,
                Blocks.OAK_LOG,
                Blocks.OAK_WOOD,
                Blocks.DARK_OAK_LOG,
                Blocks.DARK_OAK_WOOD,
                Blocks.MANGROVE_LOG,
                Blocks.MANGROVE_WOOD,
                Blocks.SPRUCE_LOG,
                Blocks.SPRUCE_WOOD,
                // stripped logs & woods
                Blocks.STRIPPED_ACACIA_LOG,
                Blocks.STRIPPED_ACACIA_WOOD,
                Blocks.STRIPPED_BIRCH_LOG,
                Blocks.STRIPPED_BIRCH_WOOD,
                Blocks.STRIPPED_CHERRY_LOG,
                Blocks.STRIPPED_CHERRY_WOOD,
                Blocks.STRIPPED_JUNGLE_LOG,
                Blocks.STRIPPED_JUNGLE_WOOD,
                Blocks.STRIPPED_OAK_LOG,
                Blocks.STRIPPED_OAK_WOOD,
                Blocks.STRIPPED_DARK_OAK_LOG,
                Blocks.STRIPPED_DARK_OAK_WOOD,
                Blocks.STRIPPED_MANGROVE_LOG,
                Blocks.STRIPPED_MANGROVE_WOOD,
                Blocks.STRIPPED_SPRUCE_LOG,
                Blocks.STRIPPED_SPRUCE_WOOD,
                // dirt variants
                Blocks.DIRT,
                Blocks.DIRT_PATH,
                Blocks.GRASS_BLOCK,
                Blocks.FARMLAND,
                Blocks.PODZOL,
                Blocks.MYCELIUM,
                Blocks.COARSE_DIRT,
                Blocks.ROOTED_DIRT
        ).map(value -> Utils.loc(value).toString()).collect(Collectors.toList());
    }

    @NotNull
    private static List<String> getFastDryingBiomeTags() {
        return Stream.of(
                Tags.Biomes.IS_DRY
        ).map(value -> Objects.requireNonNull(value.location()).toString()).collect(Collectors.toList());
    }

    @NotNull
    private static List<String> getSlowDryingBiomeTags() {
        return Stream.of(
                Tags.Biomes.IS_WET
        ).map(value -> Objects.requireNonNull(value.location()).toString()).collect(Collectors.toList());
    }

    private static boolean validateResourceLocation(@NotNull Object recipe) {
        return recipe instanceof String resourceLocation && ResourceLocation.isValidResourceLocation(resourceLocation);
    }
}
