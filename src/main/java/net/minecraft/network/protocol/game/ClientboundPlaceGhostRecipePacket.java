package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

public class ClientboundPlaceGhostRecipePacket implements Packet<ClientGamePacketListener>
{
    private final int containerId;
    private final ResourceLocation recipe;

    public ClientboundPlaceGhostRecipePacket(int p_132647_, Recipe<?> p_132648_)
    {
        this.containerId = p_132647_;
        this.recipe = p_132648_.getId();
    }

    public ClientboundPlaceGhostRecipePacket(FriendlyByteBuf p_179027_)
    {
        this.containerId = p_179027_.readByte();
        this.recipe = p_179027_.readResourceLocation();
    }

    public void write(FriendlyByteBuf pBuf)
    {
        pBuf.writeByte(this.containerId);
        pBuf.writeResourceLocation(this.recipe);
    }

    public void handle(ClientGamePacketListener pHandler)
    {
        pHandler.handlePlaceRecipe(this);
    }

    public ResourceLocation getRecipe()
    {
        return this.recipe;
    }

    public int getContainerId()
    {
        return this.containerId;
    }
}
