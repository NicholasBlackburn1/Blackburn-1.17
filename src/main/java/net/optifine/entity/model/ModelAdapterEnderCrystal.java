package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EndCrystalRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.EntityType;
import net.optifine.Config;

public class ModelAdapterEnderCrystal extends ModelAdapter
{
    public ModelAdapterEnderCrystal()
    {
        this("end_crystal");
    }

    protected ModelAdapterEnderCrystal(String name)
    {
        super(EntityType.END_CRYSTAL, name, 0.5F);
    }

    public Model makeModel()
    {
        return new EnderCrystalModel();
    }

    public ModelPart getModelRenderer(Model model, String modelPart)
    {
        if (!(model instanceof EnderCrystalModel))
        {
            return null;
        }
        else
        {
            EnderCrystalModel endercrystalmodel = (EnderCrystalModel)model;

            if (modelPart.equals("cube"))
            {
                return endercrystalmodel.cube;
            }
            else if (modelPart.equals("glass"))
            {
                return endercrystalmodel.glass;
            }
            else
            {
                return modelPart.equals("base") ? endercrystalmodel.base : null;
            }
        }
    }

    public String[] getModelRendererNames()
    {
        return new String[] {"cube", "glass", "base"};
    }

    public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize)
    {
        EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        EntityRenderer entityrenderer = (EntityRenderer) entityrenderdispatcher.getEntityRenderMap().get(EntityType.END_CRYSTAL);

        if (!(entityrenderer instanceof EndCrystalRenderer))
        {
            Config.warn("Not an instance of RenderEnderCrystal: " + entityrenderer);
            return null;
        }
        else
        {
            EndCrystalRenderer endcrystalrenderer = (EndCrystalRenderer)entityrenderer;

            if (endcrystalrenderer.getType() == null)
            {
                endcrystalrenderer = new EndCrystalRenderer(entityrenderdispatcher.getContext());
            }

            if (!(modelBase instanceof EnderCrystalModel))
            {
                Config.warn("Not a EnderCrystalModel model: " + modelBase);
                return null;
            }
            else
            {
                EnderCrystalModel endercrystalmodel = (EnderCrystalModel)modelBase;
                endcrystalrenderer = endercrystalmodel.updateRenderer(endcrystalrenderer);
                endcrystalrenderer.shadowRadius = shadowSize;
                return endcrystalrenderer;
            }
        }
    }
}
