package net.minecraft.tags;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.ImmutableSet.Builder;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public interface TagCollection<T>
{
    Map<ResourceLocation, Tag<T>> getAllTags();

    @Nullable

default Tag<T> getTag(ResourceLocation pId)
    {
        return this.getAllTags().get(pId);
    }

    Tag<T> getTagOrEmpty(ResourceLocation pId);

    @Nullable

default ResourceLocation getId(Tag.Named<T> pTag)
    {
        return pTag.getName();
    }

    @Nullable
    ResourceLocation getId(Tag<T> pTag);

default boolean hasTag(ResourceLocation pId)
    {
        return this.getAllTags().containsKey(pId);
    }

default Collection<ResourceLocation> getAvailableTags()
    {
        return this.getAllTags().keySet();
    }

default Collection<ResourceLocation> getMatchingTags(T pItem)
    {
        List<ResourceLocation> list = Lists.newArrayList();

        for (Entry<ResourceLocation, Tag<T>> entry : this.getAllTags().entrySet())
        {
            if (entry.getValue().contains(pItem))
            {
                list.add(entry.getKey());
            }
        }

        return list;
    }

default TagCollection.NetworkPayload serializeToNetwork(Registry<T> pRegistry)
    {
        Map<ResourceLocation, Tag<T>> map = this.getAllTags();
        Map<ResourceLocation, IntList> map1 = Maps.newHashMapWithExpectedSize(map.size());
        map.forEach((p_144416_, p_144417_) ->
        {
            List<T> list = p_144417_.getValues();
            IntList intlist = new IntArrayList(list.size());

            for (T t : list)
            {
                intlist.add(pRegistry.getId(t));
            }

            map1.put(p_144416_, intlist);
        });
        return new TagCollection.NetworkPayload(map1);
    }

    static <T> TagCollection<T> createFromNetwork(TagCollection.NetworkPayload pPayload, Registry <? extends T > pRegistry)
    {
        Map<ResourceLocation, Tag<T>> map = Maps.newHashMapWithExpectedSize(pPayload.tags.size());
        pPayload.tags.forEach((p_144421_, p_144422_) ->
        {
            Builder<T> builder = ImmutableSet.builder();

            for (int i : p_144422_)
            {
                builder.add(pRegistry.byId(i));
            }

            map.put(p_144421_, Tag.fromSet(builder.build()));
        });
        return of(map);
    }

    static <T> TagCollection<T> empty()
    {
        return of(ImmutableBiMap.of());
    }

    static <T> TagCollection<T> of(Map<ResourceLocation, Tag<T>> pIdTagMap)
    {
        final BiMap<ResourceLocation, Tag<T>> bimap = ImmutableBiMap.copyOf(pIdTagMap);
        return new TagCollection<T>()
        {
            private final Tag<T> empty = SetTag.empty();
            public Tag<T> getTagOrEmpty(ResourceLocation pId)
            {
                return bimap.getOrDefault(pId, this.empty);
            }
            @Nullable
            public ResourceLocation getId(Tag<T> pTag)
            {
                return pTag instanceof Tag.Named ? ((Tag.Named)pTag).getName() : bimap.inverse().get(pTag);
            }
            public Map<ResourceLocation, Tag<T>> getAllTags()
            {
                return bimap;
            }
        };
    }

    public static class NetworkPayload
    {
        final Map<ResourceLocation, IntList> tags;

        NetworkPayload(Map<ResourceLocation, IntList> pTags)
        {
            this.tags = pTags;
        }

        public void write(FriendlyByteBuf pBuffer)
        {
            pBuffer.writeMap(this.tags, FriendlyByteBuf::writeResourceLocation, FriendlyByteBuf::writeIntIdList);
        }

        public static TagCollection.NetworkPayload read(FriendlyByteBuf pBuffer)
        {
            return new TagCollection.NetworkPayload(pBuffer.readMap(FriendlyByteBuf::readResourceLocation, FriendlyByteBuf::readIntIdList));
        }
    }
}
