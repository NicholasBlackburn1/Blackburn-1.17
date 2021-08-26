package net.optifine.entity.model;

import java.util.Iterator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.SheepFurModel;
import net.minecraft.client.model.SheepModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.SheepRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.layers.SheepFurLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.optifine.Config;

public class ModelAdapterSheepWool extends ModelAdapterQuadruped
{
    public ModelAdapterSheepWool()
    {
        super(EntityType.SHEEP, "sheep_wool", 0.7F);
    }

    public Model makeModel()
    {
        return new SheepFurModel(bakeModelLayer(ModelLayers.SHEEP_FUR));
    }

    public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize)
    {
        EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        EntityRenderer entityrenderer = (EntityRenderer) entityrenderdispatcher.getEntityRenderMap().get(EntityType.SHEEP);

        if (!(entityrenderer instanceof SheepRenderer))
        {
            Config.warn("Not a RenderSheep: " + entityrenderer);
            return null;
        }
        else
        {
            if (entityrenderer.getType() == null)
            {
                SheepRenderer sheeprenderer = new SheepRenderer(entityrenderdispatcher.getContext());
                sheeprenderer.model = new SheepModel<>(bakeModelLayer(ModelLayers.SHEEP_FUR));
                sheeprenderer.shadowRadius = 0.7F;
                entityrenderer = sheeprenderer;
            }

            SheepRenderer sheeprenderer1 = (SheepRenderer)entityrenderer;
            List list = sheeprenderer1.getLayerRenderers();
            Iterator iterator = list.iterator();

            while (iterator.hasNext())
            {
                RenderLayer renderlayer = (RenderLayer)iterator.next();

                if (renderlayer instanceof SheepFurLayer)
                {
                    iterator.remove();
                }
            }

            SheepFurLayer sheepfurlayer = new SheepFurLayer(sheeprenderer1, entityrenderdispatcher.getContext().getModelSet());
            sheepfurlayer.model = (SheepFurModel)modelBase;
            sheeprenderer1.addLayer(sheepfurlayer);
            return sheeprenderer1;
        }
    }

    public boolean setTextureLocation(IEntityRenderer er, ResourceLocation textureLocation)
    {
        SheepRenderer sheeprenderer = (SheepRenderer)er;

        for (RenderLayer renderlayer : sheeprenderer.getLayerRenderers())
        {
            if (renderlayer instanceof SheepFurLayer)
            {
                SheepFurLayer sheepfurlayer = (SheepFurLayer)renderlayer;
                sheepfurlayer.model.locationTextureCustom = textureLocation;
            }
        }

        return true;
    }
}
