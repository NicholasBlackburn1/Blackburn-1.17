package net.minecraft.commands.arguments;

import com.google.common.collect.Lists;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;

public class MessageArgument implements ArgumentType<MessageArgument.Message>
{
    private static final Collection<String> EXAMPLES = Arrays.asList("Hello world!", "foo", "@e", "Hello @p :)");

    public static MessageArgument message()
    {
        return new MessageArgument();
    }

    public static Component getMessage(CommandContext<CommandSourceStack> pContext, String pName) throws CommandSyntaxException
    {
        return pContext.getArgument(pName, MessageArgument.Message.class).toComponent(pContext.getSource(), pContext.getSource().hasPermission(2));
    }

    public MessageArgument.Message parse(StringReader p_96834_) throws CommandSyntaxException
    {
        return MessageArgument.Message.parseText(p_96834_, true);
    }

    public Collection<String> getExamples()
    {
        return EXAMPLES;
    }

    public static class Message
    {
        private final String text;
        private final MessageArgument.Part[] parts;

        public Message(String p_96844_, MessageArgument.Part[] p_96845_)
        {
            this.text = p_96844_;
            this.parts = p_96845_;
        }

        public String getText()
        {
            return this.text;
        }

        public MessageArgument.Part[] getParts()
        {
            return this.parts;
        }

        public Component toComponent(CommandSourceStack pSource, boolean pAllowSelectors) throws CommandSyntaxException
        {
            if (this.parts.length != 0 && pAllowSelectors)
            {
                MutableComponent mutablecomponent = new TextComponent(this.text.substring(0, this.parts[0].getStart()));
                int i = this.parts[0].getStart();

                for (MessageArgument.Part messageargument$part : this.parts)
                {
                    Component component = messageargument$part.toComponent(pSource);

                    if (i < messageargument$part.getStart())
                    {
                        mutablecomponent.append(this.text.substring(i, messageargument$part.getStart()));
                    }

                    if (component != null)
                    {
                        mutablecomponent.append(component);
                    }

                    i = messageargument$part.getEnd();
                }

                if (i < this.text.length())
                {
                    mutablecomponent.append(this.text.substring(i, this.text.length()));
                }

                return mutablecomponent;
            }
            else
            {
                return new TextComponent(this.text);
            }
        }

        public static MessageArgument.Message parseText(StringReader pReader, boolean pAllowSelectors) throws CommandSyntaxException
        {
            String s = pReader.getString().substring(pReader.getCursor(), pReader.getTotalLength());

            if (!pAllowSelectors)
            {
                pReader.setCursor(pReader.getTotalLength());
                return new MessageArgument.Message(s, new MessageArgument.Part[0]);
            }
            else
            {
                List<MessageArgument.Part> list = Lists.newArrayList();
                int i = pReader.getCursor();

                while (true)
                {
                    int j;
                    EntitySelector entityselector;

                    while (true)
                    {
                        if (!pReader.canRead())
                        {
                            return new MessageArgument.Message(s, list.toArray(new MessageArgument.Part[list.size()]));
                        }

                        if (pReader.peek() == '@')
                        {
                            j = pReader.getCursor();

                            try
                            {
                                EntitySelectorParser entityselectorparser = new EntitySelectorParser(pReader);
                                entityselector = entityselectorparser.parse();
                                break;
                            }
                            catch (CommandSyntaxException commandsyntaxexception)
                            {
                                if (commandsyntaxexception.getType() != EntitySelectorParser.ERROR_MISSING_SELECTOR_TYPE && commandsyntaxexception.getType() != EntitySelectorParser.ERROR_UNKNOWN_SELECTOR_TYPE)
                                {
                                    throw commandsyntaxexception;
                                }

                                pReader.setCursor(j + 1);
                            }
                        }
                        else
                        {
                            pReader.skip();
                        }
                    }

                    list.add(new MessageArgument.Part(j - i, pReader.getCursor() - i, entityselector));
                }
            }
        }
    }

    public static class Part
    {
        private final int start;
        private final int end;
        private final EntitySelector selector;

        public Part(int p_96856_, int p_96857_, EntitySelector p_96858_)
        {
            this.start = p_96856_;
            this.end = p_96857_;
            this.selector = p_96858_;
        }

        public int getStart()
        {
            return this.start;
        }

        public int getEnd()
        {
            return this.end;
        }

        public EntitySelector getSelector()
        {
            return this.selector;
        }

        @Nullable
        public Component toComponent(CommandSourceStack pSource) throws CommandSyntaxException
        {
            return EntitySelector.joinNames(this.selector.findEntities(pSource));
        }
    }
}
