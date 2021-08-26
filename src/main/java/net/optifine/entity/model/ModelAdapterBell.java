package net.optifine.entity.model;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.blockentity.BellRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.optifine.Config;

public class ModelAdapterBell extends ModelAdapter
{
    public ModelAdapterBell()
    {
        super(BlockEntityType.BELL, "bell", 0.0F);
    }

    public Model makeModel()
    {
        return new BellModel();
    }

    public ModelPart getModelRenderer(Model model, String modelPart)
    {
        if (!(model instanceof BellModel))
        {
            return null;
        }
        else
        {
            BellModel bellmodel = (BellModel)model;
            return modelPart.equals("body") ? bellmodel.bellBody : null;
        }
    }

    public String[] getModelRendererNames()
    {
        return new String[] {"body"};
    }

    public IEntityRenderer makeEntityRender(Model model, float shadowSize)
    {
        BlockEntityRenderDispatcher blockentityrenderdispatcher = Config.getMinecraft().getBlockEntityRenderDispatcher();
        BlockEntityRenderer blockentityrenderer = blockentityrenderdispatcher.getRenderer(BlockEntityType.BELL);

        if (!(blockentityrenderer instanceof BellRenderer))
        {
            return null;
        }
        else
        {
            if (blockentityrenderer.getType() == null)
            {
                blockentityrenderer = new BellRenderer(blockentityrenderdispatcher.getContext());
            }

            if (!(model instanceof BellModel))
            {
                Config.warn("Not a bell model: " + model);
                return null;
            }
            else
            {
                BellModel bellmodel = (BellModel)model;
                return bellmodel.updateRenderer(blockentityrenderer);
            }
        }
    }
}
