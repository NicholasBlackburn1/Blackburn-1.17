package net.minecraft.world.level.saveddata.maps;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;

public class MapFrame
{
    private final BlockPos pos;
    private final int rotation;
    private final int entityId;

    public MapFrame(BlockPos p_77866_, int p_77867_, int p_77868_)
    {
        this.pos = p_77866_;
        this.rotation = p_77867_;
        this.entityId = p_77868_;
    }

    public static MapFrame load(CompoundTag pNbt)
    {
        BlockPos blockpos = NbtUtils.readBlockPos(pNbt.getCompound("Pos"));
        int i = pNbt.getInt("Rotation");
        int j = pNbt.getInt("EntityId");
        return new MapFrame(blockpos, i, j);
    }

    public CompoundTag save()
    {
        CompoundTag compoundtag = new CompoundTag();
        compoundtag.put("Pos", NbtUtils.writeBlockPos(this.pos));
        compoundtag.putInt("Rotation", this.rotation);
        compoundtag.putInt("EntityId", this.entityId);
        return compoundtag;
    }

    public BlockPos getPos()
    {
        return this.pos;
    }

    public int getRotation()
    {
        return this.rotation;
    }

    public int getEntityId()
    {
        return this.entityId;
    }

    public String getId()
    {
        return frameId(this.pos);
    }

    public static String frameId(BlockPos pPos)
    {
        return "frame-" + pPos.getX() + "," + pPos.getY() + "," + pPos.getZ();
    }
}
