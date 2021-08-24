package net.minecraft.nbt;

import java.io.DataInput;
import java.io.IOException;

public interface TagType<T extends Tag> {
   T load(DataInput p_129379_, int p_129380_, NbtAccounter p_129381_) throws IOException;

   default boolean isValue() {
      return false;
   }

   String getName();

   String getPrettyName();

   static TagType<EndTag> createInvalid(final int p_129378_) {
      return new TagType<EndTag>() {
         public EndTag load(DataInput p_129387_, int p_129388_, NbtAccounter p_129389_) {
            throw new IllegalArgumentException("Invalid tag id: " + p_129378_);
         }

         public String getName() {
            return "INVALID[" + p_129378_ + "]";
         }

         public String getPrettyName() {
            return "UNKNOWN_" + p_129378_;
         }
      };
   }
}