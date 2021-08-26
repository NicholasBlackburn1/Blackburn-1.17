package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.ParrotModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ParrotRenderer;
import net.minecraft.world.entity.EntityType;

public class ModelAdapterParrot extends ModelAdapter
{
    public ModelAdapterParrot()
    {
        super(EntityType.PARROT, "parrot", 0.3F);
    }

    public Model makeModel()
    {
        return new ParrotModel(bakeModelLayer(ModelLayers.PARROT));
    }

    public ModelPart getModelRenderer(Model model, String modelPart)
    {
        if (!(model instanceof ParrotModel))
        {
            return null;
        }
        else
        {
            ParrotModel parrotmodel = (ParrotModel)model;

            if (modelPart.equals("body"))
            {
                return parrotmodel.root().getChilds("body");
            }
            else if (modelPart.equals("tail"))
            {
                return parrotmodel.root().getChilds("tail");
            }
            else if (modelPart.equals("left_wing"))
            {
                return parrotmodel.root().getChilds("left_wing");
            }
            else if (modelPart.equals("right_wing"))
            {
                return parrotmodel.root().getChilds("right_wing");
            }
            else if (modelPart.equals("head"))
            {
                return parrotmodel.root().getChilds("head");
            }
            else if (modelPart.equals("left_leg"))
            {
                return parrotmodel.root().getChilds("left_leg");
            }
            else
            {
                return modelPart.equals("right_leg") ? parrotmodel.root().getChilds("right_leg") : null;
            }
        }
    }

    public String[] getModelRendererNames()
    {
        return new String[] {"body", "tail", "left_wing", "right_wing", "head", "left_leg", "right_leg"};
    }

    public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize)
    {
        EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        ParrotRenderer parrotrenderer = new ParrotRenderer(entityrenderdispatcher.getContext());
        parrotrenderer.model = (ParrotModel)modelBase;
        parrotrenderer.shadowRadius = shadowSize;
        return parrotrenderer;
    }
}
