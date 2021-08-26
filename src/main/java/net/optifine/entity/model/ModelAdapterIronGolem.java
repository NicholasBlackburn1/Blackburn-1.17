package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.IronGolemModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.IronGolemRenderer;
import net.minecraft.world.entity.EntityType;

public class ModelAdapterIronGolem extends ModelAdapter
{
    public ModelAdapterIronGolem()
    {
        super(EntityType.IRON_GOLEM, "iron_golem", 0.5F);
    }

    public Model makeModel()
    {
        return new IronGolemModel(bakeModelLayer(ModelLayers.IRON_GOLEM));
    }

    public ModelPart getModelRenderer(Model model, String modelPart)
    {
        if (!(model instanceof IronGolemModel))
        {
            return null;
        }
        else
        {
            IronGolemModel irongolemmodel = (IronGolemModel)model;

            if (modelPart.equals("head"))
            {
                return irongolemmodel.root().getChilds("head");
            }
            else if (modelPart.equals("body"))
            {
                return irongolemmodel.root().getChilds("body");
            }
            else if (modelPart.equals("right_arm"))
            {
                return irongolemmodel.root().getChilds("right_arm");
            }
            else if (modelPart.equals("left_arm"))
            {
                return irongolemmodel.root().getChilds("left_arm");
            }
            else if (modelPart.equals("left_leg"))
            {
                return irongolemmodel.root().getChilds("left_leg");
            }
            else
            {
                return modelPart.equals("right_leg") ? irongolemmodel.root().getChilds("right_leg") : null;
            }
        }
    }

    public String[] getModelRendererNames()
    {
        return new String[] {"head", "body", "right_arm", "left_arm", "left_leg", "right_leg"};
    }

    public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize)
    {
        EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        IronGolemRenderer irongolemrenderer = new IronGolemRenderer(entityrenderdispatcher.getContext());
        irongolemrenderer.model = (IronGolemModel)modelBase;
        irongolemrenderer.shadowRadius = shadowSize;
        return irongolemrenderer;
    }
}
