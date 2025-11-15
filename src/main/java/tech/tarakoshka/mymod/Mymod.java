package tech.tarakoshka.mymod;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.tarakoshka.mymod.items.MyItems;

public class Mymod implements ModInitializer {
	public static final String MOD_ID = "my-mod";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final SimpleParticleType MILK_PARTICLE = Registry.register(
            BuiltInRegistries.PARTICLE_TYPE,
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "milk"),
            FabricParticleTypes.simple()
    );

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

        MyItems.initialize();
        MySounds.registerSounds();

		LOGGER.info("Hello Fabric world!");
	}
}