package net.optifine.entity.model;

import java.util.Iterator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.LlamaModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LlamaRenderer;
import net.minecraft.client.renderer.entity.layers.LlamaDecorLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.optifine.Config;
import net.optifine.reflect.Reflector;

public class ModelAdapterLlamaDecor extends ModelAdapterLlama
{
    public ModelAdapterLlamaDecor()
    {
        super(EntityType.LLAMA, "llama_decor", 0.7F);
    }

    protected ModelAdapterLlamaDecor(EntityType entityType, String name, float shadowSize)
    {
        super(entityType, name, shadowSize);
    }

    public Model makeModel()
    {
        return new LlamaModel(bakeModelLayer(ModelLayers.LLAMA_DECOR));
    }

    public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize)
    {
        EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        EntityType entitytype = this.getType().getLeft().get();
        EntityRenderer entityrenderer = (EntityRenderer) entityrenderdispatcher.getEntityRenderMap().get(entitytype);

        if (!(entityrenderer instanceof LlamaRenderer))
        {
            Config.warn("Not a RenderLlama: " + entityrenderer);
            return null;
        }
        else
        {
            if (entityrenderer.getType() == null)
            {
                LlamaRenderer llamarenderer = new LlamaRenderer(entityrenderdispatcher.getContext(), ModelLayers.LLAMA_DECOR);
                llamarenderer.model = new LlamaModel<>(bakeModelLayer(ModelLayers.LLAMA_DECOR));
                llamarenderer.shadowRadius = 0.7F;
                entityrenderer = llamarenderer;
            }

            LlamaRenderer llamarenderer1 = (LlamaRenderer)entityrenderer;
            List list = llamarenderer1.getLayerRenderers();
            Iterator iterator = list.iterator();

            while (iterator.hasNext())
            {
                RenderLayer renderlayer = (RenderLayer)iterator.next();

                if (renderlayer instanceof LlamaDecorLayer)
                {
                    iterator.remove();
                }
            }

            LlamaDecorLayer llamadecorlayer = new LlamaDecorLayer(llamarenderer1, entityrenderdispatcher.getContext().getModelSet());

            if (!Reflector.LayerLlamaDecor_model.exists())
            {
                Config.warn("Field not found: LayerLlamaDecor.model");
                return null;
            }
            else
            {
                Reflector.LayerLlamaDecor_model.setValue(llamadecorlayer, modelBase);
                llamarenderer1.addLayer(llamadecorlayer);
                return llamarenderer1;
            }
        }
    }

    public boolean setTextureLocation(IEntityRenderer er, ResourceLocation textureLocation)
    {
        Model model = ((LlamaRenderer)er).model;
        CustomEntityModels.setTextureTopModelRenderers(this, model, textureLocation);
        return true;
    }
}
