package net.minecraft.resources;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import java.util.Optional;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;

public class RegistryWriteOps<T> extends DelegatingOps<T>
{
    private final RegistryAccess registryAccess;

    public static <T> RegistryWriteOps<T> create(DynamicOps<T> pOps, RegistryAccess pDynamicRegistries)
    {
        return new RegistryWriteOps<>(pOps, pDynamicRegistries);
    }

    private RegistryWriteOps(DynamicOps<T> p_135765_, RegistryAccess p_135766_)
    {
        super(p_135765_);
        this.registryAccess = p_135766_;
    }

    protected <E> DataResult<T> encode(E pInstance, T pPrefix, ResourceKey <? extends Registry<E >> pRegistryKey, Codec<E> pMapCodec)
    {
        Optional<WritableRegistry<E>> optional = this.registryAccess.ownedRegistry(pRegistryKey);

        if (optional.isPresent())
        {
            WritableRegistry<E> writableregistry = optional.get();
            Optional<ResourceKey<E>> optional1 = writableregistry.getResourceKey(pInstance);

            if (optional1.isPresent())
            {
                ResourceKey<E> resourcekey = optional1.get();
                return ResourceLocation.CODEC.encode(resourcekey.location(), this.delegate, pPrefix);
            }
        }

        return pMapCodec.encode(pInstance, this, pPrefix);
    }
}
