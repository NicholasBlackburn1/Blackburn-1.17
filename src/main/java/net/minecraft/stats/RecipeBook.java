package net.minecraft.stats;

import com.google.common.collect.Sets;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.item.crafting.Recipe;

public class RecipeBook
{
    protected final Set<ResourceLocation> known = Sets.newHashSet();
    protected final Set<ResourceLocation> highlight = Sets.newHashSet();
    private final RecipeBookSettings bookSettings = new RecipeBookSettings();

    public void copyOverData(RecipeBook pThat)
    {
        this.known.clear();
        this.highlight.clear();
        this.bookSettings.replaceFrom(pThat.bookSettings);
        this.known.addAll(pThat.known);
        this.highlight.addAll(pThat.highlight);
    }

    public void add(Recipe<?> pResourceLocation)
    {
        if (!pResourceLocation.isSpecial())
        {
            this.add(pResourceLocation.getId());
        }
    }

    protected void add(ResourceLocation pResourceLocation)
    {
        this.known.add(pResourceLocation);
    }

    public boolean contains(@Nullable Recipe<?> pId)
    {
        return pId == null ? false : this.known.contains(pId.getId());
    }

    public boolean contains(ResourceLocation pId)
    {
        return this.known.contains(pId);
    }

    public void remove(Recipe<?> pResourceLocation)
    {
        this.remove(pResourceLocation.getId());
    }

    protected void remove(ResourceLocation pResourceLocation)
    {
        this.known.remove(pResourceLocation);
        this.highlight.remove(pResourceLocation);
    }

    public boolean willHighlight(Recipe<?> pRecipe)
    {
        return this.highlight.contains(pRecipe.getId());
    }

    public void removeHighlight(Recipe<?> pRecipe)
    {
        this.highlight.remove(pRecipe.getId());
    }

    public void addHighlight(Recipe<?> pResourceLocation)
    {
        this.addHighlight(pResourceLocation.getId());
    }

    protected void addHighlight(ResourceLocation pResourceLocation)
    {
        this.highlight.add(pResourceLocation);
    }

    public boolean isOpen(RecipeBookType p_12692_)
    {
        return this.bookSettings.isOpen(p_12692_);
    }

    public void setOpen(RecipeBookType p_12694_, boolean p_12695_)
    {
        this.bookSettings.setOpen(p_12694_, p_12695_);
    }

    public boolean isFiltering(RecipeBookMenu<?> p_12690_)
    {
        return this.isFiltering(p_12690_.getRecipeBookType());
    }

    public boolean isFiltering(RecipeBookType p_12705_)
    {
        return this.bookSettings.isFiltering(p_12705_);
    }

    public void setFiltering(RecipeBookType p_12707_, boolean p_12708_)
    {
        this.bookSettings.setFiltering(p_12707_, p_12708_);
    }

    public void setBookSettings(RecipeBookSettings p_12688_)
    {
        this.bookSettings.replaceFrom(p_12688_);
    }

    public RecipeBookSettings getBookSettings()
    {
        return this.bookSettings.copy();
    }

    public void setBookSetting(RecipeBookType p_12697_, boolean p_12698_, boolean p_12699_)
    {
        this.bookSettings.setOpen(p_12697_, p_12698_);
        this.bookSettings.setFiltering(p_12697_, p_12699_);
    }
}
