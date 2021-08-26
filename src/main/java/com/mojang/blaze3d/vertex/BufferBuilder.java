package com.mojang.blaze3d.vertex;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.primitives.Floats;
import com.mojang.blaze3d.platform.MemoryTracker;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Vector3f;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntConsumer;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.optifine.Config;
import net.optifine.SmartAnimations;
import net.optifine.render.MultiTextureBuilder;
import net.optifine.render.MultiTextureData;
import net.optifine.render.RenderEnv;
import net.optifine.render.VertexPosition;
import net.optifine.shaders.SVertexBuilder;
import net.optifine.util.BufferUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BufferBuilder extends DefaultedVertexConsumer implements BufferVertexConsumer
{
    private static final int GROWTH_SIZE = 2097152;
    private static final Logger LOGGER = LogManager.getLogger();
    private ByteBuffer buffer;
    private final List<BufferBuilder.DrawState> drawStates = Lists.newArrayList();
    private int lastPoppedStateIndex;
    private int totalRenderedBytes;
    private int nextElementByte;
    private int totalUploadedBytes;
    private int vertices;
    @Nullable
    private VertexFormatElement currentElement;
    private int elementIndex;
    private VertexFormat format;
    private VertexFormat.Mode mode;
    private boolean fastFormat;
    private boolean fullFormat;
    private boolean building;
    @Nullable
    private Vector3f[] sortingPoints;
    private float sortX = Float.NaN;
    private float sortY = Float.NaN;
    private float sortZ = Float.NaN;
    private boolean indexOnly;
    private RenderType renderType;
    private boolean renderBlocks;
    private TextureAtlasSprite[] quadSprites = null;
    private TextureAtlasSprite[] quadSpritesPrev = null;
    private int[] quadOrdering = null;
    private TextureAtlasSprite quadSprite = null;
    private MultiTextureBuilder multiTextureBuilder = new MultiTextureBuilder();
    public SVertexBuilder sVertexBuilder;
    public RenderEnv renderEnv = null;
    public BitSet animatedSprites = null;
    public BitSet animatedSpritesCached = new BitSet();
    private ByteBuffer byteBufferTriangles;
    private Vector3f tempVec3f = new Vector3f();
    private float[] tempFloat4 = new float[4];
    private int[] tempInt4 = new int[4];
    private IntBuffer intBuffer;
    private FloatBuffer floatBuffer;
    private MultiBufferSource.BufferSource renderTypeBuffer;
    private FloatBuffer floatBufferSort;
    private VertexPosition[] quadVertexPositions;
    private Vector3f midBlock = new Vector3f();

    public BufferBuilder(int p_85664_)
    {
        this.buffer = MemoryTracker.m_182527_(p_85664_ * 6);
        this.intBuffer = this.buffer.asIntBuffer();
        this.floatBuffer = this.buffer.asFloatBuffer();
        SVertexBuilder.initVertexBuilder(this);
    }

    private void ensureVertexCapacity()
    {
        this.ensureCapacity(this.format.getVertexSize());
    }

    private void ensureCapacity(int pIncreaseAmount)
    {
        if (this.nextElementByte + pIncreaseAmount > this.buffer.capacity())
        {
            int i = this.buffer.capacity();
            int j = i + roundUp(pIncreaseAmount);
            LOGGER.debug("Needed to grow BufferBuilder buffer: Old size {} bytes, new size {} bytes.", i, j);
            ByteBuffer bytebuffer = MemoryTracker.m_182529_(this.buffer, j);
            ((Buffer)bytebuffer).rewind();
            this.buffer = bytebuffer;
            this.intBuffer = this.buffer.asIntBuffer();
            this.floatBuffer = this.buffer.asFloatBuffer();

            if (this.quadSprites != null)
            {
                TextureAtlasSprite[] atextureatlassprite = this.quadSprites;
                int k = this.getBufferQuadSize();
                this.quadSprites = new TextureAtlasSprite[k];
                System.arraycopy(atextureatlassprite, 0, this.quadSprites, 0, Math.min(atextureatlassprite.length, this.quadSprites.length));
                this.quadSpritesPrev = null;
            }
        }
    }

    private static int roundUp(int pX)
    {
        int i = 2097152;

        if (pX == 0)
        {
            return i;
        }
        else
        {
            if (pX < 0)
            {
                i *= -1;
            }

            int j = pX % i;
            return j == 0 ? pX : pX + i - j;
        }
    }

    public void setQuadSortOrigin(float p_166772_, float p_166773_, float p_166774_)
    {
        if (this.mode == VertexFormat.Mode.QUADS && (this.sortX != p_166772_ || this.sortY != p_166773_ || this.sortZ != p_166774_))
        {
            this.sortX = p_166772_;
            this.sortY = p_166773_;
            this.sortZ = p_166774_;

            if (this.sortingPoints == null)
            {
                this.sortingPoints = this.makeQuadSortingPoints();
            }
        }
    }

    public BufferBuilder.SortState getSortState()
    {
        TextureAtlasSprite[] atextureatlassprite = null;

        if (this.quadSprites != null)
        {
            int i = this.vertices / 4;
            atextureatlassprite = Arrays.copyOfRange(this.quadSprites, 0, i);
        }

        return new BufferBuilder.SortState(this.mode, this.vertices, this.sortingPoints, this.sortX, this.sortY, this.sortZ, atextureatlassprite);
    }

    public void restoreSortState(BufferBuilder.SortState p_166776_)
    {
        ((Buffer)this.buffer).clear();
        this.mode = p_166776_.mode;
        this.vertices = p_166776_.vertices;
        this.nextElementByte = this.totalRenderedBytes;
        this.sortingPoints = p_166776_.sortingPoints;
        this.sortX = p_166776_.sortX;
        this.sortY = p_166776_.sortY;
        this.sortZ = p_166776_.sortZ;
        this.indexOnly = true;

        if (this.quadSprites != null && p_166776_.quadSprites != null)
        {
            System.arraycopy(p_166776_.quadSprites, 0, this.quadSprites, 0, Math.min(p_166776_.quadSprites.length, this.quadSprites.length));
        }
    }

    public void begin(VertexFormat.Mode p_166780_, VertexFormat p_166781_)
    {
        if (this.building)
        {
            throw new IllegalStateException("Already building!");
        }
        else
        {
            this.building = true;
            this.mode = p_166780_;
            this.switchFormat(p_166781_);
            this.currentElement = p_166781_.getElements().get(0);
            this.elementIndex = 0;
            ((Buffer)this.buffer).clear();

            if (Config.isShaders())
            {
                SVertexBuilder.endSetVertexFormat(this);
            }

            if (Config.isMultiTexture())
            {
                this.initQuadSprites();
            }

            if (SmartAnimations.isActive())
            {
                if (this.animatedSprites == null)
                {
                    this.animatedSprites = this.animatedSpritesCached;
                }

                this.animatedSprites.clear();
            }
            else if (this.animatedSprites != null)
            {
                this.animatedSprites = null;
            }
        }
    }

    public VertexConsumer uv(float pU, float pV)
    {
        if (this.quadSprite != null && this.quadSprites != null)
        {
            pU = this.quadSprite.toSingleU(pU);
            pV = this.quadSprite.toSingleV(pV);
            this.quadSprites[this.vertices / 4] = this.quadSprite;
        }

        return BufferVertexConsumer.super.uv(pU, pV);
    }

    private void switchFormat(VertexFormat pVertexFormat)
    {
        if (this.format != pVertexFormat)
        {
            this.format = pVertexFormat;
            boolean flag = pVertexFormat == DefaultVertexFormat.NEW_ENTITY;
            boolean flag1 = pVertexFormat == DefaultVertexFormat.BLOCK;
            this.fastFormat = flag || flag1;
            this.fullFormat = flag;
        }
    }

    private IntConsumer intConsumer(VertexFormat.IndexType p_166778_)
    {
        switch (p_166778_)
        {
            case BYTE:
                return (valueIn) ->
                {
                    this.buffer.put((byte)valueIn);
                };
            case SHORT:
                return (valueIn) ->
                {
                    this.buffer.putShort((short)valueIn);
                };
            case INT:
            default:
                return (valueIn) ->
                {
                    this.buffer.putInt(valueIn);
                };
        }
    }

    private Vector3f[] makeQuadSortingPoints()
    {
        FloatBuffer floatbuffer = this.buffer.asFloatBuffer();
        int i = this.totalRenderedBytes / 4;
        int j = this.format.getIntegerSize();
        int k = j * this.mode.primitiveStride;
        int l = this.vertices / this.mode.primitiveStride;
        Vector3f[] avector3f = new Vector3f[l];

        for (int i1 = 0; i1 < l; ++i1)
        {
            float f = floatbuffer.get(i + i1 * k + 0);
            float f1 = floatbuffer.get(i + i1 * k + 1);
            float f2 = floatbuffer.get(i + i1 * k + 2);
            float f3 = floatbuffer.get(i + i1 * k + j * 2 + 0);
            float f4 = floatbuffer.get(i + i1 * k + j * 2 + 1);
            float f5 = floatbuffer.get(i + i1 * k + j * 2 + 2);
            float f6 = (f + f3) / 2.0F;
            float f7 = (f1 + f4) / 2.0F;
            float f8 = (f2 + f5) / 2.0F;
            avector3f[i1] = new Vector3f(f6, f7, f8);
        }

        return avector3f;
    }

    private void putSortedQuadIndices(VertexFormat.IndexType p_166787_)
    {
        float[] afloat = new float[this.sortingPoints.length];
        int[] aint = new int[this.sortingPoints.length];

        for (int i = 0; i < this.sortingPoints.length; aint[i] = i++)
        {
            float f = this.sortingPoints[i].x() - this.sortX;
            float f1 = this.sortingPoints[i].y() - this.sortY;
            float f2 = this.sortingPoints[i].z() - this.sortZ;
            afloat[i] = f * f + f1 * f1 + f2 * f2;
        }

        IntArrays.mergeSort(aint, (index1, index2) ->
        {
            return Floats.compare(afloat[index2], afloat[index1]);
        });
        IntConsumer intconsumer = this.intConsumer(p_166787_);
        ((Buffer)this.buffer).position(this.nextElementByte);

        for (int j : aint)
        {
            intconsumer.accept(j * this.mode.primitiveStride + 0);
            intconsumer.accept(j * this.mode.primitiveStride + 1);
            intconsumer.accept(j * this.mode.primitiveStride + 2);
            intconsumer.accept(j * this.mode.primitiveStride + 2);
            intconsumer.accept(j * this.mode.primitiveStride + 3);
            intconsumer.accept(j * this.mode.primitiveStride + 0);
        }

        if (this.quadSprites != null)
        {
            this.quadOrdering = aint;
        }
    }

    public void end()
    {
        if (!this.building)
        {
            throw new IllegalStateException("Not building!");
        }
        else
        {
            int i = this.mode.indexCount(this.vertices);
            VertexFormat.IndexType vertexformat$indextype = VertexFormat.IndexType.least(i);
            boolean flag;

            if (this.sortingPoints != null)
            {
                int j = Mth.roundToward(i * vertexformat$indextype.bytes, 4);
                this.ensureCapacity(j);
                this.putSortedQuadIndices(vertexformat$indextype);
                flag = false;
                this.nextElementByte += j;
                this.totalRenderedBytes += this.vertices * this.format.getVertexSize() + j;
            }
            else
            {
                flag = true;
                this.totalRenderedBytes += this.vertices * this.format.getVertexSize();
            }

            this.building = false;
            MultiTextureData multitexturedata = this.multiTextureBuilder.build(this.vertices, this.renderType, this.quadSprites, this.quadOrdering);
            this.drawStates.add(new BufferBuilder.DrawState(this.format, this.vertices, i, this.mode, vertexformat$indextype, this.indexOnly, flag, multitexturedata));
            this.renderType = null;
            this.renderBlocks = false;

            if (this.quadSprites != null)
            {
                this.quadSpritesPrev = this.quadSprites;
            }

            this.quadSprites = null;
            this.quadSprite = null;
            this.quadOrdering = null;
            this.vertices = 0;
            this.currentElement = null;
            this.elementIndex = 0;
            this.sortingPoints = null;
            this.sortX = Float.NaN;
            this.sortY = Float.NaN;
            this.sortZ = Float.NaN;
            this.indexOnly = false;
        }
    }

    public void putByte(int pIndex, byte pByteValue)
    {
        this.buffer.put(this.nextElementByte + pIndex, pByteValue);
    }

    public void putShort(int pIndex, short pShortValue)
    {
        this.buffer.putShort(this.nextElementByte + pIndex, pShortValue);
    }

    public void putFloat(int pIndex, float pFloatValue)
    {
        this.buffer.putFloat(this.nextElementByte + pIndex, pFloatValue);
    }

    public void endVertex()
    {
        if (this.elementIndex != 0)
        {
            throw new IllegalStateException("Not filled all elements of the vertex");
        }
        else
        {
            ++this.vertices;
            this.ensureVertexCapacity();

            if (this.mode == VertexFormat.Mode.LINES || this.mode == VertexFormat.Mode.LINE_STRIP)
            {
                int i = this.format.getVertexSize();
                ((Buffer)this.buffer).position(this.nextElementByte);
                ByteBuffer bytebuffer = this.buffer.duplicate();
                ((Buffer)bytebuffer).position(this.nextElementByte - i).limit(this.nextElementByte);
                this.buffer.put(bytebuffer);
                this.nextElementByte += i;
                ++this.vertices;
                this.ensureVertexCapacity();
            }

            if (Config.isShaders())
            {
                SVertexBuilder.endAddVertex(this);
            }
        }
    }

    public void nextElement()
    {
        ImmutableList<VertexFormatElement> immutablelist = this.format.getElements();
        this.elementIndex = (this.elementIndex + 1) % immutablelist.size();
        this.nextElementByte += this.currentElement.getByteSize();
        VertexFormatElement vertexformatelement = immutablelist.get(this.elementIndex);
        this.currentElement = vertexformatelement;

        if (vertexformatelement.getUsage() == VertexFormatElement.Usage.PADDING)
        {
            this.nextElement();
        }

        if (this.defaultColorSet && this.currentElement.getUsage() == VertexFormatElement.Usage.COLOR)
        {
            BufferVertexConsumer.super.color(this.defaultR, this.defaultG, this.defaultB, this.defaultA);
        }
    }

    public VertexConsumer color(int pRed, int pGreen, int pBlue, int pAlpha)
    {
        if (this.defaultColorSet)
        {
            throw new IllegalStateException();
        }
        else
        {
            return BufferVertexConsumer.super.color(pRed, pGreen, pBlue, pAlpha);
        }
    }

    public void vertex(float pX, float pY, float pZ, float pRed, float pGreen, float pBlue, float pAlpha, float pTexU, float pTexV, int pOverlayUV, int pLightmapUV, float pNormalX, float pNormalY, float pNormalZ)
    {
        if (this.defaultColorSet)
        {
            throw new IllegalStateException();
        }
        else
        {
            if (this.fastFormat)
            {
                this.putFloat(0, pX);
                this.putFloat(4, pY);
                this.putFloat(8, pZ);
                this.putByte(12, (byte)((int)(pRed * 255.0F)));
                this.putByte(13, (byte)((int)(pGreen * 255.0F)));
                this.putByte(14, (byte)((int)(pBlue * 255.0F)));
                this.putByte(15, (byte)((int)(pAlpha * 255.0F)));
                this.putFloat(16, pTexU);
                this.putFloat(20, pTexV);
                int i;

                if (this.fullFormat)
                {
                    this.putShort(24, (short)(pOverlayUV & 65535));
                    this.putShort(26, (short)(pOverlayUV >> 16 & 65535));
                    i = 28;
                }
                else
                {
                    i = 24;
                }

                this.putShort(i + 0, (short)(pLightmapUV & 65535));
                this.putShort(i + 2, (short)(pLightmapUV >> 16 & 65535));
                this.putByte(i + 4, BufferVertexConsumer.normalIntValue(pNormalX));
                this.putByte(i + 5, BufferVertexConsumer.normalIntValue(pNormalY));
                this.putByte(i + 6, BufferVertexConsumer.normalIntValue(pNormalZ));
                this.nextElementByte += this.format.getVertexSize();
                this.endVertex();
            }
            else
            {
                super.vertex(pX, pY, pZ, pRed, pGreen, pBlue, pAlpha, pTexU, pTexV, pOverlayUV, pLightmapUV, pNormalX, pNormalY, pNormalZ);
            }
        }
    }

    public Pair<BufferBuilder.DrawState, ByteBuffer> popNextBuffer()
    {
        BufferBuilder.DrawState bufferbuilder$drawstate = this.drawStates.get(this.lastPoppedStateIndex++);
        ((Buffer)this.buffer).position(this.totalUploadedBytes);
        this.totalUploadedBytes += Mth.roundToward(bufferbuilder$drawstate.bufferSize(), 4);
        ((Buffer)this.buffer).limit(this.totalUploadedBytes);

        if (this.lastPoppedStateIndex == this.drawStates.size() && this.vertices == 0)
        {
            this.clear();
        }

        ByteBuffer bytebuffer = this.buffer.slice();
        bytebuffer.order(this.buffer.order());
        ((Buffer)this.buffer).clear();
        return Pair.of(bufferbuilder$drawstate, bytebuffer);
    }

    public void clear()
    {
        if (this.totalRenderedBytes != this.totalUploadedBytes)
        {
            LOGGER.warn("Bytes mismatch {} {}", this.totalRenderedBytes, this.totalUploadedBytes);
        }

        this.discard();
    }

    public void discard()
    {
        this.totalRenderedBytes = 0;
        this.totalUploadedBytes = 0;
        this.nextElementByte = 0;
        this.drawStates.clear();
        this.lastPoppedStateIndex = 0;
        this.quadSprite = null;
    }

    public VertexFormatElement currentElement()
    {
        if (this.currentElement == null)
        {
            throw new IllegalStateException("BufferBuilder not started");
        }
        else
        {
            return this.currentElement;
        }
    }

    public boolean building()
    {
        return this.building;
    }

    public void putSprite(TextureAtlasSprite sprite)
    {
        if (this.animatedSprites != null && sprite != null && sprite.isTerrain() && sprite.getAnimationIndex() >= 0)
        {
            this.animatedSprites.set(sprite.getAnimationIndex());
        }

        if (this.quadSprites != null)
        {
            int i = this.vertices / 4;
            this.quadSprites[i] = sprite;
        }
    }

    public void setSprite(TextureAtlasSprite sprite)
    {
        if (this.animatedSprites != null && sprite != null && sprite.isTerrain() && sprite.getAnimationIndex() >= 0)
        {
            this.animatedSprites.set(sprite.getAnimationIndex());
        }

        if (this.quadSprites != null)
        {
            this.quadSprite = sprite;
        }
    }

    public boolean isMultiTexture()
    {
        return this.quadSprites != null;
    }

    public void setRenderType(RenderType renderType)
    {
        this.renderType = renderType;
    }

    public RenderType getRenderType()
    {
        return this.renderType;
    }

    public void setRenderBlocks(boolean renderBlocks)
    {
        this.renderBlocks = renderBlocks;

        if (Config.isMultiTexture())
        {
            this.initQuadSprites();
        }
    }

    public void setBlockLayer(RenderType layer)
    {
        this.renderType = layer;
        this.renderBlocks = true;
    }

    private void initQuadSprites()
    {
        if (this.renderBlocks)
        {
            if (this.renderType != null)
            {
                if (this.quadSprites == null)
                {
                    if (this.building)
                    {
                        if (this.vertices > 0)
                        {
                            VertexFormat.Mode vertexformat$mode = this.mode;
                            VertexFormat vertexformat = this.format;
                            RenderType rendertype = this.renderType;
                            boolean flag = this.renderBlocks;
                            this.renderType.end(this, 0, 0, 0);
                            this.begin(vertexformat$mode, vertexformat);
                            this.renderType = rendertype;
                            this.renderBlocks = flag;
                        }

                        this.quadSprites = this.quadSpritesPrev;

                        if (this.quadSprites == null || this.quadSprites.length < this.getBufferQuadSize())
                        {
                            this.quadSprites = new TextureAtlasSprite[this.getBufferQuadSize()];
                        }
                    }
                }
            }
        }
    }

    private int getBufferQuadSize()
    {
        return this.buffer.capacity() / this.format.getVertexSize();
    }

    public RenderEnv getRenderEnv(BlockState blockStateIn, BlockPos blockPosIn)
    {
        if (this.renderEnv == null)
        {
            this.renderEnv = new RenderEnv(blockStateIn, blockPosIn);
            return this.renderEnv;
        }
        else
        {
            this.renderEnv.reset(blockStateIn, blockPosIn);
            return this.renderEnv;
        }
    }

    private static void quadsToTriangles(ByteBuffer byteBuffer, VertexFormat vertexFormat, int vertexCount, ByteBuffer byteBufferTriangles)
    {
        int i = vertexFormat.getVertexSize();
        int j = byteBuffer.limit();
        ((Buffer)byteBuffer).rewind();
        ((Buffer)byteBufferTriangles).clear();

        for (int k = 0; k < vertexCount; k += 4)
        {
            ((Buffer)byteBuffer).limit((k + 3) * i);
            ((Buffer)byteBuffer).position(k * i);
            byteBufferTriangles.put(byteBuffer);
            ((Buffer)byteBuffer).limit((k + 1) * i);
            ((Buffer)byteBuffer).position(k * i);
            byteBufferTriangles.put(byteBuffer);
            ((Buffer)byteBuffer).limit((k + 2 + 2) * i);
            ((Buffer)byteBuffer).position((k + 2) * i);
            byteBufferTriangles.put(byteBuffer);
        }

        ((Buffer)byteBuffer).limit(j);
        ((Buffer)byteBuffer).rewind();
        ((Buffer)byteBufferTriangles).flip();
    }

    public VertexFormat.Mode getDrawMode()
    {
        return this.mode;
    }

    public int getVertexCount()
    {
        return this.vertices;
    }

    public Vector3f getTempVec3f(Vector3f vec)
    {
        this.tempVec3f.set(vec.x(), vec.y(), vec.z());
        return this.tempVec3f;
    }

    public Vector3f getTempVec3f(float x, float y, float z)
    {
        this.tempVec3f.set(x, y, z);
        return this.tempVec3f;
    }

    public float[] getTempFloat4(float f1, float f2, float f3, float f4)
    {
        this.tempFloat4[0] = f1;
        this.tempFloat4[1] = f2;
        this.tempFloat4[2] = f3;
        this.tempFloat4[3] = f4;
        return this.tempFloat4;
    }

    public int[] getTempInt4(int i1, int i2, int i3, int i4)
    {
        this.tempInt4[0] = i1;
        this.tempInt4[1] = i2;
        this.tempInt4[2] = i3;
        this.tempInt4[3] = i4;
        return this.tempInt4;
    }

    public ByteBuffer getByteBuffer()
    {
        return this.buffer;
    }

    public FloatBuffer getFloatBuffer()
    {
        return this.floatBuffer;
    }

    public IntBuffer getIntBuffer()
    {
        return this.intBuffer;
    }

    public int getBufferIntSize()
    {
        return this.vertices * this.format.getIntegerSize();
    }

    private FloatBuffer getFloatBufferSort(int size)
    {
        if (this.floatBufferSort == null || this.floatBufferSort.capacity() < size)
        {
            this.floatBufferSort = BufferUtil.createDirectFloatBuffer(size);
        }

        return this.floatBufferSort;
    }

    public MultiBufferSource.BufferSource getRenderTypeBuffer()
    {
        return this.renderTypeBuffer;
    }

    public void setRenderTypeBuffer(MultiBufferSource.BufferSource renderTypeBuffer)
    {
        this.renderTypeBuffer = renderTypeBuffer;
    }

    public void addVertexText(float x, float y, float z, int red, int green, int blue, int alpha, float texU, float texV, int lightmapU, int lightmapV)
    {
        if (this.format.getVertexSize() != DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP.getVertexSize())
        {
            throw new IllegalStateException("Invalid text vertex format: " + this.format);
        }
        else
        {
            this.putFloat(0, x);
            this.putFloat(4, y);
            this.putFloat(8, z);
            this.putByte(12, (byte)red);
            this.putByte(13, (byte)green);
            this.putByte(14, (byte)blue);
            this.putByte(15, (byte)alpha);
            this.putFloat(16, texU);
            this.putFloat(20, texV);
            this.putShort(24, (short)lightmapU);
            this.putShort(26, (short)lightmapV);
            this.nextElementByte += this.format.getVertexSize();
            this.endVertex();
        }
    }

    public void setQuadVertexPositions(VertexPosition[] vps)
    {
        this.quadVertexPositions = vps;
    }

    public VertexPosition[] getQuadVertexPositions()
    {
        return this.quadVertexPositions;
    }

    public void setMidBlock(float mx, float my, float mz)
    {
        this.midBlock.set(mx, my, mz);
    }

    public Vector3f getMidBlock()
    {
        return this.midBlock;
    }

    public void putBulkData(ByteBuffer buffer)
    {
        if (Config.isShaders())
        {
            SVertexBuilder.beginAddVertexData(this, buffer);
        }

        this.ensureCapacity(buffer.limit() + this.format.getVertexSize());
        ((Buffer)this.buffer).position(this.vertices * this.format.getVertexSize());
        this.buffer.put(buffer);
        this.vertices += buffer.limit() / this.format.getVertexSize();
        this.nextElementByte += buffer.limit();

        if (Config.isShaders())
        {
            SVertexBuilder.endAddVertexData(this);
        }
    }

    public VertexFormat getVertexFormat()
    {
        return this.format;
    }

    public int getStartPosition()
    {
        return this.totalRenderedBytes;
    }

    public int getIntStartPosition()
    {
        return this.totalRenderedBytes / 4;
    }

    public static final class DrawState
    {
        private final VertexFormat format;
        private final int vertexCount;
        private final int indexCount;
        private final VertexFormat.Mode mode;
        private final VertexFormat.IndexType indexType;
        private final boolean indexOnly;
        private final boolean sequentialIndex;
        private MultiTextureData multiTextureData;

        private DrawState(VertexFormat formatIn, int vertexCountIn, int indexCountIn, VertexFormat.Mode drawModeIn, VertexFormat.IndexType indexTypeIn, boolean indexOnlyIn, boolean sequentialIndexIn, MultiTextureData multiTextureData)
        {
            this(formatIn, vertexCountIn, indexCountIn, drawModeIn, indexTypeIn, indexOnlyIn, sequentialIndexIn);
            this.multiTextureData = multiTextureData;
        }

        public MultiTextureData getMultiTextureData()
        {
            return this.multiTextureData;
        }

        DrawState(VertexFormat p_166802_, int p_166803_, int p_166804_, VertexFormat.Mode p_166805_, VertexFormat.IndexType p_166806_, boolean p_166807_, boolean p_166808_)
        {
            this.format = p_166802_;
            this.vertexCount = p_166803_;
            this.indexCount = p_166804_;
            this.mode = p_166805_;
            this.indexType = p_166806_;
            this.indexOnly = p_166807_;
            this.sequentialIndex = p_166808_;
        }

        public VertexFormat format()
        {
            return this.format;
        }

        public int vertexCount()
        {
            return this.vertexCount;
        }

        public int indexCount()
        {
            return this.indexCount;
        }

        public VertexFormat.Mode mode()
        {
            return this.mode;
        }

        public VertexFormat.IndexType indexType()
        {
            return this.indexType;
        }

        public int vertexBufferSize()
        {
            return this.vertexCount * this.format.getVertexSize();
        }

        private int indexBufferSize()
        {
            return this.sequentialIndex ? 0 : this.indexCount * this.indexType.bytes;
        }

        public int bufferSize()
        {
            return this.vertexBufferSize() + this.indexBufferSize();
        }

        public boolean indexOnly()
        {
            return this.indexOnly;
        }

        public boolean sequentialIndex()
        {
            return this.sequentialIndex;
        }
    }

    public static class SortState
    {
        final VertexFormat.Mode mode;
        final int vertices;
        @Nullable
        final Vector3f[] sortingPoints;
        final float sortX;
        final float sortY;
        final float sortZ;
        private TextureAtlasSprite[] quadSprites;

        private SortState(VertexFormat.Mode modeIn, int verticesIn, @Nullable Vector3f[] sortingPointsIn, float sortXIn, float sortYIn, float sortZIn, TextureAtlasSprite[] quadSpritesIn)
        {
            this(modeIn, verticesIn, sortingPointsIn, sortXIn, sortYIn, sortZIn);
            this.quadSprites = quadSpritesIn;
        }

        SortState(VertexFormat.Mode p_166824_, int p_166825_, @Nullable Vector3f[] p_166826_, float p_166827_, float p_166828_, float p_166829_)
        {
            this.mode = p_166824_;
            this.vertices = p_166825_;
            this.sortingPoints = p_166826_;
            this.sortX = p_166827_;
            this.sortY = p_166828_;
            this.sortZ = p_166829_;
        }
    }
}
