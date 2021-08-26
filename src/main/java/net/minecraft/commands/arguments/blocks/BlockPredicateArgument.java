package net.minecraft.commands.arguments.blocks;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagContainer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.properties.Property;

public class BlockPredicateArgument implements ArgumentType<BlockPredicateArgument.Result>
{
    private static final Collection<String> EXAMPLES = Arrays.asList("stone", "minecraft:stone", "stone[foo=bar]", "#stone", "#stone[foo=bar]{baz=nbt}");
    private static final DynamicCommandExceptionType ERROR_UNKNOWN_TAG = new DynamicCommandExceptionType((p_115580_) ->
    {
        return new TranslatableComponent("arguments.block.tag.unknown", p_115580_);
    });

    public static BlockPredicateArgument blockPredicate()
    {
        return new BlockPredicateArgument();
    }

    public BlockPredicateArgument.Result parse(StringReader p_115572_) throws CommandSyntaxException
    {
        BlockStateParser blockstateparser = (new BlockStateParser(p_115572_, true)).parse(true);

        if (blockstateparser.getState() != null)
        {
            BlockPredicateArgument.BlockPredicate blockpredicateargument$blockpredicate = new BlockPredicateArgument.BlockPredicate(blockstateparser.getState(), blockstateparser.getProperties().keySet(), blockstateparser.getNbt());
            return (p_115578_) ->
            {
                return blockpredicateargument$blockpredicate;
            };
        }
        else
        {
            ResourceLocation resourcelocation = blockstateparser.getTag();
            return (p_173736_) ->
            {
                Tag<Block> tag = p_173736_.getTagOrThrow(Registry.BLOCK_REGISTRY, resourcelocation, (p_173732_) -> {
                    return ERROR_UNKNOWN_TAG.create(p_173732_.toString());
                });
                return new BlockPredicateArgument.TagPredicate(tag, blockstateparser.getVagueProperties(), blockstateparser.getNbt());
            };
        }
    }

    public static Predicate<BlockInWorld> getBlockPredicate(CommandContext<CommandSourceStack> pContext, String pName) throws CommandSyntaxException
    {
        return pContext.getArgument(pName, BlockPredicateArgument.Result.class).create(pContext.getSource().getServer().getTags());
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> p_115587_, SuggestionsBuilder p_115588_)
    {
        StringReader stringreader = new StringReader(p_115588_.getInput());
        stringreader.setCursor(p_115588_.getStart());
        BlockStateParser blockstateparser = new BlockStateParser(stringreader, true);

        try
        {
            blockstateparser.parse(true);
        }
        catch (CommandSyntaxException commandsyntaxexception)
        {
        }

        return blockstateparser.fillSuggestions(p_115588_, BlockTags.getAllTags());
    }

    public Collection<String> getExamples()
    {
        return EXAMPLES;
    }

    static class BlockPredicate implements Predicate<BlockInWorld>
    {
        private final BlockState state;
        private final Set < Property<? >> properties;
        @Nullable
        private final CompoundTag nbt;

        public BlockPredicate(BlockState p_115595_, Set < Property<? >> p_115596_, @Nullable CompoundTag p_115597_)
        {
            this.state = p_115595_;
            this.properties = p_115596_;
            this.nbt = p_115597_;
        }

        public boolean test(BlockInWorld p_115599_)
        {
            BlockState blockstate = p_115599_.getState();

            if (!blockstate.is(this.state.getBlock()))
            {
                return false;
            }
            else
            {
                for (Property<?> property : this.properties)
                {
                    if (blockstate.getValue(property) != this.state.getValue(property))
                    {
                        return false;
                    }
                }

                if (this.nbt == null)
                {
                    return true;
                }
                else
                {
                    BlockEntity blockentity = p_115599_.getEntity();
                    return blockentity != null && NbtUtils.compareNbt(this.nbt, blockentity.save(new CompoundTag()), true);
                }
            }
        }
    }

    public interface Result
    {
        Predicate<BlockInWorld> create(TagContainer p_115603_) throws CommandSyntaxException;
    }

    static class TagPredicate implements Predicate<BlockInWorld>
    {
        private final Tag<Block> tag;
        @Nullable
        private final CompoundTag nbt;
        private final Map<String, String> vagueProperties;

        TagPredicate(Tag<Block> p_115608_, Map<String, String> p_115609_, @Nullable CompoundTag p_115610_)
        {
            this.tag = p_115608_;
            this.vagueProperties = p_115609_;
            this.nbt = p_115610_;
        }

        public boolean test(BlockInWorld p_115617_)
        {
            BlockState blockstate = p_115617_.getState();

            if (!blockstate.is(this.tag))
            {
                return false;
            }
            else
            {
                for (Entry<String, String> entry : this.vagueProperties.entrySet())
                {
                    Property<?> property = blockstate.getBlock().getStateDefinition().getProperty(entry.getKey());

                    if (property == null)
                    {
                        return false;
                    }

                    Comparable<?> comparable = (Comparable)property.getValue(entry.getValue()).orElse(null);

                    if (comparable == null)
                    {
                        return false;
                    }

                    if (blockstate.getValue(property) != comparable)
                    {
                        return false;
                    }
                }

                if (this.nbt == null)
                {
                    return true;
                }
                else
                {
                    BlockEntity blockentity = p_115617_.getEntity();
                    return blockentity != null && NbtUtils.compareNbt(this.nbt, blockentity.save(new CompoundTag()), true);
                }
            }
        }
    }
}
