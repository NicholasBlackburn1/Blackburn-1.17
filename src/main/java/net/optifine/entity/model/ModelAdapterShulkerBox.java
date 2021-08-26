package net.optifine.entity.model;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.ShulkerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.ShulkerBoxRenderer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.optifine.Config;
import net.optifine.reflect.Reflector;

public class ModelAdapterShulkerBox extends ModelAdapter
{
    public ModelAdapterShulkerBox()
    {
        super(BlockEntityType.SHULKER_BOX, "shulker_box", 0.0F);
    }

    public Model makeModel()
    {
        return new ShulkerModel(bakeModelLayer(ModelLayers.SHULKER));
    }

    public ModelPart getModelRenderer(Model model, String modelPart)
    {
        if (!(model instanceof ShulkerModel))
        {
            return null;
        }
        else
        {
            ShulkerModel shulkermodel = (ShulkerModel)model;

            if (modelPart.equals("base"))
            {
                return (ModelPart)Reflector.ModelShulker_ModelRenderers.getValue(shulkermodel, 0);
            }
            else if (modelPart.equals("lid"))
            {
                return (ModelPart)Reflector.ModelShulker_ModelRenderers.getValue(shulkermodel, 1);
            }
            else
            {
                return modelPart.equals("head") ? (ModelPart)Reflector.ModelShulker_ModelRenderers.getValue(shulkermodel, 2) : null;
            }
        }
    }

    public String[] getModelRendererNames()
    {
        return new String[] {"base", "lid", "head"};
    }

    public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize)
    {
        BlockEntityRenderDispatcher blockentityrenderdispatcher = Config.getMinecraft().getBlockEntityRenderDispatcher();
        BlockEntityRenderer blockentityrenderer = blockentityrenderdispatcher.getRenderer(BlockEntityType.SHULKER_BOX);

        if (!(blockentityrenderer instanceof ShulkerBoxRenderer))
        {
            return null;
        }
        else
        {
            if (blockentityrenderer.getType() == null)
            {
                blockentityrenderer = new ShulkerBoxRenderer(blockentityrenderdispatcher.getContext());
            }

            if (!Reflector.TileEntityShulkerBoxRenderer_model.exists())
            {
                Config.warn("Field not found: TileEntityShulkerBoxRenderer.model");
                return null;
            }
            else
            {
                Reflector.setFieldValue(blockentityrenderer, Reflector.TileEntityShulkerBoxRenderer_model, modelBase);
                return blockentityrenderer;
            }
        }
    }
}
