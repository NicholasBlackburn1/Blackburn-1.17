package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.VexModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.VexRenderer;
import net.minecraft.world.entity.EntityType;
import net.optifine.Config;
import net.optifine.reflect.Reflector;

public class ModelAdapterVex extends ModelAdapterBiped
{
    public ModelAdapterVex()
    {
        super(EntityType.VEX, "vex", 0.3F);
    }

    public ModelPart getModelRenderer(Model model, String modelPart)
    {
        if (!(model instanceof VexModel))
        {
            return null;
        }
        else
        {
            ModelPart modelpart = super.getModelRenderer(model, modelPart);

            if (modelpart != null)
            {
                return modelpart;
            }
            else
            {
                VexModel vexmodel = (VexModel)model;

                if (modelPart.equals("left_wing"))
                {
                    return (ModelPart)Reflector.getFieldValue(vexmodel, Reflector.ModelVex_leftWing);
                }
                else
                {
                    return modelPart.equals("right_wing") ? (ModelPart)Reflector.getFieldValue(vexmodel, Reflector.ModelVex_rightWing) : null;
                }
            }
        }
    }

    public String[] getModelRendererNames()
    {
        String[] astring = super.getModelRendererNames();
        return (String[])Config.addObjectsToArray(astring, new String[] {"left_wing", "right_wing"});
    }

    public Model makeModel()
    {
        return new VexModel(bakeModelLayer(ModelLayers.VEX));
    }

    public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize)
    {
        EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        VexRenderer vexrenderer = new VexRenderer(entityrenderdispatcher.getContext());
        vexrenderer.model = (VexModel)modelBase;
        vexrenderer.shadowRadius = shadowSize;
        return vexrenderer;
    }
}
