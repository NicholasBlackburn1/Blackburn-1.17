package net.minecraft.network.syncher;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.Rotations;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CrudeIncrementalIntIdentityHashBiMap;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class EntityDataSerializers
{
    private static final CrudeIncrementalIntIdentityHashBiMap < EntityDataSerializer<? >> SERIALIZERS = new CrudeIncrementalIntIdentityHashBiMap<>(16);
    public static final EntityDataSerializer<Byte> BYTE = new EntityDataSerializer<Byte>()
    {
        public void write(FriendlyByteBuf pBuf, Byte pValue)
        {
            pBuf.writeByte(pValue);
        }
        public Byte read(FriendlyByteBuf pBuf)
        {
            return pBuf.readByte();
        }
        public Byte copy(Byte pValue)
        {
            return pValue;
        }
    };
    public static final EntityDataSerializer<Integer> INT = new EntityDataSerializer<Integer>()
    {
        public void write(FriendlyByteBuf pBuf, Integer pValue)
        {
            pBuf.writeVarInt(pValue);
        }
        public Integer read(FriendlyByteBuf pBuf)
        {
            return pBuf.readVarInt();
        }
        public Integer copy(Integer pValue)
        {
            return pValue;
        }
    };
    public static final EntityDataSerializer<Float> FLOAT = new EntityDataSerializer<Float>()
    {
        public void write(FriendlyByteBuf pBuf, Float pValue)
        {
            pBuf.writeFloat(pValue);
        }
        public Float read(FriendlyByteBuf pBuf)
        {
            return pBuf.readFloat();
        }
        public Float copy(Float pValue)
        {
            return pValue;
        }
    };
    public static final EntityDataSerializer<String> STRING = new EntityDataSerializer<String>()
    {
        public void write(FriendlyByteBuf pBuf, String pValue)
        {
            pBuf.writeUtf(pValue);
        }
        public String read(FriendlyByteBuf pBuf)
        {
            return pBuf.readUtf();
        }
        public String copy(String pValue)
        {
            return pValue;
        }
    };
    public static final EntityDataSerializer<Component> COMPONENT = new EntityDataSerializer<Component>()
    {
        public void write(FriendlyByteBuf pBuf, Component pValue)
        {
            pBuf.writeComponent(pValue);
        }
        public Component read(FriendlyByteBuf pBuf)
        {
            return pBuf.readComponent();
        }
        public Component copy(Component pValue)
        {
            return pValue;
        }
    };
    public static final EntityDataSerializer<Optional<Component>> OPTIONAL_COMPONENT = new EntityDataSerializer<Optional<Component>>()
    {
        public void write(FriendlyByteBuf pBuf, Optional<Component> pValue)
        {
            if (pValue.isPresent())
            {
                pBuf.writeBoolean(true);
                pBuf.writeComponent(pValue.get());
            }
            else
            {
                pBuf.writeBoolean(false);
            }
        }
        public Optional<Component> read(FriendlyByteBuf pBuf)
        {
            return pBuf.readBoolean() ? Optional.of(pBuf.readComponent()) : Optional.empty();
        }
        public Optional<Component> copy(Optional<Component> pValue)
        {
            return pValue;
        }
    };
    public static final EntityDataSerializer<ItemStack> ITEM_STACK = new EntityDataSerializer<ItemStack>()
    {
        public void write(FriendlyByteBuf pBuf, ItemStack pValue)
        {
            pBuf.writeItem(pValue);
        }
        public ItemStack read(FriendlyByteBuf pBuf)
        {
            return pBuf.readItem();
        }
        public ItemStack copy(ItemStack pValue)
        {
            return pValue.copy();
        }
    };
    public static final EntityDataSerializer<Optional<BlockState>> BLOCK_STATE = new EntityDataSerializer<Optional<BlockState>>()
    {
        public void write(FriendlyByteBuf pBuf, Optional<BlockState> pValue)
        {
            if (pValue.isPresent())
            {
                pBuf.writeVarInt(Block.getId(pValue.get()));
            }
            else
            {
                pBuf.writeVarInt(0);
            }
        }
        public Optional<BlockState> read(FriendlyByteBuf pBuf)
        {
            int i = pBuf.readVarInt();
            return i == 0 ? Optional.empty() : Optional.of(Block.stateById(i));
        }
        public Optional<BlockState> copy(Optional<BlockState> pValue)
        {
            return pValue;
        }
    };
    public static final EntityDataSerializer<Boolean> BOOLEAN = new EntityDataSerializer<Boolean>()
    {
        public void write(FriendlyByteBuf pBuf, Boolean pValue)
        {
            pBuf.writeBoolean(pValue);
        }
        public Boolean read(FriendlyByteBuf pBuf)
        {
            return pBuf.readBoolean();
        }
        public Boolean copy(Boolean pValue)
        {
            return pValue;
        }
    };
    public static final EntityDataSerializer<ParticleOptions> PARTICLE = new EntityDataSerializer<ParticleOptions>()
    {
        public void write(FriendlyByteBuf pBuf, ParticleOptions pValue)
        {
            pBuf.writeVarInt(Registry.PARTICLE_TYPE.getId(pValue.getType()));
            pValue.writeToNetwork(pBuf);
        }
        public ParticleOptions read(FriendlyByteBuf pBuf)
        {
            return this.readParticle(pBuf, Registry.PARTICLE_TYPE.byId(pBuf.readVarInt()));
        }
        private <T extends ParticleOptions> T readParticle(FriendlyByteBuf p_135230_, ParticleType<T> p_135231_)
        {
            return p_135231_.getDeserializer().fromNetwork(p_135231_, p_135230_);
        }
        public ParticleOptions copy(ParticleOptions pValue)
        {
            return pValue;
        }
    };
    public static final EntityDataSerializer<Rotations> ROTATIONS = new EntityDataSerializer<Rotations>()
    {
        public void write(FriendlyByteBuf pBuf, Rotations pValue)
        {
            pBuf.writeFloat(pValue.getX());
            pBuf.writeFloat(pValue.getY());
            pBuf.writeFloat(pValue.getZ());
        }
        public Rotations read(FriendlyByteBuf pBuf)
        {
            return new Rotations(pBuf.readFloat(), pBuf.readFloat(), pBuf.readFloat());
        }
        public Rotations copy(Rotations pValue)
        {
            return pValue;
        }
    };
    public static final EntityDataSerializer<BlockPos> BLOCK_POS = new EntityDataSerializer<BlockPos>()
    {
        public void write(FriendlyByteBuf pBuf, BlockPos pValue)
        {
            pBuf.writeBlockPos(pValue);
        }
        public BlockPos read(FriendlyByteBuf pBuf)
        {
            return pBuf.readBlockPos();
        }
        public BlockPos copy(BlockPos pValue)
        {
            return pValue;
        }
    };
    public static final EntityDataSerializer<Optional<BlockPos>> OPTIONAL_BLOCK_POS = new EntityDataSerializer<Optional<BlockPos>>()
    {
        public void write(FriendlyByteBuf pBuf, Optional<BlockPos> pValue)
        {
            pBuf.writeBoolean(pValue.isPresent());

            if (pValue.isPresent())
            {
                pBuf.writeBlockPos(pValue.get());
            }
        }
        public Optional<BlockPos> read(FriendlyByteBuf pBuf)
        {
            return !pBuf.readBoolean() ? Optional.empty() : Optional.of(pBuf.readBlockPos());
        }
        public Optional<BlockPos> copy(Optional<BlockPos> pValue)
        {
            return pValue;
        }
    };
    public static final EntityDataSerializer<Direction> DIRECTION = new EntityDataSerializer<Direction>()
    {
        public void write(FriendlyByteBuf pBuf, Direction pValue)
        {
            pBuf.writeEnum(pValue);
        }
        public Direction read(FriendlyByteBuf pBuf)
        {
            return pBuf.readEnum(Direction.class);
        }
        public Direction copy(Direction pValue)
        {
            return pValue;
        }
    };
    public static final EntityDataSerializer<Optional<UUID>> OPTIONAL_UUID = new EntityDataSerializer<Optional<UUID>>()
    {
        public void write(FriendlyByteBuf pBuf, Optional<UUID> pValue)
        {
            pBuf.writeBoolean(pValue.isPresent());

            if (pValue.isPresent())
            {
                pBuf.writeUUID(pValue.get());
            }
        }
        public Optional<UUID> read(FriendlyByteBuf pBuf)
        {
            return !pBuf.readBoolean() ? Optional.empty() : Optional.of(pBuf.readUUID());
        }
        public Optional<UUID> copy(Optional<UUID> pValue)
        {
            return pValue;
        }
    };
    public static final EntityDataSerializer<CompoundTag> COMPOUND_TAG = new EntityDataSerializer<CompoundTag>()
    {
        public void write(FriendlyByteBuf pBuf, CompoundTag pValue)
        {
            pBuf.writeNbt(pValue);
        }
        public CompoundTag read(FriendlyByteBuf pBuf)
        {
            return pBuf.readNbt();
        }
        public CompoundTag copy(CompoundTag pValue)
        {
            return pValue.copy();
        }
    };
    public static final EntityDataSerializer<VillagerData> VILLAGER_DATA = new EntityDataSerializer<VillagerData>()
    {
        public void write(FriendlyByteBuf pBuf, VillagerData pValue)
        {
            pBuf.writeVarInt(Registry.VILLAGER_TYPE.getId(pValue.getType()));
            pBuf.writeVarInt(Registry.VILLAGER_PROFESSION.getId(pValue.getProfession()));
            pBuf.writeVarInt(pValue.getLevel());
        }
        public VillagerData read(FriendlyByteBuf pBuf)
        {
            return new VillagerData(Registry.VILLAGER_TYPE.byId(pBuf.readVarInt()), Registry.VILLAGER_PROFESSION.byId(pBuf.readVarInt()), pBuf.readVarInt());
        }
        public VillagerData copy(VillagerData pValue)
        {
            return pValue;
        }
    };
    public static final EntityDataSerializer<OptionalInt> OPTIONAL_UNSIGNED_INT = new EntityDataSerializer<OptionalInt>()
    {
        public void write(FriendlyByteBuf pBuf, OptionalInt pValue)
        {
            pBuf.writeVarInt(pValue.orElse(-1) + 1);
        }
        public OptionalInt read(FriendlyByteBuf pBuf)
        {
            int i = pBuf.readVarInt();
            return i == 0 ? OptionalInt.empty() : OptionalInt.of(i - 1);
        }
        public OptionalInt copy(OptionalInt pValue)
        {
            return pValue;
        }
    };
    public static final EntityDataSerializer<Pose> POSE = new EntityDataSerializer<Pose>()
    {
        public void write(FriendlyByteBuf pBuf, Pose pValue)
        {
            pBuf.writeEnum(pValue);
        }
        public Pose read(FriendlyByteBuf pBuf)
        {
            return pBuf.readEnum(Pose.class);
        }
        public Pose copy(Pose pValue)
        {
            return pValue;
        }
    };

    public static void registerSerializer(EntityDataSerializer<?> pSerializer)
    {
        SERIALIZERS.add(pSerializer);
    }

    @Nullable
    public static EntityDataSerializer<?> getSerializer(int pId)
    {
        return SERIALIZERS.byId(pId);
    }

    public static int getSerializedId(EntityDataSerializer<?> pSerializer)
    {
        return SERIALIZERS.getId(pSerializer);
    }

    private EntityDataSerializers()
    {
    }

    static
    {
        registerSerializer(BYTE);
        registerSerializer(INT);
        registerSerializer(FLOAT);
        registerSerializer(STRING);
        registerSerializer(COMPONENT);
        registerSerializer(OPTIONAL_COMPONENT);
        registerSerializer(ITEM_STACK);
        registerSerializer(BOOLEAN);
        registerSerializer(ROTATIONS);
        registerSerializer(BLOCK_POS);
        registerSerializer(OPTIONAL_BLOCK_POS);
        registerSerializer(DIRECTION);
        registerSerializer(OPTIONAL_UUID);
        registerSerializer(BLOCK_STATE);
        registerSerializer(COMPOUND_TAG);
        registerSerializer(PARTICLE);
        registerSerializer(VILLAGER_DATA);
        registerSerializer(OPTIONAL_UNSIGNED_INT);
        registerSerializer(POSE);
    }
}
