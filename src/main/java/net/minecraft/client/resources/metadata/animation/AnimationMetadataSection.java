package net.minecraft.client.resources.metadata.animation;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import java.util.List;

public class AnimationMetadataSection
{
    public static final AnimationMetadataSectionSerializer SERIALIZER = new AnimationMetadataSectionSerializer();
    public static final String SECTION_NAME = "animation";
    public static final int DEFAULT_FRAME_TIME = 1;
    public static final int UNKNOWN_SIZE = -1;
    public static final AnimationMetadataSection EMPTY = new AnimationMetadataSection(Lists.newArrayList(), -1, -1, 1, false)
    {
        public Pair<Integer, Integer> getFrameSize(int pWidth, int pHeight)
        {
            return Pair.of(pWidth, pHeight);
        }
    };
    private final List<AnimationFrame> frames;
    private final int frameWidth;
    private final int frameHeight;
    private final int defaultFrameTime;
    private final boolean interpolatedFrames;

    public AnimationMetadataSection(List<AnimationFrame> p_119020_, int p_119021_, int p_119022_, int p_119023_, boolean p_119024_)
    {
        this.frames = p_119020_;
        this.frameWidth = p_119021_;
        this.frameHeight = p_119022_;
        this.defaultFrameTime = p_119023_;
        this.interpolatedFrames = p_119024_;
    }

    private static boolean isDivisionInteger(int pValMul, int pVal)
    {
        return pValMul / pVal * pVal == pValMul;
    }

    public Pair<Integer, Integer> getFrameSize(int pWidth, int pHeight)
    {
        Pair<Integer, Integer> pair = this.calculateFrameSize(pWidth, pHeight);
        int i = pair.getFirst();
        int j = pair.getSecond();

        if (isDivisionInteger(pWidth, i) && isDivisionInteger(pHeight, j))
        {
            return pair;
        }
        else
        {
            throw new IllegalArgumentException(String.format("Image size %s,%s is not multiply of frame size %s,%s", pWidth, pHeight, i, j));
        }
    }

    private Pair<Integer, Integer> calculateFrameSize(int pDefWidth, int pDefHeight)
    {
        if (this.frameWidth != -1)
        {
            return this.frameHeight != -1 ? Pair.of(this.frameWidth, this.frameHeight) : Pair.of(this.frameWidth, pDefHeight);
        }
        else if (this.frameHeight != -1)
        {
            return Pair.of(pDefWidth, this.frameHeight);
        }
        else
        {
            int i = Math.min(pDefWidth, pDefHeight);
            return Pair.of(i, i);
        }
    }

    public int getFrameHeight(int pDefHeight)
    {
        return this.frameHeight == -1 ? pDefHeight : this.frameHeight;
    }

    public int getFrameWidth(int pDefWidth)
    {
        return this.frameWidth == -1 ? pDefWidth : this.frameWidth;
    }

    public int getDefaultFrameTime()
    {
        return this.defaultFrameTime;
    }

    public boolean isInterpolatedFrames()
    {
        return this.interpolatedFrames;
    }

    public void forEachFrame(AnimationMetadataSection.FrameOutput p_174862_)
    {
        for (AnimationFrame animationframe : this.frames)
        {
            p_174862_.accept(animationframe.getIndex(), animationframe.getTime(this.defaultFrameTime));
        }
    }

    public List<AnimationFrame> getAnimationFrames()
    {
        return this.frames;
    }

    public int getFrameCount()
    {
        return this.frames.size();
    }

    @FunctionalInterface
    public interface FrameOutput
    {
        void accept(int p_174864_, int p_174865_);
    }
}
