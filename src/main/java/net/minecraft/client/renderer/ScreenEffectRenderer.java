package net.minecraft.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.optifine.Config;
import net.optifine.SmartAnimations;
import net.optifine.reflect.Reflector;
import net.optifine.shaders.Shaders;
import org.apache.commons.lang3.tuple.Pair;

public class ScreenEffectRenderer
{
    private static final ResourceLocation UNDERWATER_LOCATION = new ResourceLocation("textures/misc/underwater.png");

    public static void renderScreenEffect(Minecraft pMinecraft, PoseStack pMatrixStack)
    {
        Player player = pMinecraft.player;

        if (!player.noPhysics)
        {
            if (Reflector.ForgeEventFactory_renderBlockOverlay.exists() && Reflector.ForgeBlockModelShapes_getTexture3.exists())
            {
                Pair<BlockState, BlockPos> pair = getOverlayBlock(player);

                if (pair != null)
                {
                    Object object = Reflector.getFieldValue(Reflector.RenderBlockOverlayEvent_OverlayType_BLOCK);

                    if (!Reflector.ForgeEventFactory_renderBlockOverlay.callBoolean(player, pMatrixStack, object, pair.getLeft(), pair.getRight()))
                    {
                        TextureAtlasSprite textureatlassprite = (TextureAtlasSprite)Reflector.call(pMinecraft.getBlockRenderer().getBlockModelShaper(), Reflector.ForgeBlockModelShapes_getTexture3, pair.getLeft(), pMinecraft.level, pair.getRight());
                        renderTex(textureatlassprite, pMatrixStack);
                    }
                }
            }
            else
            {
                BlockState blockstate = getViewBlockingState(player);

                if (blockstate != null)
                {
                    renderTex(pMinecraft.getBlockRenderer().getBlockModelShaper().getParticleIcon(blockstate), pMatrixStack);
                }
            }
        }

        if (!pMinecraft.player.isSpectator())
        {
            if (pMinecraft.player.isEyeInFluid(FluidTags.WATER) && !Reflector.ForgeEventFactory_renderWaterOverlay.callBoolean(player, pMatrixStack))
            {
                renderWater(pMinecraft, pMatrixStack);
            }

            if (pMinecraft.player.isOnFire() && !Reflector.ForgeEventFactory_renderFireOverlay.callBoolean(player, pMatrixStack))
            {
                renderFire(pMinecraft, pMatrixStack);
            }
        }
    }

    @Nullable
    private static BlockState getViewBlockingState(Player pPlayer)
    {
        Pair<BlockState, BlockPos> pair = getOverlayBlock(pPlayer);
        return pair == null ? null : pair.getLeft();
    }

    private static Pair<BlockState, BlockPos> getOverlayBlock(Player playerIn)
    {
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

        for (int i = 0; i < 8; ++i)
        {
            double d0 = playerIn.getX() + (double)(((float)((i >> 0) % 2) - 0.5F) * playerIn.getBbWidth() * 0.8F);
            double d1 = playerIn.getEyeY() + (double)(((float)((i >> 1) % 2) - 0.5F) * 0.1F);
            double d2 = playerIn.getZ() + (double)(((float)((i >> 2) % 2) - 0.5F) * playerIn.getBbWidth() * 0.8F);
            blockpos$mutableblockpos.set(d0, d1, d2);
            BlockState blockstate = playerIn.level.getBlockState(blockpos$mutableblockpos);

            if (blockstate.getRenderShape() != RenderShape.INVISIBLE && blockstate.isViewBlocking(playerIn.level, blockpos$mutableblockpos))
            {
                return Pair.of(blockstate, blockpos$mutableblockpos.immutable());
            }
        }

        return null;
    }

    private static void renderTex(TextureAtlasSprite p_173297_, PoseStack p_173298_)
    {
        if (SmartAnimations.isActive())
        {
            SmartAnimations.spriteRendered(p_173297_);
        }

        RenderSystem.setShaderTexture(0, p_173297_.atlas().location());
        RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        float f = 0.1F;
        float f1 = -1.0F;
        float f2 = 1.0F;
        float f3 = -1.0F;
        float f4 = 1.0F;
        float f5 = -0.5F;
        float f6 = p_173297_.getU0();
        float f7 = p_173297_.getU1();
        float f8 = p_173297_.getV0();
        float f9 = p_173297_.getV1();
        Matrix4f matrix4f = p_173298_.last().pose();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
        bufferbuilder.vertex(matrix4f, -1.0F, -1.0F, -0.5F).color(0.1F, 0.1F, 0.1F, 1.0F).uv(f7, f9).endVertex();
        bufferbuilder.vertex(matrix4f, 1.0F, -1.0F, -0.5F).color(0.1F, 0.1F, 0.1F, 1.0F).uv(f6, f9).endVertex();
        bufferbuilder.vertex(matrix4f, 1.0F, 1.0F, -0.5F).color(0.1F, 0.1F, 0.1F, 1.0F).uv(f6, f8).endVertex();
        bufferbuilder.vertex(matrix4f, -1.0F, 1.0F, -0.5F).color(0.1F, 0.1F, 0.1F, 1.0F).uv(f7, f8).endVertex();
        bufferbuilder.end();
        BufferUploader.end(bufferbuilder);
    }

