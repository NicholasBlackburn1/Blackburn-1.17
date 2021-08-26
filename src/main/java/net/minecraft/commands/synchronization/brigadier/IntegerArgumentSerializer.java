package net.minecraft.commands.synchronization.brigadier;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.synchronization.ArgumentSerializer;
import net.minecraft.network.FriendlyByteBuf;

public class IntegerArgumentSerializer implements ArgumentSerializer<IntegerArgumentType>
{
    public void serializeToNetwork(IntegerArgumentType pArgument, FriendlyByteBuf pBuffer)
    {
        boolean flag = pArgument.getMinimum() != Integer.MIN_VALUE;
        boolean flag1 = pArgument.getMaximum() != Integer.MAX_VALUE;
        pBuffer.writeByte(BrigadierArgumentSerializers.createNumberFlags(flag, flag1));

        if (flag)
        {
            pBuffer.writeInt(pArgument.getMinimum());
        }

        if (flag1)
        {
            pBuffer.writeInt(pArgument.getMaximum());
        }
    }

    public IntegerArgumentType deserializeFromNetwork(FriendlyByteBuf pBuffer)
    {
        byte b0 = pBuffer.readByte();
        int i = BrigadierArgumentSerializers.numberHasMin(b0) ? pBuffer.readInt() : Integer.MIN_VALUE;
        int j = BrigadierArgumentSerializers.numberHasMax(b0) ? pBuffer.readInt() : Integer.MAX_VALUE;
        return IntegerArgumentType.integer(i, j);
    }

    public void serializeToJson(IntegerArgumentType p_121735_, JsonObject p_121736_)
    {
        if (p_121735_.getMinimum() != Integer.MIN_VALUE)
        {
            p_121736_.addProperty("min", p_121735_.getMinimum());
        }

        if (p_121735_.getMaximum() != Integer.MAX_VALUE)
        {
            p_121736_.addProperty("max", p_121735_.getMaximum());
        }
    }
}
