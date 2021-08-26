package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.EnchantmentTableBlockEntity;

public class EnchantTableRenderer implements BlockEntityRenderer<EnchantmentTableBlockEntity>
{
    public static final Material BOOK_LOCATION = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("entity/enchanting_table_book"));
    private final BookModel bookModel;

    public EnchantTableRenderer(BlockEntityRendererProvider.Context p_173619_)
    {
        this.bookModel = new BookModel(p_173619_.bakeLayer(ModelLayers.BOOK));
    }

    public void render(EnchantmentTableBlockEntity pBlockEntity, float pPartialTicks, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pCombinedLight, int pCombinedOverlay)
    {
        pMatrixStack.pushPose();
        pMatrixStack.translate(0.5D, 0.75D, 0.5D);
        float f = (float)pBlockEntity.time + pPartialTicks;
        pMatrixStack.translate(0.0D, (double)(0.1F + Mth.sin(f * 0.1F) * 0.01F), 0.0D);
        float f1;

        for (f1 = pBlockEntity.rot - pBlockEntity.oRot; f1 >= (float)Math.PI; f1 -= ((float)Math.PI * 2F))
        {
        }

        while (f1 < -(float)Math.PI)
        {
            f1 += ((float)Math.PI * 2F);
        }

        float f2 = pBlockEntity.oRot + f1 * pPartialTicks;
        pMatrixStack.mulPose(Vector3f.YP.rotation(-f2));
        pMatrixStack.mulPose(Vector3f.ZP.rotationDegrees(80.0F));
        float f3 = Mth.lerp(pPartialTicks, pBlockEntity.oFlip, pBlockEntity.flip);
        float f4 = Mth.frac(f3 + 0.25F) * 1.6F - 0.3F;
        float f5 = Mth.frac(f3 + 0.75F) * 1.6F - 0.3F;
        float f6 = Mth.lerp(pPartialTicks, pBlockEntity.oOpen, pBlockEntity.open);
        this.bookModel.setupAnim(f, Mth.clamp(f4, 0.0F, 1.0F), Mth.clamp(f5, 0.0F, 1.0F), f6);
        VertexConsumer vertexconsumer = BOOK_LOCATION.buffer(pBuffer, RenderType::entitySolid);
        this.bookModel.render(pMatrixStack, vertexconsumer, pCombinedLight, pCombinedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
        pMatrixStack.popPose();
    }
}
