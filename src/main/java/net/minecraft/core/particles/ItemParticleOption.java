package net.minecraft.core.particles;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.commands.arguments.item.ItemParser;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

public class ItemParticleOption implements ParticleOptions
{
    public static final ParticleOptions.Deserializer<ItemParticleOption> DESERIALIZER = new ParticleOptions.Deserializer<ItemParticleOption>()
    {
        public ItemParticleOption fromCommand(ParticleType<ItemParticleOption> pParticleType, StringReader pReader) throws CommandSyntaxException
        {
            pReader.expect(' ');
            ItemParser itemparser = (new ItemParser(pReader, false)).parse();
            ItemStack itemstack = (new ItemInput(itemparser.getItem(), itemparser.getNbt())).createItemStack(1, false);
            return new ItemParticleOption(pParticleType, itemstack);
        }
        public ItemParticleOption fromNetwork(ParticleType<ItemParticleOption> pParticleType, FriendlyByteBuf pBuffer)
        {
            return new ItemParticleOption(pParticleType, pBuffer.readItem());
        }
    };
    private final ParticleType<ItemParticleOption> type;
    private final ItemStack itemStack;

    public static Codec<ItemParticleOption> codec(ParticleType<ItemParticleOption> p_123711_)
    {
        return ItemStack.CODEC.xmap((p_123714_) ->
        {
            return new ItemParticleOption(p_123711_, p_123714_);
        }, (p_123709_) ->
        {
            return p_123709_.itemStack;
        });
    }

    public ItemParticleOption(ParticleType<ItemParticleOption> p_123705_, ItemStack p_123706_)
    {
        this.type = p_123705_;
        this.itemStack = p_123706_;
    }

    public void writeToNetwork(FriendlyByteBuf pBuffer)
    {
        pBuffer.writeItem(this.itemStack);
    }

    public String writeToString()
    {
        return Registry.PARTICLE_TYPE.getKey(this.getType()) + " " + (new ItemInput(this.itemStack.getItem(), this.itemStack.getTag())).serialize();
    }

    public ParticleType<ItemParticleOption> getType()
    {
        return this.type;
    }

    public ItemStack getItem()
    {
        return this.itemStack;
    }
}
