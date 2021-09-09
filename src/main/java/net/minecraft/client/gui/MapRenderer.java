package net.minecraft.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

public class MapRenderer implements AutoCloseable
{
    private static final ResourceLocation MAP_ICONS_LOCATION = new ResourceLocation("textures/map/map_icons.png");
    static final RenderType MAP_ICONS = RenderType.text(MAP_ICONS_LOCATION);
    private static final int WIDTH = 128;
    private static final int HEIGHT = 128;
    final TextureManager textureManager;
    private final Int2ObjectMap<MapRenderer.MapInstance> maps = new Int2ObjectOpenHashMap<>();

    public MapRenderer(TextureManager p_93259_)
    {
        this.textureManager = p_93259_;
    }

    public void update(int p_168766_, MapItemSavedData p_168767_)
    {
        this.getOrCreateMapInstance(p_168766_, p_168767_).m_182566_();
    }

    public void render(PoseStack p_168772_, MultiBufferSource p_168773_, int p_168774_, MapItemSavedData p_168775_, boolean p_168776_, int p_168777_)
    {
        this.getOrCreateMapInstance(p_168774_, p_168775_).draw(p_168772_, p_168773_, p_168776_, p_168777_);
    }

    private MapRenderer.MapInstance getOrCreateMapInstance(int p_168779_, MapItemSavedData p_168780_)
    {
        return this.maps.compute(p_168779_, (p_182563_, p_182564_) ->
        {
            if (p_182564_ == null)
            {
                return new MapRenderer.MapInstance(p_182563_, p_168780_);
            }
            else {
                p_182564_.m_182567_(p_168780_);
                return p_182564_;
            }
        });
    }

    public void resetData()
    {
        for (MapRenderer.MapInstance maprenderer$mapinstance : this.maps.values())
        {
            maprenderer$mapinstance.close();
        }

        this.maps.clear();
    }

    public void close()
    {
        this.resetData();
    }

    class MapInstance implements AutoCloseable
    {
        private MapItemSavedData data;
        private final DynamicTexture texture;
        private final RenderType renderType;
        private boolean f_182565_ = true;

        MapInstance(int p_168783_, MapItemSavedData p_168784_)
        {
            this.data = p_168784_;
            this.texture = new DynamicTexture(128, 128, true);
            ResourceLocation resourcelocation = MapRenderer.this.textureManager.register("map/" + p_168783_, this.texture);
            
            this.renderType = RenderType.text(resourcelocation);
        }

        void m_182567_(MapItemSavedData p_182568_)
        {
            boolean flag = this.data != p_182568_;
            this.data = p_182568_;
            this.f_182565_ |= flag;
        }

        public void m_182566_()
        {
            this.f_182565_ = true;
        }

        private void updateTexture()
        {
            for (int i = 0; i < 128; ++i)
            {
                for (int j = 0; j < 128; ++j)
                {
                    int k = j + i * 128;
                    int l = this.data.colors[k] & 255;

                    if (l / 4 == 0)
                    {
                        this.texture.getPixels().setPixelRGBA(j, i, 0);
                    }
                    else
                    {
                        this.texture.getPixels().setPixelRGBA(j, i, MaterialColor.MATERIAL_COLORS[l / 4].calculateRGBColor(l & 3));
                    }
                }
            }

            this.texture.upload();
        }

