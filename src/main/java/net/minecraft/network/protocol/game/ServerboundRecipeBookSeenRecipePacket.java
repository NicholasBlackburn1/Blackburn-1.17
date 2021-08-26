package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

public class ServerboundRecipeBookSeenRecipePacket implements Packet<ServerGamePacketListener>
{
    private final ResourceLocation recipe;

    public ServerboundRecipeBookSeenRecipePacket(Recipe<?> p_134383_)
    {
        this.recipe = p_134383_.getId();
    }

    public ServerboundRecipeBookSeenRecipePacket(FriendlyByteBuf p_179736_)
    {
        this.recipe = p_179736_.readResourceLocation();
    }

    public void write(FriendlyByteBuf pBuf)
    {
        pBuf.writeResourceLocation(this.recipe);
    }

    public void handle(ServerGamePacketListener pHandler)
    {
        pHandler.handleRecipeBookSeenRecipePacket(this);
    }

    public ResourceLocation getRecipe()
    {
        return this.recipe;
    }
}
