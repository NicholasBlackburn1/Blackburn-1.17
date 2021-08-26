package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.PufferfishSmallModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.PufferfishRenderer;
import net.minecraft.world.entity.EntityType;
import net.optifine.Config;
import net.optifine.reflect.Reflector;

public class ModelAdapterPufferFishSmall extends ModelAdapter
{
    public ModelAdapterPufferFishSmall()
    {
        super(EntityType.PUFFERFISH, "puffer_fish_small", 0.2F);
    }

    public Model makeModel()
    {
        return new PufferfishSmallModel(bakeModelLayer(ModelLayers.PUFFERFISH_SMALL));
    }

    public ModelPart getModelRenderer(Model model, String modelPart)
    {
        if (!(model instanceof PufferfishSmallModel))
        {
            return null;
        }
        else
        {
            PufferfishSmallModel pufferfishsmallmodel = (PufferfishSmallModel)model;

            if (modelPart.equals("body"))
            {
                return pufferfishsmallmodel.root().getChildModelDeep("body");
            }
            else if (modelPart.equals("eye_right"))
            {
                return pufferfishsmallmodel.root().getChildModelDeep("right_eye");
            }
            else if (modelPart.equals("eye_left"))
            {
                return pufferfishsmallmodel.root().getChildModelDeep("left_eye");
            }
            else if (modelPart.equals("fin_right"))
            {
                return pufferfishsmallmodel.root().getChildModelDeep("right_fin");
            }
            else if (modelPart.equals("fin_left"))
            {
                return pufferfishsmallmodel.root().getChildModelDeep("left_fin");
            }
            else
            {
                return modelPart.equals("tail") ? pufferfishsmallmodel.root().getChildModelDeep("back_fin") : null;
            }
        }
    }

    public String[] getModelRendererNames()
    {
        return new String[] {"body", "eye_right", "eye_left", "tail", "fin_right", "fin_left"};
    }

    public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize)
    {
        EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        EntityRenderer entityrenderer = (EntityRenderer) entityrenderdispatcher.getEntityRenderMap().get(EntityType.PUFFERFISH);

        if (!(entityrenderer instanceof PufferfishRenderer))
        {
            Config.warn("Not a PufferfishRenderer: " + entityrenderer);
            return null;
        }
        else
        {
            if (entityrenderer.getType() == null)
            {
                PufferfishRenderer pufferfishrenderer = new PufferfishRenderer(entityrenderdispatcher.getContext());
                pufferfishrenderer.shadowRadius = shadowSize;
                entityrenderer = pufferfishrenderer;
            }

            PufferfishRenderer pufferfishrenderer1 = (PufferfishRenderer)entityrenderer;

            if (!Reflector.RenderPufferfish_modelSmall.exists())
            {
                Config.warn("Model field not found: RenderPufferfish.modelSmall");
                return null;
            }
            else
            {
                Reflector.RenderPufferfish_modelSmall.setValue(pufferfishrenderer1, modelBase);
                return pufferfishrenderer1;
            }
        }
    }
}
