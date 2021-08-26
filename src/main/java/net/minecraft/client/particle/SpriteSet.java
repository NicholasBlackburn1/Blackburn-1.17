package net.minecraft.client.particle;

import java.util.Random;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public interface SpriteSet
{
    TextureAtlasSprite get(int pParticleAge, int pParticleMaxAge);

    TextureAtlasSprite get(Random pParticleAge);
}
