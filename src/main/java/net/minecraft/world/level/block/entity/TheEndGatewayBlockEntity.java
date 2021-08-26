package net.minecraft.world.level.block.entity;

import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.worldgen.Features;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.EndGatewayConfiguration;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TheEndGatewayBlockEntity extends TheEndPortalBlockEntity
{
    private static final Logger LOGGER = LogManager.getLogger();
    private static final int SPAWN_TIME = 200;
    private static final int COOLDOWN_TIME = 40;
    private static final int ATTENTION_INTERVAL = 2400;
    private static final int EVENT_COOLDOWN = 1;
    private static final int GATEWAY_HEIGHT_ABOVE_SURFACE = 10;
    private long age;
    private int teleportCooldown;
    @Nullable
    private BlockPos exitPortal;
    private boolean exactTeleport;

    public TheEndGatewayBlockEntity(BlockPos p_155813_, BlockState p_155814_)
    {
        super(BlockEntityType.END_GATEWAY, p_155813_, p_155814_);
    }

    public CompoundTag save(CompoundTag pCompound)
    {
        super.save(pCompound);
        pCompound.putLong("Age", this.age);

        if (this.exitPortal != null)
        {
            pCompound.put("ExitPortal", NbtUtils.writeBlockPos(this.exitPortal));
        }

        if (this.exactTeleport)
        {
            pCompound.putBoolean("ExactTeleport", this.exactTeleport);
        }

        return pCompound;
    }

    public void load(CompoundTag p_155840_)
    {
        super.load(p_155840_);
        this.age = p_155840_.getLong("Age");

        if (p_155840_.contains("ExitPortal", 10))
        {
            BlockPos blockpos = NbtUtils.readBlockPos(p_155840_.getCompound("ExitPortal"));

            if (Level.isInSpawnableBounds(blockpos))
            {
                this.exitPortal = blockpos;
            }
        }

        this.exactTeleport = p_155840_.getBoolean("ExactTeleport");
    }

    public static void beamAnimationTick(Level p_155835_, BlockPos p_155836_, BlockState p_155837_, TheEndGatewayBlockEntity p_155838_)
    {
        ++p_155838_.age;

        if (p_155838_.isCoolingDown())
        {
            --p_155838_.teleportCooldown;
        }
    }

    public static void teleportTick(Level p_155845_, BlockPos p_155846_, BlockState p_155847_, TheEndGatewayBlockEntity p_155848_)
    {
        boolean flag = p_155848_.isSpawning();
        boolean flag1 = p_155848_.isCoolingDown();
        ++p_155848_.age;

        if (flag1)
        {
            --p_155848_.teleportCooldown;
        }
        else
        {
            List<Entity> list = p_155845_.getEntitiesOfClass(Entity.class, new AABB(p_155846_), TheEndGatewayBlockEntity::canEntityTeleport);

            if (!list.isEmpty())
            {
                teleportEntity(p_155845_, p_155846_, p_155847_, list.get(p_155845_.random.nextInt(list.size())), p_155848_);
            }

            if (p_155848_.age % 2400L == 0L)
            {
                triggerCooldown(p_155845_, p_155846_, p_155847_, p_155848_);
            }
        }

        if (flag != p_155848_.isSpawning() || flag1 != p_155848_.isCoolingDown())
        {
            setChanged(p_155845_, p_155846_, p_155847_);
        }
    }

    public static boolean canEntityTeleport(Entity p_59941_)
    {
        return EntitySelector.NO_SPECTATORS.test(p_59941_) && !p_59941_.getRootVehicle().isOnPortalCooldown();
    }

    public boolean isSpawning()
    {
        return this.age < 200L;
    }

    public boolean isCoolingDown()
    {
        return this.teleportCooldown > 0;
    }

    public float getSpawnPercent(float pPartialTicks)
    {
        return Mth.clamp(((float)this.age + pPartialTicks) / 200.0F, 0.0F, 1.0F);
    }

    public float getCooldownPercent(float pPartialTicks)
    {
        return 1.0F - Mth.clamp(((float)this.teleportCooldown - pPartialTicks) / 40.0F, 0.0F, 1.0F);
    }

    @Nullable
    public ClientboundBlockEntityDataPacket getUpdatePacket()
    {
        return new ClientboundBlockEntityDataPacket(this.worldPosition, 8, this.getUpdateTag());
    }

    public CompoundTag getUpdateTag()
    {
        return this.save(new CompoundTag());
    }

    private static void triggerCooldown(Level p_155850_, BlockPos p_155851_, BlockState p_155852_, TheEndGatewayBlockEntity p_155853_)
    {
        if (!p_155850_.isClientSide)
        {
            p_155853_.teleportCooldown = 40;
            p_155850_.blockEvent(p_155851_, p_155852_.getBlock(), 1, 0);
            setChanged(p_155850_, p_155851_, p_155852_);
        }
    }

    public boolean triggerEvent(int pId, int pType)
    {
        if (pId == 1)
        {
            this.teleportCooldown = 40;
            return true;
        }
        else
        {
            return super.triggerEvent(pId, pType);
        }
    }

    public static void teleportEntity(Level p_155829_, BlockPos p_155830_, BlockState p_155831_, Entity p_155832_, TheEndGatewayBlockEntity p_155833_)
    {
        if (p_155829_ instanceof ServerLevel && !p_155833_.isCoolingDown())
        {
            ServerLevel serverlevel = (ServerLevel)p_155829_;
            p_155833_.teleportCooldown = 100;

            if (p_155833_.exitPortal == null && p_155829_.dimension() == Level.END)
            {
                BlockPos blockpos = findOrCreateValidTeleportPos(serverlevel, p_155830_);
                blockpos = blockpos.above(10);
                LOGGER.debug("Creating portal at {}", (Object)blockpos);
                spawnGatewayPortal(serverlevel, blockpos, EndGatewayConfiguration.knownExit(p_155830_, false));
                p_155833_.exitPortal = blockpos;
            }

            if (p_155833_.exitPortal != null)
            {
                BlockPos blockpos1 = p_155833_.exactTeleport ? p_155833_.exitPortal : findExitPosition(p_155829_, p_155833_.exitPortal);
                Entity entity;

                if (p_155832_ instanceof ThrownEnderpearl)
                {
                    Entity entity1 = ((ThrownEnderpearl)p_155832_).getOwner();

                    if (entity1 instanceof ServerPlayer)
                    {
                        CriteriaTriggers.ENTER_BLOCK.trigger((ServerPlayer)entity1, p_155831_);
                    }

                    if (entity1 != null)
                    {
                        entity = entity1;
                        p_155832_.discard();
                    }
                    else
                    {
                        entity = p_155832_;
                    }
                }
                else
                {
                    entity = p_155832_.getRootVehicle();
                }

                entity.setPortalCooldown();
                entity.teleportToWithTicket((double)blockpos1.getX() + 0.5D, (double)blockpos1.getY(), (double)blockpos1.getZ() + 0.5D);
            }

            triggerCooldown(p_155829_, p_155830_, p_155831_, p_155833_);
        }
    }

    private static BlockPos findExitPosition(Level p_155826_, BlockPos p_155827_)
    {
        BlockPos blockpos = findTallestBlock(p_155826_, p_155827_.offset(0, 2, 0), 5, false);
        LOGGER.debug("Best exit position for portal at {} is {}", p_155827_, blockpos);
        return blockpos.above();
    }

    private static BlockPos findOrCreateValidTeleportPos(ServerLevel p_155819_, BlockPos p_155820_)
    {
        Vec3 vec3 = findExitPortalXZPosTentative(p_155819_, p_155820_);
        LevelChunk levelchunk = getChunk(p_155819_, vec3);
        BlockPos blockpos = findValidSpawnInChunk(levelchunk);

        if (blockpos == null)
        {
            blockpos = new BlockPos(vec3.x + 0.5D, 75.0D, vec3.z + 0.5D);
            LOGGER.debug("Failed to find a suitable block to teleport to, spawning an island on {}", (Object)blockpos);
            Features.END_ISLAND.place(p_155819_, p_155819_.getChunkSource().getGenerator(), new Random(blockpos.asLong()), blockpos);
        }
        else
        {
            LOGGER.debug("Found suitable block to teleport to: {}", (Object)blockpos);
        }

        return findTallestBlock(p_155819_, blockpos, 16, true);
    }

    private static Vec3 findExitPortalXZPosTentative(ServerLevel p_155842_, BlockPos p_155843_)
    {
        Vec3 vec3 = (new Vec3((double)p_155843_.getX(), 0.0D, (double)p_155843_.getZ())).normalize();
        int i = 1024;
        Vec3 vec31 = vec3.scale(1024.0D);

        for (int j = 16; !isChunkEmpty(p_155842_, vec31) && j-- > 0; vec31 = vec31.add(vec3.scale(-16.0D)))
        {
            LOGGER.debug("Skipping backwards past nonempty chunk at {}", (Object)vec31);
        }

        for (int k = 16; isChunkEmpty(p_155842_, vec31) && k-- > 0; vec31 = vec31.add(vec3.scale(16.0D)))
        {
            LOGGER.debug("Skipping forward past empty chunk at {}", (Object)vec31);
        }

        LOGGER.debug("Found chunk at {}", (Object)vec31);
        return vec31;
    }

    private static boolean isChunkEmpty(ServerLevel p_155816_, Vec3 p_155817_)
    {
        return getChunk(p_155816_, p_155817_).getHighestSectionPosition() <= p_155816_.getMinBuildHeight();
    }

    private static BlockPos findTallestBlock(BlockGetter pLevel, BlockPos pPos, int pRadius, boolean pAllowBedrock)
    {
        BlockPos blockpos = null;

        for (int i = -pRadius; i <= pRadius; ++i)
        {
            for (int j = -pRadius; j <= pRadius; ++j)
            {
                if (i != 0 || j != 0 || pAllowBedrock)
                {
                    for (int k = pLevel.getMaxBuildHeight() - 1; k > (blockpos == null ? pLevel.getMinBuildHeight() : blockpos.getY()); --k)
                    {
                        BlockPos blockpos1 = new BlockPos(pPos.getX() + i, k, pPos.getZ() + j);
                        BlockState blockstate = pLevel.getBlockState(blockpos1);

                        if (blockstate.isCollisionShapeFullBlock(pLevel, blockpos1) && (pAllowBedrock || !blockstate.is(Blocks.BEDROCK)))
                        {
                            blockpos = blockpos1;
                            break;
                        }
                    }
                }
            }
        }

        return blockpos == null ? pPos : blockpos;
    }

    private static LevelChunk getChunk(Level pLevel, Vec3 pVec3)
    {
        return pLevel.getChunk(Mth.floor(pVec3.x / 16.0D), Mth.floor(pVec3.z / 16.0D));
    }

    @Nullable
    private static BlockPos findValidSpawnInChunk(LevelChunk pChunk)
    {
        ChunkPos chunkpos = pChunk.getPos();
        BlockPos blockpos = new BlockPos(chunkpos.getMinBlockX(), 30, chunkpos.getMinBlockZ());
        int i = pChunk.getHighestSectionPosition() + 16 - 1;
        BlockPos blockpos1 = new BlockPos(chunkpos.getMaxBlockX(), i, chunkpos.getMaxBlockZ());
        BlockPos blockpos2 = null;
        double d0 = 0.0D;

        for (BlockPos blockpos3 : BlockPos.betweenClosed(blockpos, blockpos1))
        {
            BlockState blockstate = pChunk.getBlockState(blockpos3);
            BlockPos blockpos4 = blockpos3.above();
            BlockPos blockpos5 = blockpos3.above(2);

            if (blockstate.is(Blocks.END_STONE) && !pChunk.getBlockState(blockpos4).isCollisionShapeFullBlock(pChunk, blockpos4) && !pChunk.getBlockState(blockpos5).isCollisionShapeFullBlock(pChunk, blockpos5))
            {
                double d1 = blockpos3.distSqr(0.0D, 0.0D, 0.0D, true);

                if (blockpos2 == null || d1 < d0)
                {
                    blockpos2 = blockpos3;
                    d0 = d1;
                }
            }
        }

        return blockpos2;
    }

    private static void spawnGatewayPortal(ServerLevel p_155822_, BlockPos p_155823_, EndGatewayConfiguration p_155824_)
    {
        Feature.END_GATEWAY.configured(p_155824_).place(p_155822_, p_155822_.getChunkSource().getGenerator(), new Random(), p_155823_);
    }

    public boolean shouldRenderFace(Direction pFace)
    {
        return Block.shouldRenderFace(this.getBlockState(), this.level, this.getBlockPos(), pFace, this.getBlockPos().relative(pFace));
    }

    public int getParticleAmount()
    {
        int i = 0;

        for (Direction direction : Direction.values())
        {
            i += this.shouldRenderFace(direction) ? 1 : 0;
        }

        return i;
    }

    public void setExitPosition(BlockPos pExitPortal, boolean p_59957_)
    {
        this.exactTeleport = p_59957_;
        this.exitPortal = pExitPortal;
    }
}
