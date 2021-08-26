package net.minecraft.commands.synchronization.brigadier;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.LongArgumentType;
import net.minecraft.commands.synchronization.ArgumentSerializer;
import net.minecraft.network.FriendlyByteBuf;

public class LongArgumentSerializer implements ArgumentSerializer<LongArgumentType>
{
    public void serializeToNetwork(LongArgumentType pArgument, FriendlyByteBuf pBuffer)
    {
        boolean flag = pArgument.getMinimum() != Long.MIN_VALUE;
        boolean flag1 = pArgument.getMaximum() != Long.MAX_VALUE;
        pBuffer.writeByte(BrigadierArgumentSerializers.createNumberFlags(flag, flag1));

        if (flag)
        {
            pBuffer.writeLong(pArgument.getMinimum());
        }

        if (flag1)
        {
            pBuffer.writeLong(pArgument.getMaximum());
        }
    }

    public LongArgumentType deserializeFromNetwork(FriendlyByteBuf pBuffer)
    {
        byte b0 = pBuffer.readByte();
        long i = BrigadierArgumentSerializers.numberHasMin(b0) ? pBuffer.readLong() : Long.MIN_VALUE;
        long j = BrigadierArgumentSerializers.numberHasMax(b0) ? pBuffer.readLong() : Long.MAX_VALUE;
        return LongArgumentType.longArg(i, j);
    }

    public void serializeToJson(LongArgumentType p_121752_, JsonObject p_121753_)
    {
        if (p_121752_.getMinimum() != Long.MIN_VALUE)
        {
            p_121753_.addProperty("min", p_121752_.getMinimum());
        }

        if (p_121752_.getMaximum() != Long.MAX_VALUE)
        {
            p_121753_.addProperty("max", p_121752_.getMaximum());
        }
    }
}
