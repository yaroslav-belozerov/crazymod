package tech.tarakoshka.mending_mastery;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.resources.ResourceLocation;
import tech.tarakoshka.mending_mastery.particles.MilkParticle;

public class MyModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
        ParticleFactoryRegistry.getInstance().register(MyMod.MILK_PARTICLE, MilkParticle.Factory::new);
        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, ResourceLocation.fromNamespaceAndPath(MyMod.MOD_ID, "before_chat"), new ModHud());
	}
}