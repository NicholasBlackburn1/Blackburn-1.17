package net.minecraft.network.protocol.game;

import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.protocol.Packet;

public class ClientboundCommandSuggestionsPacket implements Packet<ClientGamePacketListener>
{
    private final int id;
    private final Suggestions suggestions;

    public ClientboundCommandSuggestionsPacket(int p_131846_, Suggestions p_131847_)
    {
        this.id = p_131846_;
        this.suggestions = p_131847_;
    }

    public ClientboundCommandSuggestionsPacket(FriendlyByteBuf p_178790_)
    {
        this.id = p_178790_.readVarInt();
        int i = p_178790_.readVarInt();
        int j = p_178790_.readVarInt();
        StringRange stringrange = StringRange.between(i, i + j);
        List<Suggestion> list = p_178790_.readList((p_178793_) ->
        {
            String s = p_178793_.readUtf();
            Component component = p_178793_.readBoolean() ? p_178793_.readComponent() : null;
            return new Suggestion(stringrange, s, component);
        });
        this.suggestions = new Suggestions(stringrange, list);
    }

    public void write(FriendlyByteBuf pBuf)
    {
        pBuf.writeVarInt(this.id);
        pBuf.writeVarInt(this.suggestions.getRange().getStart());
        pBuf.writeVarInt(this.suggestions.getRange().getLength());
        pBuf.writeCollection(this.suggestions.getList(), (p_178795_, p_178796_) ->
        {
            p_178795_.writeUtf(p_178796_.getText());
            p_178795_.writeBoolean(p_178796_.getTooltip() != null);

            if (p_178796_.getTooltip() != null)
            {
                p_178795_.writeComponent(ComponentUtils.fromMessage(p_178796_.getTooltip()));
            }
        });
    }

    public void handle(ClientGamePacketListener pHandler)
    {
        pHandler.handleCommandSuggestions(this);
    }

    public int getId()
    {
        return this.id;
    }

    public Suggestions getSuggestions()
    {
        return this.suggestions;
    }
}
