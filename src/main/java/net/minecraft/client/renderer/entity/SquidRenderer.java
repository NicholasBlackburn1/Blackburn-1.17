package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.model.SquidModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Squid;

public class SquidRenderer<T extends Squid> extends MobRenderer<T, SquidModel<T>>
{
    private static final ResourceLocation SQUID_LOCATION = new ResourceLocation("textures/entity/squid/squid.png");

    public SquidRenderer(EntityRendererProvider.Context p_174406_, SquidModel<T> p_174407_)
    {
        super(p_174406_, p_174407_, 0.7F);
    }

    public ResourceLocation getTextureLocation(T pEntity)
    {
        return SQUID_LOCATION;
    }

    protected void setupRotations(T pEntityLiving, PoseStack pMatrixStack, float pAgeInTicks, float pRotationYaw, float pPartialTicks)
    {
        float f = Mth.lerp(pPartialTicks, pEntityLiving.xBodyRotO, pEntityLiving.xBodyRot);
        float f1 = Mth.lerp(pPartialTicks, pEntityLiving.zBodyRotO, pEntityLiving.zBodyRot);
        pMatrixStack.translate(0.0D, 0.5D, 0.0D);
        pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(180.0F - pRotationYaw));
        pMatrixStack.mulPose(Vector3f.XP.rotationDegrees(f));
        pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(f1));
        pMatrixStack.translate(0.0D, (double) - 1.2F, 0.0D);
    }

    protected float getBob(T pLivingBase, float pPartialTicks)
    {
        return Mth.lerp(pPartialTicks, pLivingBase.oldTentacleAngle, pLivingBase.tentacleAngle);
    }
}
