package net.minecraftforge.client.model;

import java.util.Collections;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.model.data.IModelData;

public class ModelDataManager
{
    public static Map<BlockPos, IModelData> getModelData(Level world, ChunkPos pos)
    {
        return Collections.emptyMap();
    }
}
