package net.minecraft.client.renderer.block.model;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;
import net.minecraftforge.client.model.pipeline.IVertexProducer;
import net.optifine.Config;
import net.optifine.model.BakedQuadRetextured;
import net.optifine.model.QuadBounds;
import net.optifine.reflect.Reflector;
import net.optifine.render.QuadVertexPositions;
import net.optifine.render.VertexPosition;

public class BakedQuad implements IVertexProducer
{
    protected int[] vertices;
    protected final int tintIndex;
    protected Direction direction;
    protected TextureAtlasSprite sprite;
    private final boolean shade;
    private int[] vertexDataSingle = null;
    private QuadBounds quadBounds;
    private boolean quadEmissiveChecked;
    private BakedQuad quadEmissive;
    private QuadVertexPositions quadVertexPositions;

    public BakedQuad(int[] p_111298_, int p_111299_, Direction p_111300_, TextureAtlasSprite p_111301_, boolean p_111302_)
    {
        this.vertices = p_111298_;
        this.tintIndex = p_111299_;
        this.direction = p_111300_;
        this.sprite = p_111301_;
        this.shade = p_111302_;
        this.fixVertexData();
    }

    public TextureAtlasSprite getSprite()
    {
        if (this.sprite == null)
        {
            this.sprite = getSpriteByUv(this.getVertices());
        }

        return this.sprite;
    }

    public int[] getVertices()
    {
        this.fixVertexData();
        return this.vertices;
    }

    public boolean isTinted()
    {
        return this.tintIndex != -1;
    }

    public int getTintIndex()
    {
        return this.tintIndex;
    }

    public Direction getDirection()
    {
        if (this.direction == null)
        {
            this.direction = FaceBakery.m_111612_(this.getVertices());
        }

        return this.direction;
    }

    public boolean isShade()
    {
        return this.shade;
    }

    public int[] getVertexDataSingle()
    {
        if (this.vertexDataSingle == null)
        {
            this.vertexDataSingle = makeVertexDataSingle(this.getVertices(), this.getSprite());
        }

        if (this.vertexDataSingle.length != this.getVertices().length)
        {
            this.vertexDataSingle = makeVertexDataSingle(this.getVertices(), this.getSprite());
        }

        return this.vertexDataSingle;
    }

    private static int[] makeVertexDataSingle(int[] vd, TextureAtlasSprite sprite)
    {
        int[] aint = (int[])vd.clone();
        int i = aint.length / 4;

        for (int j = 0; j < 4; ++j)
        {
            int k = j * i;
            float f = Float.intBitsToFloat(aint[k + 4]);
            float f1 = Float.intBitsToFloat(aint[k + 4 + 1]);
            float f2 = sprite.toSingleU(f);
            float f3 = sprite.toSingleV(f1);
            aint[k + 4] = Float.floatToRawIntBits(f2);
            aint[k + 4 + 1] = Float.floatToRawIntBits(f3);
        }

        return aint;
    }

    public void pipe(IVertexConsumer consumer)
    {
        Reflector.callVoid(Reflector.LightUtil_putBakedQuad, consumer, this);
    }

    private static TextureAtlasSprite getSpriteByUv(int[] vertexData)
    {
        float f = 1.0F;
        float f1 = 1.0F;
        float f2 = 0.0F;
        float f3 = 0.0F;
        int i = vertexData.length / 4;

        for (int j = 0; j < 4; ++j)
        {
            int k = j * i;
            float f4 = Float.intBitsToFloat(vertexData[k + 4]);
            float f5 = Float.intBitsToFloat(vertexData[k + 4 + 1]);
            f = Math.min(f, f4);
            f1 = Math.min(f1, f5);
            f2 = Math.max(f2, f4);
            f3 = Math.max(f3, f5);
        }

        float f6 = (f + f2) / 2.0F;
        float f7 = (f1 + f3) / 2.0F;
        return Config.getTextureMap().getIconByUV((double)f6, (double)f7);
    }

    protected void fixVertexData()
    {
        if (Config.isShaders())
        {
            if (this.vertices.length == DefaultVertexFormat.BLOCK_VANILLA_SIZE)
            {
                this.vertices = fixVertexDataSize(this.vertices, DefaultVertexFormat.BLOCK_SHADERS_SIZE);
            }
        }
        else if (this.vertices.length == DefaultVertexFormat.BLOCK_SHADERS_SIZE)
        {
            this.vertices = fixVertexDataSize(this.vertices, DefaultVertexFormat.BLOCK_VANILLA_SIZE);
        }
    }

    private static int[] fixVertexDataSize(int[] vd, int sizeNew)
    {
        int i = vd.length / 4;
        int j = sizeNew / 4;
        int[] aint = new int[j * 4];

        for (int k = 0; k < 4; ++k)
        {
            int l = Math.min(i, j);
            System.arraycopy(vd, k * i, aint, k * j, l);
        }

        return aint;
    }

    public QuadBounds getQuadBounds()
    {
        if (this.quadBounds == null)
        {
            this.quadBounds = new QuadBounds(this.getVertices());
        }

        return this.quadBounds;
    }

    public float getMidX()
    {
        QuadBounds quadbounds = this.getQuadBounds();
        return (quadbounds.getMaxX() + quadbounds.getMinX()) / 2.0F;
    }

    public double getMidY()
    {
        QuadBounds quadbounds = this.getQuadBounds();
        return (double)((quadbounds.getMaxY() + quadbounds.getMinY()) / 2.0F);
    }

    public double getMidZ()
    {
        QuadBounds quadbounds = this.getQuadBounds();
        return (double)((quadbounds.getMaxZ() + quadbounds.getMinZ()) / 2.0F);
    }

    public boolean isFaceQuad()
    {
        QuadBounds quadbounds = this.getQuadBounds();
        return quadbounds.isFaceQuad(this.direction);
    }

    public boolean isFullQuad()
    {
        QuadBounds quadbounds = this.getQuadBounds();
        return quadbounds.isFullQuad(this.direction);
    }

    public boolean isFullFaceQuad()
    {
        return this.isFullQuad() && this.isFaceQuad();
    }

    public BakedQuad getQuadEmissive()
    {
        if (this.quadEmissiveChecked)
        {
            return this.quadEmissive;
        }
        else
        {
            if (this.quadEmissive == null && this.sprite != null && this.sprite.spriteEmissive != null)
            {
                this.quadEmissive = new BakedQuadRetextured(this, this.sprite.spriteEmissive);
            }

            this.quadEmissiveChecked = true;
            return this.quadEmissive;
        }
    }

    public VertexPosition[] getVertexPositions(int key)
    {
        if (this.quadVertexPositions == null)
        {
            this.quadVertexPositions = new QuadVertexPositions();
        }

        return this.quadVertexPositions.get(key);
    }

    public String toString()
    {
        return "vertexData: " + this.vertices.length + ", tint: " + this.tintIndex + ", facing: " + this.direction + ", sprite: " + this.sprite;
    }
}
