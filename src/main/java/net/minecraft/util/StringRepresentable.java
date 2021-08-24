package net.minecraft.util;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Keyable;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public interface StringRepresentable {
   String getSerializedName();

   static <E extends Enum<E> & StringRepresentable> Codec<E> fromEnum(Supplier<E[]> p_14351_, Function<? super String, ? extends E> p_14352_) {
      E[] ae = (E[])p_14351_.get();
      return fromStringResolver((p_144987_) -> {
         return p_144987_.ordinal();
      }, (p_144990_) -> {
         return ae[p_144990_];
      }, p_14352_);
   }

   static <E extends StringRepresentable> Codec<E> fromStringResolver(final ToIntFunction<E> p_14354_, final IntFunction<E> p_14355_, final Function<? super String, ? extends E> p_14356_) {
      return new Codec<E>() {
         public <T> DataResult<T> encode(E p_14370_, DynamicOps<T> p_14371_, T p_14372_) {
            return p_14371_.compressMaps() ? p_14371_.mergeToPrimitive(p_14372_, p_14371_.createInt(p_14354_.applyAsInt(p_14370_))) : p_14371_.mergeToPrimitive(p_14372_, p_14371_.createString(p_14370_.getSerializedName()));
         }

         public <T> DataResult<Pair<E, T>> decode(DynamicOps<T> p_14390_, T p_14391_) {
            return p_14390_.compressMaps() ? p_14390_.getNumberValue(p_14391_).flatMap((p_14385_) -> {
               return Optional.ofNullable(p_14355_.apply(p_14385_.intValue())).map(DataResult::success).orElseGet(() -> {
                  return DataResult.error("Unknown element id: " + p_14385_);
               });
            }).map((p_14388_) -> {
               return Pair.of(p_14388_, p_14390_.empty());
            }) : p_14390_.getStringValue(p_14391_).flatMap((p_14382_) -> {
               return Optional.ofNullable(p_14356_.apply(p_14382_)).map(DataResult::success).orElseGet(() -> {
                  return DataResult.error("Unknown element name: " + p_14382_);
               });
            }).map((p_14375_) -> {
               return Pair.of(p_14375_, p_14390_.empty());
            });
         }

         public String toString() {
            return "StringRepresentable[" + p_14354_ + "]";
         }
      };
   }

   static Keyable keys(final StringRepresentable[] p_14358_) {
      return new Keyable() {
         public <T> Stream<T> keys(DynamicOps<T> p_14401_) {
            return p_14401_.compressMaps() ? IntStream.range(0, p_14358_.length).mapToObj(p_14401_::createInt) : Arrays.stream(p_14358_).map(StringRepresentable::getSerializedName).map(p_14401_::createString);
         }
      };
   }
}
