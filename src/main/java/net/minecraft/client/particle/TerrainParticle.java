package net.minecraft.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class TerrainParticle extends TextureSheetParticle
{
    private final BlockPos pos;
    private final float uo;
    private final float vo;

    public TerrainParticle(ClientLevel p_108282_, double p_108283_, double p_108284_, double p_108285_, double p_108286_, double p_108287_, double p_108288_, BlockState p_108289_)
    {
        this(p_108282_, p_108283_, p_108284_, p_108285_, p_108286_, p_108287_, p_108288_, p_108289_, new BlockPos(p_108283_, p_108284_, p_108285_));
    }

    public TerrainParticle(ClientLevel p_172451_, double p_172452_, double p_172453_, double p_172454_, double p_172455_, double p_172456_, double p_172457_, BlockState p_172458_, BlockPos p_172459_)
    {
        super(p_172451_, p_172452_, p_172453_, p_172454_, p_172455_, p_172456_, p_172457_);
        this.pos = p_172459_;
        this.setSprite(Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getParticleIcon(p_172458_));
        this.gravity = 1.0F;
        this.rCol = 0.6F;
        this.gCol = 0.6F;
        this.bCol = 0.6F;

        if (!p_172458_.is(Blocks.GRASS_BLOCK))
        {
            int i = Minecraft.getInstance().getBlockColors().getColor(p_172458_, p_172451_, p_172459_, 0);
            this.rCol *= (float)(i >> 16 & 255) / 255.0F;
            this.gCol *= (float)(i >> 8 & 255) / 255.0F;
            this.bCol *= (float)(i & 255) / 255.0F;
        }

        this.quadSize /= 2.0F;
        this.uo = this.random.nextFloat() * 3.0F;
        this.vo = this.random.nextFloat() * 3.0F;
    }

    public ParticleRenderType getRenderType()
    {
        return ParticleRenderType.TERRAIN_SHEET;
    }

    protected float getU0()
    {
        return this.sprite.getU((double)((this.uo + 1.0F) / 4.0F * 16.0F));
    }

    protected float getU1()
    {
        return this.sprite.getU((double)(this.uo / 4.0F * 16.0F));
    }

    protected float getV0()
    {
        return this.sprite.getV((double)(this.vo / 4.0F * 16.0F));
    }

    protected float getV1()
    {
        return this.sprite.getV((double)((this.vo + 1.0F) / 4.0F * 16.0F));
    }

    public int getLightColor(float pPartialTick)
    {
        int i = super.getLightColor(pPartialTick);
        return i == 0 && this.level.hasChunkAt(this.pos) ? LevelRenderer.getLightColor(this.level, this.pos) : i;
    }

    public static class Provider implements ParticleProvider<BlockParticleOption>
    {
        public Particle createParticle(BlockParticleOption pType, ClientLevel pLevel, double pX, double p_108307_, double pY, double p_108309_, double pZ, double p_108311_)
        {
            BlockState blockstate = pType.getState();
            return !blockstate.isAir() && !blockstate.is(Blocks.MOVING_PISTON) ? new TerrainParticle(pLevel, pX, p_108307_, pY, p_108309_, pZ, p_108311_, blockstate) : null;
        }
    }
}
