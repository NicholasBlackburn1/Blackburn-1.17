package net.minecraft.world.level.chunk;

public class OldDataLayer {
   public final byte[] data;
   private final int depthBits;
   private final int depthBitsPlusFour;

   public OldDataLayer(byte[] p_63054_, int p_63055_) {
      this.data = p_63054_;
      this.depthBits = p_63055_;
      this.depthBitsPlusFour = p_63055_ + 4;
   }

   public int get(int p_63057_, int p_63058_, int p_63059_) {
      int i = p_63057_ << this.depthBitsPlusFour | p_63059_ << this.depthBits | p_63058_;
      int j = i >> 1;
      int k = i & 1;
      return k == 0 ? this.data[j] & 15 : this.data[j] >> 4 & 15;
   }
}