package net.minecraft.client.renderer.texture;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.realmsclient.RealmsMainScreen;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.optifine.Config;
import net.optifine.CustomGuis;
import net.optifine.EmissiveTextures;
import net.optifine.shaders.ShadersTex;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TextureManager implements PreparableReloadListener, Tickable, AutoCloseable
{
    private static final Logger LOGGER = LogManager.getLogger();
    public static final ResourceLocation INTENTIONAL_MISSING_TEXTURE = new ResourceLocation("");
    private final Map<ResourceLocation, AbstractTexture> byPath = Maps.newHashMap();
    private final Set<Tickable> tickableTextures = Sets.newHashSet();
    private final Map<String, Integer> prefixRegister = Maps.newHashMap();
    private final ResourceManager resourceManager;
    private AbstractTexture boundTexture;
    private ResourceLocation boundTextureLocation;
    private Int2ObjectMap<AbstractTexture> mapTexturesById = new Int2ObjectOpenHashMap<>();

    public TextureManager(ResourceManager p_118474_)
    {
        this.resourceManager = p_118474_;
    }

    public void bindForSetup(ResourceLocation p_174785_)
    {
        if (!RenderSystem.isOnRenderThread())
        {
            RenderSystem.recordRenderCall(() ->
            {
                this._bind(p_174785_);
            });
        }
        else
        {
            this._bind(p_174785_);
        }
    }

    private void _bind(ResourceLocation pResource)
    {
        if (Config.isCustomGuis())
        {
            pResource = CustomGuis.getTextureLocation(pResource);
        }

        AbstractTexture abstracttexture = this.byPath.get(pResource);

        if (abstracttexture == null)
        {
            abstracttexture = new SimpleTexture(pResource);
            this.register(pResource, abstracttexture);
        }

        if (Config.isShaders())
        {
            ShadersTex.bindTexture(abstracttexture);
        }
        else
        {
            abstracttexture.bind();
        }

        this.boundTexture = abstracttexture;
        this.boundTextureLocation = pResource;
    }

    public void register(ResourceLocation pName, AbstractTexture pTexture)
    {
        pTexture = this.loadTexture(pName, pTexture);
        AbstractTexture abstracttexture = this.byPath.put(pName, pTexture);

        if (abstracttexture != pTexture)
        {
            if (abstracttexture != null && abstracttexture != MissingTextureAtlasSprite.getTexture())
            {
                this.tickableTextures.remove(abstracttexture);
                this.safeClose(pName, abstracttexture);
            }

            if (pTexture instanceof Tickable)
            {
                this.tickableTextures.add((Tickable)pTexture);
            }
        }

        int i = pTexture.getId();

        if (i > 0)
        {
            this.mapTexturesById.put(i, pTexture);
        }
    }

    private void safeClose(ResourceLocation p_118509_, AbstractTexture p_118510_)
    {
        if (p_118510_ != MissingTextureAtlasSprite.getTexture())
        {
            try
            {
                p_118510_.close();
            }
            catch (Exception exception)
            {
                LOGGER.warn("Failed to close texture {}", p_118509_, exception);
            }
        }

        p_118510_.releaseId();
    }

    private AbstractTexture loadTexture(ResourceLocation p_118516_, AbstractTexture p_118517_)
    {
        try
        {
            p_118517_.load(this.resourceManager);
            return p_118517_;
        }
        catch (IOException ioexception)
        {
            if (p_118516_ != INTENTIONAL_MISSING_TEXTURE)
            {
                LOGGER.warn("Failed to load texture: {}", p_118516_, ioexception);
            }

            return MissingTextureAtlasSprite.getTexture();
        }
        catch (Throwable throwable)
        {
            CrashReport crashreport = CrashReport.forThrowable(throwable, "Registering texture");
            CrashReportCategory crashreportcategory = crashreport.addCategory("Resource location being registered");
            crashreportcategory.setDetail("Resource location", p_118516_);
            crashreportcategory.setDetail("Texture object class", () ->
            {
                return p_118517_.getClass().getName();
            });
            throw new ReportedException(crashreport);
        }
    }

    public AbstractTexture getTexture(ResourceLocation pTextureLocation)
    {
        AbstractTexture abstracttexture = this.byPath.get(pTextureLocation);

        if (abstracttexture == null)
        {
            abstracttexture = new SimpleTexture(pTextureLocation);
            this.register(pTextureLocation, abstracttexture);
        }

        return abstracttexture;
    }

    public AbstractTexture getTexture(ResourceLocation pTextureLocation, AbstractTexture p_174788_)
    {
        return this.byPath.getOrDefault(pTextureLocation, p_174788_);
    }

    public ResourceLocation register(String pName, DynamicTexture pTexture)
    {
        Integer integer = this.prefixRegister.get(pName);

        if (integer == null)
        {
            integer = 1;
        }
        else
        {
            integer = integer + 1;
        }

        this.prefixRegister.put(pName, integer);
        ResourceLocation resourcelocation = new ResourceLocation(String.format(Locale.ROOT, "dynamic/%s_%d", pName, integer));
        this.register(resourcelocation, pTexture);
        return resourcelocation;
    }

    public CompletableFuture<Void> preload(ResourceLocation pTextureLocation, Executor pExecutor)
    {
        if (!this.byPath.containsKey(pTextureLocation))
        {
            PreloadedTexture preloadedtexture = new PreloadedTexture(this.resourceManager, pTextureLocation, pExecutor);
            this.byPath.put(pTextureLocation, preloadedtexture);
            return preloadedtexture.getFuture().thenRunAsync(() ->
            {
                this.register(pTextureLocation, preloadedtexture);
            }, TextureManager::execute);
        }
        else
        {
            return CompletableFuture.completedFuture((Void)null);
        }
    }

    private static void execute(Runnable pRunnable)
    {
        Minecraft.getInstance().execute(() ->
        {
            RenderSystem.recordRenderCall(pRunnable::run);
        });
    }

    public void tick()
    {
        for (Tickable tickable : this.tickableTextures)
        {
            tickable.tick();
        }
    }

    public void release(ResourceLocation pTextureLocation)
    {
        AbstractTexture abstracttexture = this.getTexture(pTextureLocation, MissingTextureAtlasSprite.getTexture());

        if (abstracttexture != MissingTextureAtlasSprite.getTexture())
        {
            this.byPath.remove(pTextureLocation);
            TextureUtil.releaseTextureId(abstracttexture.getId());
        }
    }

    public void close()
    {
        this.byPath.forEach(this::safeClose);
        this.byPath.clear();
        this.tickableTextures.clear();
        this.prefixRegister.clear();
        this.mapTexturesById.clear();
    }

    public CompletableFuture<Void> reload(PreparableReloadListener.PreparationBarrier pStage, ResourceManager pResourceManager, ProfilerFiller pPreparationsProfiler, ProfilerFiller pReloadProfiler, Executor pBackgroundExecutor, Executor pGameExecutor)
    {
        Config.dbg("*** Reloading textures ***");
        Config.log("Resource packs: " + Config.getResourcePackNames());
        Iterator iterator = this.byPath.keySet().iterator();

        while (iterator.hasNext())
        {
            ResourceLocation resourcelocation = (ResourceLocation)iterator.next();
            String s = resourcelocation.getPath();

            if (s.startsWith("optifine/") || EmissiveTextures.isEmissive(resourcelocation))
            {
                AbstractTexture abstracttexture = this.byPath.get(resourcelocation);

                if (abstracttexture instanceof AbstractTexture)
                {
                    abstracttexture.releaseId();
                }

                iterator.remove();
            }
        }

        EmissiveTextures.update();
        return CompletableFuture.allOf(TitleScreen.preloadResources(this, pBackgroundExecutor), this.preload(AbstractWidget.WIDGETS_LOCATION, pBackgroundExecutor)).thenCompose(pStage::wait).thenAcceptAsync((voidIn) ->
        {
            MissingTextureAtlasSprite.getTexture();
            RealmsMainScreen.updateTeaserImages(this.resourceManager);
            Set<Entry<ResourceLocation, AbstractTexture>> set = new HashSet<>(this.byPath.entrySet());
            Iterator<Entry<ResourceLocation, AbstractTexture>> iterator1 = set.iterator();

            while (iterator1.hasNext())
            {
                Entry<ResourceLocation, AbstractTexture> entry = iterator1.next();
                ResourceLocation resourcelocation1 = entry.getKey();
                AbstractTexture abstracttexture1 = entry.getValue();

                if (abstracttexture1 == MissingTextureAtlasSprite.getTexture() && !resourcelocation1.equals(MissingTextureAtlasSprite.getLocation()))
                {
                    iterator1.remove();
                }
                else
                {
                    abstracttexture1.reset(this, pResourceManager, resourcelocation1, pGameExecutor);
                }
            }
        }, (runnableIn) ->
        {
            RenderSystem.recordRenderCall(runnableIn::run);
        });
    }

    public AbstractTexture getBoundTexture()
    {
        return this.boundTexture;
    }

    public ResourceLocation getBoundTextureLocation()
    {
        return this.boundTextureLocation;
    }

    public AbstractTexture getTextureById(int id)
    {
        AbstractTexture abstracttexture = this.mapTexturesById.get(id);

        if (abstracttexture != null && abstracttexture.getId() != id)
        {
            this.mapTexturesById.remove(id);
            this.mapTexturesById.put(abstracttexture.getId(), abstracttexture);
            abstracttexture = null;
        }

        return abstracttexture;
    }
}
