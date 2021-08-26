package net.minecraft.client.sounds;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

public class WeighedSoundEvents implements Weighted<Sound>
{
    private final List<Weighted<Sound>> list = Lists.newArrayList();
    private final Random random = new Random();
    private final ResourceLocation location;
    @Nullable
    private final Component subtitle;

    public WeighedSoundEvents(ResourceLocation p_120446_, @Nullable String p_120447_)
    {
        this.location = p_120446_;
        this.subtitle = p_120447_ == null ? null : new TranslatableComponent(p_120447_);
    }

    public int getWeight()
    {
        int i = 0;

        for (Weighted<Sound> weighted : this.list)
        {
            i += weighted.getWeight();
        }

        return i;
    }

    public Sound getSound()
    {
        int i = this.getWeight();

        if (!this.list.isEmpty() && i != 0)
        {
            int j = this.random.nextInt(i);

            for (Weighted<Sound> weighted : this.list)
            {
                j -= weighted.getWeight();

                if (j < 0)
                {
                    return weighted.getSound();
                }
            }

            return SoundManager.EMPTY_SOUND;
        }
        else
        {
            return SoundManager.EMPTY_SOUND;
        }
    }

    public void addSound(Weighted<Sound> pAccessor)
    {
        this.list.add(pAccessor);
    }

    public ResourceLocation getResourceLocation()
    {
        return this.location;
    }

    @Nullable
    public Component getSubtitle()
    {
        return this.subtitle;
    }

    public void preloadIfRequired(SoundEngine pEngine)
    {
        for (Weighted<Sound> weighted : this.list)
        {
            weighted.preloadIfRequired(pEngine);
        }
    }
}
