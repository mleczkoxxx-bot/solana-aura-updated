package pl.solanaaura;

import net.minecraft.client.particle.BillboardParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.util.math.random.Random;

import java.util.HashMap;
import java.util.Map;

/**
 * Jedno logo Solany krążące wokół gracza. Jako cząsteczka działa też w widoku z pierwszej osoby.
 */
public class SolanaLogoParticle extends BillboardParticle {

    // ---- USTAWIENIA ----
    public static final int   COUNT  = 6;      // ile logo krąży
    public static final float RADIUS = 0.95f;  // odległość od gracza
    public static final float SIZE   = 0.22f;  // połowa boku logo (w blokach)
    public static final float SPEED  = 0.06f;  // prędkość obrotu (rad/tick)
    private static final int  LIFETIME = 400;  // po tylu tickach logo jest odtwarzane

    /** entityId gracza -> jego aktualne logo (żeby wiedzieć, kiedy odtworzyć zużyte) */
    static final Map<Integer, SolanaLogoParticle[]> LIVE = new HashMap<>();

    private final int ownerId;
    private final int index;

    protected SolanaLogoParticle(ClientWorld world, double x, double y, double z,
                                 Sprite sprite, int ownerId, int index) {
        super(world, x, y, z, sprite);
        this.ownerId = ownerId;
        this.index = index;
        this.maxAge = LIFETIME;
        this.scale = SIZE;
        this.collidesWithWorld = false;
        this.gravityStrength = 0f;
    }

    private void follow(Entity owner) {
        double angle = age * SPEED + index * (Math.PI * 2 / COUNT);
        int level = index % 3; // 3 "piętra"
        double yOff = 1.0 + (level - 1) * 0.45 + Math.sin(age * 0.08 + index * 1.3) * 0.12;
        setPos(owner.getX() + Math.cos(angle) * RADIUS,
               owner.getY() + yOff,
               owner.getZ() + Math.sin(angle) * RADIUS);
    }

    @Override
    public void tick() {
        lastX = x;
        lastY = y;
        lastZ = z;

        Entity owner = world.getEntityById(ownerId);
        if (owner == null || owner.isRemoved() || age++ >= maxAge) {
            markDead();
            return;
        }
        follow(owner);
    }

    @Override
    public int getBrightness(float tint) {
        return 0xF000F0; // pełna jasność (świeci w ciemności)
    }

    @Override
    protected BillboardParticle.RenderType getRenderType() {
        return BillboardParticle.RenderType.PARTICLE_ATLAS_TRANSLUCENT;
    }

    // ---- fabryka ----
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
            // velocityX = numer logo, velocityY = id gracza (prosty sposób przekazania parametrów)
            int index = (int) velocityX;
            int ownerId = (int) velocityY;
            Entity owner = world.getEntityById(ownerId);
            if (owner == null || index < 0 || index >= COUNT) return null;

            SolanaLogoParticle p = new SolanaLogoParticle(world, x, y, z, sprites.getFirst(), ownerId, index);
            p.follow(owner);
            LIVE.computeIfAbsent(ownerId, k -> new SolanaLogoParticle[COUNT])[index] = p;
            return p;
        }
    }
}
