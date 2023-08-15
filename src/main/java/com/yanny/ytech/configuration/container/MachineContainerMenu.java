package com.yanny.ytech.configuration.container;

import com.yanny.ytech.configuration.MachineItemStackHandler;
import com.yanny.ytech.configuration.block_entity.MachineBlockEntity;
import com.yanny.ytech.registration.Holder;
import com.yanny.ytech.registration.IBlockHolder;
import com.yanny.ytech.registration.IMenuEntityBlockHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class MachineContainerMenu extends AbstractContainerMenu {
    protected final Block block;
    protected final BlockPos pos;
    @NotNull protected final ItemStackHandler itemStackHandler;
    @NotNull protected final MachineBlockEntity blockEntity;
    private final int inputSlots;

    public MachineContainerMenu(Holder holder, int windowId, Player player, BlockPos pos) {
        super(getMenuEntityBlockHolder(holder).getMenuRegistry().get(), windowId);
        block = getBlockHolder(holder).getBlockRegistry().get();
        this.pos = pos;

        LevelAccessor level = player.level();

        if (level.getBlockEntity(pos) instanceof MachineBlockEntity entity) {
            MachineItemStackHandler items = entity.getItems();

            // Client side MENU should have dummy ItemStackHandler - it's content is override by netcode
            if (level.isClientSide()) {
                itemStackHandler = new ItemStackHandler(items.getSlots());
            } else {
                itemStackHandler = items;
            }

            blockEntity = entity;
            inputSlots = items.getInputSlots();

            for (int index = 0; index < itemStackHandler.getSlots(); index++) {
                addSlot(new SlotItemHandler(itemStackHandler, index, items.getX(index), items.getY(index)));
            }
        } else {
            throw new IllegalArgumentException("BlockEntity is not instanceof MachineBlockEntity");
        }

        Utils.layoutPlayerInventorySlots(this::addSlot, player.getInventory(), 8, 84);
    }

    @NotNull
    @Override
    public ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        int slotCount = itemStackHandler.getSlots();

        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            itemstack = stack.copy();

            if (index < slotCount) {
                if (!moveItemStackTo(stack, slotCount, Inventory.INVENTORY_SIZE + slotCount, true)) {
                    return ItemStack.EMPTY;
                }
            }
            if (!moveItemStackTo(stack, 0, inputSlots, false)) {
                if (index < Inventory.INVENTORY_SIZE - 9 + slotCount) {
                    if (!moveItemStackTo(stack, Inventory.INVENTORY_SIZE - 9 + slotCount, Inventory.INVENTORY_SIZE + slotCount, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index < Inventory.INVENTORY_SIZE + slotCount && !moveItemStackTo(stack, slotCount, Inventory.INVENTORY_SIZE - 9 + slotCount, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (stack.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, stack);
        }

        return itemstack;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return stillValid(ContainerLevelAccess.create(player.level(), pos), player, block);
    }

    @NotNull
    public MachineBlockEntity getBlockEntity() {
        return blockEntity;
    }

    static IMenuEntityBlockHolder getMenuEntityBlockHolder(Holder holder) {
        if (holder instanceof IMenuEntityBlockHolder entityHolder) {
            return entityHolder;
        } else {
            throw new IllegalStateException("Invalid holder type");
        }
    }

    static IBlockHolder getBlockHolder(Holder holder) {
        if (holder instanceof IBlockHolder entityHolder) {
            return entityHolder;
        } else {
            throw new IllegalStateException("Invalid holder type");
        }
    }
}