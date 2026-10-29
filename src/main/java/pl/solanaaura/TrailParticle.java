package pl.solanaaura;

import net.minecraft.client.particle.BillboardParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.util.math.random.Random;

/**
 * Jeden "punkt" smugi. Gęsto ułożone punkty dają ciągłą, zanikającą linię za graczem.
 */
public class TrailParticle extends BillboardParticle {

    // ---- USTAWIENIA ----
    public static final int     LIFETIME = 30;     // ile ticków żyje smuga (20 ticków = 1 s)
    public static final float   WIDTH    = 0.07f;  // połowa grubości linii (w blokach)
    public static final boolean GRADIENT_TO_GREEN = false; // true = fiolet -> zieleń jak w logo Solany

    private static final float[] PURPLE = {0.60f, 0.27f, 1.00f}; // #9945FF
    private static final float[] GREEN  = {0.08f, 0.95f, 0.58f}; // #14F195

    protected TrailParticle(ClientWorld world, double x, double y, double z, Sprite sprite) {
        super(world, x, y, z, sprite);
        this.maxAge = LIFETIME;
        this.scale = WIDTH;
        this.collidesWithWorld = false;
        this.gravityStrength = 0f;
        setColor(PURPLE[0], PURPLE[1], PURPLE[2]);
    }

    @Override
    public void tick() {
        lastX = x;
        lastY = y;
        lastZ = z;
        if (age++ >= maxAge) {
            markDead();
            return;
        }
        float t = age / (float) maxAge;
        setAlpha(1f - t); // zanikanie
        if (GRADIENT_TO_GREEN) {
            setColor(PURPLE[0] + (GREEN[0] - PURPLE[0]) * t,
                     PURPLE[1] + (GREEN[1] - PURPLE[1]) * t,
                     PURPLE[2] + (GREEN[2] - PURPLE[2]) * t);
        }
    }

    @Override
    public int getBrightness(float tint) {
        return 0xF000F0;
    }

    @Override
    protected BillboardParticle.RenderType getRenderType() {
        return BillboardParticle.RenderType.PARTICLE_ATLAS_TRANSLUCENT;
    }

    public static class Factory implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider sprites;

        public Factory(SpriteProvider sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType params, ClientWorld world,
                                       double x, double y, double z,
                                       double velocityX, double velocityY, double velocityZ,
                                       Random random) {
            return new TrailParticle(world, x, y, z, sprites.getFirst());
        }
    }
}