    private static void renderWater(Minecraft pMinecraft, PoseStack pMatrixStack)
    {
        if (!Config.isShaders() || Shaders.isUnderwaterOverlay())
        {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.enableTexture();
            RenderSystem.setShaderTexture(0, UNDERWATER_LOCATION);

            if (SmartAnimations.isActive())
            {
                SmartAnimations.textureRendered(pMinecraft.getTextureManager().getTexture(UNDERWATER_LOCATION).getId());
            }

            BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
            float f = pMinecraft.player.getBrightness();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(f, f, f, 0.1F);
            float f1 = 4.0F;
            float f2 = -1.0F;
            float f3 = 1.0F;
            float f4 = -1.0F;
            float f5 = 1.0F;
            float f6 = -0.5F;
            float f7 = -pMinecraft.player.getYRot() / 64.0F;
            float f8 = pMinecraft.player.getXRot() / 64.0F;
            Matrix4f matrix4f = pMatrixStack.last().pose();
            bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            bufferbuilder.vertex(matrix4f, -1.0F, -1.0F, -0.5F).uv(4.0F + f7, 4.0F + f8).endVertex();
            bufferbuilder.vertex(matrix4f, 1.0F, -1.0F, -0.5F).uv(0.0F + f7, 4.0F + f8).endVertex();
            bufferbuilder.vertex(matrix4f, 1.0F, 1.0F, -0.5F).uv(0.0F + f7, 0.0F + f8).endVertex();
            bufferbuilder.vertex(matrix4f, -1.0F, 1.0F, -0.5F).uv(4.0F + f7, 0.0F + f8).endVertex();
            bufferbuilder.end();
            BufferUploader.end(bufferbuilder);
            RenderSystem.disableBlend();
        }
    }

    private static void renderFire(Minecraft pMinecraft, PoseStack pMatrixStack)
    {
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
        RenderSystem.depthFunc(519);
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableTexture();
        TextureAtlasSprite textureatlassprite = ModelBakery.FIRE_1.sprite();

        if (SmartAnimations.isActive())
        {
            SmartAnimations.spriteRendered(textureatlassprite);
        }

        RenderSystem.setShaderTexture(0, textureatlassprite.atlas().location());
        float f = textureatlassprite.getU0();
        float f1 = textureatlassprite.getU1();
        float f2 = (f + f1) / 2.0F;
        float f3 = textureatlassprite.getV0();
        float f4 = textureatlassprite.getV1();
        float f5 = (f3 + f4) / 2.0F;
        float f6 = textureatlassprite.uvShrinkRatio();
        float f7 = Mth.lerp(f6, f, f2);
        float f8 = Mth.lerp(f6, f1, f2);
        float f9 = Mth.lerp(f6, f3, f5);
        float f10 = Mth.lerp(f6, f4, f5);
        float f11 = 1.0F;

        for (int i = 0; i < 2; ++i)
        {
            pMatrixStack.pushPose();
            float f12 = -0.5F;
            float f13 = 0.5F;
            float f14 = -0.5F;
            float f15 = 0.5F;
            float f16 = -0.5F;
            pMatrixStack.translate((double)((float)(-(i * 2 - 1)) * 0.24F), (double) - 0.3F, 0.0D);
            pMatrixStack.mulPose(Vector3f.YP.rotationDegrees((float)(i * 2 - 1) * 10.0F));
            Matrix4f matrix4f = pMatrixStack.last().pose();
            bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
            bufferbuilder.vertex(matrix4f, -0.5F, -0.5F, -0.5F).color(1.0F, 1.0F, 1.0F, 0.9F).uv(f8, f10).endVertex();
            bufferbuilder.vertex(matrix4f, 0.5F, -0.5F, -0.5F).color(1.0F, 1.0F, 1.0F, 0.9F).uv(f7, f10).endVertex();
            bufferbuilder.vertex(matrix4f, 0.5F, 0.5F, -0.5F).color(1.0F, 1.0F, 1.0F, 0.9F).uv(f7, f9).endVertex();
            bufferbuilder.vertex(matrix4f, -0.5F, 0.5F, -0.5F).color(1.0F, 1.0F, 1.0F, 0.9F).uv(f8, f9).endVertex();
            bufferbuilder.end();
            BufferUploader.end(bufferbuilder);
            pMatrixStack.popPose();
        }

        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.depthFunc(515);
    }
}
