package net.minecraft.world.level.levelgen.feature.blockplacers;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;

public class BlockPlacerType<P extends BlockPlacer>
{
    public static final BlockPlacerType<SimpleBlockPlacer> SIMPLE_BLOCK_PLACER = register("simple_block_placer", SimpleBlockPlacer.CODEC);
    public static final BlockPlacerType<DoublePlantPlacer> DOUBLE_PLANT_PLACER = register("double_plant_placer", DoublePlantPlacer.CODEC);
    public static final BlockPlacerType<ColumnPlacer> COLUMN_PLACER = register("column_placer", ColumnPlacer.CODEC);
    private final Codec<P> codec;

    private static <P extends BlockPlacer> BlockPlacerType<P> register(String pName, Codec<P> pCodec)
    {
        return Registry.register(Registry.BLOCK_PLACER_TYPES, pName, new BlockPlacerType<>(pCodec));
    }

    private BlockPlacerType(Codec<P> p_67493_)
    {
        this.codec = p_67493_;
    }

    public Codec<P> codec()
    {
        return this.codec;
    }
}
