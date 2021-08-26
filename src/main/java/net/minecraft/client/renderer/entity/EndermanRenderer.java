package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Random;
import net.minecraft.client.model.EndermanModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.CarriedBlockLayer;
import net.minecraft.client.renderer.entity.layers.EnderEyesLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class EndermanRenderer extends MobRenderer<EnderMan, EndermanModel<EnderMan>>
{
    private static final ResourceLocation ENDERMAN_LOCATION = new ResourceLocation("textures/entity/enderman/enderman.png");
    private final Random random = new Random();

    public EndermanRenderer(EntityRendererProvider.Context p_173992_)
    {
        super(p_173992_, new EndermanModel<>(p_173992_.bakeLayer(ModelLayers.ENDERMAN)), 0.5F);
        this.addLayer(new EnderEyesLayer<>(this));
        this.addLayer(new CarriedBlockLayer(this));
    }

    public void render(EnderMan pEntity, float pEntityYaw, float pPartialTicks, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight)
    {
        BlockState blockstate = pEntity.getCarriedBlock();
        EndermanModel<EnderMan> endermanmodel = this.getModel();
        endermanmodel.carrying = blockstate != null;
        endermanmodel.creepy = pEntity.isCreepy();
        super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
    }

    public Vec3 getRenderOffset(EnderMan pEntity, float pPartialTicks)
    {
        if (pEntity.isCreepy())
        {
            double d0 = 0.02D;
            return new Vec3(this.random.nextGaussian() * 0.02D, 0.0D, this.random.nextGaussian() * 0.02D);
        }
        else
        {
            return super.getRenderOffset(pEntity, pPartialTicks);
        }
    }

    public ResourceLocation getTextureLocation(EnderMan pEntity)
    {
        return ENDERMAN_LOCATION;
    }
}
