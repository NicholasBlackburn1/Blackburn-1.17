package net.minecraft.client.server;

import com.mojang.authlib.GameProfile;
import java.net.SocketAddress;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.storage.PlayerDataStorage;

public class IntegratedPlayerList extends PlayerList
{
    private CompoundTag playerData;

    public IntegratedPlayerList(IntegratedServer p_120003_, RegistryAccess.RegistryHolder p_120004_, PlayerDataStorage p_120005_)
    {
        super(p_120003_, p_120004_, p_120005_, 8);
        this.setViewDistance(10);
    }

    protected void save(ServerPlayer pPlayer)
    {
        if (pPlayer.getName().getString().equals(this.getServer().getSingleplayerName()))
        {
            this.playerData = pPlayer.saveWithoutId(new CompoundTag());
        }

        super.save(pPlayer);
    }

    public Component canPlayerLogin(SocketAddress p_120007_, GameProfile p_120008_)
    {
        return (Component)(p_120008_.getName().equalsIgnoreCase(this.getServer().getSingleplayerName()) && this.getPlayerByName(p_120008_.getName()) != null ? new TranslatableComponent("multiplayer.disconnect.name_taken") : super.canPlayerLogin(p_120007_, p_120008_));
    }

    public IntegratedServer getServer()
    {
        return (IntegratedServer)super.getServer();
    }

    public CompoundTag getSingleplayerData()
    {
        return this.playerData;
    }
}
