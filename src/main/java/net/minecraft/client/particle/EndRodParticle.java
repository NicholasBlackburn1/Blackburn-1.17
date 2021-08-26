package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;

public class EndRodParticle extends SimpleAnimatedParticle
{
    EndRodParticle(ClientLevel p_106531_, double p_106532_, double p_106533_, double p_106534_, double p_106535_, double p_106536_, double p_106537_, SpriteSet p_106538_)
    {
        super(p_106531_, p_106532_, p_106533_, p_106534_, p_106538_, 0.0125F);
        this.xd = p_106535_;
        this.yd = p_106536_;
        this.zd = p_106537_;
        this.quadSize *= 0.75F;
        this.lifetime = 60 + this.random.nextInt(12);
        this.setFadeColor(15916745);
        this.setSpriteFromAge(p_106538_);
    }

    public void move(double pX, double p_106551_, double pY)
    {
        this.setBoundingBox(this.getBoundingBox().move(pX, p_106551_, pY));
        this.setLocationFromBoundingbox();
    }

    public static class Provider implements ParticleProvider<SimpleParticleType>
    {
        private final SpriteSet sprites;

        public Provider(SpriteSet p_106555_)
        {
            this.sprites = p_106555_;
        }

        public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double p_106569_, double pY, double p_106571_, double pZ, double p_106573_)
        {
            return new EndRodParticle(pLevel, pX, p_106569_, pY, p_106571_, pZ, p_106573_, this.sprites);
        }
    }
}
