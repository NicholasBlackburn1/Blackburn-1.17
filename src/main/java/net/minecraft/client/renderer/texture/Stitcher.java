package net.minecraft.client.renderer.texture;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.util.Mth;
import net.optifine.util.MathUtils;

public class Stitcher
{
    private static final Comparator<Stitcher.Holder> HOLDER_COMPARATOR = Comparator.comparing(( Stitcher.Holder holderIn) ->
    {
        return -holderIn.height;
    }).thenComparing((holderIn) ->
    {
        return -holderIn.width;
    }).thenComparing((holderIn) ->
    {
        return holderIn.spriteInfo.name();
    });
    private final int mipLevel;
    private final Set<Stitcher.Holder> texturesToBeStitched = Sets.newHashSetWithExpectedSize(256);
    private final List<Stitcher.Region> storage = Lists.newArrayListWithCapacity(256);
    private int storageX;
    private int storageY;
    private final int maxWidth;
    private final int maxHeight;

    public Stitcher(int p_118171_, int p_118172_, int p_118173_)
    {
        this.mipLevel = p_118173_;
        this.maxWidth = p_118171_;
        this.maxHeight = p_118172_;
    }

    public int getWidth()
    {
        return this.storageX;
    }

    public int getHeight()
    {
        return this.storageY;
    }

    public void registerSprite(TextureAtlasSprite.Info pSpriteInfo)
    {
        Stitcher.Holder stitcher$holder = new Stitcher.Holder(pSpriteInfo, this.mipLevel);
        this.texturesToBeStitched.add(stitcher$holder);
    }

    public void stitch()
    {
        List<Stitcher.Holder> list = Lists.newArrayList(this.texturesToBeStitched);
        list.sort(HOLDER_COMPARATOR);

        for (Stitcher.Holder stitcher$holder : list)
        {
            if (!this.addToStorage(stitcher$holder))
            {
                throw new StitcherException(stitcher$holder.spriteInfo, list.stream().map((holderIn) ->
                {
                    return holderIn.spriteInfo;
                }).collect(ImmutableList.toImmutableList()), this.storageX, this.storageY, this.maxWidth, this.maxHeight);
            }
        }

        this.storageX = Mth.smallestEncompassingPowerOfTwo(this.storageX);
        this.storageY = Mth.smallestEncompassingPowerOfTwo(this.storageY);
    }

    public void gatherSprites(Stitcher.SpriteLoader pSpriteLoader)
    {
        for (Stitcher.Region stitcher$region : this.storage)
        {
            stitcher$region.walk((regionIn) ->
            {
                Stitcher.Holder stitcher$holder = regionIn.getHolder();
                TextureAtlasSprite.Info textureatlassprite$info = stitcher$holder.spriteInfo;
                pSpriteLoader.load(textureatlassprite$info, this.storageX, this.storageY, regionIn.getX(), regionIn.getY());
            });
        }
    }

    static int smallestFittingMinTexel(int pDimension, int pMipmapLevel)
    {
        return (pDimension >> pMipmapLevel) + ((pDimension & (1 << pMipmapLevel) - 1) == 0 ? 0 : 1) << pMipmapLevel;
    }

    private boolean addToStorage(Stitcher.Holder pHolder)
    {
        for (Stitcher.Region stitcher$region : this.storage)
        {
            if (stitcher$region.add(pHolder))
            {
                return true;
            }
        }

        return this.expand(pHolder);
    }

    private boolean expand(Stitcher.Holder pHolder)
    {
        int i = Mth.smallestEncompassingPowerOfTwo(this.storageX);
        int j = Mth.smallestEncompassingPowerOfTwo(this.storageY);
        int k = Mth.smallestEncompassingPowerOfTwo(this.storageX + pHolder.width);
        int l = Mth.smallestEncompassingPowerOfTwo(this.storageY + pHolder.height);
        boolean flag = k <= this.maxWidth;
        boolean flag1 = l <= this.maxHeight;

        if (!flag && !flag1)
        {
            return false;
        }
        else
        {
            int i1 = MathUtils.roundDownToPowerOfTwo(this.storageY);
            boolean flag2 = flag && k <= 2 * i1;

            if (this.storageX == 0 && this.storageY == 0)
            {
                flag2 = true;
            }

            Stitcher.Region stitcher$region;

            if (flag2)
            {
                if (this.storageY == 0)
                {
                    this.storageY = pHolder.height;
                }

                stitcher$region = new Stitcher.Region(this.storageX, 0, pHolder.width, this.storageY);
                this.storageX += pHolder.width;
            }
            else
            {
                stitcher$region = new Stitcher.Region(0, this.storageY, this.storageX, pHolder.height);
                this.storageY += pHolder.height;
            }

            stitcher$region.add(pHolder);
            this.storage.add(stitcher$region);
            return true;
        }
    }

