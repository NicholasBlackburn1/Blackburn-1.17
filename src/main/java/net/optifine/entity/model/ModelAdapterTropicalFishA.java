package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.TropicalFishModelA;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.TropicalFishRenderer;
import net.minecraft.world.entity.EntityType;
import net.optifine.Config;
import net.optifine.reflect.Reflector;

public class ModelAdapterTropicalFishA extends ModelAdapter
{
    public ModelAdapterTropicalFishA()
    {
        super(EntityType.TROPICAL_FISH, "tropical_fish_a", 0.2F);
    }

    public Model makeModel()
    {
        return new TropicalFishModelA(bakeModelLayer(ModelLayers.TROPICAL_FISH_SMALL));
    }

    public ModelPart getModelRenderer(Model model, String modelPart)
    {
        if (!(model instanceof TropicalFishModelA))
        {
            return null;
        }
        else
        {
            TropicalFishModelA tropicalfishmodela = (TropicalFishModelA)model;

            if (modelPart.equals("body"))
            {
                return tropicalfishmodela.root().getChildModelDeep("body");
            }
            else if (modelPart.equals("tail"))
            {
                return tropicalfishmodela.root().getChildModelDeep("tail");
            }
            else if (modelPart.equals("fin_right"))
            {
                return tropicalfishmodela.root().getChildModelDeep("right_fin");
            }
            else if (modelPart.equals("fin_left"))
            {
                return tropicalfishmodela.root().getChildModelDeep("left_fin");
            }
            else
            {
                return modelPart.equals("fin_top") ? tropicalfishmodela.root().getChildModelDeep("top_fin") : null;
            }
        }
    }

    public String[] getModelRendererNames()
    {
        return new String[] {"body", "tail", "fin_right", "fin_left", "fin_top"};
    }

    public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize)
    {
        EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        EntityRenderer entityrenderer = (EntityRenderer) entityrenderdispatcher.getEntityRenderMap().get(EntityType.TROPICAL_FISH);

        if (!(entityrenderer instanceof TropicalFishRenderer))
        {
            Config.warn("Not a TropicalFishRenderer: " + entityrenderer);
            return null;
        }
        else
        {
            if (entityrenderer.getType() == null)
            {
                TropicalFishRenderer tropicalfishrenderer = new TropicalFishRenderer(entityrenderdispatcher.getContext());
                tropicalfishrenderer.shadowRadius = shadowSize;
                entityrenderer = tropicalfishrenderer;
            }

            TropicalFishRenderer tropicalfishrenderer1 = (TropicalFishRenderer)entityrenderer;

            if (!Reflector.RenderTropicalFish_modelA.exists())
            {
                Config.warn("Model field not found: RenderTropicalFish.modelA");
                return null;
            }
            else
            {
                Reflector.RenderTropicalFish_modelA.setValue(tropicalfishrenderer1, modelBase);
                return tropicalfishrenderer1;
            }
        }
    }
}
