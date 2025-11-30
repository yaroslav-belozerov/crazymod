package tech.tarakoshka.mending_mastery.blocks.infuser;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;

import static tech.tarakoshka.mending_mastery.blocks.infuser.Infuser.INFUSER_MENU;

public class InfuserMenu extends AbstractContainerMenu {
    private final Container container;
    private final ContainerData data;

    public InfuserMenu(int syncId, Inventory playerInventory) {
        this(syncId, playerInventory, new SimpleContainer(3), new SimpleContainerData(4));
    }

    public InfuserMenu(int syncId, Inventory playerInventory, Container container, ContainerData data) {
        super(INFUSER_MENU, syncId);
        this.container = container;
        this.data = data;

        checkContainerSize(container, 3);
        checkContainerDataCount(data, 4);

        container.startOpen(playerInventory.player);

        this.addSlot(new Slot(container, 0, 56, 17));
        this.addSlot(new Slot(container, 1, 56, 53));
        this.addSlot(new Slot(container, 2, 116, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        this.addStandardInventorySlots(playerInventory, 8, 84);
        this.addDataSlots(data);
    }

    public int getBurnProgress() {
        int burnTime = this.data.get(0);
        int burnDuration = this.data.get(1);
        return burnDuration != 0 ? burnTime * 13 / burnDuration : 0;
    }

    public int getCookProgress() {
        int cookTime = this.data.get(2);
        int cookTimeTotal = this.data.get(3);
        return cookTimeTotal != 0 ? cookTime * 24 / cookTimeTotal : 0;
    }

    public boolean isLit() {
        return this.data.get(0) > 0;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemStack = slotStack.copy();

            if (index == 2) {
                if (!this.moveItemStackTo(slotStack, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= 3) {
                if (!this.moveItemStackTo(slotStack, 0, 2, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!this.moveItemStackTo(slotStack, 3, 39, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (slotStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.container.stopOpen(player);
    }
}