package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.DolphinModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.DolphinRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.EntityType;

public class ModelAdapterDolphin extends ModelAdapter
{
    public ModelAdapterDolphin()
    {
        super(EntityType.DOLPHIN, "dolphin", 0.7F);
    }

    public Model makeModel()
    {
        return new DolphinModel(bakeModelLayer(ModelLayers.DOLPHIN));
    }

    public ModelPart getModelRenderer(Model model, String modelPart)
    {
        if (!(model instanceof DolphinModel))
        {
            return null;
        }
        else
        {
            DolphinModel dolphinmodel = (DolphinModel)model;
            ModelPart modelpart = dolphinmodel.root().getChilds("body");

            if (modelpart == null)
            {
                return null;
            }
            else if (modelPart.equals("body"))
            {
                return modelpart;
            }
            else if (modelPart.equals("back_fin"))
            {
                return modelpart.getChilds("back_fin");
            }
            else if (modelPart.equals("left_fin"))
            {
                return modelpart.getChilds("left_fin");
            }
            else if (modelPart.equals("right_fin"))
            {
                return modelpart.getChilds("right_fin");
            }
            else if (modelPart.equals("tail"))
            {
                return modelpart.getChilds("tail");
            }
            else if (modelPart.equals("tail_fin"))
            {
                return modelpart.getChilds("tail").getChilds("tail_fin");
            }
            else
            {
                return modelPart.equals("head") ? modelpart.getChilds("head") : null;
            }
        }
    }

    public String[] getModelRendererNames()
    {
        return new String[] {"body", "back_fin", "left_fin", "right_fin", "tail", "tail_fin", "head"};
    }

    public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize)
    {
        EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        DolphinRenderer dolphinrenderer = new DolphinRenderer(entityrenderdispatcher.getContext());
        dolphinrenderer.model = (DolphinModel)modelBase;
        dolphinrenderer.shadowRadius = shadowSize;
        return dolphinrenderer;
    }
}
