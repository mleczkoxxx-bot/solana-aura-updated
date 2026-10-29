package pl.solanaaura;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class SolanaAuraClient implements ClientModInitializer {

    public static final String MOD_ID = "solanaaura";

    public static final SimpleParticleType LOGO  = FabricParticleTypes.simple();
    public static final SimpleParticleType TRAIL = FabricParticleTypes.simple();

    private static final double TRAIL_SPACING = 0.10; // co ile bloków punkt smugi (mniej = gładsza linia)
    private static final double TRAIL_HEIGHT  = 0.15; // wysokość linii nad ziemią
    private static final double MAX_DISTANCE  = 48;   // dla dalszych graczy nie rysujemy

    private final Map<UUID, Vec3d> lastPos = new HashMap<>();

    @Override
    public void onInitializeClient() {
        Registry.register(Registries.PARTICLE_TYPE, Identifier.of(MOD_ID, "logo"), LOGO);
        Registry.register(Registries.PARTICLE_TYPE, Identifier.of(MOD_ID, "trail"), TRAIL);

        ParticleFactoryRegistry.getInstance().register(LOGO, SolanaLogoParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(TRAIL, TrailParticle.Factory::new);

        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
    }

    private void onTick(MinecraftClient client) {
        ClientWorld world = client.world;
        if (world == null || client.player == null) {
            lastPos.clear();
            SolanaLogoParticle.LIVE.clear();
            return;
        }
        if (client.isPaused()) return;

        Set<UUID> present = new HashSet<>();
        for (AbstractClientPlayerEntity p : world.getPlayers()) {
            UUID id = p.getUuid();
            present.add(id);

            Vec3d now = new Vec3d(p.getX(), p.getY(), p.getZ());
            Vec3d prev = lastPos.put(id, now);

            if (client.player.squaredDistanceTo(p) > MAX_DISTANCE * MAX_DISTANCE) continue;

            // --- smuga: ciągła linia z gęstych punktów między poprzednią a obecną pozycją ---
            if (prev != null) {
                double dist = now.distanceTo(prev);
                if (dist > 0.02 && dist < 8) {
                    int steps = Math.max(1, (int) Math.ceil(dist / TRAIL_SPACING));
                    for (int i = 0; i < steps; i++) {
                        double t = (i + 1) / (double) steps;
                        world.addParticleClient(TRAIL,
                                prev.x + (now.x - prev.x) * t,
                                prev.y + (now.y - prev.y) * t + TRAIL_HEIGHT,
                                prev.z + (now.z - prev.z) * t,
                                0, 0, 0);
                    }
                }
            }

            // --- logo: dbamy, żeby każdy gracz miał komplet krążących logo ---
            SolanaLogoParticle[] ring = SolanaLogoParticle.LIVE.get(p.getId());
            for (int i = 0; i < SolanaLogoParticle.COUNT; i++) {
                if (ring == null || ring[i] == null || !ring[i].isAlive()) {
                    // vx = numer logo, vy = id gracza (odczytuje to fabryka)
                    world.addParticleClient(LOGO, p.getX(), p.getY() + 1.0, p.getZ(), i, p.getId(), 0);
                }
            }
        }
        lastPos.keySet().retainAll(present);
    }
}
