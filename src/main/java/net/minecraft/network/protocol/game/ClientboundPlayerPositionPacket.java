package net.minecraft.network.protocol.game;

import java.util.EnumSet;
import java.util.Set;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundPlayerPositionPacket implements Packet<ClientGamePacketListener>
{
    private final double x;
    private final double y;
    private final double z;
    private final float yRot;
    private final float xRot;
    private final Set<ClientboundPlayerPositionPacket.RelativeArgument> relativeArguments;
    private final int id;
    private final boolean dismountVehicle;

    public ClientboundPlayerPositionPacket(double p_179149_, double p_179150_, double p_179151_, float p_179152_, float p_179153_, Set<ClientboundPlayerPositionPacket.RelativeArgument> p_179154_, int p_179155_, boolean p_179156_)
    {
        this.x = p_179149_;
        this.y = p_179150_;
        this.z = p_179151_;
        this.yRot = p_179152_;
        this.xRot = p_179153_;
        this.relativeArguments = p_179154_;
        this.id = p_179155_;
        this.dismountVehicle = p_179156_;
    }

    public ClientboundPlayerPositionPacket(FriendlyByteBuf p_179158_)
    {
        this.x = p_179158_.readDouble();
        this.y = p_179158_.readDouble();
        this.z = p_179158_.readDouble();
        this.yRot = p_179158_.readFloat();
        this.xRot = p_179158_.readFloat();
        this.relativeArguments = ClientboundPlayerPositionPacket.RelativeArgument.unpack(p_179158_.readUnsignedByte());
        this.id = p_179158_.readVarInt();
        this.dismountVehicle = p_179158_.readBoolean();
    }

    public void write(FriendlyByteBuf pBuf)
    {
        pBuf.writeDouble(this.x);
        pBuf.writeDouble(this.y);
        pBuf.writeDouble(this.z);
        pBuf.writeFloat(this.yRot);
        pBuf.writeFloat(this.xRot);
        pBuf.writeByte(ClientboundPlayerPositionPacket.RelativeArgument.pack(this.relativeArguments));
        pBuf.writeVarInt(this.id);
        pBuf.writeBoolean(this.dismountVehicle);
    }

    public void handle(ClientGamePacketListener pHandler)
    {
        pHandler.handleMovePlayer(this);
    }

    public double getX()
    {
        return this.x;
    }

    public double getY()
    {
        return this.y;
    }

    public double getZ()
    {
        return this.z;
    }

    public float getYRot()
    {
        return this.yRot;
    }

    public float getXRot()
    {
        return this.xRot;
    }

    public int getId()
    {
        return this.id;
    }

    public boolean requestDismountVehicle()
    {
        return this.dismountVehicle;
    }

    public Set<ClientboundPlayerPositionPacket.RelativeArgument> getRelativeArguments()
    {
        return this.relativeArguments;
    }

    public static enum RelativeArgument
    {
        X(0),
        Y(1),
        Z(2),
        Y_ROT(3),
        X_ROT(4);

        private final int bit;

        private RelativeArgument(int p_132838_)
        {
            this.bit = p_132838_;
        }

        private int getMask()
        {
            return 1 << this.bit;
        }

        private boolean isSet(int pFlags)
        {
            return (pFlags & this.getMask()) == this.getMask();
        }

        public static Set<ClientboundPlayerPositionPacket.RelativeArgument> unpack(int pFlags)
        {
            Set<ClientboundPlayerPositionPacket.RelativeArgument> set = EnumSet.noneOf(ClientboundPlayerPositionPacket.RelativeArgument.class);

            for (ClientboundPlayerPositionPacket.RelativeArgument clientboundplayerpositionpacket$relativeargument : values())
            {
                if (clientboundplayerpositionpacket$relativeargument.isSet(pFlags))
                {
                    set.add(clientboundplayerpositionpacket$relativeargument);
                }
            }

            return set;
        }

        public static int pack(Set<ClientboundPlayerPositionPacket.RelativeArgument> pFlags)
        {
            int i = 0;

            for (ClientboundPlayerPositionPacket.RelativeArgument clientboundplayerpositionpacket$relativeargument : pFlags)
            {
                i |= clientboundplayerpositionpacket$relativeargument.getMask();
            }

            return i;
        }
    }
}
