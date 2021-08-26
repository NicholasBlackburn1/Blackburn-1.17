package net.minecraftforge.client.model.pipeline;

import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.core.Direction;

public interface IVertexConsumer
{
    VertexFormat getVertexFormat();

    void setQuadTint(int var1);

    void setQuadOrientation(Direction var1);

    void setQuadColored();

    void put(int var1, float... var2);
}
