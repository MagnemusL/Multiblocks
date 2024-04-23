package net.lindholm.multiblocks.block.entity;

import net.lindholm.multiblocks.screen.MasterMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class MasterBlockEntity extends BlockEntity {
    private final ItemStackHandler itemHandler = new ItemStackHandler(5) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return switch (slot) {
                case 0, 1 -> true;
                case 2, 3, 4 -> false;
                default -> super.isItemValid(slot, stack);
            };
        }
    };

    private static final int ACID_SLOT = 0;
    private static final int FOSSIL_SLOT = 1;
    private static final int OUTPUT_SLOT_1 = 2;
    private static final int OUTPUT_SLOT_2 = 3;
    private static final int OUTPUT_SLOT_3 = 4;

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    protected final ContainerData data;
    private int progress = 0;
    private int maxProgress = 20;

    public MasterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MASTER_BE.get(), pos, state);
        this.data = new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> MasterBlockEntity.this.progress;
                    case 1 -> MasterBlockEntity.this.maxProgress;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch (index) {
                    case 0 -> MasterBlockEntity.this.progress = value;
                    case 1 -> MasterBlockEntity.this.maxProgress = value;
                }
            }

            @Override
            public int getCount() {
                return 2;
            }
        };
    }

    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }

        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHandler.cast();
        }

        return super.getCapability(cap, side);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        tag.put("inventory", itemHandler.serializeNBT());
        super.saveAdditional(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        itemHandler.deserializeNBT(tag.getCompound("inventory"));
    }

    /* CRAFTING */

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (isOutputSlotEmpty()) {
            increaseCraftingProgress();
            setChanged(level, pos, state);

            if (hasProgressFinished()) {
                craftItem();
                resetProgress();
            }
        } else {
            resetProgress();
        }
    }


    private void resetProgress() {
        this.progress = 0;
    }

    private void craftItem() {
        if(this.itemHandler.getStackInSlot(OUTPUT_SLOT_1).getCount() >= this.itemHandler.getStackInSlot(OUTPUT_SLOT_1).getMaxStackSize()) {
            this.itemHandler.setStackInSlot(OUTPUT_SLOT_2, new ItemStack(Items.EMERALD, this.itemHandler.getStackInSlot(OUTPUT_SLOT_2).getCount() + 1));
        }
    }

    private boolean hasProgressFinished() {
        return this.progress >= this.maxProgress;
    }

    private void increaseCraftingProgress() {
        this.progress++;
    }

    private boolean canInsertItem(Item item) {

        if (this.itemHandler.getStackInSlot(OUTPUT_SLOT_1).is(item) || this.itemHandler.getStackInSlot(OUTPUT_SLOT_1).isEmpty()) {
            return true;
        }
        if (this.itemHandler.getStackInSlot(OUTPUT_SLOT_2).is(item) || this.itemHandler.getStackInSlot(OUTPUT_SLOT_2).isEmpty()) {
            return true;
        }
        if (this.itemHandler.getStackInSlot(OUTPUT_SLOT_3).is(item) || this.itemHandler.getStackInSlot(OUTPUT_SLOT_3).isEmpty()) {
            return true;
        }

        return false;
    }

    private boolean canInsertAmount(int count) {
        return this.itemHandler.getStackInSlot(OUTPUT_SLOT_1).getMaxStackSize() > this.itemHandler.getStackInSlot(OUTPUT_SLOT_1).getCount() + count ||
                this.itemHandler.getStackInSlot(OUTPUT_SLOT_2).getMaxStackSize() > this.itemHandler.getStackInSlot(OUTPUT_SLOT_2).getCount() + count ||
                this.itemHandler.getStackInSlot(OUTPUT_SLOT_3).getMaxStackSize() > this.itemHandler.getStackInSlot(OUTPUT_SLOT_3).getCount() + count;
    }

    private boolean isOutputSlotEmpty() {
        return this.itemHandler.getStackInSlot(OUTPUT_SLOT_1).isEmpty() ||
                this.itemHandler.getStackInSlot(OUTPUT_SLOT_1).getCount() < this.itemHandler.getStackInSlot(OUTPUT_SLOT_1).getMaxStackSize() ||
                this.itemHandler.getStackInSlot(OUTPUT_SLOT_2).isEmpty() ||
                this.itemHandler.getStackInSlot(OUTPUT_SLOT_2).getCount() < this.itemHandler.getStackInSlot(OUTPUT_SLOT_2).getMaxStackSize() ||
                this.itemHandler.getStackInSlot(OUTPUT_SLOT_3).isEmpty() ||
                this.itemHandler.getStackInSlot(OUTPUT_SLOT_3).getCount() < this.itemHandler.getStackInSlot(OUTPUT_SLOT_3).getMaxStackSize();
    }
}
