package mod.pilot.jar_of_chaos.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

public class StarParticle extends TextureSheetParticle {
    protected StarParticle(ClientLevel level, double pX, double pY, double pZ, SpriteSet sprites, double pXSpeed, double pYSpeed, double pZSpeed) {
        super(level, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
        this.gravity = 0f;
        this.hasPhysics = false;
        this.friction = -0.1F;
        this.quadSize *= 2F;
        this.lifetime = 120;
        this.sprite = sprites;
        this.setSpriteFromAge(sprites);

        this.rCol = 1f;
        this.gCol = 1f;
        this.bCol = 1f;
    }
    private final SpriteSet sprite;

    @Override
    public void tick() {
        super.tick();
        setSpriteFromAge(sprite);
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet spriteSet) {
            this.sprites = spriteSet;
        }

        public Particle createParticle(@NotNull SimpleParticleType particleType, @NotNull ClientLevel level,
                                       double x, double y, double z,
                                       double dx, double dy, double dz) {
            return new StarParticle(level, x, y, z, this.sprites, dx, dy + 0.1, dz);
        }
    }
}