    static class Holder
    {
        public final TextureAtlasSprite.Info spriteInfo;
        public final int width;
        public final int height;

        public Holder(TextureAtlasSprite.Info p_118206_, int p_118207_)
        {
            this.spriteInfo = p_118206_;
            this.width = Stitcher.smallestFittingMinTexel(p_118206_.width(), p_118207_);
            this.height = Stitcher.smallestFittingMinTexel(p_118206_.height(), p_118207_);
        }

        public String toString()
        {
            return "Holder{width=" + this.width + ", height=" + this.height + ", name=" + this.spriteInfo.name() + "}";
        }
    }

    public static class Region
    {
        private final int originX;
        private final int originY;
        private final int width;
        private final int height;
        private List<Stitcher.Region> subSlots;
        private Stitcher.Holder holder;

        public Region(int p_118216_, int p_118217_, int p_118218_, int p_118219_)
        {
            this.originX = p_118216_;
            this.originY = p_118217_;
            this.width = p_118218_;
            this.height = p_118219_;
        }

        public Stitcher.Holder getHolder()
        {
            return this.holder;
        }

        public int getX()
        {
            return this.originX;
        }

        public int getY()
        {
            return this.originY;
        }

        public boolean add(Stitcher.Holder pHolder)
        {
            if (this.holder != null)
            {
                return false;
            }
            else
            {
                int i = pHolder.width;
                int j = pHolder.height;

                if (i <= this.width && j <= this.height)
                {
                    if (i == this.width && j == this.height)
                    {
                        this.holder = pHolder;
                        return true;
                    }
                    else
                    {
                        if (this.subSlots == null)
                        {
                            this.subSlots = Lists.newArrayListWithCapacity(1);
                            this.subSlots.add(new Stitcher.Region(this.originX, this.originY, i, j));
                            int k = this.width - i;
                            int l = this.height - j;

                            if (l > 0 && k > 0)
                            {
                                int i1 = Math.max(this.height, k);
                                int j1 = Math.max(this.width, l);

                                if (i1 >= j1)
                                {
                                    this.subSlots.add(new Stitcher.Region(this.originX, this.originY + j, i, l));
                                    this.subSlots.add(new Stitcher.Region(this.originX + i, this.originY, k, this.height));
                                }
                                else
                                {
                                    this.subSlots.add(new Stitcher.Region(this.originX + i, this.originY, k, j));
                                    this.subSlots.add(new Stitcher.Region(this.originX, this.originY + j, this.width, l));
                                }
                            }
                            else if (k == 0)
                            {
                                this.subSlots.add(new Stitcher.Region(this.originX, this.originY + j, i, l));
                            }
                            else if (l == 0)
                            {
                                this.subSlots.add(new Stitcher.Region(this.originX + i, this.originY, k, j));
                            }
                        }

                        for (Stitcher.Region stitcher$region : this.subSlots)
                        {
                            if (stitcher$region.add(pHolder))
                            {
                                return true;
                            }
                        }

                        return false;
                    }
                }
                else
                {
                    return false;
                }
            }
        }

        public void walk(Consumer<Stitcher.Region> pSlots)
        {
            if (this.holder != null)
            {
                pSlots.accept(this);
            }
            else if (this.subSlots != null)
            {
                for (Stitcher.Region stitcher$region : this.subSlots)
                {
                    stitcher$region.walk(pSlots);
                }
            }
        }

        public String toString()
        {
            return "Slot{originX=" + this.originX + ", originY=" + this.originY + ", width=" + this.width + ", height=" + this.height + ", texture=" + this.holder + ", subSlots=" + this.subSlots + "}";
        }
    }

    public interface SpriteLoader
    {
        void load(TextureAtlasSprite.Info p_118229_, int p_118230_, int p_118231_, int p_118232_, int p_118233_);
    }
}
