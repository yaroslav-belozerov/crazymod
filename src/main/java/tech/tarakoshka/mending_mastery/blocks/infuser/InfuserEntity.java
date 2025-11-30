package tech.tarakoshka.mending_mastery.blocks.infuser;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import tech.tarakoshka.mending_mastery.items.ModItems;

import java.util.Map;

import static tech.tarakoshka.mending_mastery.blocks.infuser.Infuser.INFUSER_ENTITY;

public class InfuserEntity extends BlockEntity implements MenuProvider, Container {
    private final NonNullList<ItemStack> inventory = NonNullList.withSize(3, ItemStack.EMPTY);

    public static final int INPUT_SLOT = 0;
    public static final int FUEL_SLOT = 1;
    public static final int OUTPUT_SLOT = 2;

    private int burnTime = 0;
    private int burnDuration = 0;
    private int cookTime = 0;
    private static final int COOK_TIME_TOTAL = 200;

    public InfuserEntity(BlockPos blockPos, BlockState blockState) {
        super(INFUSER_ENTITY, blockPos, blockState);
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Infuser");
    }

    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
        return new InfuserMenu(syncId, playerInventory, this, this.getContainerData());
    }

    @Override
    public int getContainerSize() {
        return this.inventory.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : this.inventory) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return this.inventory.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(this.inventory, slot, amount);
        if (!result.isEmpty()) {
            this.setChanged();
        }
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(this.inventory, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        ItemStack current = this.inventory.get(slot);
        boolean sameItem = !stack.isEmpty() && ItemStack.isSameItemSameComponents(stack, current);
        this.inventory.set(slot, stack);

        if (stack.getCount() > this.getMaxStackSize()) {
            stack.setCount(this.getMaxStackSize());
        }

        if (slot == INPUT_SLOT && !sameItem) {
            this.cookTime = 0;
            this.setChanged();
        }
    }

    @Override
    public boolean stillValid(Player player) {
        if (this.level.getBlockEntity(this.worldPosition) != this) {
            return false;
        }
        return player.distanceToSqr(
                this.worldPosition.getX() + 0.5,
                this.worldPosition.getY() + 0.5,
                this.worldPosition.getZ() + 0.5
        ) <= 64.0;
    }

    @Override
    public void clearContent() {
        this.inventory.clear();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, InfuserEntity entity) {
        if (level.isClientSide()) return;

        boolean wasLit = entity.isLit();
        boolean changed = false;

        if (entity.isLit()) {
            entity.burnTime--;
        }

        ItemStack input = entity.inventory.get(INPUT_SLOT);
        ItemStack fuel = entity.inventory.get(FUEL_SLOT);
        ItemStack output = entity.inventory.get(OUTPUT_SLOT);

        if (!entity.isLit()) {
            int fuelTime = entity.getFuelTime(fuel);
            if (fuelTime > 0 && entity.canCraft(input, output)) {
                entity.burnTime = fuelTime;
                entity.burnDuration = fuelTime;
                fuel.shrink(1);
                changed = true;
            }
        }

        if (entity.isLit() && entity.canCraft(input, output)) {
            entity.cookTime++;

            if (entity.cookTime >= COOK_TIME_TOTAL) {
                entity.cookTime = 0;
                entity.craftItem(input, output);
                changed = true;
            }
        } else {
            entity.cookTime = 0;
        }

        if (wasLit != entity.isLit()) {
            changed = true;
            level.setBlock(pos, state.setValue(BlockStateProperties.LIT, entity.isLit()), 3);
        }

        if (changed) {
            entity.setChanged();
        }
    }

    private boolean isLit() {
        return burnTime > 0;
    }

    private boolean canCraft(ItemStack input, ItemStack output) {
        if (input.isEmpty()) return false;

        ItemStack result = getRecipeResult(input);
        if (result.isEmpty()) return false;

        if (output.isEmpty()) return true;
        if (!ItemStack.isSameItemSameComponents(output, result)) return false;

        return output.getCount() + result.getCount() <= output.getMaxStackSize();
    }

    private void craftItem(ItemStack input, ItemStack output) {
        ItemStack result = getRecipeResult(input);
        if (result.isEmpty()) return;

        if (output.isEmpty()) {
            inventory.set(OUTPUT_SLOT, result.copy());
        } else {
            output.grow(result.getCount());
        }

        input.shrink(1);
    }

    private ItemStack getRecipeResult(ItemStack input) {
        if (input.is(Items.STICK)) {
            return new ItemStack(ModItems.WAND);
        }
        return ItemStack.EMPTY;
    }

    private int getFuelTime(ItemStack fuel) {
        if (fuel.isEmpty()) return 0;

        if (fuel.is(Items.DIAMOND)) return 1600;

        return 0;
    }

    public ContainerData getContainerData() {
        return new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> InfuserEntity.this.burnTime;
                    case 1 -> InfuserEntity.this.burnDuration;
                    case 2 -> InfuserEntity.this.cookTime;
                    case 3 -> COOK_TIME_TOTAL;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch (index) {
                    case 0 -> InfuserEntity.this.burnTime = value;
                    case 1 -> InfuserEntity.this.burnDuration = value;
                    case 2 -> InfuserEntity.this.cookTime = value;
                }
            }

            @Override
            public int getCount() {
                return 4;
            }
        };
    }

    @Override
    protected void saveAdditional(ValueOutput valueOutput) {
        super.saveAdditional(valueOutput);

        for (int i = 0; i < this.inventory.size(); i++) {
            ItemStack stack = this.inventory.get(i);
            if (!stack.isEmpty()) {
                ValueOutput slotOutput = valueOutput.child("Slot" + i);
                slotOutput.store("item", ItemStack.CODEC, stack);
            }
        }

        valueOutput.putInt("BurnTime", this.burnTime);
        valueOutput.putInt("BurnDuration", this.burnDuration);
        valueOutput.putInt("CookTime", this.cookTime);
    }

    @Override
    protected void loadAdditional(ValueInput valueInput) {
        super.loadAdditional(valueInput);

        this.inventory.clear();
        for (int i = 0; i < this.inventory.size(); i++) {
            ValueInput slotInput = valueInput.childOrEmpty("Slot" + i);
            int finalI = i;
            slotInput.read("item", ItemStack.CODEC).ifPresent(stack -> {
                this.inventory.set(finalI, stack);
            });
        }

        this.burnTime = valueInput.getIntOr("BurnTime", 0);
        this.burnDuration = valueInput.getIntOr("BurnDuration", 0);
        this.cookTime = valueInput.getIntOr("CookTime", 0);
    }
}