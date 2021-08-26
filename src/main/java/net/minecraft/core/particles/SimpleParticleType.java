package net.minecraft.core.particles;

import com.mojang.brigadier.StringReader;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;

public class SimpleParticleType extends ParticleType<SimpleParticleType> implements ParticleOptions
{
    private static final ParticleOptions.Deserializer<SimpleParticleType> DESERIALIZER = new ParticleOptions.Deserializer<SimpleParticleType>()
    {
        public SimpleParticleType fromCommand(ParticleType<SimpleParticleType> pParticleType, StringReader pReader)
        {
            return (SimpleParticleType)pParticleType;
        }
        public SimpleParticleType fromNetwork(ParticleType<SimpleParticleType> pParticleType, FriendlyByteBuf pBuffer)
        {
            return (SimpleParticleType)pParticleType;
        }
    };
    private final Codec<SimpleParticleType> codec = Codec.unit(this::getType);

    protected SimpleParticleType(boolean p_123837_)
    {
        super(p_123837_, DESERIALIZER);
    }

    public SimpleParticleType getType()
    {
        return this;
    }

    public Codec<SimpleParticleType> codec()
    {
        return this.codec;
    }

    public void writeToNetwork(FriendlyByteBuf pBuffer)
    {
    }

    public String writeToString()
    {
        return Registry.PARTICLE_TYPE.getKey(this).toString();
    }
}
