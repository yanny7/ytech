package com.yanny.ytech.generation;

import com.yanny.ytech.GeneralUtils;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.EntityLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;

import static com.yanny.ytech.registration.Registration.HOLDER;

class YTechLootTables extends LootTableProvider {
    public YTechLootTables(PackOutput packOutput) {
        super(packOutput, Collections.emptySet(), getSubProviders());
    }

    private static List<SubProviderEntry> getSubProviders() {
        return List.of(
                new LootTableProvider.SubProviderEntry(YTechBlockLootSub::new, LootContextParamSets.BLOCK),
                new LootTableProvider.SubProviderEntry(YTechEntityLootSub::new, LootContextParamSets.ENTITY)
        );
    }

    private static class YTechBlockLootSub extends BlockLootSubProvider {
        protected YTechBlockLootSub() {
            super(new HashSet<>(), FeatureFlags.REGISTRY.allFlags());
        }

        @Override
        protected void generate() {
            GeneralUtils.mapToStream(HOLDER.blocks()).forEach(h -> h.object.registerLoot(h, this));
            HOLDER.simpleBlocks().values().forEach(h -> h.object.registerLoot(h, this));
        }

        @NotNull
        @Override
        protected Iterable<Block> getKnownBlocks() {
            return Stream.of(
                    GeneralUtils.mapToStream(HOLDER.blocks()).map(e -> e.block.get()),
                    HOLDER.simpleBlocks().values().stream().map(e -> e.block.get())
            ).flatMap(i -> i).toList();
        }
    }

    private static class YTechEntityLootSub extends EntityLootSubProvider {

        protected YTechEntityLootSub() {
            super(FeatureFlagSet.of(FeatureFlags.VANILLA), FeatureFlagSet.of());
        }

        @Override
        public void generate() {
            HOLDER.entities().values().forEach(h -> h.object.registerLoot(h, this));
        }
    }
}
