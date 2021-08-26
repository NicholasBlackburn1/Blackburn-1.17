package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.DecoderException;
import java.util.List;
import java.util.zip.Inflater;

public class CompressionDecoder extends ByteToMessageDecoder
{
    public static final int f_182671_ = 2097152;
    public static final int f_182672_ = 8388608;
    private final Inflater inflater;
    private int threshold;
    private boolean f_182673_;

    public CompressionDecoder(int p_182675_, boolean p_182676_)
    {
        this.threshold = p_182675_;
        this.f_182673_ = p_182676_;
        this.inflater = new Inflater();
    }

    protected void decode(ChannelHandlerContext p_129441_, ByteBuf p_129442_, List<Object> p_129443_) throws Exception
    {
        if (p_129442_.readableBytes() != 0)
        {
            FriendlyByteBuf friendlybytebuf = new FriendlyByteBuf(p_129442_);
            int i = friendlybytebuf.readVarInt();

            if (i == 0)
            {
                p_129443_.add(friendlybytebuf.readBytes(friendlybytebuf.readableBytes()));
            }
            else
            {
                if (this.f_182673_)
                {
                    if (i < this.threshold)
                    {
                        throw new DecoderException("Badly compressed packet - size of " + i + " is below server threshold of " + this.threshold);
                    }

                    if (i > 8388608)
                    {
                        throw new DecoderException("Badly compressed packet - size of " + i + " is larger than protocol maximum of 8388608");
                    }
                }

                byte[] abyte = new byte[friendlybytebuf.readableBytes()];
                friendlybytebuf.readBytes(abyte);
                this.inflater.setInput(abyte);
                byte[] abyte1 = new byte[i];
                this.inflater.inflate(abyte1);
                p_129443_.add(Unpooled.wrappedBuffer(abyte1));
                this.inflater.reset();
            }
        }
    }

    public void m_182677_(int p_182678_, boolean p_182679_)
    {
        this.threshold = p_182678_;
        this.f_182673_ = p_182679_;
    }
}
