package net.minecraft.commands.synchronization;

import com.google.common.collect.Maps;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

public class SuggestionProviders
{
    private static final Map<ResourceLocation, SuggestionProvider<SharedSuggestionProvider>> PROVIDERS_BY_NAME = Maps.newHashMap();
    private static final ResourceLocation DEFAULT_NAME = new ResourceLocation("ask_server");
    public static final SuggestionProvider<SharedSuggestionProvider> ASK_SERVER = register(DEFAULT_NAME, (p_121673_, p_121674_) ->
    {
        return p_121673_.getSource().customSuggestion(p_121673_, p_121674_);
    });
    public static final SuggestionProvider<CommandSourceStack> ALL_RECIPES = register(new ResourceLocation("all_recipes"), (p_121670_, p_121671_) ->
    {
        return SharedSuggestionProvider.suggestResource(p_121670_.getSource().getRecipeNames(), p_121671_);
    });
    public static final SuggestionProvider<CommandSourceStack> AVAILABLE_SOUNDS = register(new ResourceLocation("available_sounds"), (p_121667_, p_121668_) ->
    {
        return SharedSuggestionProvider.suggestResource(p_121667_.getSource().getAvailableSoundEvents(), p_121668_);
    });
    public static final SuggestionProvider<CommandSourceStack> AVAILABLE_BIOMES = register(new ResourceLocation("available_biomes"), (p_121662_, p_121663_) ->
    {
        return SharedSuggestionProvider.suggestResource(p_121662_.getSource().registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).keySet(), p_121663_);
    });
    public static final SuggestionProvider<CommandSourceStack> SUMMONABLE_ENTITIES = register(new ResourceLocation("summonable_entities"), (p_121652_, p_121653_) ->
    {
        return SharedSuggestionProvider.suggestResource(Registry.ENTITY_TYPE.stream().filter(EntityType::canSummon), p_121653_, EntityType::getKey, (p_175213_) -> {
            return new TranslatableComponent(Util.makeDescriptionId("entity", EntityType.getKey(p_175213_)));
        });
    });

    public static <S extends SharedSuggestionProvider> SuggestionProvider<S> register(ResourceLocation pId, SuggestionProvider<SharedSuggestionProvider> pProvider)
    {
        if (PROVIDERS_BY_NAME.containsKey(pId))
        {
            throw new IllegalArgumentException("A command suggestion provider is already registered with the name " + pId);
        }
        else
        {
            PROVIDERS_BY_NAME.put(pId, pProvider);
            return (SuggestionProvider<S>)new SuggestionProviders.Wrapper(pId, pProvider);
        }
    }

    public static SuggestionProvider<SharedSuggestionProvider> getProvider(ResourceLocation pId)
    {
        return PROVIDERS_BY_NAME.getOrDefault(pId, ASK_SERVER);
    }

    public static ResourceLocation getName(SuggestionProvider<SharedSuggestionProvider> pProvider)
    {
        return pProvider instanceof SuggestionProviders.Wrapper ? ((SuggestionProviders.Wrapper)pProvider).name : DEFAULT_NAME;
    }

    public static SuggestionProvider<SharedSuggestionProvider> safelySwap(SuggestionProvider<SharedSuggestionProvider> pProvider)
    {
        return pProvider instanceof SuggestionProviders.Wrapper ? pProvider : ASK_SERVER;
    }

    protected static class Wrapper implements SuggestionProvider<SharedSuggestionProvider>
    {
        private final SuggestionProvider<SharedSuggestionProvider> delegate;
        final ResourceLocation name;

        public Wrapper(ResourceLocation p_121678_, SuggestionProvider<SharedSuggestionProvider> p_121679_)
        {
            this.delegate = p_121679_;
            this.name = p_121678_;
        }

        public CompletableFuture<Suggestions> getSuggestions(CommandContext<SharedSuggestionProvider> p_121683_, SuggestionsBuilder p_121684_) throws CommandSyntaxException
        {
            return this.delegate.getSuggestions(p_121683_, p_121684_);
        }
    }
}
