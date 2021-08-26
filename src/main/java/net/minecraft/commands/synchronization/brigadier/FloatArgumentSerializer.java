package net.minecraft.commands.synchronization.brigadier;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.FloatArgumentType;
import net.minecraft.commands.synchronization.ArgumentSerializer;
import net.minecraft.network.FriendlyByteBuf;

public class FloatArgumentSerializer implements ArgumentSerializer<FloatArgumentType>
{
    public void serializeToNetwork(FloatArgumentType pArgument, FriendlyByteBuf pBuffer)
    {
        boolean flag = pArgument.getMinimum() != -Float.MAX_VALUE;
        boolean flag1 = pArgument.getMaximum() != Float.MAX_VALUE;
        pBuffer.writeByte(BrigadierArgumentSerializers.createNumberFlags(flag, flag1));

        if (flag)
        {
            pBuffer.writeFloat(pArgument.getMinimum());
        }

        if (flag1)
        {
            pBuffer.writeFloat(pArgument.getMaximum());
        }
    }

    public FloatArgumentType deserializeFromNetwork(FriendlyByteBuf pBuffer)
    {
        byte b0 = pBuffer.readByte();
        float f = BrigadierArgumentSerializers.numberHasMin(b0) ? pBuffer.readFloat() : -Float.MAX_VALUE;
        float f1 = BrigadierArgumentSerializers.numberHasMax(b0) ? pBuffer.readFloat() : Float.MAX_VALUE;
        return FloatArgumentType.floatArg(f, f1);
    }

    public void serializeToJson(FloatArgumentType p_121718_, JsonObject p_121719_)
    {
        if (p_121718_.getMinimum() != -Float.MAX_VALUE)
        {
            p_121719_.addProperty("min", p_121718_.getMinimum());
        }

        if (p_121718_.getMaximum() != Float.MAX_VALUE)
        {
            p_121719_.addProperty("max", p_121718_.getMaximum());
        }
    }
}
