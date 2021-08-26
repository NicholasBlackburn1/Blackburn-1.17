package net.optifine.entity.model;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.optifine.Config;

public class ModelAdapterBanner extends ModelAdapter
{
    public ModelAdapterBanner()
    {
        super(BlockEntityType.BANNER, "banner", 0.0F);
    }

    public Model makeModel()
    {
        return new BannerModel();
    }

    public ModelPart getModelRenderer(Model model, String modelPart)
    {
        if (!(model instanceof BannerModel))
        {
            return null;
        }
        else
        {
            BannerModel bannermodel = (BannerModel)model;

            if (modelPart.equals("slate"))
            {
                return bannermodel.bannerSlate;
            }
            else if (modelPart.equals("stand"))
            {
                return bannermodel.bannerStand;
            }
            else
            {
                return modelPart.equals("top") ? bannermodel.bannerTop : null;
            }
        }
    }

    public String[] getModelRendererNames()
    {
        return new String[] {"slate", "stand", "top"};
    }

    public IEntityRenderer makeEntityRender(Model model, float shadowSize)
    {
        BlockEntityRenderDispatcher blockentityrenderdispatcher = Config.getMinecraft().getBlockEntityRenderDispatcher();
        BlockEntityRenderer blockentityrenderer = blockentityrenderdispatcher.getRenderer(BlockEntityType.BANNER);

        if (!(blockentityrenderer instanceof BannerRenderer))
        {
            return null;
        }
        else
        {
            if (blockentityrenderer.getType() == null)
            {
                blockentityrenderer = new BannerRenderer(blockentityrenderdispatcher.getContext());
            }

            if (!(model instanceof BannerModel))
            {
                Config.warn("Not a banner model: " + model);
                return null;
            }
            else
            {
                BannerModel bannermodel = (BannerModel)model;
                return bannermodel.updateRenderer(blockentityrenderer);
            }
        }
    }
}
