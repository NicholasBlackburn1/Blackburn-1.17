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

public interface StringRepresentable
{
    String getSerializedName();

    static <E extends Enum<E> & StringRepresentable> Codec<E> fromEnum(Supplier<E[]> pElementSupplier, Function <? super String, ? extends E > pNamingFunction)
    {
        E[] ae = (E[])pElementSupplier.get();
        return fromStringResolver((p_144987_) ->
        {
            return p_144987_.ordinal();
        }, (p_144990_) ->
        {
            return ae[p_144990_];
        }, pNamingFunction);
    }

    static <E extends StringRepresentable> Codec<E> fromStringResolver(final ToIntFunction<E> pElementSupplier, final IntFunction<E> pSelectorFunction, final Function <? super String, ? extends E > pNamingFunction)
    {
        return new Codec<E>()
        {
            public <T> DataResult<T> encode(E p_14370_, DynamicOps<T> p_14371_, T p_14372_)
            {
                return p_14371_.compressMaps() ? p_14371_.mergeToPrimitive(p_14372_, p_14371_.createInt(pElementSupplier.applyAsInt(p_14370_))) : p_14371_.mergeToPrimitive(p_14372_, p_14371_.createString(p_14370_.getSerializedName()));
            }
            public <T> DataResult<Pair<E, T>> decode(DynamicOps<T> p_14390_, T p_14391_)
            {
                return p_14390_.compressMaps() ? p_14390_.getNumberValue(p_14391_).flatMap((p_14385_) ->
                {
                    return Optional.ofNullable(pSelectorFunction.apply(p_14385_.intValue())).map(DataResult::success).orElseGet(() -> {
                        return DataResult.error("Unknown element id: " + p_14385_);
                    });
                }).map((p_14388_) ->
                {
                    return Pair.of(p_14388_, p_14390_.empty());
                }) : p_14390_.getStringValue(p_14391_).flatMap((p_14382_) ->
                {
                    return Optional.ofNullable(pNamingFunction.apply(p_14382_)).map(DataResult::success).orElseGet(() -> {
                        return DataResult.error("Unknown element name: " + p_14382_);
                    });
                }).map((p_14375_) ->
                {
                    return Pair.of(p_14375_, p_14390_.empty());
                });
            }
            public String toString()
            {
                return "StringRepresentable[" + pElementSupplier + "]";
            }
        };
    }

    static Keyable m_14357_(final StringRepresentable[] p_14358_)
    {
        return new Keyable()
        {
            public <T> Stream<T> keys(DynamicOps<T> p_14401_)
            {
                return p_14401_.compressMaps() ? IntStream.range(0, p_14358_.length).mapToObj(p_14401_::createInt) : Arrays.stream(p_14358_).map(StringRepresentable::getSerializedName).map(p_14401_::createString);
            }
        };
    }
}
