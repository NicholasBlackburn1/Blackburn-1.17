package net.minecraft.network.protocol.game;

import com.google.common.collect.Sets;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;

public class ClientboundLoginPacket implements Packet<ClientGamePacketListener> {
   private static final int HARDCORE_FLAG = 8;
   private final int playerId;
   private final long seed;
   private final boolean hardcore;
   private final GameType gameType;
   @Nullable
   private final GameType previousGameType;
   private final Set<ResourceKey<Level>> levels;
   private final RegistryAccess.RegistryHolder registryHolder;
   private final DimensionType dimensionType;
   private final ResourceKey<Level> dimension;
   private final int maxPlayers;
   private final int chunkRadius;
   private final boolean reducedDebugInfo;
   private final boolean showDeathScreen;
   private final boolean isDebug;
   private final boolean isFlat;

   public ClientboundLoginPacket(int p_132377_, GameType p_132378_, @Nullable GameType p_132379_, long p_132380_, boolean p_132381_, Set<ResourceKey<Level>> p_132382_, RegistryAccess.RegistryHolder p_132383_, DimensionType p_132384_, ResourceKey<Level> p_132385_, int p_132386_, int p_132387_, boolean p_132388_, boolean p_132389_, boolean p_132390_, boolean p_132391_) {
      this.playerId = p_132377_;
      this.levels = p_132382_;
      this.registryHolder = p_132383_;
      this.dimensionType = p_132384_;
      this.dimension = p_132385_;
      this.seed = p_132380_;
      this.gameType = p_132378_;
      this.previousGameType = p_132379_;
      this.maxPlayers = p_132386_;
      this.hardcore = p_132381_;
      this.chunkRadius = p_132387_;
      this.reducedDebugInfo = p_132388_;
      this.showDeathScreen = p_132389_;
      this.isDebug = p_132390_;
      this.isFlat = p_132391_;
   }

   public ClientboundLoginPacket(FriendlyByteBuf p_178960_) {
      this.playerId = p_178960_.readInt();
      this.hardcore = p_178960_.readBoolean();
      this.gameType = GameType.byId(p_178960_.readByte());
      this.previousGameType = GameType.byNullableId(p_178960_.readByte());
      this.levels = p_178960_.readCollection(Sets::newHashSetWithExpectedSize, (p_178965_) -> {
         return ResourceKey.create(Registry.DIMENSION_REGISTRY, p_178965_.readResourceLocation());
      });
      this.registryHolder = p_178960_.readWithCodec(RegistryAccess.RegistryHolder.NETWORK_CODEC);
      this.dimensionType = p_178960_.readWithCodec(DimensionType.CODEC).get();
      this.dimension = ResourceKey.create(Registry.DIMENSION_REGISTRY, p_178960_.readResourceLocation());
      this.seed = p_178960_.readLong();
      this.maxPlayers = p_178960_.readVarInt();
      this.chunkRadius = p_178960_.readVarInt();
      this.reducedDebugInfo = p_178960_.readBoolean();
      this.showDeathScreen = p_178960_.readBoolean();
      this.isDebug = p_178960_.readBoolean();
      this.isFlat = p_178960_.readBoolean();
   }

   public void write(FriendlyByteBuf p_132400_) {
      p_132400_.writeInt(this.playerId);
      p_132400_.writeBoolean(this.hardcore);
      p_132400_.writeByte(this.gameType.getId());
      p_132400_.writeByte(GameType.getNullableId(this.previousGameType));
      p_132400_.writeCollection(this.levels, (p_178962_, p_178963_) -> {
         p_178962_.writeResourceLocation(p_178963_.location());
      });
      p_132400_.writeWithCodec(RegistryAccess.RegistryHolder.NETWORK_CODEC, this.registryHolder);
      p_132400_.writeWithCodec(DimensionType.CODEC, () -> {
         return this.dimensionType;
      });
      p_132400_.writeResourceLocation(this.dimension.location());
      p_132400_.writeLong(this.seed);
      p_132400_.writeVarInt(this.maxPlayers);
      p_132400_.writeVarInt(this.chunkRadius);
      p_132400_.writeBoolean(this.reducedDebugInfo);
      p_132400_.writeBoolean(this.showDeathScreen);
      p_132400_.writeBoolean(this.isDebug);
      p_132400_.writeBoolean(this.isFlat);
   }

   public void handle(ClientGamePacketListener p_132397_) {
      p_132397_.handleLogin(this);
   }

   public int getPlayerId() {
      return this.playerId;
   }

   public long getSeed() {
      return this.seed;
   }

   public boolean isHardcore() {
      return this.hardcore;
   }

   public GameType getGameType() {
      return this.gameType;
   }

   @Nullable
   public GameType getPreviousGameType() {
      return this.previousGameType;
   }

   public Set<ResourceKey<Level>> levels() {
      return this.levels;
   }

   public RegistryAccess registryAccess() {
      return this.registryHolder;
   }

   public DimensionType getDimensionType() {
      return this.dimensionType;
   }

   public ResourceKey<Level> getDimension() {
      return this.dimension;
   }

   public int getMaxPlayers() {
      return this.maxPlayers;
   }

   public int getChunkRadius() {
      return this.chunkRadius;
   }

   public boolean isReducedDebugInfo() {
      return this.reducedDebugInfo;
   }

   public boolean shouldShowDeathScreen() {
      return this.showDeathScreen;
   }

   public boolean isDebug() {
      return this.isDebug;
   }

   public boolean isFlat() {
      return this.isFlat;
   }
}