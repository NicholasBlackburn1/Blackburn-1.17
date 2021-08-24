package net.minecraft.world.level;

public enum TickPriority {
   EXTREMELY_HIGH(-3),
   VERY_HIGH(-2),
   HIGH(-1),
   NORMAL(0),
   LOW(1),
   VERY_LOW(2),
   EXTREMELY_LOW(3);

   private final int value;

   private TickPriority(int p_47362_) {
      this.value = p_47362_;
   }

   public static TickPriority byValue(int p_47365_) {
      for(TickPriority tickpriority : values()) {
         if (tickpriority.value == p_47365_) {
            return tickpriority;
         }
      }

      return p_47365_ < EXTREMELY_HIGH.value ? EXTREMELY_HIGH : EXTREMELY_LOW;
   }

   public int getValue() {
      return this.value;
   }
}