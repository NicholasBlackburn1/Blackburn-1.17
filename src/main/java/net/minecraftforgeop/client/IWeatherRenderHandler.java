package net.minecraftforgeop.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;

@FunctionalInterface
public interface IWeatherRenderHandler
{
    void render(int var1, float var2, ClientLevel var3, Minecraft var4, LightTexture var5, double var6, double var8, double var10);
}
