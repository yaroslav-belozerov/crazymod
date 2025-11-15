package tech.tarakoshka.mymod.particles;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.particle.v1.FabricSpriteProvider;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public class MilkParticle extends SingleQuadParticle {

    protected MilkParticle(ClientLevel world, double x, double y, double z, double xvel, double yvel, double zvel,
                           TextureAtlasSprite sprite) {
        super(world, x, y, z, sprite);

        this.friction = 0.96F;
        this.gravity = 0.0F;
        this.quadSize = 0.3F;
        this.lifetime = 10;

        this.rCol = 1.0F;
        this.gCol = 1.0F;
        this.bCol = 1.0F;
        this.alpha = 1.0F;

        this.xd = xvel;
        this.yd = yvel;
        this.zd = zvel;
    }

    @Override
    public void tick() {
        super.tick();
        // Fade out over time
        this.alpha = 1.0F - ((float)this.age / (float)this.lifetime);
    }

    @Override
    protected @NotNull Layer getLayer() {
        return Layer.OPAQUE;
    }

    @Environment(EnvType.CLIENT)
    public record Factory(FabricSpriteProvider spriteProvider) implements ParticleProvider<SimpleParticleType> {
        @Override
        public @NotNull Particle createParticle(SimpleParticleType particleOptions,
                                                ClientLevel clientLevel,
                                                double d, double e, double f,
                                                double g, double h, double i,
                                                RandomSource randomSource) {
            return new MilkParticle(clientLevel, d, e, f, g, h, i, spriteProvider.getSprites().getFirst());
        }
    }
}