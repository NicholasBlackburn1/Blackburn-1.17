package net.minecraftforge.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;

public interface IItemRenderProperties
{
    IItemRenderProperties DUMMY = new IItemRenderProperties()
    {
    };

default BlockEntityWithoutLevelRenderer getItemStackRenderer()
    {
        return Minecraft.getInstance().getItemRenderer().getBlockEntityRenderer();
    }
}
