package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.WitherBossModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.WitherBossRenderer;
import net.minecraft.world.entity.EntityType;

public class ModelAdapterWither extends ModelAdapter
{
    public ModelAdapterWither()
    {
        super(EntityType.WITHER, "wither", 0.5F);
    }

    public Model makeModel()
    {
        return new WitherBossModel(bakeModelLayer(ModelLayers.WITHER));
    }

    public ModelPart getModelRenderer(Model model, String modelPart)
    {
        if (!(model instanceof WitherBossModel))
        {
            return null;
        }
        else
        {
            WitherBossModel witherbossmodel = (WitherBossModel)model;

            if (modelPart.equals("body1"))
            {
                return witherbossmodel.root().getChildModelDeep("shoulders");
            }
            else if (modelPart.equals("body2"))
            {
                return witherbossmodel.root().getChildModelDeep("ribcage");
            }
            else if (modelPart.equals("body3"))
            {
                return witherbossmodel.root().getChildModelDeep("tail");
            }
            else if (modelPart.equals("head1"))
            {
                return witherbossmodel.root().getChildModelDeep("center_head");
            }
            else if (modelPart.equals("head2"))
            {
                return witherbossmodel.root().getChildModelDeep("right_head");
            }
            else
            {
                return modelPart.equals("head3") ? witherbossmodel.root().getChildModelDeep("left_head") : null;
            }
        }
    }

    public String[] getModelRendererNames()
    {
        return new String[] {"body1", "body2", "body3", "head1", "head2", "head3"};
    }

    public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize)
    {
        EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        WitherBossRenderer witherbossrenderer = new WitherBossRenderer(entityrenderdispatcher.getContext());
        witherbossrenderer.model = (WitherBossModel)modelBase;
        witherbossrenderer.shadowRadius = shadowSize;
        return witherbossrenderer;
    }
}
