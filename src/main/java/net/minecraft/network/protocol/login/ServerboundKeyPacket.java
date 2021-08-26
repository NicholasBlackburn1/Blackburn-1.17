package net.minecraft.network.protocol.login;

import java.security.PrivateKey;
import java.security.PublicKey;
import javax.crypto.SecretKey;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;

public class ServerboundKeyPacket implements Packet<ServerLoginPacketListener>
{
    private final byte[] keybytes;
    private final byte[] nonce;

    public ServerboundKeyPacket(SecretKey p_134856_, PublicKey p_134857_, byte[] p_134858_) throws CryptException
    {
        this.keybytes = Crypt.m_13594_(p_134857_, p_134856_.getEncoded());
        this.nonce = Crypt.m_13594_(p_134857_, p_134858_);
    }

    public ServerboundKeyPacket(FriendlyByteBuf p_179829_)
    {
        this.keybytes = p_179829_.readByteArray();
        this.nonce = p_179829_.readByteArray();
    }

    public void write(FriendlyByteBuf pBuf)
    {
        pBuf.m_130087_(this.keybytes);
        pBuf.m_130087_(this.nonce);
    }

    public void handle(ServerLoginPacketListener pHandler)
    {
        pHandler.handleKey(this);
    }

    public SecretKey getSecretKey(PrivateKey pKey) throws CryptException
    {
        return Crypt.m_13597_(pKey, this.keybytes);
    }

    public byte[] getNonce(PrivateKey pKey) throws CryptException
    {
        return Crypt.m_13605_(pKey, this.nonce);
    }
}
