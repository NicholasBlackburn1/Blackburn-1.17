package net.minecraft.world.inventory;

import java.util.Collections;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;

public interface RecipeHolder
{
    void setRecipeUsed(@Nullable Recipe<?> pRecipe);

    @Nullable
    Recipe<?> getRecipeUsed();

default void awardUsedRecipes(Player pPlayer)
    {
        Recipe<?> recipe = this.getRecipeUsed();

        if (recipe != null && !recipe.isSpecial())
        {
            pPlayer.awardRecipes(Collections.singleton(recipe));
            this.setRecipeUsed((Recipe<?>)null);
        }
    }

default boolean setRecipeUsed(Level pRecipe, ServerPlayer p_40137_, Recipe<?> p_40138_)
    {
        if (!p_40138_.isSpecial() && pRecipe.getGameRules().getBoolean(GameRules.RULE_LIMITED_CRAFTING) && !p_40137_.getRecipeBook().contains(p_40138_))
        {
            return false;
        }
        else
        {
            this.setRecipeUsed(p_40138_);
            return true;
        }
    }
}
