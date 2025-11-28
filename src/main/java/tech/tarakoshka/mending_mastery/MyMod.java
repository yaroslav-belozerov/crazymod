package tech.tarakoshka.mending_mastery;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.tarakoshka.mending_mastery.data.MyDataComponents;
import tech.tarakoshka.mending_mastery.items.ModItems;
import tech.tarakoshka.mending_mastery.items.wand.WandShapes;

public class MyMod implements ModInitializer {
	public static final String MOD_ID = "mending-mastery";

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

        ModItems.initialize();
        MySounds.registerSounds();
        MyDataComponents.register();
        WandShapes.registerShapePayload();

		LOGGER.info("Hello Fabric world!");
	}
}