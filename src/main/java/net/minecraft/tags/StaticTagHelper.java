package net.minecraft.tags;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class StaticTagHelper<T>
{
    private final ResourceKey <? extends Registry<T >> key;
    private final String directory;
    private TagCollection<T> source = TagCollection.empty();
    private final List<StaticTagHelper.Wrapper<T>> wrappers = Lists.newArrayList();

    public StaticTagHelper(ResourceKey <? extends Registry<T >> pKey, String pDirectory)
    {
        this.key = pKey;
        this.directory = pDirectory;
    }

    public Tag.Named<T> bind(String pId)
    {
        StaticTagHelper.Wrapper<T> wrapper = new StaticTagHelper.Wrapper<>(new ResourceLocation(pId));
        this.wrappers.add(wrapper);
        return wrapper;
    }

    public void resetToEmpty()
    {
        this.source = TagCollection.empty();
        Tag<T> tag = SetTag.empty();
        this.wrappers.forEach((p_13235_) ->
        {
            p_13235_.rebind((p_144335_) -> {
                return tag;
            });
        });
    }

    public void reset(TagContainer pSupplier)
    {
        TagCollection<T> tagcollection = pSupplier.getOrEmpty(this.key);
        this.source = tagcollection;
        this.wrappers.forEach((p_13241_) ->
        {
            p_13241_.rebind(tagcollection::getTag);
        });
    }

    public TagCollection<T> getAllTags()
    {
        return this.source;
    }

    public Set<ResourceLocation> getMissingTags(TagContainer pSupplier)
    {
        TagCollection<T> tagcollection = pSupplier.getOrEmpty(this.key);
        Set<ResourceLocation> set = this.wrappers.stream().map(StaticTagHelper.Wrapper::getName).collect(Collectors.toSet());
        ImmutableSet<ResourceLocation> immutableset = ImmutableSet.copyOf(tagcollection.getAvailableTags());
        return Sets.difference(set, immutableset);
    }

    public ResourceKey <? extends Registry<T >> getKey()
    {
        return this.key;
    }

    public String getDirectory()
    {
        return this.directory;
    }

    protected void addToCollection(TagContainer.Builder pBuilder)
    {
        pBuilder.add(this.key, TagCollection.of(this.wrappers.stream().collect(Collectors.toMap(Tag.Named::getName, (p_144332_) ->
        {
            return p_144332_;
        }))));
    }

    static class Wrapper<T> implements Tag.Named<T>
    {
        @Nullable
        private Tag<T> tag;
        protected final ResourceLocation name;

        Wrapper(ResourceLocation pName)
        {
            this.name = pName;
        }

        public ResourceLocation getName()
        {
            return this.name;
        }

        private Tag<T> resolve()
        {
            if (this.tag == null)
            {
                throw new IllegalStateException("Tag " + this.name + " used before it was bound");
            }
            else
            {
                return this.tag;
            }
        }

        void rebind(Function<ResourceLocation, Tag<T>> pIdToTagFunction)
        {
            this.tag = pIdToTagFunction.apply(this.name);
        }

        public boolean contains(T pElement)
        {
            return this.resolve().contains(pElement);
        }

        public List<T> getValues()
        {
            return this.resolve().getValues();
        }
    }
}
