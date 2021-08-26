package net.minecraft.client.particle;

import javax.annotation.Nullable;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleOptions;

public interface ParticleProvider<T extends ParticleOptions>
{
    @Nullable
    Particle createParticle(T pType, ClientLevel pLevel, double pX, double p_107424_, double pY, double p_107426_, double pZ, double p_107428_);
}
