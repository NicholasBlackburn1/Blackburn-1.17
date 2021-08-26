package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import javax.annotation.Nullable;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.ArgumentTypes;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundCommandsPacket implements Packet<ClientGamePacketListener>
{
    private static final byte MASK_TYPE = 3;
    private static final byte FLAG_EXECUTABLE = 4;
    private static final byte FLAG_REDIRECT = 8;
    private static final byte FLAG_CUSTOM_SUGGESTIONS = 16;
    private static final byte TYPE_ROOT = 0;
    private static final byte TYPE_LITERAL = 1;
    private static final byte TYPE_ARGUMENT = 2;
    private final RootCommandNode<SharedSuggestionProvider> root;

    public ClientboundCommandsPacket(RootCommandNode<SharedSuggestionProvider> p_131861_)
    {
        this.root = p_131861_;
    }

    public ClientboundCommandsPacket(FriendlyByteBuf p_178805_)
    {
        List<ClientboundCommandsPacket.Entry> list = p_178805_.readList(ClientboundCommandsPacket::readNode);
        resolveEntries(list);
        int i = p_178805_.readVarInt();
        this.root = (RootCommandNode)(list.get(i)).node;
    }

    public void write(FriendlyByteBuf pBuf)
    {
        Object2IntMap<CommandNode<SharedSuggestionProvider>> object2intmap = enumerateNodes(this.root);
        List<CommandNode<SharedSuggestionProvider>> list = getNodesInIdOrder(object2intmap);
        pBuf.writeCollection(list, (p_178810_, p_178811_) ->
        {
            writeNode(p_178810_, p_178811_, object2intmap);
        });
        pBuf.writeVarInt(object2intmap.get(this.root));
    }

    private static void resolveEntries(List<ClientboundCommandsPacket.Entry> p_178813_)
    {
        List<ClientboundCommandsPacket.Entry> list = Lists.newArrayList(p_178813_);

        while (!list.isEmpty())
        {
            boolean flag = list.removeIf((p_178816_) ->
            {
                return p_178816_.build(p_178813_);
            });

            if (!flag)
            {
                throw new IllegalStateException("Server sent an impossible command tree");
            }
        }
    }

    private static Object2IntMap<CommandNode<SharedSuggestionProvider>> enumerateNodes(RootCommandNode<SharedSuggestionProvider> pRootNode)
    {
        Object2IntMap<CommandNode<SharedSuggestionProvider>> object2intmap = new Object2IntOpenHashMap<>();
        Queue<CommandNode<SharedSuggestionProvider>> queue = Queues.newArrayDeque();
        queue.add(pRootNode);
        CommandNode<SharedSuggestionProvider> commandnode;

        while ((commandnode = queue.poll()) != null)
        {
            if (!object2intmap.containsKey(commandnode))
            {
                int i = object2intmap.size();
                object2intmap.put(commandnode, i);
                queue.addAll(commandnode.getChildren());

                if (commandnode.getRedirect() != null)
                {
                    queue.add(commandnode.getRedirect());
                }
            }
        }

        return object2intmap;
    }

    private static List<CommandNode<SharedSuggestionProvider>> getNodesInIdOrder(Object2IntMap<CommandNode<SharedSuggestionProvider>> p_178807_)
    {
        ObjectArrayList<CommandNode<SharedSuggestionProvider>> objectarraylist = new ObjectArrayList<>(p_178807_.size());
        objectarraylist.size(p_178807_.size());

        for (Object2IntMap.Entry<CommandNode<SharedSuggestionProvider>> entry : Object2IntMaps.fastIterable(p_178807_))
        {
            objectarraylist.set(entry.getIntValue(), entry.getKey());
        }

        return objectarraylist;
    }

    private static ClientboundCommandsPacket.Entry readNode(FriendlyByteBuf pBuffer)
    {
        byte b0 = pBuffer.readByte();
        int[] aint = pBuffer.readVarIntArray();
        int i = (b0 & 8) != 0 ? pBuffer.readVarInt() : 0;
        ArgumentBuilder < SharedSuggestionProvider, ? > argumentbuilder = createBuilder(pBuffer, b0);
        return new ClientboundCommandsPacket.Entry(argumentbuilder, b0, i, aint);
    }

    @Nullable
    private static ArgumentBuilder < SharedSuggestionProvider, ? > createBuilder(FriendlyByteBuf pBuffer, byte pBuf)
    {
        int i = pBuf & 3;

        if (i == 2)
        {
            String s = pBuffer.readUtf();
            ArgumentType<?> argumenttype = ArgumentTypes.deserialize(pBuffer);

            if (argumenttype == null)
            {
                return null;
            }
            else
            {
                RequiredArgumentBuilder < SharedSuggestionProvider, ? > requiredargumentbuilder = RequiredArgumentBuilder.argument(s, argumenttype);

                if ((pBuf & 16) != 0)
                {
                    requiredargumentbuilder.suggests(SuggestionProviders.getProvider(pBuffer.readResourceLocation()));
                }

                return requiredargumentbuilder;
            }
        }
        else
        {
            return i == 1 ? LiteralArgumentBuilder.literal(pBuffer.readUtf()) : null;
        }
    }

    private static void writeNode(FriendlyByteBuf pBuffer, CommandNode<SharedSuggestionProvider> pBuf, Map<CommandNode<SharedSuggestionProvider>, Integer> pNode)
    {
        byte b0 = 0;

        if (pBuf.getRedirect() != null)
        {
            b0 = (byte)(b0 | 8);
        }

        if (pBuf.getCommand() != null)
        {
            b0 = (byte)(b0 | 4);
        }

        if (pBuf instanceof RootCommandNode)
        {
            b0 = (byte)(b0 | 0);
        }
        else if (pBuf instanceof ArgumentCommandNode)
        {
            b0 = (byte)(b0 | 2);

            if (((ArgumentCommandNode)pBuf).getCustomSuggestions() != null)
            {
                b0 = (byte)(b0 | 16);
            }
        }
        else
        {
            if (!(pBuf instanceof LiteralCommandNode))
            {
                throw new UnsupportedOperationException("Unknown node type " + pBuf);
            }

            b0 = (byte)(b0 | 1);
        }

        pBuffer.writeByte(b0);
        pBuffer.writeVarInt(pBuf.getChildren().size());

        for (CommandNode<SharedSuggestionProvider> commandnode : pBuf.getChildren())
        {
            pBuffer.writeVarInt(pNode.get(commandnode));
        }

        if (pBuf.getRedirect() != null)
        {
            pBuffer.writeVarInt(pNode.get(pBuf.getRedirect()));
        }

        if (pBuf instanceof ArgumentCommandNode)
        {
            ArgumentCommandNode < SharedSuggestionProvider, ? > argumentcommandnode = (ArgumentCommandNode)pBuf;
            pBuffer.writeUtf(argumentcommandnode.getName());
            ArgumentTypes.serialize(pBuffer, argumentcommandnode.getType());

            if (argumentcommandnode.getCustomSuggestions() != null)
            {
                pBuffer.writeResourceLocation(SuggestionProviders.getName(argumentcommandnode.getCustomSuggestions()));
            }
        }
        else if (pBuf instanceof LiteralCommandNode)
        {
            pBuffer.writeUtf(((LiteralCommandNode)pBuf).getLiteral());
        }
    }

    public void handle(ClientGamePacketListener pHandler)
    {
        pHandler.handleCommands(this);
    }

    public RootCommandNode<SharedSuggestionProvider> getRoot()
    {
        return this.root;
    }

    static class Entry
    {
        @Nullable
        private final ArgumentBuilder < SharedSuggestionProvider, ? > builder;
        private final byte flags;
        private final int redirect;
        private final int[] children;
        @Nullable
        CommandNode<SharedSuggestionProvider> node;

        Entry(@Nullable ArgumentBuilder < SharedSuggestionProvider, ? > p_131895_, byte p_131896_, int p_131897_, int[] p_131898_)
        {
            this.builder = p_131895_;
            this.flags = p_131896_;
            this.redirect = p_131897_;
            this.children = p_131898_;
        }

        public boolean build(List<ClientboundCommandsPacket.Entry> p_178818_)
        {
            if (this.node == null)
            {
                if (this.builder == null)
                {
                    this.node = new RootCommandNode<>();
                }
                else
                {
                    if ((this.flags & 8) != 0)
                    {
                        if ((p_178818_.get(this.redirect)).node == null)
                        {
                            return false;
                        }

                        this.builder.redirect((p_178818_.get(this.redirect)).node);
                    }

                    if ((this.flags & 4) != 0)
                    {
                        this.builder.executes((p_131906_) ->
                        {
                            return 0;
                        });
                    }

                    this.node = this.builder.build();
                }
            }

            for (int i : this.children)
            {
                if ((p_178818_.get(i)).node == null)
                {
                    return false;
                }
            }

            for (int j : this.children)
            {
                CommandNode<SharedSuggestionProvider> commandnode = (p_178818_.get(j)).node;

                if (!(commandnode instanceof RootCommandNode))
                {
                    this.node.addChild(commandnode);
                }
            }

            return true;
        }
    }
}
