package net.optifine.entity.model;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.ConduitRenderer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.optifine.Config;

public class ModelAdapterConduit extends ModelAdapter
{
    public ModelAdapterConduit()
    {
        super(BlockEntityType.CONDUIT, "conduit", 0.0F);
    }

    public Model makeModel()
    {
        return new ConduitModel();
    }

    public ModelPart getModelRenderer(Model model, String modelPart)
    {
        if (!(model instanceof ConduitModel))
        {
            return null;
        }
        else
        {
            ConduitModel conduitmodel = (ConduitModel)model;

            if (modelPart.equals("eye"))
            {
                return conduitmodel.eye;
            }
            else if (modelPart.equals("wind"))
            {
                return conduitmodel.wind;
            }
            else if (modelPart.equals("base"))
            {
                return conduitmodel.base;
            }
            else
            {
                return modelPart.equals("cage") ? conduitmodel.cage : null;
            }
        }
    }

    public String[] getModelRendererNames()
    {
        return new String[] {"eye", "wind", "base", "cage"};
    }

    public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize)
    {
        BlockEntityRenderDispatcher blockentityrenderdispatcher = Config.getMinecraft().getBlockEntityRenderDispatcher();
        BlockEntityRenderer blockentityrenderer = blockentityrenderdispatcher.getRenderer(BlockEntityType.CONDUIT);

        if (!(blockentityrenderer instanceof ConduitRenderer))
        {
            return null;
        }
        else
        {
            if (blockentityrenderer.getType() == null)
            {
                blockentityrenderer = new ConduitRenderer(blockentityrenderdispatcher.getContext());
            }

            if (!(modelBase instanceof ConduitModel))
            {
                Config.warn("Not a conduit model: " + modelBase);
                return null;
            }
            else
            {
                ConduitModel conduitmodel = (ConduitModel)modelBase;
                return conduitmodel.updateRenderer(blockentityrenderer);
            }
        }
    }
}
