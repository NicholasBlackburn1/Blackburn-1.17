package net.optifine.entity.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.BoatModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.BoatRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.Boat;
import net.optifine.Config;
import net.optifine.reflect.Reflector;

public class ModelAdapterBoat extends ModelAdapter
{
    public ModelAdapterBoat()
    {
        super(EntityType.BOAT, "boat", 0.5F);
    }

    public Model makeModel()
    {
        return new BoatModel(bakeModelLayer(ModelLayers.createBoatModelName(Boat.Type.OAK)));
    }

    public ModelPart getModelRenderer(Model model, String modelPart)
    {
        if (!(model instanceof BoatModel))
        {
            return null;
        }
        else
        {
            BoatModel boatmodel = (BoatModel)model;
            ImmutableList<ModelPart> immutablelist = boatmodel.parts();

            if (immutablelist != null)
            {
                if (modelPart.equals("bottom"))
                {
                    return ModelRendererUtils.getModelRenderer(immutablelist, 0);
                }

                if (modelPart.equals("back"))
                {
                    return ModelRendererUtils.getModelRenderer(immutablelist, 1);
                }

                if (modelPart.equals("front"))
                {
                    return ModelRendererUtils.getModelRenderer(immutablelist, 2);
                }

                if (modelPart.equals("right"))
                {
                    return ModelRendererUtils.getModelRenderer(immutablelist, 3);
                }

                if (modelPart.equals("left"))
                {
                    return ModelRendererUtils.getModelRenderer(immutablelist, 4);
                }

                if (modelPart.equals("paddle_left"))
                {
                    return ModelRendererUtils.getModelRenderer(immutablelist, 5);
                }

                if (modelPart.equals("paddle_right"))
                {
                    return ModelRendererUtils.getModelRenderer(immutablelist, 6);
                }
            }

            return modelPart.equals("bottom_no_water") ? boatmodel.waterPatch() : null;
        }
    }

    public String[] getModelRendererNames()
    {
        return new String[] {"bottom", "back", "front", "right", "left", "paddle_left", "paddle_right", "bottom_no_water"};
    }

    public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize)
    {
        EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        BoatRenderer boatrenderer = new BoatRenderer(entityrenderdispatcher.getContext());

        if (!Reflector.RenderBoat_boatResources.exists())
        {
            Config.warn("Field not found: RenderBoat.boatResources");
            return null;
        }
        else
        {
            Map<Boat.Type, Pair<ResourceLocation, BoatModel>> map = (Map)Reflector.RenderBoat_boatResources.getValue(boatrenderer);

            if (map instanceof ImmutableMap)
            {
                map = new HashMap<>(map);
                Reflector.RenderBoat_boatResources.setValue(boatrenderer, map);
            }

            for (Boat.Type boat$type : new HashSet<>(map.keySet()))
            {
                Pair<ResourceLocation, BoatModel> pair = map.get(boat$type);
                pair = Pair.of(pair.getFirst(), (BoatModel)modelBase);
                map.put(boat$type, pair);
            }

            boatrenderer.shadowRadius = shadowSize;
            return boatrenderer;
        }
    }
}
