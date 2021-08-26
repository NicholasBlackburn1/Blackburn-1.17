package net.minecraft.commands.synchronization.brigadier;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType.StringType;
import net.minecraft.commands.synchronization.ArgumentSerializer;
import net.minecraft.network.FriendlyByteBuf;

public class StringArgumentSerializer implements ArgumentSerializer<StringArgumentType>
{
    public void serializeToNetwork(StringArgumentType pArgument, FriendlyByteBuf pBuffer)
    {
        pBuffer.writeEnum(pArgument.getType());
    }

    public StringArgumentType deserializeFromNetwork(FriendlyByteBuf pBuffer)
    {
        StringType stringtype = pBuffer.readEnum(StringType.class);

        switch (stringtype)
        {
            case SINGLE_WORD:
                return StringArgumentType.word();

            case QUOTABLE_PHRASE:
                return StringArgumentType.string();

            case GREEDY_PHRASE:
            default:
                return StringArgumentType.greedyString();
        }
    }

    public void serializeToJson(StringArgumentType p_121769_, JsonObject p_121770_)
    {
        switch (p_121769_.getType())
        {
            case SINGLE_WORD:
                p_121770_.addProperty("type", "word");
                break;

            case QUOTABLE_PHRASE:
                p_121770_.addProperty("type", "phrase");
                break;

            case GREEDY_PHRASE:
            default:
                p_121770_.addProperty("type", "greedy");
        }
    }
}
