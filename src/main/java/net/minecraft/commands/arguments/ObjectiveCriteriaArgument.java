package net.minecraft.commands.arguments;

import com.google.common.collect.Lists;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

public class ObjectiveCriteriaArgument implements ArgumentType<ObjectiveCriteria>
{
    private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo.bar.baz", "minecraft:foo");
    public static final DynamicCommandExceptionType ERROR_INVALID_VALUE = new DynamicCommandExceptionType((p_102569_) ->
    {
        return new TranslatableComponent("argument.criteria.invalid", p_102569_);
    });

    private ObjectiveCriteriaArgument()
    {
    }

    public static ObjectiveCriteriaArgument criteria()
    {
        return new ObjectiveCriteriaArgument();
    }

    public static ObjectiveCriteria getCriteria(CommandContext<CommandSourceStack> pContext, String pName)
    {
        return pContext.getArgument(pName, ObjectiveCriteria.class);
    }

    public ObjectiveCriteria parse(StringReader p_102560_) throws CommandSyntaxException
    {
        int i = p_102560_.getCursor();

        while (p_102560_.canRead() && p_102560_.peek() != ' ')
        {
            p_102560_.skip();
        }

        String s = p_102560_.getString().substring(i, p_102560_.getCursor());
        return ObjectiveCriteria.byName(s).orElseThrow(() ->
        {
            p_102560_.setCursor(i);
            return ERROR_INVALID_VALUE.create(s);
        });
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> p_102572_, SuggestionsBuilder p_102573_)
    {
        List<String> list = Lists.newArrayList(ObjectiveCriteria.getCustomCriteriaNames());

        for (StatType<?> stattype : Registry.STAT_TYPE)
        {
            for (Object object : stattype.getRegistry())
            {
                String s = this.getName(stattype, object);
                list.add(s);
            }
        }

        return SharedSuggestionProvider.suggest(list, p_102573_);
    }

    public <T> String getName(StatType<T> pType, Object pValue)
    {
        return Stat.buildName(pType, (T)pValue);
    }

    public Collection<String> getExamples()
    {
        return EXAMPLES;
    }
}
