package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.inventory.RecipeBookType;

public class ServerboundRecipeBookChangeSettingsPacket implements Packet<ServerGamePacketListener>
{
    private final RecipeBookType bookType;
    private final boolean isOpen;
    private final boolean isFiltering;

    public ServerboundRecipeBookChangeSettingsPacket(RecipeBookType p_134366_, boolean p_134367_, boolean p_134368_)
    {
        this.bookType = p_134366_;
        this.isOpen = p_134367_;
        this.isFiltering = p_134368_;
    }

    public ServerboundRecipeBookChangeSettingsPacket(FriendlyByteBuf p_179734_)
    {
        this.bookType = p_179734_.readEnum(RecipeBookType.class);
        this.isOpen = p_179734_.readBoolean();
        this.isFiltering = p_179734_.readBoolean();
    }

    public void write(FriendlyByteBuf pBuf)
    {
        pBuf.writeEnum(this.bookType);
        pBuf.writeBoolean(this.isOpen);
        pBuf.writeBoolean(this.isFiltering);
    }

    public void handle(ServerGamePacketListener pHandler)
    {
        pHandler.handleRecipeBookChangeSettingsPacket(this);
    }

    public RecipeBookType getBookType()
    {
        return this.bookType;
    }

    public boolean isOpen()
    {
        return this.isOpen;
    }

    public boolean isFiltering()
    {
        return this.isFiltering;
    }
}
