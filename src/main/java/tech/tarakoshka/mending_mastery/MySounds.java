package tech.tarakoshka.mending_mastery;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public class MySounds {
    public static final SoundEvent SHOOT = registerSound("shoot");
    public static final SoundEvent DEFLATE = registerSound("deflate");

    private static SoundEvent registerSound(String sound) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(MyMod.MOD_ID, sound);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
    }

    public static void registerSounds() {
        // Called in main initializer
    }
}