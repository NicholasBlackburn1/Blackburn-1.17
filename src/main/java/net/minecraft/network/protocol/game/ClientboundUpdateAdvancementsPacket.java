package net.minecraft.network.protocol.game;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;

public class ClientboundUpdateAdvancementsPacket implements Packet<ClientGamePacketListener>
{
    private final boolean reset;
    private final Map<ResourceLocation, Advancement.Builder> added;
    private final Set<ResourceLocation> removed;
    private final Map<ResourceLocation, AdvancementProgress> progress;

    public ClientboundUpdateAdvancementsPacket(boolean p_133560_, Collection<Advancement> p_133561_, Set<ResourceLocation> p_133562_, Map<ResourceLocation, AdvancementProgress> p_133563_)
    {
        this.reset = p_133560_;
        Builder<ResourceLocation, Advancement.Builder> builder = ImmutableMap.builder();

        for (Advancement advancement : p_133561_)
        {
            builder.put(advancement.getId(), advancement.deconstruct());
        }

        this.added = builder.build();
        this.removed = ImmutableSet.copyOf(p_133562_);
        this.progress = ImmutableMap.copyOf(p_133563_);
    }

    public ClientboundUpdateAdvancementsPacket(FriendlyByteBuf p_179439_)
    {
        this.reset = p_179439_.readBoolean();
        this.added = p_179439_.readMap(FriendlyByteBuf::readResourceLocation, Advancement.Builder::fromNetwork);
        this.removed = p_179439_.readCollection(Sets::newLinkedHashSetWithExpectedSize, FriendlyByteBuf::readResourceLocation);
        this.progress = p_179439_.readMap(FriendlyByteBuf::readResourceLocation, AdvancementProgress::fromNetwork);
    }

    public void write(FriendlyByteBuf pBuf)
    {
        pBuf.writeBoolean(this.reset);
        pBuf.writeMap(this.added, FriendlyByteBuf::writeResourceLocation, (p_179441_, p_179442_) ->
        {
            p_179442_.serializeToNetwork(p_179441_);
        });
        pBuf.writeCollection(this.removed, FriendlyByteBuf::writeResourceLocation);
        pBuf.writeMap(this.progress, FriendlyByteBuf::writeResourceLocation, (p_179444_, p_179445_) ->
        {
            p_179445_.serializeToNetwork(p_179444_);
        });
    }

    public void handle(ClientGamePacketListener pHandler)
    {
        pHandler.handleUpdateAdvancementsPacket(this);
    }

    public Map<ResourceLocation, Advancement.Builder> getAdded()
    {
        return this.added;
    }

    public Set<ResourceLocation> getRemoved()
    {
        return this.removed;
    }

    public Map<ResourceLocation, AdvancementProgress> getProgress()
    {
        return this.progress;
    }

    public boolean shouldReset()
    {
        return this.reset;
    }
}