        void draw(PoseStack pMatrixStack, MultiBufferSource pBuffer, boolean pActive, int pPackedLight)
        {
            if (this.f_182565_)
            {
                this.updateTexture();
                this.f_182565_ = false;
            }

            int i = 0;
            int j = 0;
            float f = 0.0F;
            Matrix4f matrix4f = pMatrixStack.last().pose();
            VertexConsumer vertexconsumer = pBuffer.getBuffer(this.renderType);
            vertexconsumer.vertex(matrix4f, 0.0F, 128.0F, -0.01F).color(255, 255, 255, 255).uv(0.0F, 1.0F).uv2(pPackedLight).endVertex();
            vertexconsumer.vertex(matrix4f, 128.0F, 128.0F, -0.01F).color(255, 255, 255, 255).uv(1.0F, 1.0F).uv2(pPackedLight).endVertex();
            vertexconsumer.vertex(matrix4f, 128.0F, 0.0F, -0.01F).color(255, 255, 255, 255).uv(1.0F, 0.0F).uv2(pPackedLight).endVertex();
            vertexconsumer.vertex(matrix4f, 0.0F, 0.0F, -0.01F).color(255, 255, 255, 255).uv(0.0F, 0.0F).uv2(pPackedLight).endVertex();
            int k = 0;

            for (MapDecoration mapdecoration : this.data.getDecorations())
            {
                if (!pActive || mapdecoration.renderOnFrame())
                {
                    pMatrixStack.pushPose();
                    pMatrixStack.translate((double)(0.0F + (float)mapdecoration.getX() / 2.0F + 64.0F), (double)(0.0F + (float)mapdecoration.getY() / 2.0F + 64.0F), (double) - 0.02F);
                    pMatrixStack.mulPose(Vector3f.ZP.rotationDegrees((float)(mapdecoration.getRot() * 360) / 16.0F));
                    pMatrixStack.scale(4.0F, 4.0F, 3.0F);
                    pMatrixStack.translate(-0.125D, 0.125D, 0.0D);
                    byte b0 = mapdecoration.getImage();
                    float f1 = (float)(b0 % 16 + 0) / 16.0F;
                    float f2 = (float)(b0 / 16 + 0) / 16.0F;
                    float f3 = (float)(b0 % 16 + 1) / 16.0F;
                    float f4 = (float)(b0 / 16 + 1) / 16.0F;
                    Matrix4f matrix4f1 = pMatrixStack.last().pose();
                    float f5 = -0.001F;
                    VertexConsumer vertexconsumer1 = pBuffer.getBuffer(MapRenderer.MAP_ICONS);
                    vertexconsumer1.vertex(matrix4f1, -1.0F, 1.0F, (float)k * -0.001F).color(255, 255, 255, 255).uv(f1, f2).uv2(pPackedLight).endVertex();
                    vertexconsumer1.vertex(matrix4f1, 1.0F, 1.0F, (float)k * -0.001F).color(255, 255, 255, 255).uv(f3, f2).uv2(pPackedLight).endVertex();
                    vertexconsumer1.vertex(matrix4f1, 1.0F, -1.0F, (float)k * -0.001F).color(255, 255, 255, 255).uv(f3, f4).uv2(pPackedLight).endVertex();
                    vertexconsumer1.vertex(matrix4f1, -1.0F, -1.0F, (float)k * -0.001F).color(255, 255, 255, 255).uv(f1, f4).uv2(pPackedLight).endVertex();
                    pMatrixStack.popPose();

                    if (mapdecoration.getName() != null)
                    {
                        Font font = Minecraft.getInstance().font;
                        Component component = mapdecoration.getName();
                        float f6 = (float)font.width(component);
                        float f7 = Mth.clamp(25.0F / f6, 0.0F, 6.0F / 9.0F);
                        pMatrixStack.pushPose();
                        pMatrixStack.translate((double)(0.0F + (float)mapdecoration.getX() / 2.0F + 64.0F - f6 * f7 / 2.0F), (double)(0.0F + (float)mapdecoration.getY() / 2.0F + 64.0F + 4.0F), (double) - 0.025F);
                        pMatrixStack.scale(f7, f7, 1.0F);
                        pMatrixStack.translate(0.0D, 0.0D, (double) - 0.1F);
                        font.drawInBatch(component, 0.0F, 0.0F, -1, false, pMatrixStack.last().pose(), pBuffer, false, Integer.MIN_VALUE, pPackedLight);
                        pMatrixStack.popPose();
                    }

                    ++k;
                }
            }
        }

        public void close()
        {
            this.texture.close();
        }
    }
}
