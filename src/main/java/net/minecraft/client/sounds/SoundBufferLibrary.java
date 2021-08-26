package net.minecraft.client.sounds;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.audio.OggAudioStream;
import com.mojang.blaze3d.audio.SoundBuffer;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import net.minecraft.Util;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

public class SoundBufferLibrary
{
    private final ResourceManager resourceManager;
    private final Map<ResourceLocation, CompletableFuture<SoundBuffer>> cache = Maps.newHashMap();

    public SoundBufferLibrary(ResourceManager p_120192_)
    {
        this.resourceManager = p_120192_;
    }

    public CompletableFuture<SoundBuffer> getCompleteBuffer(ResourceLocation pSoundID)
    {
        return this.cache.computeIfAbsent(pSoundID, (p_120208_) ->
        {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    Resource resource = this.resourceManager.getResource(p_120208_);

                    SoundBuffer soundbuffer;

                    try {
                        InputStream inputstream = resource.getInputStream();

                        try {
                            OggAudioStream oggaudiostream = new OggAudioStream(inputstream);

                            try {
                                ByteBuffer bytebuffer = oggaudiostream.readAll();
                                soundbuffer = new SoundBuffer(bytebuffer, oggaudiostream.getFormat());
                            }
                            catch (Throwable throwable3)
                            {
                                try
                                {
                                    oggaudiostream.close();
                                }
                                catch (Throwable throwable2)
                                {
                                    throwable3.addSuppressed(throwable2);
                                }

                                throw throwable3;
                            }

                            oggaudiostream.close();
                        }
                        catch (Throwable throwable4)
                        {
                            if (inputstream != null)
                            {
                                try
                                {
                                    inputstream.close();
                                }
                                catch (Throwable throwable1)
                                {
                                    throwable4.addSuppressed(throwable1);
                                }
                            }

                            throw throwable4;
                        }

                        if (inputstream != null)
                        {
                            inputstream.close();
                        }
                    }
                    catch (Throwable throwable5)
                    {
                        if (resource != null)
                        {
                            try
                            {
                                resource.close();
                            }
                            catch (Throwable throwable)
                            {
                                throwable5.addSuppressed(throwable);
                            }
                        }

                        throw throwable5;
                    }

                    if (resource != null)
                    {
                        resource.close();
                    }

                    return soundbuffer;
                }
                catch (IOException ioexception)
                {
                    throw new CompletionException(ioexception);
                }
            }, Util.backgroundExecutor());
        });
    }

    public CompletableFuture<AudioStream> getStream(ResourceLocation pResourceLocation, boolean pIsWrapper)
    {
        return CompletableFuture.supplyAsync(() ->
        {
            try {
                Resource resource = this.resourceManager.getResource(pResourceLocation);
                InputStream inputstream = resource.getInputStream();
                return (AudioStream)(pIsWrapper ? new LoopingAudioStream(OggAudioStream::new, inputstream) : new OggAudioStream(inputstream));
            }
            catch (IOException ioexception)
            {
                throw new CompletionException(ioexception);
            }
        }, Util.backgroundExecutor());
    }

    public void clear()
    {
        this.cache.values().forEach((p_120201_) ->
        {
            p_120201_.thenAccept(SoundBuffer::discardAlBuffer);
        });
        this.cache.clear();
    }

    public CompletableFuture<?> preload(Collection<Sound> pSounds)
    {
        return CompletableFuture.allOf(pSounds.stream().map((p_120197_) ->
        {
            return this.getCompleteBuffer(p_120197_.getPath());
        }).toArray((p_120195_) ->
        {
            return new CompletableFuture[p_120195_];
        }));
    }
}
