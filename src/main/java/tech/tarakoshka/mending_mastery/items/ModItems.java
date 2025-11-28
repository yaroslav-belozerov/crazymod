package tech.tarakoshka.mending_mastery.items;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import tech.tarakoshka.mending_mastery.MyMod;
import tech.tarakoshka.mending_mastery.items.wand.MagicWand;

import java.util.function.Function;

public final class ModItems {
    private ModItems() {}

    public static final Item WAND = register("magic-wand", MagicWand::new);

    public static Item register(String path, Function<Item.Properties, Item> factory) {
        final ResourceKey<Item> registryKey = ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MyMod.MOD_ID, path));
        return Items.registerItem(registryKey, factory);
    }

    public static void initialize() {
        // Get the event for modifying entries in the ingredients group.
    // And register an event handler that adds our suspicious item to the ingredients group.
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.COMBAT)
            .register((itemGroup) -> itemGroup.prepend(ModItems.WAND));
    }
}