package net.optifine.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.SliderButton;
import net.minecraft.client.renderer.GameRenderer;

public class GuiUtils
{
    public static int getWidth(AbstractWidget widget)
    {
        return SliderButton.getWidth(widget);
    }

    public static int getHeight(AbstractWidget widget)
    {
        return SliderButton.getHeight(widget);
    }

    public static void fill(Matrix4f matrixIn, GuiRect[] rects, int color)
    {
        float f = (float)(color >> 24 & 255) / 255.0F;
        float f1 = (float)(color >> 16 & 255) / 255.0F;
        float f2 = (float)(color >> 8 & 255) / 255.0F;
        float f3 = (float)(color & 255) / 255.0F;
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        for (int i = 0; i < rects.length; ++i)
        {
            GuiRect guirect = rects[i];

            if (guirect != null)
            {
                int j = guirect.getLeft();
                int k = guirect.getTop();
                int l = guirect.getRight();
                int i1 = guirect.getBottom();

                if (j < l)
                {
                    int j1 = j;
                    j = l;
                    l = j1;
                }

                if (k < i1)
                {
                    int k1 = k;
                    k = i1;
                    i1 = k1;
                }

                bufferbuilder.vertex(matrixIn, (float)j, (float)i1, 0.0F).color(f1, f2, f3, f).endVertex();
                bufferbuilder.vertex(matrixIn, (float)l, (float)i1, 0.0F).color(f1, f2, f3, f).endVertex();
                bufferbuilder.vertex(matrixIn, (float)l, (float)k, 0.0F).color(f1, f2, f3, f).endVertex();
                bufferbuilder.vertex(matrixIn, (float)j, (float)k, 0.0F).color(f1, f2, f3, f).endVertex();
            }
        }

        bufferbuilder.end();
        BufferUploader.end(bufferbuilder);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }
}
