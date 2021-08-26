package net.minecraft.server.commands.data;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;

public interface DataAccessor
{
    void setData(CompoundTag pOther) throws CommandSyntaxException;

    CompoundTag getData() throws CommandSyntaxException;

    Component getModifiedSuccess();

    Component getPrintSuccess(Tag pPath);

    Component getPrintSuccess(NbtPathArgument.NbtPath pPath, double pScale, int p_139322_);
}
