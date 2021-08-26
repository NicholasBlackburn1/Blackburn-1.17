package net.minecraft.commands.arguments;

import com.google.gson.JsonParseException;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import java.util.Arrays;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

public class ComponentArgument implements ArgumentType<Component>
{
    private static final Collection<String> EXAMPLES = Arrays.asList("\"hello world\"", "\"\"", "\"{\"text\":\"hello world\"}", "[\"\"]");
    public static final DynamicCommandExceptionType ERROR_INVALID_JSON = new DynamicCommandExceptionType((p_87121_) ->
    {
        return new TranslatableComponent("argument.component.invalid", p_87121_);
    });

    private ComponentArgument()
    {
    }

    public static Component getComponent(CommandContext<CommandSourceStack> pContext, String pName)
    {
        return pContext.getArgument(pName, Component.class);
    }

    public static ComponentArgument textComponent()
    {
        return new ComponentArgument();
    }

    public Component parse(StringReader p_87116_) throws CommandSyntaxException
    {
        try
        {
            Component component = Component.Serializer.fromJson(p_87116_);

            if (component == null)
            {
                throw ERROR_INVALID_JSON.createWithContext(p_87116_, "empty");
            }
            else
            {
                return component;
            }
        }
        catch (JsonParseException jsonparseexception)
        {
            String s = jsonparseexception.getCause() != null ? jsonparseexception.getCause().getMessage() : jsonparseexception.getMessage();
            throw ERROR_INVALID_JSON.createWithContext(p_87116_, s);
        }
    }

    public Collection<String> getExamples()
    {
        return EXAMPLES;
    }
}
