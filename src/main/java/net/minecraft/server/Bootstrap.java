package net.minecraft.server;

import java.io.PrintStream;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.SharedConstants;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.selector.options.EntitySelectorOptions;
import net.minecraft.commands.synchronization.ArgumentTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.locale.Language;
import net.minecraft.tags.StaticTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.FireBlock;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Bootstrap
{
    public static final PrintStream STDOUT = System.out;
    private static volatile boolean isBootstrapped;
    private static final Logger LOGGER = LogManager.getLogger();

    public static void bootStrap()
    {
        if (!isBootstrapped)
        {
            isBootstrapped = true;

            if (Registry.REGISTRY.keySet().isEmpty())
            {
                throw new IllegalStateException("Unable to load registries");
            }
            else
            {
                FireBlock.bootStrap();
                ComposterBlock.bootStrap();

                if (EntityType.getKey(EntityType.PLAYER) == null)
                {
                    throw new IllegalStateException("Failed loading EntityTypes");
                }
                else
                {
                    PotionBrewing.bootStrap();
                    EntitySelectorOptions.bootStrap();
                    DispenseItemBehavior.bootStrap();
                    CauldronInteraction.bootStrap();
                    ArgumentTypes.bootStrap();
                    StaticTags.bootStrap();
                    wrapStreams();
                }
            }
        }
    }

    private static <T> void checkTranslations(Iterable<T> pObjects, Function<T, String> pObjectToKeyFunction, Set<String> pTranslationSet)
    {
        Language language = Language.getInstance();
        pObjects.forEach((p_135883_) ->
        {
            String s = pObjectToKeyFunction.apply(p_135883_);

            if (!language.has(s))
            {
                pTranslationSet.add(s);
            }
        });
    }

    private static void checkGameruleTranslations(final Set<String> pTranslations)
    {
        final Language language = Language.getInstance();
        GameRules.visitGameRuleTypes(new GameRules.GameRuleTypeVisitor()
        {
            public <T extends GameRules.Value<T>> void visit(GameRules.Key<T> pKey, GameRules.Type<T> pType)
            {
                if (!language.has(pKey.getDescriptionId()))
                {
                    pTranslations.add(pKey.getId());
                }
            }
        });
    }

    public static Set<String> getMissingTranslations()
    {
        Set<String> set = new TreeSet<>();
        checkTranslations(Registry.ATTRIBUTE, Attribute::getDescriptionId, set);
        checkTranslations(Registry.ENTITY_TYPE, EntityType::getDescriptionId, set);
        checkTranslations(Registry.MOB_EFFECT, MobEffect::getDescriptionId, set);
        checkTranslations(Registry.ITEM, Item::getDescriptionId, set);
        checkTranslations(Registry.ENCHANTMENT, Enchantment::getDescriptionId, set);
        checkTranslations(Registry.BLOCK, Block::getDescriptionId, set);
        checkTranslations(Registry.CUSTOM_STAT, (p_135885_) ->
        {
            return "stat." + p_135885_.toString().replace(':', '.');
        }, set);
        checkGameruleTranslations(set);
        return set;
    }

    public static void checkBootstrapCalled(Supplier<String> p_179913_)
    {
        if (!isBootstrapped)
        {
            throw createBootstrapException(p_179913_);
        }
    }

    private static RuntimeException createBootstrapException(Supplier<String> p_179917_)
    {
        try
        {
            String s = p_179917_.get();
            return new IllegalArgumentException("Not bootstrapped (called from " + s + ")");
        }
        catch (Exception exception)
        {
            RuntimeException runtimeexception = new IllegalArgumentException("Not bootstrapped (failed to resolve location)");
            runtimeexception.addSuppressed(exception);
            return runtimeexception;
        }
    }

    public static void validate()
    {
        checkBootstrapCalled(() ->
        {
            return "validate";
        });

        if (SharedConstants.IS_RUNNING_IN_IDE)
        {
            getMissingTranslations().forEach((p_179915_) ->
            {
                LOGGER.error("Missing translations: {}", (Object)p_179915_);
            });
            Commands.validate();
        }

        DefaultAttributes.validate();
    }

    private static void wrapStreams()
    {
        if (LOGGER.isDebugEnabled())
        {
            System.setErr(new DebugLoggedPrintStream("STDERR", System.err));
            System.setOut(new DebugLoggedPrintStream("STDOUT", STDOUT));
        }
        else
        {
            System.setErr(new LoggedPrintStream("STDERR", System.err));
            System.setOut(new LoggedPrintStream("STDOUT", STDOUT));
        }
    }

    public static void realStdoutPrintln(String pMessage)
    {
        STDOUT.println(pMessage);
    }
}
