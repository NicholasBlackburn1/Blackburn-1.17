package net.minecraft.world.level.border;

import com.google.common.collect.Lists;
import com.mojang.serialization.DynamicLike;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WorldBorder
{
    public static final double MAX_SIZE = 5.9999968E7D;
    private final List<BorderChangeListener> listeners = Lists.newArrayList();
    private double damagePerBlock = 0.2D;
    private double damageSafeZone = 5.0D;
    private int warningTime = 15;
    private int warningBlocks = 5;
    private double centerX;
    private double centerZ;
    int absoluteMaxSize = 29999984;
    private WorldBorder.BorderExtent extent = new WorldBorder.StaticBorderExtent(5.9999968E7D);
    public static final WorldBorder.Settings DEFAULT_SETTINGS = new WorldBorder.Settings(0.0D, 0.0D, 0.2D, 5.0D, 5, 15, 5.9999968E7D, 0L, 0.0D);

    public boolean isWithinBounds(BlockPos pPos)
    {
        return (double)(pPos.getX() + 1) > this.getMinX() && (double)pPos.getX() < this.getMaxX() && (double)(pPos.getZ() + 1) > this.getMinZ() && (double)pPos.getZ() < this.getMaxZ();
    }

    public boolean isWithinBounds(ChunkPos pPos)
    {
        return (double)pPos.getMaxBlockX() > this.getMinX() && (double)pPos.getMinBlockX() < this.getMaxX() && (double)pPos.getMaxBlockZ() > this.getMinZ() && (double)pPos.getMinBlockZ() < this.getMaxZ();
    }

    public boolean isWithinBounds(double pPos, double p_156095_)
    {
        return pPos > this.getMinX() && pPos < this.getMaxX() && p_156095_ > this.getMinZ() && p_156095_ < this.getMaxZ();
    }

    public boolean isWithinBounds(AABB pPos)
    {
        return pPos.maxX > this.getMinX() && pPos.minX < this.getMaxX() && pPos.maxZ > this.getMinZ() && pPos.minZ < this.getMaxZ();
    }

    public double getDistanceToBorder(Entity pX)
    {
        return this.getDistanceToBorder(pX.getX(), pX.getZ());
    }

    public VoxelShape getCollisionShape()
    {
        return this.extent.getCollisionShape();
    }

    public double getDistanceToBorder(double pX, double p_61943_)
    {
        double d0 = p_61943_ - this.getMinZ();
        double d1 = this.getMaxZ() - p_61943_;
        double d2 = pX - this.getMinX();
        double d3 = this.getMaxX() - pX;
        double d4 = Math.min(d2, d3);
        d4 = Math.min(d4, d0);
        return Math.min(d4, d1);
    }

    public BorderStatus getStatus()
    {
        return this.extent.getStatus();
    }

    public double getMinX()
    {
        return this.extent.getMinX();
    }

    public double getMinZ()
    {
        return this.extent.getMinZ();
    }

    public double getMaxX()
    {
        return this.extent.getMaxX();
    }

    public double getMaxZ()
    {
        return this.extent.getMaxZ();
    }

    public double getCenterX()
    {
        return this.centerX;
    }

    public double getCenterZ()
    {
        return this.centerZ;
    }

    public void setCenter(double pX, double p_61951_)
    {
        this.centerX = pX;
        this.centerZ = p_61951_;
        this.extent.onCenterChange();

        for (BorderChangeListener borderchangelistener : this.getListeners())
        {
            borderchangelistener.onBorderCenterSet(this, pX, p_61951_);
        }
    }

    public double getSize()
    {
        return this.extent.getSize();
    }

    public long getLerpRemainingTime()
    {
        return this.extent.getLerpRemainingTime();
    }

    public double getLerpTarget()
    {
        return this.extent.getLerpTarget();
    }

    public void setSize(double pNewSize)
    {
        this.extent = new WorldBorder.StaticBorderExtent(pNewSize);

        for (BorderChangeListener borderchangelistener : this.getListeners())
        {
            borderchangelistener.onBorderSizeSet(this, pNewSize);
        }
    }

    public void lerpSizeBetween(double pOldSize, double p_61921_, long pNewSize)
    {
        this.extent = (WorldBorder.BorderExtent)(pOldSize == p_61921_ ? new WorldBorder.StaticBorderExtent(p_61921_) : new WorldBorder.MovingBorderExtent(pOldSize, p_61921_, pNewSize));

        for (BorderChangeListener borderchangelistener : this.getListeners())
        {
            borderchangelistener.onBorderSizeLerping(this, pOldSize, p_61921_, pNewSize);
        }
    }

    protected List<BorderChangeListener> getListeners()
    {
        return Lists.newArrayList(this.listeners);
    }

    public void addListener(BorderChangeListener pListener)
    {
        this.listeners.add(pListener);
    }

    public void removeListener(BorderChangeListener p_156097_)
    {
        this.listeners.remove(p_156097_);
    }

    public void setAbsoluteMaxSize(int pSize)
    {
        this.absoluteMaxSize = pSize;
        this.extent.onAbsoluteMaxSizeChange();
    }

    public int getAbsoluteMaxSize()
    {
        return this.absoluteMaxSize;
    }

    public double getDamageSafeZone()
    {
        return this.damageSafeZone;
    }

    public void setDamageSafeZone(double pBufferSize)
    {
        this.damageSafeZone = pBufferSize;

        for (BorderChangeListener borderchangelistener : this.getListeners())
        {
            borderchangelistener.onBorderSetDamageSafeZOne(this, pBufferSize);
        }
    }

    public double getDamagePerBlock()
    {
        return this.damagePerBlock;
    }

    public void setDamagePerBlock(double pNewAmount)
    {
        this.damagePerBlock = pNewAmount;

        for (BorderChangeListener borderchangelistener : this.getListeners())
        {
            borderchangelistener.onBorderSetDamagePerBlock(this, pNewAmount);
        }
    }

    public double getLerpSpeed()
    {
        return this.extent.getLerpSpeed();
    }

    public int getWarningTime()
    {
        return this.warningTime;
    }

    public void setWarningTime(int pWarningTime)
    {
        this.warningTime = pWarningTime;

        for (BorderChangeListener borderchangelistener : this.getListeners())
        {
            borderchangelistener.onBorderSetWarningTime(this, pWarningTime);
        }
    }

    public int getWarningBlocks()
    {
        return this.warningBlocks;
    }

    public void setWarningBlocks(int pWarningDistance)
    {
        this.warningBlocks = pWarningDistance;

        for (BorderChangeListener borderchangelistener : this.getListeners())
        {
            borderchangelistener.onBorderSetWarningBlocks(this, pWarningDistance);
        }
    }

    public void tick()
    {
        this.extent = this.extent.update();
    }

    public WorldBorder.Settings createSettings()
    {
        return new WorldBorder.Settings(this);
    }

    public void applySettings(WorldBorder.Settings pSerializer)
    {
        this.setCenter(pSerializer.getCenterX(), pSerializer.getCenterZ());
        this.setDamagePerBlock(pSerializer.getDamagePerBlock());
        this.setDamageSafeZone(pSerializer.getSafeZone());
        this.setWarningBlocks(pSerializer.getWarningBlocks());
        this.setWarningTime(pSerializer.getWarningTime());

        if (pSerializer.getSizeLerpTime() > 0L)
        {
            this.lerpSizeBetween(pSerializer.getSize(), pSerializer.getSizeLerpTarget(), pSerializer.getSizeLerpTime());
        }
        else
        {
            this.setSize(pSerializer.getSize());
        }
    }

    interface BorderExtent
    {
        double getMinX();

        double getMaxX();

        double getMinZ();

        double getMaxZ();

        double getSize();

        double getLerpSpeed();

        long getLerpRemainingTime();

        double getLerpTarget();

        BorderStatus getStatus();

        void onAbsoluteMaxSizeChange();

        void onCenterChange();

        WorldBorder.BorderExtent update();

        VoxelShape getCollisionShape();
    }

    class MovingBorderExtent implements WorldBorder.BorderExtent
    {
        private final double from;
        private final double to;
        private final long lerpEnd;
        private final long lerpBegin;
        private final double lerpDuration;

        MovingBorderExtent(double p_61979_, double p_61980_, long p_61981_)
        {
            this.from = p_61979_;
            this.to = p_61980_;
            this.lerpDuration = (double)p_61981_;
            this.lerpBegin = Util.getMillis();
            this.lerpEnd = this.lerpBegin + p_61981_;
        }

        public double getMinX()
        {
            return Mth.clamp(WorldBorder.this.getCenterX() - this.getSize() / 2.0D, (double)(-WorldBorder.this.absoluteMaxSize), (double)WorldBorder.this.absoluteMaxSize);
        }

        public double getMinZ()
        {
            return Mth.clamp(WorldBorder.this.getCenterZ() - this.getSize() / 2.0D, (double)(-WorldBorder.this.absoluteMaxSize), (double)WorldBorder.this.absoluteMaxSize);
        }

        public double getMaxX()
        {
            return Mth.clamp(WorldBorder.this.getCenterX() + this.getSize() / 2.0D, (double)(-WorldBorder.this.absoluteMaxSize), (double)WorldBorder.this.absoluteMaxSize);
        }

        public double getMaxZ()
        {
            return Mth.clamp(WorldBorder.this.getCenterZ() + this.getSize() / 2.0D, (double)(-WorldBorder.this.absoluteMaxSize), (double)WorldBorder.this.absoluteMaxSize);
        }

        public double getSize()
        {
            double d0 = (double)(Util.getMillis() - this.lerpBegin) / this.lerpDuration;
            return d0 < 1.0D ? Mth.lerp(d0, this.from, this.to) : this.to;
        }

        public double getLerpSpeed()
        {
            return Math.abs(this.from - this.to) / (double)(this.lerpEnd - this.lerpBegin);
        }

        public long getLerpRemainingTime()
        {
            return this.lerpEnd - Util.getMillis();
        }

        public double getLerpTarget()
        {
            return this.to;
        }

        public BorderStatus getStatus()
        {
            return this.to < this.from ? BorderStatus.SHRINKING : BorderStatus.GROWING;
        }

        public void onCenterChange()
        {
        }

        public void onAbsoluteMaxSizeChange()
        {
        }

        public WorldBorder.BorderExtent update()
        {
            return (WorldBorder.BorderExtent)(this.getLerpRemainingTime() <= 0L ? WorldBorder.this.new StaticBorderExtent(this.to) : this);
        }

        public VoxelShape getCollisionShape()
        {
            return Shapes.join(Shapes.INFINITY, Shapes.box(Math.floor(this.getMinX()), Double.NEGATIVE_INFINITY, Math.floor(this.getMinZ()), Math.ceil(this.getMaxX()), Double.POSITIVE_INFINITY, Math.ceil(this.getMaxZ())), BooleanOp.ONLY_FIRST);
        }
    }

    public static class Settings
    {
        private final double centerX;
        private final double centerZ;
        private final double damagePerBlock;
        private final double safeZone;
        private final int warningBlocks;
        private final int warningTime;
        private final double size;
        private final long sizeLerpTime;
        private final double sizeLerpTarget;

        Settings(double p_62011_, double p_62012_, double p_62013_, double p_62014_, int p_62015_, int p_62016_, double p_62017_, long p_62018_, double p_62019_)
        {
            this.centerX = p_62011_;
            this.centerZ = p_62012_;
            this.damagePerBlock = p_62013_;
            this.safeZone = p_62014_;
            this.warningBlocks = p_62015_;
            this.warningTime = p_62016_;
            this.size = p_62017_;
            this.sizeLerpTime = p_62018_;
            this.sizeLerpTarget = p_62019_;
        }

        Settings(WorldBorder p_62032_)
        {
            this.centerX = p_62032_.getCenterX();
            this.centerZ = p_62032_.getCenterZ();
            this.damagePerBlock = p_62032_.getDamagePerBlock();
            this.safeZone = p_62032_.getDamageSafeZone();
            this.warningBlocks = p_62032_.getWarningBlocks();
            this.warningTime = p_62032_.getWarningTime();
            this.size = p_62032_.getSize();
            this.sizeLerpTime = p_62032_.getLerpRemainingTime();
            this.sizeLerpTarget = p_62032_.getLerpTarget();
        }

        public double getCenterX()
        {
            return this.centerX;
        }

        public double getCenterZ()
        {
            return this.centerZ;
        }

        public double getDamagePerBlock()
        {
            return this.damagePerBlock;
        }

        public double getSafeZone()
        {
            return this.safeZone;
        }

        public int getWarningBlocks()
        {
            return this.warningBlocks;
        }

        public int getWarningTime()
        {
            return this.warningTime;
        }

        public double getSize()
        {
            return this.size;
        }

        public long getSizeLerpTime()
        {
            return this.sizeLerpTime;
        }

        public double getSizeLerpTarget()
        {
            return this.sizeLerpTarget;
        }

        public static WorldBorder.Settings read(DynamicLike<?> pDynamic, WorldBorder.Settings pDefaultValue)
        {
            double d0 = pDynamic.get("BorderCenterX").asDouble(pDefaultValue.centerX);
            double d1 = pDynamic.get("BorderCenterZ").asDouble(pDefaultValue.centerZ);
            double d2 = pDynamic.get("BorderSize").asDouble(pDefaultValue.size);
            long i = pDynamic.get("BorderSizeLerpTime").asLong(pDefaultValue.sizeLerpTime);
            double d3 = pDynamic.get("BorderSizeLerpTarget").asDouble(pDefaultValue.sizeLerpTarget);
            double d4 = pDynamic.get("BorderSafeZone").asDouble(pDefaultValue.safeZone);
            double d5 = pDynamic.get("BorderDamagePerBlock").asDouble(pDefaultValue.damagePerBlock);
            int j = pDynamic.get("BorderWarningBlocks").asInt(pDefaultValue.warningBlocks);
            int k = pDynamic.get("BorderWarningTime").asInt(pDefaultValue.warningTime);
            return new WorldBorder.Settings(d0, d1, d5, d4, j, k, d2, i, d3);
        }

        public void write(CompoundTag pNbt)
        {
            pNbt.putDouble("BorderCenterX", this.centerX);
            pNbt.putDouble("BorderCenterZ", this.centerZ);
            pNbt.putDouble("BorderSize", this.size);
            pNbt.putLong("BorderSizeLerpTime", this.sizeLerpTime);
            pNbt.putDouble("BorderSafeZone", this.safeZone);
            pNbt.putDouble("BorderDamagePerBlock", this.damagePerBlock);
            pNbt.putDouble("BorderSizeLerpTarget", this.sizeLerpTarget);
            pNbt.putDouble("BorderWarningBlocks", (double)this.warningBlocks);
            pNbt.putDouble("BorderWarningTime", (double)this.warningTime);
        }
    }

    class StaticBorderExtent implements WorldBorder.BorderExtent
    {
        private final double size;
        private double minX;
        private double minZ;
        private double maxX;
        private double maxZ;
        private VoxelShape shape;

        public StaticBorderExtent(double p_62059_)
        {
            this.size = p_62059_;
            this.updateBox();
        }

        public double getMinX()
        {
            return this.minX;
        }

        public double getMaxX()
        {
            return this.maxX;
        }

        public double getMinZ()
        {
            return this.minZ;
        }

        public double getMaxZ()
        {
            return this.maxZ;
        }

        public double getSize()
        {
            return this.size;
        }

        public BorderStatus getStatus()
        {
            return BorderStatus.STATIONARY;
        }

        public double getLerpSpeed()
        {
            return 0.0D;
        }

        public long getLerpRemainingTime()
        {
            return 0L;
        }

        public double getLerpTarget()
        {
            return this.size;
        }

        private void updateBox()
        {
            this.minX = Mth.clamp(WorldBorder.this.getCenterX() - this.size / 2.0D, (double)(-WorldBorder.this.absoluteMaxSize), (double)WorldBorder.this.absoluteMaxSize);
            this.minZ = Mth.clamp(WorldBorder.this.getCenterZ() - this.size / 2.0D, (double)(-WorldBorder.this.absoluteMaxSize), (double)WorldBorder.this.absoluteMaxSize);
            this.maxX = Mth.clamp(WorldBorder.this.getCenterX() + this.size / 2.0D, (double)(-WorldBorder.this.absoluteMaxSize), (double)WorldBorder.this.absoluteMaxSize);
            this.maxZ = Mth.clamp(WorldBorder.this.getCenterZ() + this.size / 2.0D, (double)(-WorldBorder.this.absoluteMaxSize), (double)WorldBorder.this.absoluteMaxSize);
            this.shape = Shapes.join(Shapes.INFINITY, Shapes.box(Math.floor(this.getMinX()), Double.NEGATIVE_INFINITY, Math.floor(this.getMinZ()), Math.ceil(this.getMaxX()), Double.POSITIVE_INFINITY, Math.ceil(this.getMaxZ())), BooleanOp.ONLY_FIRST);
        }

        public void onAbsoluteMaxSizeChange()
        {
            this.updateBox();
        }

        public void onCenterChange()
        {
            this.updateBox();
        }

        public WorldBorder.BorderExtent update()
        {
            return this;
        }

        public VoxelShape getCollisionShape()
        {
            return this.shape;
        }
    }
}
