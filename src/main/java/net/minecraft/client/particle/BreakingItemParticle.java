package net.minecraft.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class BreakingItemParticle extends TextureSheetParticle
{
    private final float uo;
    private final float vo;

    BreakingItemParticle(ClientLevel p_105646_, double p_105647_, double p_105648_, double p_105649_, double p_105650_, double p_105651_, double p_105652_, ItemStack p_105653_)
    {
        this(p_105646_, p_105647_, p_105648_, p_105649_, p_105653_);
        this.xd *= (double)0.1F;
        this.yd *= (double)0.1F;
        this.zd *= (double)0.1F;
        this.xd += p_105650_;
        this.yd += p_105651_;
        this.zd += p_105652_;
    }

    public ParticleRenderType getRenderType()
    {
        return ParticleRenderType.TERRAIN_SHEET;
    }

    protected BreakingItemParticle(ClientLevel p_105665_, double p_105666_, double p_105667_, double p_105668_, ItemStack p_105669_)
    {
        super(p_105665_, p_105666_, p_105667_, p_105668_, 0.0D, 0.0D, 0.0D);
        this.setSprite(Minecraft.getInstance().getItemRenderer().getModel(p_105669_, p_105665_, (LivingEntity)null, 0).getParticleIcon());
        this.gravity = 1.0F;
        this.quadSize /= 2.0F;
        this.uo = this.random.nextFloat() * 3.0F;
        this.vo = this.random.nextFloat() * 3.0F;
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

    public static class Provider implements ParticleProvider<ItemParticleOption>
    {
        public Particle createParticle(ItemParticleOption pType, ClientLevel pLevel, double pX, double p_105680_, double pY, double p_105682_, double pZ, double p_105684_)
        {
            return new BreakingItemParticle(pLevel, pX, p_105680_, pY, p_105682_, pZ, p_105684_, pType.getItem());
        }
    }

    public static class SlimeProvider implements ParticleProvider<SimpleParticleType>
    {
        public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double p_105708_, double pY, double p_105710_, double pZ, double p_105712_)
        {
            return new BreakingItemParticle(pLevel, pX, p_105708_, pY, new ItemStack(Items.SLIME_BALL));
        }
    }

    public static class SnowballProvider implements ParticleProvider<SimpleParticleType>
    {
        public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double p_105727_, double pY, double p_105729_, double pZ, double p_105731_)
        {
            return new BreakingItemParticle(pLevel, pX, p_105727_, pY, new ItemStack(Items.SNOWBALL));
        }
    }
}
