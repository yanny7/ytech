package com.yanny.ytech.configuration.container;

import com.yanny.ytech.registration.Holder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;

public class CrusherContainerMenu extends MachineContainerMenu {
    public CrusherContainerMenu(Holder holder, int windowId, Player player, BlockPos pos) {
        super(holder, windowId, player, pos);
    }
}