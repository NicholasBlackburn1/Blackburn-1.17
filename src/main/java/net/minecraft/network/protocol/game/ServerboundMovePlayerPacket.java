package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public abstract class ServerboundMovePlayerPacket implements Packet<ServerGamePacketListener>
{
    protected final double x;
    protected final double y;
    protected final double z;
    protected final float yRot;
    protected final float xRot;
    protected final boolean onGround;
    protected final boolean hasPos;
    protected final boolean hasRot;

    protected ServerboundMovePlayerPacket(double p_179675_, double p_179676_, double p_179677_, float p_179678_, float p_179679_, boolean p_179680_, boolean p_179681_, boolean p_179682_)
    {
        this.x = p_179675_;
        this.y = p_179676_;
        this.z = p_179677_;
        this.yRot = p_179678_;
        this.xRot = p_179679_;
        this.onGround = p_179680_;
        this.hasPos = p_179681_;
        this.hasRot = p_179682_;
    }

    public void handle(ServerGamePacketListener pHandler)
    {
        pHandler.handleMovePlayer(this);
    }

    public double getX(double pDefaultValue)
    {
        return this.hasPos ? this.x : pDefaultValue;
    }

    public double getY(double pDefaultValue)
    {
        return this.hasPos ? this.y : pDefaultValue;
    }

    public double getZ(double pDefaultValue)
    {
        return this.hasPos ? this.z : pDefaultValue;
    }

    public float getYRot(float pDefaultValue)
    {
        return this.hasRot ? this.yRot : pDefaultValue;
    }

    public float getXRot(float pDefaultValue)
    {
        return this.hasRot ? this.xRot : pDefaultValue;
    }

    public boolean isOnGround()
    {
        return this.onGround;
    }

    public boolean hasPosition()
    {
        return this.hasPos;
    }

    public boolean hasRotation()
    {
        return this.hasRot;
    }

    public static class Pos extends ServerboundMovePlayerPacket
    {
        public Pos(double p_134150_, double p_134151_, double p_134152_, boolean p_134153_)
        {
            super(p_134150_, p_134151_, p_134152_, 0.0F, 0.0F, p_134153_, true, false);
        }

        public static ServerboundMovePlayerPacket.Pos read(FriendlyByteBuf p_179686_)
        {
            double d0 = p_179686_.readDouble();
            double d1 = p_179686_.readDouble();
            double d2 = p_179686_.readDouble();
            boolean flag = p_179686_.readUnsignedByte() != 0;
            return new ServerboundMovePlayerPacket.Pos(d0, d1, d2, flag);
        }

        public void write(FriendlyByteBuf pBuf)
        {
            pBuf.writeDouble(this.x);
            pBuf.writeDouble(this.y);
            pBuf.writeDouble(this.z);
            pBuf.writeByte(this.onGround ? 1 : 0);
        }
    }

    public static class PosRot extends ServerboundMovePlayerPacket
    {
        public PosRot(double p_134162_, double p_134163_, double p_134164_, float p_134165_, float p_134166_, boolean p_134167_)
        {
            super(p_134162_, p_134163_, p_134164_, p_134165_, p_134166_, p_134167_, true, true);
        }

        public static ServerboundMovePlayerPacket.PosRot read(FriendlyByteBuf p_179688_)
        {
            double d0 = p_179688_.readDouble();
            double d1 = p_179688_.readDouble();
            double d2 = p_179688_.readDouble();
            float f = p_179688_.readFloat();
            float f1 = p_179688_.readFloat();
            boolean flag = p_179688_.readUnsignedByte() != 0;
            return new ServerboundMovePlayerPacket.PosRot(d0, d1, d2, f, f1, flag);
        }

        public void write(FriendlyByteBuf pBuf)
        {
            pBuf.writeDouble(this.x);
            pBuf.writeDouble(this.y);
            pBuf.writeDouble(this.z);
            pBuf.writeFloat(this.yRot);
            pBuf.writeFloat(this.xRot);
            pBuf.writeByte(this.onGround ? 1 : 0);
        }
    }

    public static class Rot extends ServerboundMovePlayerPacket
    {
        public Rot(float p_134176_, float p_134177_, boolean p_134178_)
        {
            super(0.0D, 0.0D, 0.0D, p_134176_, p_134177_, p_134178_, false, true);
        }

        public static ServerboundMovePlayerPacket.Rot read(FriendlyByteBuf p_179690_)
        {
            float f = p_179690_.readFloat();
            float f1 = p_179690_.readFloat();
            boolean flag = p_179690_.readUnsignedByte() != 0;
            return new ServerboundMovePlayerPacket.Rot(f, f1, flag);
        }

        public void write(FriendlyByteBuf pBuf)
        {
            pBuf.writeFloat(this.yRot);
            pBuf.writeFloat(this.xRot);
            pBuf.writeByte(this.onGround ? 1 : 0);
        }
    }

    public static class StatusOnly extends ServerboundMovePlayerPacket
    {
        public StatusOnly(boolean p_179692_)
        {
            super(0.0D, 0.0D, 0.0D, 0.0F, 0.0F, p_179692_, false, false);
        }

        public static ServerboundMovePlayerPacket.StatusOnly read(FriendlyByteBuf p_179698_)
        {
            boolean flag = p_179698_.readUnsignedByte() != 0;
            return new ServerboundMovePlayerPacket.StatusOnly(flag);
        }

        public void write(FriendlyByteBuf p_179694_)
        {
            p_179694_.writeByte(this.onGround ? 1 : 0);
        }
    }
}
