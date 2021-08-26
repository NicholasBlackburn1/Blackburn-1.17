package net.minecraft.server.commands.data;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Locale;
import java.util.function.Function;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BlockDataAccessor implements DataAccessor
{
    static final SimpleCommandExceptionType ERROR_NOT_A_BLOCK_ENTITY = new SimpleCommandExceptionType(new TranslatableComponent("commands.data.block.invalid"));
    public static final Function<String, DataCommands.DataProvider> PROVIDER = (p_139305_) ->
    {
        return new DataCommands.DataProvider()
        {
            public DataAccessor access(CommandContext<CommandSourceStack> pContext) throws CommandSyntaxException
            {
                BlockPos blockpos = BlockPosArgument.getLoadedBlockPos(pContext, p_139305_ + "Pos");
                BlockEntity blockentity = pContext.getSource().getLevel().getBlockEntity(blockpos);

                if (blockentity == null)
                {
                    throw BlockDataAccessor.ERROR_NOT_A_BLOCK_ENTITY.create();
                }
                else
                {
                    return new BlockDataAccessor(blockentity, blockpos);
                }
            }
            public ArgumentBuilder < CommandSourceStack, ? > wrap(ArgumentBuilder < CommandSourceStack, ? > pBuilder, Function < ArgumentBuilder < CommandSourceStack, ? >, ArgumentBuilder < CommandSourceStack, ? >> pAction)
            {
                return pBuilder.then(Commands.literal("block").then(pAction.apply(Commands.argument(p_139305_ + "Pos", BlockPosArgument.blockPos()))));
            }
        };
    };
    private final BlockEntity entity;
    private final BlockPos pos;

    public BlockDataAccessor(BlockEntity p_139297_, BlockPos p_139298_)
    {
        this.entity = p_139297_;
        this.pos = p_139298_;
    }

    public void setData(CompoundTag pOther)
    {
        pOther.putInt("x", this.pos.getX());
        pOther.putInt("y", this.pos.getY());
        pOther.putInt("z", this.pos.getZ());
        BlockState blockstate = this.entity.getLevel().getBlockState(this.pos);
        this.entity.load(pOther);
        this.entity.setChanged();
        this.entity.getLevel().sendBlockUpdated(this.pos, blockstate, blockstate, 3);
    }

    public CompoundTag getData()
    {
        return this.entity.save(new CompoundTag());
    }

    public Component getModifiedSuccess()
    {
        return new TranslatableComponent("commands.data.block.modified", this.pos.getX(), this.pos.getY(), this.pos.getZ());
    }

    public Component getPrintSuccess(Tag pPath)
    {
        return new TranslatableComponent("commands.data.block.query", this.pos.getX(), this.pos.getY(), this.pos.getZ(), NbtUtils.toPrettyComponent(pPath));
    }

    public Component getPrintSuccess(NbtPathArgument.NbtPath pPath, double pScale, int p_139303_)
    {
        return new TranslatableComponent("commands.data.block.get", pPath, this.pos.getX(), this.pos.getY(), this.pos.getZ(), String.format(Locale.ROOT, "%.2f", pScale), p_139303_);
    }
}
