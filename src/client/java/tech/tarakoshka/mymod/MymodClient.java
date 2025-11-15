package tech.tarakoshka.mymod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import tech.tarakoshka.mymod.particles.MilkParticle;

public class MymodClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
        ParticleFactoryRegistry.getInstance().register(Mymod.MILK_PARTICLE, MilkParticle.Factory::new);
	}
}