package net.minecraft.client.gui.screens.social;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.SocialInteractionsService;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PlayerSocialManager {
   private final Minecraft minecraft;
   private final Set<UUID> hiddenPlayers = Sets.newHashSet();
   private final SocialInteractionsService service;
   private final Map<String, UUID> discoveredNamesToUUID = Maps.newHashMap();

   public PlayerSocialManager(Minecraft p_100673_, SocialInteractionsService p_100674_) {
      this.minecraft = p_100673_;
      this.service = p_100674_;
   }

   public void hidePlayer(UUID p_100681_) {
      this.hiddenPlayers.add(p_100681_);
   }

   public void showPlayer(UUID p_100683_) {
      this.hiddenPlayers.remove(p_100683_);
   }

   public boolean shouldHideMessageFrom(UUID p_100685_) {
      return this.isHidden(p_100685_) || this.isBlocked(p_100685_);
   }

   public boolean isHidden(UUID p_100687_) {
      return this.hiddenPlayers.contains(p_100687_);
   }

   public boolean isBlocked(UUID p_100689_) {
      return this.service.isBlockedPlayer(p_100689_);
   }

   public Set<UUID> getHiddenPlayers() {
      return this.hiddenPlayers;
   }

   public UUID getDiscoveredUUID(String p_100679_) {
      return this.discoveredNamesToUUID.getOrDefault(p_100679_, Util.NIL_UUID);
   }

   public void addPlayer(PlayerInfo p_100677_) {
      GameProfile gameprofile = p_100677_.getProfile();
      if (gameprofile.isComplete()) {
         this.discoveredNamesToUUID.put(gameprofile.getName(), gameprofile.getId());
      }

      Screen screen = this.minecraft.screen;
      if (screen instanceof SocialInteractionsScreen) {
         SocialInteractionsScreen socialinteractionsscreen = (SocialInteractionsScreen)screen;
         socialinteractionsscreen.onAddPlayer(p_100677_);
      }

   }

   public void removePlayer(UUID p_100691_) {
      Screen screen = this.minecraft.screen;
      if (screen instanceof SocialInteractionsScreen) {
         SocialInteractionsScreen socialinteractionsscreen = (SocialInteractionsScreen)screen;
         socialinteractionsscreen.onRemovePlayer(p_100691_);
      }

   }
}