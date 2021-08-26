package net.minecraft.server.commands.data;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Function;
import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class EntityDataAccessor implements DataAccessor
{
    private static final SimpleCommandExceptionType ERROR_NO_PLAYERS = new SimpleCommandExceptionType(new TranslatableComponent("commands.data.entity.invalid"));
    public static final Function<String, DataCommands.DataProvider> PROVIDER = (p_139517_) ->
    {
        return new DataCommands.DataProvider()
        {
            public DataAccessor access(CommandContext<CommandSourceStack> pContext) throws CommandSyntaxException
            {
                return new EntityDataAccessor(EntityArgument.getEntity(pContext, p_139517_));
            }
            public ArgumentBuilder < CommandSourceStack, ? > wrap(ArgumentBuilder < CommandSourceStack, ? > pBuilder, Function < ArgumentBuilder < CommandSourceStack, ? >, ArgumentBuilder < CommandSourceStack, ? >> pAction)
            {
                return pBuilder.then(Commands.literal("entity").then(pAction.apply(Commands.argument(p_139517_, EntityArgument.entity()))));
            }
        };
    };
    private final Entity entity;

    public EntityDataAccessor(Entity p_139510_)
    {
        this.entity = p_139510_;
    }

    public void setData(CompoundTag pOther) throws CommandSyntaxException
    {
        if (this.entity instanceof Player)
        {
            throw ERROR_NO_PLAYERS.create();
        }
        else
        {
            UUID uuid = this.entity.getUUID();
            this.entity.load(pOther);
            this.entity.setUUID(uuid);
        }
    }

    public CompoundTag getData()
    {
        return NbtPredicate.getEntityTagToCompare(this.entity);
    }

    public Component getModifiedSuccess()
    {
        return new TranslatableComponent("commands.data.entity.modified", this.entity.getDisplayName());
    }

    public Component getPrintSuccess(Tag pPath)
    {
        return new TranslatableComponent("commands.data.entity.query", this.entity.getDisplayName(), NbtUtils.toPrettyComponent(pPath));
    }

    public Component getPrintSuccess(NbtPathArgument.NbtPath pPath, double pScale, int p_139515_)
    {
        return new TranslatableComponent("commands.data.entity.get", pPath, this.entity.getDisplayName(), String.format(Locale.ROOT, "%.2f", pScale), p_139515_);
    }
}
