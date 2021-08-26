package net.optifine.entity.model;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.optifine.Config;

public class ModelAdapterTrappedChest extends ModelAdapter
{
    public ModelAdapterTrappedChest()
    {
        super(BlockEntityType.TRAPPED_CHEST, "trapped_chest", 0.0F);
    }

    public Model makeModel()
    {
        return new ChestModel();
    }

    public ModelPart getModelRenderer(Model model, String modelPart)
    {
        if (!(model instanceof ChestModel))
        {
            return null;
        }
        else
        {
            ChestModel chestmodel = (ChestModel)model;

            if (modelPart.equals("lid"))
            {
                return chestmodel.lid;
            }
            else if (modelPart.equals("base"))
            {
                return chestmodel.base;
            }
            else
            {
                return modelPart.equals("knob") ? chestmodel.knob : null;
            }
        }
    }

    public String[] getModelRendererNames()
    {
        return new String[] {"lid", "base", "knob"};
    }

    public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize)
    {
        BlockEntityRenderDispatcher blockentityrenderdispatcher = Config.getMinecraft().getBlockEntityRenderDispatcher();
        BlockEntityRenderer blockentityrenderer = blockentityrenderdispatcher.getRenderer(BlockEntityType.TRAPPED_CHEST);

        if (!(blockentityrenderer instanceof ChestRenderer))
        {
            return null;
        }
        else
        {
            if (blockentityrenderer.getType() == null)
            {
                blockentityrenderer = new ChestRenderer(blockentityrenderdispatcher.getContext());
            }

            if (!(modelBase instanceof ChestModel))
            {
                Config.warn("Not a chest model: " + modelBase);
                return null;
            }
            else
            {
                ChestModel chestmodel = (ChestModel)modelBase;
                return chestmodel.updateRenderer(blockentityrenderer);
            }
        }
    }
}
