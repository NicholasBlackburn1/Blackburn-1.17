package net.minecraft.advancements;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AdvancementList
{
    private static final Logger LOGGER = LogManager.getLogger();
    private final Map<ResourceLocation, Advancement> advancements = Maps.newHashMap();
    private final Set<Advancement> roots = Sets.newLinkedHashSet();
    private final Set<Advancement> tasks = Sets.newLinkedHashSet();
    private AdvancementList.Listener listener;

    private void remove(Advancement pIds)
    {
        for (Advancement advancement : pIds.getChildren())
        {
            this.remove(advancement);
        }

        LOGGER.info("Forgot about advancement {}", (Object)pIds.getId());
        this.advancements.remove(pIds.getId());

        if (pIds.getParent() == null)
        {
            this.roots.remove(pIds);

            if (this.listener != null)
            {
                this.listener.onRemoveAdvancementRoot(pIds);
            }
        }
        else
        {
            this.tasks.remove(pIds);

            if (this.listener != null)
            {
                this.listener.onRemoveAdvancementTask(pIds);
            }
        }
    }

    public void remove(Set<ResourceLocation> pIds)
    {
        for (ResourceLocation resourcelocation : pIds)
        {
            Advancement advancement = this.advancements.get(resourcelocation);

            if (advancement == null)
            {
                LOGGER.warn("Told to remove advancement {} but I don't know what that is", (Object)resourcelocation);
            }
            else
            {
                this.remove(advancement);
            }
        }
    }

    public void add(Map<ResourceLocation, Advancement.Builder> pAdvancements)
    {
        Map<ResourceLocation, Advancement.Builder> map = Maps.newHashMap(pAdvancements);

        while (!map.isEmpty())
        {
            boolean flag = false;
            Iterator<Entry<ResourceLocation, Advancement.Builder>> iterator = map.entrySet().iterator();

            while (iterator.hasNext())
            {
                Entry<ResourceLocation, Advancement.Builder> entry = iterator.next();
                ResourceLocation resourcelocation = entry.getKey();
                Advancement.Builder advancement$builder = entry.getValue();

                if (advancement$builder.canBuild(this.advancements::get))
                {
                    Advancement advancement = advancement$builder.build(resourcelocation);
                    this.advancements.put(resourcelocation, advancement);
                    flag = true;
                    iterator.remove();

                    if (advancement.getParent() == null)
                    {
                        this.roots.add(advancement);

                        if (this.listener != null)
                        {
                            this.listener.onAddAdvancementRoot(advancement);
                        }
                    }
                    else
                    {
                        this.tasks.add(advancement);

                        if (this.listener != null)
                        {
                            this.listener.onAddAdvancementTask(advancement);
                        }
                    }
                }
            }

            if (!flag)
            {
                for (Entry<ResourceLocation, Advancement.Builder> entry1 : map.entrySet())
                {
                    LOGGER.error("Couldn't load advancement {}: {}", entry1.getKey(), entry1.getValue());
                }

                break;
            }
        }

        LOGGER.info("Loaded {} advancements", (int)this.advancements.size());
    }

    public void clear()
    {
        this.advancements.clear();
        this.roots.clear();
        this.tasks.clear();

        if (this.listener != null)
        {
            this.listener.onAdvancementsCleared();
        }
    }

    public Iterable<Advancement> getRoots()
    {
        return this.roots;
    }

    public Collection<Advancement> getAllAdvancements()
    {
        return this.advancements.values();
    }

    @Nullable
    public Advancement get(ResourceLocation pId)
    {
        return this.advancements.get(pId);
    }

    public void setListener(@Nullable AdvancementList.Listener pListener)
    {
        this.listener = pListener;

        if (pListener != null)
        {
            for (Advancement advancement : this.roots)
            {
                pListener.onAddAdvancementRoot(advancement);
            }

            for (Advancement advancement1 : this.tasks)
            {
                pListener.onAddAdvancementTask(advancement1);
            }
        }
    }

    public interface Listener
    {
        void onAddAdvancementRoot(Advancement pAdvancement);

        void onRemoveAdvancementRoot(Advancement pAdvancement);

        void onAddAdvancementTask(Advancement pAdvancement);

        void onRemoveAdvancementTask(Advancement pAdvancement);

        void onAdvancementsCleared();
    }
}
