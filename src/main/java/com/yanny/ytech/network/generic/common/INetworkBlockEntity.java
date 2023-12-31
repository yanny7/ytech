package com.yanny.ytech.network.generic.common;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface INetworkBlockEntity {
    @Nullable Level getLevel();
    int getNetworkId();
    @NotNull BlockPos getBlockPos();
    void setNetworkId(int networkId);
    @NotNull List<BlockPos> getValidNeighbors();
}
