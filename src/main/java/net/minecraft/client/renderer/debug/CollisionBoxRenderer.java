package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CollisionBoxRenderer implements DebugRenderer.SimpleDebugRenderer
{
    private final Minecraft minecraft;
    private double lastUpdateTime = Double.MIN_VALUE;
    private List<VoxelShape> shapes = Collections.emptyList();

    public CollisionBoxRenderer(Minecraft p_113404_)
    {
        this.minecraft = p_113404_;
    }

    public void render(PoseStack pMatrixStack, MultiBufferSource pBuffer, double pCamX, double p_113411_, double pCamY)
    {
        double d0 = (double)Util.getNanos();

        if (d0 - this.lastUpdateTime > 1.0E8D)
        {
            this.lastUpdateTime = d0;
            Entity entity = this.minecraft.gameRenderer.getMainCamera().getEntity();
            this.shapes = entity.level.getCollisions(entity, entity.getBoundingBox().inflate(6.0D), (p_113406_) ->
            {
                return true;
            }).collect(Collectors.toList());
        }

        VertexConsumer vertexconsumer = pBuffer.getBuffer(RenderType.lines());

        for (VoxelShape voxelshape : this.shapes)
        {
            LevelRenderer.renderVoxelShape(pMatrixStack, vertexconsumer, voxelshape, -pCamX, -p_113411_, -pCamY, 1.0F, 1.0F, 1.0F, 1.0F);
        }
    }
}
