package com.mojang.blaze3d.vertex;

import com.mojang.blaze3d.systems.RenderSystem;
import net.optifine.SmartAnimations;

public class Tesselator
{
    private static final int MAX_MEMORY_USE = 8388608;
    private static final int MAX_FLOATS = 2097152;
    private final BufferBuilder builder;
    private static final Tesselator INSTANCE = new Tesselator();

    public static Tesselator getInstance()
    {
        RenderSystem.assertThread(RenderSystem::isOnGameThreadOrInit);
        return INSTANCE;
    }

    public Tesselator(int p_85912_)
    {
        this.builder = new BufferBuilder(p_85912_);
    }

    public Tesselator()
    {
        this(2097152);
    }

    public void end()
    {
        if (this.builder.animatedSprites != null)
        {
            SmartAnimations.spritesRendered(this.builder.animatedSprites);
        }

        this.builder.end();
        BufferUploader.end(this.builder);
    }

    public BufferBuilder getBuilder()
    {
        return this.builder;
    }
}
