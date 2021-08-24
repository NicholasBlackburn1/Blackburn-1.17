package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.ChatVisiblity;

public class ServerboundClientInformationPacket implements Packet<ServerGamePacketListener> {
   public static final int MAX_LANGUAGE_LENGTH = 16;
   private final String language;
   private final int viewDistance;
   private final ChatVisiblity chatVisibility;
   private final boolean chatColors;
   private final int modelCustomisation;
   private final HumanoidArm mainHand;
   private final boolean textFilteringEnabled;

   public ServerboundClientInformationPacket(String p_179552_, int p_179553_, ChatVisiblity p_179554_, boolean p_179555_, int p_179556_, HumanoidArm p_179557_, boolean p_179558_) {
      this.language = p_179552_;
      this.viewDistance = p_179553_;
      this.chatVisibility = p_179554_;
      this.chatColors = p_179555_;
      this.modelCustomisation = p_179556_;
      this.mainHand = p_179557_;
      this.textFilteringEnabled = p_179558_;
   }

   public ServerboundClientInformationPacket(FriendlyByteBuf p_179560_) {
      this.language = p_179560_.readUtf(16);
      this.viewDistance = p_179560_.readByte();
      this.chatVisibility = p_179560_.readEnum(ChatVisiblity.class);
      this.chatColors = p_179560_.readBoolean();
      this.modelCustomisation = p_179560_.readUnsignedByte();
      this.mainHand = p_179560_.readEnum(HumanoidArm.class);
      this.textFilteringEnabled = p_179560_.readBoolean();
   }

   public void write(FriendlyByteBuf p_133884_) {
      p_133884_.writeUtf(this.language);
      p_133884_.writeByte(this.viewDistance);
      p_133884_.writeEnum(this.chatVisibility);
      p_133884_.writeBoolean(this.chatColors);
      p_133884_.writeByte(this.modelCustomisation);
      p_133884_.writeEnum(this.mainHand);
      p_133884_.writeBoolean(this.textFilteringEnabled);
   }

   public void handle(ServerGamePacketListener p_133882_) {
      p_133882_.handleClientInformation(this);
   }

   public String getLanguage() {
      return this.language;
   }

   public int getViewDistance() {
      return this.viewDistance;
   }

   public ChatVisiblity getChatVisibility() {
      return this.chatVisibility;
   }

   public boolean getChatColors() {
      return this.chatColors;
   }

   public int getModelCustomisation() {
      return this.modelCustomisation;
   }

   public HumanoidArm getMainHand() {
      return this.mainHand;
   }

   public boolean isTextFilteringEnabled() {
      return this.textFilteringEnabled;
   }
}