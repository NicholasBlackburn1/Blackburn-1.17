package net.minecraft.client.renderer.texture;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.PngInfo;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import java.awt.Dimension;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.inventory.InventoryMenu;
import net.optifine.Config;
import net.optifine.EmissiveTextures;
import net.optifine.SmartAnimations;
import net.optifine.reflect.Reflector;
import net.optifine.shaders.ITextureFormat;
import net.optifine.shaders.Shaders;
import net.optifine.shaders.ShadersTex;
import net.optifine.shaders.ShadersTextureType;
import net.optifine.texture.ColorBlenderLinear;
import net.optifine.texture.IColorBlender;
import net.optifine.util.CounterInt;
import net.optifine.util.TextureUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TextureAtlas extends AbstractTexture implements Tickable
{
    private static final Logger LOGGER = LogManager.getLogger();

    @Deprecated
    public static final ResourceLocation LOCATION_BLOCKS = InventoryMenu.BLOCK_ATLAS;

    @Deprecated
    public static final ResourceLocation LOCATION_PARTICLES = new ResourceLocation("textures/atlas/particles.png");
    private static final String FILE_EXTENSION = ".png";
    private final List<Tickable> animatedTextures = Lists.newArrayList();
    private final Set<ResourceLocation> sprites = Sets.newHashSet();
    private final Map<ResourceLocation, TextureAtlasSprite> texturesByName = Maps.newHashMap();
    private final ResourceLocation location;
    private final int maxSupportedTextureSize;
    private Map<ResourceLocation, TextureAtlasSprite> mapRegisteredSprites = new LinkedHashMap<>();
    private Map<ResourceLocation, TextureAtlasSprite> mapMissingSprites = new LinkedHashMap<>();
    private TextureAtlasSprite[] iconGrid = null;
    private int iconGridSize = -1;
    private int iconGridCountX = -1;
    private int iconGridCountY = -1;
    private double iconGridSizeU = -1.0D;
    private double iconGridSizeV = -1.0D;
    private CounterInt counterIndexInMap = new CounterInt(0);
    public int atlasWidth = 0;
    public int atlasHeight = 0;
    public int mipmapLevel = 0;
    private int countAnimationsActive;
    private int frameCountAnimations;
    private boolean terrain;
    private boolean shaders;
    private boolean multiTexture;
    private ITextureFormat textureFormat;

    public TextureAtlas(ResourceLocation p_118269_)
    {
        this.location = p_118269_;
        this.maxSupportedTextureSize = RenderSystem.maxSupportedTextureSize();
        this.terrain = p_118269_.equals(LOCATION_BLOCKS);
        this.shaders = Config.isShaders();
        this.multiTexture = Config.isMultiTexture();

        if (this.terrain)
        {
            Config.setTextureMap(this);
        }
    }

    public void load(ResourceManager pManager)
    {
    }

    public void reload(TextureAtlas.Preparations pSheetData)
    {
        this.sprites.clear();
        this.sprites.addAll(pSheetData.sprites);
        LOGGER.info("Created: {}x{}x{} {}-atlas", pSheetData.width, pSheetData.height, pSheetData.mipLevel, this.location);
        TextureUtil.prepareImage(this.getId(), pSheetData.mipLevel, pSheetData.width, pSheetData.height);
        this.atlasWidth = pSheetData.width;
        this.atlasHeight = pSheetData.height;
        this.mipmapLevel = pSheetData.mipLevel;

        if (this.shaders)
        {
            ShadersTex.allocateTextureMapNS(pSheetData.mipLevel, pSheetData.width, pSheetData.height, this);
        }

        this.clearTextureData();

        for (TextureAtlasSprite textureatlassprite : pSheetData.regions)
        {
            this.texturesByName.put(textureatlassprite.getName(), textureatlassprite);

            try
            {
                textureatlassprite.uploadFirstFrame();
            }
            catch (Throwable throwable)
            {
                CrashReport crashreport = CrashReport.forThrowable(throwable, "Stitching texture atlas");
                CrashReportCategory crashreportcategory = crashreport.addCategory("Texture being stitched together");
                crashreportcategory.setDetail("Atlas path", this.location);
                crashreportcategory.setDetail("Sprite", textureatlassprite);
                throw new ReportedException(crashreport);
            }

            Tickable tickable = textureatlassprite.getAnimationTicker();

            if (tickable != null)
            {
                textureatlassprite.setAnimationIndex(this.animatedTextures.size());
                this.animatedTextures.add(tickable);
            }
        }

        TextureUtils.refreshCustomSprites(this);
        Config.log("Animated sprites: " + this.animatedTextures.size());

        if (Config.isMultiTexture())
        {
            for (TextureAtlasSprite textureatlassprite1 : pSheetData.regions)
            {
                uploadMipmapsSingle(textureatlassprite1);

                if (textureatlassprite1.spriteNormal != null)
                {
                    uploadMipmapsSingle(textureatlassprite1.spriteNormal);
                }

                if (textureatlassprite1.spriteSpecular != null)
                {
                    uploadMipmapsSingle(textureatlassprite1.spriteSpecular);
                }
            }

            GlStateManager._bindTexture(this.getId());
        }

        if (Config.isShaders())
        {
            List list = pSheetData.regions;

            if (Shaders.configNormalMap)
            {
                GlStateManager._bindTexture(this.getMultiTexID().norm);

                for (Object textureatlassprite20 : list)
                {
                    TextureAtlasSprite textureatlassprite2 = (TextureAtlasSprite) textureatlassprite20;
                    TextureAtlasSprite textureatlassprite4 = textureatlassprite2.spriteNormal;

                    if (textureatlassprite4 != null)
                    {
                        textureatlassprite4.uploadFirstFrame();
                    }
                }
            }

            if (Shaders.configSpecularMap)
            {
                GlStateManager._bindTexture(this.getMultiTexID().spec);

                for (Object textureatlassprite30 : list)
                {
                    TextureAtlasSprite textureatlassprite3 = (TextureAtlasSprite) textureatlassprite30;
                    TextureAtlasSprite textureatlassprite5 = textureatlassprite3.spriteSpecular;

                    if (textureatlassprite5 != null)
                    {
                        textureatlassprite5.uploadFirstFrame();
                    }
                }
            }

            GlStateManager._bindTexture(this.getId());
        }

        Reflector.callVoid(Reflector.ForgeHooksClient_onTextureStitchedPost, this);
        this.updateIconGrid(pSheetData.width, pSheetData.height);

        if (Config.equals(System.getProperty("saveTextureMap"), "true"))
        {
            Config.dbg("Exporting texture map: " + this.location);
            TextureUtils.saveGlTexture("debug/" + this.location.getPath().replaceAll("/", "_"), this.getId(), pSheetData.mipLevel, pSheetData.width, pSheetData.height);

            if (this.shaders)
            {
                if (Shaders.configNormalMap)
                {
                    TextureUtils.saveGlTexture("debug/" + this.location.getPath().replaceAll("/", "_").replace(".png", "_n.png"), this.multiTex.norm, pSheetData.mipLevel, pSheetData.width, pSheetData.height);
                }

                if (Shaders.configSpecularMap)
                {
                    TextureUtils.saveGlTexture("debug/" + this.location.getPath().replaceAll("/", "_").replace(".png", "_s.png"), this.multiTex.spec, pSheetData.mipLevel, pSheetData.width, pSheetData.height);
                }

                GlStateManager._bindTexture(this.getId());
            }
        }
    }

    public TextureAtlas.Preparations prepareToStitch(ResourceManager pResourceManager, Stream<ResourceLocation> pResourceLocations, ProfilerFiller pProfiler, int pMaxMipmapLevel)
    {
        this.terrain = this.location.equals(LOCATION_BLOCKS);
        this.shaders = Config.isShaders();
        this.multiTexture = Config.isMultiTexture();
        this.textureFormat = ITextureFormat.readConfiguration();
        int i = pMaxMipmapLevel;
        this.mapRegisteredSprites.clear();
        this.mapMissingSprites.clear();
        this.counterIndexInMap.reset();
        pProfiler.push("preparing");
        Set<ResourceLocation> set = pResourceLocations.peek((locIn) ->
        {
            if (locIn == null)
            {
                throw new IllegalArgumentException("Location cannot be null!");
            }
        }).collect(Collectors.toSet());
        Config.dbg("Multitexture: " + Config.isMultiTexture());
        TextureUtils.registerCustomSprites(this);
        set.addAll(this.mapRegisteredSprites.keySet());
        Set<ResourceLocation> set1 = newHashSet(set, this.mapRegisteredSprites.keySet());
        EmissiveTextures.updateIcons(this, set1);
        set.addAll(this.mapRegisteredSprites.keySet());

        if (pMaxMipmapLevel >= 4)
        {
            i = this.detectMaxMipmapLevel(set, pResourceManager);
            Config.log("Mipmap levels: " + i);
        }

        int j = TextureUtils.getGLMaximumTextureSize();
        Stitcher stitcher = new Stitcher(j, j, pMaxMipmapLevel);
        int k = Integer.MAX_VALUE;
        int l = getMinSpriteSize(i);
        this.iconGridSize = l;
        int i1 = 1 << pMaxMipmapLevel;
        pProfiler.popPush("extracting_frames");
        Reflector.callVoid(Reflector.ForgeHooksClient_onTextureStitchedPre, this, set);

        for (TextureAtlasSprite.Info textureatlassprite$info : this.getBasicSpriteInfos(pResourceManager, set))
        {
            int j1 = textureatlassprite$info.width();
            int k1 = textureatlassprite$info.height();

            if (j1 >= 1 && k1 >= 1)
            {
                if (j1 < l || i > 0)
                {
                    int l1 = i > 0 ? TextureUtils.scaleToGrid(j1, l) : TextureUtils.scaleToMin(j1, l);

                    if (l1 != j1)
                    {
                        if (!TextureUtils.isPowerOfTwo(j1))
                        {
                            Config.log("Scaled non power of 2: " + textureatlassprite$info.name() + ", " + j1 + " -> " + l1);
                        }
                        else
                        {
                            Config.log("Scaled too small texture: " + textureatlassprite$info.name() + ", " + j1 + " -> " + l1);
                        }

                        int i2 = k1 * l1 / j1;
                        textureatlassprite$info.setSpriteWidth(l1);
                        textureatlassprite$info.setSpriteHeight(i2);
                        textureatlassprite$info.setScaleFactor((double)l1 * 1.0D / (double)j1);
                    }
                }

                k = Math.min(k, Math.min(textureatlassprite$info.width(), textureatlassprite$info.height()));
                int i3 = Math.min(Integer.lowestOneBit(textureatlassprite$info.width()), Integer.lowestOneBit(textureatlassprite$info.height()));

                if (i3 < i1)
                {
                    LOGGER.warn("Texture {} with size {}x{} limits mip level from {} to {}", textureatlassprite$info.name(), textureatlassprite$info.width(), textureatlassprite$info.height(), Mth.log2(i1), Mth.log2(i3));
                    i1 = i3;
                }

                stitcher.registerSprite(textureatlassprite$info);
            }
            else
            {
                Config.warn("Invalid sprite size: " + textureatlassprite$info.name());
            }
        }

        int j2 = Math.min(k, i1);
        int k2 = Mth.log2(j2);

        if (k2 < 0)
        {
            k2 = 0;
        }

        int l2;

        if (k2 < pMaxMipmapLevel)
        {
            LOGGER.warn("{}: dropping miplevel from {} to {}, because of minimum power of two: {}", this.location, pMaxMipmapLevel, k2, j2);
            l2 = k2;
        }
        else
        {
            l2 = pMaxMipmapLevel;
        }

        pProfiler.popPush("register");
        TextureAtlasSprite.Info textureatlassprite$info1 = fixSpriteSize(MissingTextureAtlasSprite.info(), l);
        stitcher.registerSprite(textureatlassprite$info1);
        pProfiler.popPush("stitching");

        try
        {
            stitcher.stitch();
        }
        catch (StitcherException stitcherexception)
        {
            CrashReport crashreport = CrashReport.forThrowable(stitcherexception, "Stitching");
            CrashReportCategory crashreportcategory = crashreport.addCategory("Stitcher");
            crashreportcategory.setDetail("Sprites", stitcherexception.getAllSprites().stream().map((infoIn) ->
            {
                return String.format("%s[%dx%d]", infoIn.name(), infoIn.width(), infoIn.height());
            }).collect(Collectors.joining(",")));
            crashreportcategory.setDetail("Max Texture Size", j);
            throw new ReportedException(crashreport);
        }

        pProfiler.popPush("loading");
        List<TextureAtlasSprite> list = this.getLoadedSprites(pResourceManager, stitcher, l2);
        pProfiler.pop();
        return new TextureAtlas.Preparations(set, stitcher.getWidth(), stitcher.getHeight(), l2, list);
    }

    private Collection<TextureAtlasSprite.Info> getBasicSpriteInfos(ResourceManager pResourceManager, Set<ResourceLocation> pSpriteLocations)
    {
        List < CompletableFuture<? >> list = Lists.newArrayList();
        Queue<TextureAtlasSprite.Info> queue = new ConcurrentLinkedQueue<>();

        for (ResourceLocation resourcelocation : pSpriteLocations)
        {
            if (!MissingTextureAtlasSprite.getLocation().equals(resourcelocation))
            {
                list.add(CompletableFuture.runAsync(() ->
                {
                    ResourceLocation resourcelocation1 = this.getResourceLocation(resourcelocation);

                    TextureAtlasSprite.Info textureatlassprite$info;

                    try {
                        Resource resource = pResourceManager.getResource(resourcelocation1);

                        try {
                            PngInfo pnginfo = new PngInfo(resource.toString(), resource.getInputStream());
                            AnimationMetadataSection animationmetadatasection = resource.getMetadata(AnimationMetadataSection.SERIALIZER);

                            if (animationmetadatasection == null)
                            {
                                animationmetadatasection = AnimationMetadataSection.EMPTY;
                            }

                            Pair<Integer, Integer> pair = animationmetadatasection.getFrameSize(pnginfo.width, pnginfo.height);
                            textureatlassprite$info = new TextureAtlasSprite.Info(resourcelocation, pair.getFirst(), pair.getSecond(), animationmetadatasection);
                        }
                        catch (Throwable throwable11)
                        {
                            if (resource != null)
                            {
                                try
                                {
                                    resource.close();
                                }
                                catch (Throwable throwable1)
                                {
                                    throwable11.addSuppressed(throwable1);
                                }
                            }

                            throw throwable11;
                        }

                        if (resource != null)
                        {
                            resource.close();
                        }
                    }
                    catch (RuntimeException runtimeexception)
                    {
                        LOGGER.error("Unable to parse metadata from {} : {}", resourcelocation1, runtimeexception);
                        this.onSpriteMissing(resourcelocation);
                        return;
                    }
                    catch (IOException ioexception1)
                    {
                        LOGGER.error("Using missing texture, unable to load {} : {}", resourcelocation1, ioexception1);
                        this.onSpriteMissing(resourcelocation);
                        return;
                    }

                    queue.add(textureatlassprite$info);
                }, Util.backgroundExecutor()));
            }
        }

        CompletableFuture.allOf(list.toArray(new CompletableFuture[0])).join();
        return queue;
    }

    private List<TextureAtlasSprite> getLoadedSprites(ResourceManager pResourceManager, Stitcher pStitcher, int pMipmapLevel)
    {
        Queue<TextureAtlasSprite> queue = new ConcurrentLinkedQueue<>();
        List < CompletableFuture<? >> list = Lists.newArrayList();
        pStitcher.gatherSprites((p_211595_5_, p_211595_6_, p_211595_7_, p_211595_8_, p_211595_9_) ->
        {
            if (p_211595_5_.name().equals(MissingTextureAtlasSprite.info().name()))
            {
                MissingTextureAtlasSprite missingtextureatlassprite = new MissingTextureAtlasSprite(this, p_211595_5_, pMipmapLevel, p_211595_6_, p_211595_7_, p_211595_8_, p_211595_9_);
                missingtextureatlassprite.update(pResourceManager);
                queue.add(missingtextureatlassprite);
            }
            else {
                list.add(CompletableFuture.runAsync(() -> {
                    TextureAtlasSprite textureatlassprite = this.load(pResourceManager, p_211595_5_, p_211595_6_, p_211595_7_, pMipmapLevel, p_211595_8_, p_211595_9_);

                    if (textureatlassprite != null)
                    {
                        queue.add(textureatlassprite);
                    }
                }, Util.backgroundExecutor()));
            }
        });
        CompletableFuture.allOf(list.toArray(new CompletableFuture[0])).join();
        return Lists.newArrayList(queue);
    }

    @Nullable
    private TextureAtlasSprite load(ResourceManager pManager, TextureAtlasSprite.Info p_118289_, int p_118290_, int p_118291_, int p_118292_, int p_118293_, int p_118294_)
    {
        ResourceLocation resourcelocation = this.getResourceLocation(p_118289_.name());

        try
        {
            Resource resource = pManager.getResource(resourcelocation);
            TextureAtlasSprite textureatlassprite = null;

            try
            {
                NativeImage nativeimage = NativeImage.read(resource.getInputStream());

                if (Reflector.ForgeHooksClient_loadTextureAtlasSprite.exists())
                {
                    textureatlassprite = (TextureAtlasSprite)Reflector.ForgeHooksClient_loadTextureAtlasSprite.call(this, pManager, p_118289_, resource, p_118290_, p_118291_, p_118293_, p_118294_, p_118292_, nativeimage);
                }

                if (textureatlassprite == null)
                {
                    textureatlassprite = new TextureAtlasSprite(this, p_118289_, p_118292_, p_118290_, p_118291_, p_118293_, p_118294_, nativeimage);
                }

                textureatlassprite.update(pManager);
            }
            catch (Throwable throwable11)
            {
                if (resource != null)
                {
                    try
                    {
                        resource.close();
                    }
                    catch (Throwable throwable)
                    {
                        throwable11.addSuppressed(throwable);
                    }
                }

                throw throwable11;
            }

            if (resource != null)
            {
                resource.close();
            }

            return textureatlassprite;
        }
        catch (RuntimeException runtimeexception)
        {
            LOGGER.error("Unable to parse metadata from {}", resourcelocation, runtimeexception);
            return null;
        }
        catch (IOException ioexception1)
        {
            LOGGER.error("Using missing texture, unable to load {}", resourcelocation, ioexception1);
            return null;
        }
    }

    public ResourceLocation getResourceLocation(ResourceLocation pLocation)
    {
        return this.isAbsoluteLocation(pLocation) ? new ResourceLocation(pLocation.getNamespace(), pLocation.getPath() + ".png") : new ResourceLocation(pLocation.getNamespace(), String.format("textures/%s%s", pLocation.getPath(), ".png"));
    }

    public void cycleAnimationFrames()
    {
        boolean flag = false;
        boolean flag1 = false;

        if (!this.animatedTextures.isEmpty())
        {
            this.bind();
        }

        int i = 0;

        for (Tickable tickable : this.animatedTextures)
        {
            if (tickable instanceof TextureAtlasSprite.AnimatedTexture)
            {
                TextureAtlasSprite textureatlassprite = ((TextureAtlasSprite.AnimatedTexture)tickable).getSprite();

                if (this.isAnimationEnabled(textureatlassprite))
                {
                    tickable.tick();

                    if (textureatlassprite.isAnimationActive())
                    {
                        ++i;
                    }

                    if (textureatlassprite.spriteNormal != null)
                    {
                        flag = true;
                    }

                    if (textureatlassprite.spriteSpecular != null)
                    {
                        flag1 = true;
                    }
                }
            }
            else
            {
                tickable.tick();
            }
        }

        if (Config.isShaders())
        {
            if (flag)
            {
                GlStateManager._bindTexture(this.getMultiTexID().norm);

                for (Tickable tickable1 : this.animatedTextures)
                {
                    if (tickable1 instanceof TextureAtlasSprite.AnimatedTexture)
                    {
                        TextureAtlasSprite textureatlassprite1 = ((TextureAtlasSprite.AnimatedTexture)tickable1).getSprite();

                        if (textureatlassprite1.spriteNormal != null && this.isAnimationEnabled(textureatlassprite1) && textureatlassprite1.isAnimationActive())
                        {
                            textureatlassprite1.spriteNormal.updateAnimation();

                            if (textureatlassprite1.spriteNormal.isAnimationActive())
                            {
                                ++i;
                            }
                        }
                    }
                }
            }

            if (flag1)
            {
                GlStateManager._bindTexture(this.getMultiTexID().spec);

                for (Tickable tickable2 : this.animatedTextures)
                {
                    if (tickable2 instanceof TextureAtlasSprite.AnimatedTexture)
                    {
                        TextureAtlasSprite textureatlassprite2 = ((TextureAtlasSprite.AnimatedTexture)tickable2).getSprite();

                        if (textureatlassprite2.spriteSpecular != null && this.isAnimationEnabled(textureatlassprite2) && textureatlassprite2.isAnimationActive())
                        {
                            textureatlassprite2.spriteSpecular.updateAnimation();

                            if (textureatlassprite2.spriteSpecular.isAnimationActive())
                            {
                                ++i;
                            }
                        }
                    }
                }
            }

            if (flag || flag1)
            {
                GlStateManager._bindTexture(this.getId());
            }
        }

        if (Config.isMultiTexture())
        {
            for (Tickable tickable3 : this.animatedTextures)
            {
                if (tickable3 instanceof TextureAtlasSprite.AnimatedTexture)
                {
                    TextureAtlasSprite textureatlassprite3 = ((TextureAtlasSprite.AnimatedTexture)tickable3).getSprite();

                    if (this.isAnimationEnabled(textureatlassprite3) && textureatlassprite3.isAnimationActive())
                    {
                        i += updateAnimationSingle(textureatlassprite3);

                        if (textureatlassprite3.spriteNormal != null)
                        {
                            i += updateAnimationSingle(textureatlassprite3.spriteNormal);
                        }

                        if (textureatlassprite3.spriteSpecular != null)
                        {
                            i += updateAnimationSingle(textureatlassprite3.spriteSpecular);
                        }
                    }
                }
            }

            GlStateManager._bindTexture(this.getId());
        }

        if (this.terrain)
        {
            int j = Config.getMinecraft().levelRenderer.getFrameCount();

            if (j != this.frameCountAnimations)
            {
                this.countAnimationsActive = i;
                this.frameCountAnimations = j;
            }

            if (SmartAnimations.isActive())
            {
                SmartAnimations.resetSpritesRendered(this);
            }
        }
    }

    public void tick()
    {
        if (!RenderSystem.isOnRenderThread())
        {
            RenderSystem.recordRenderCall(this::cycleAnimationFrames);
        }
        else
        {
            this.cycleAnimationFrames();
        }
    }

    public TextureAtlasSprite getSprite(ResourceLocation pLocation)
    {
        TextureAtlasSprite textureatlassprite = this.texturesByName.get(pLocation);
        return textureatlassprite == null ? this.texturesByName.get(MissingTextureAtlasSprite.getLocation()) : textureatlassprite;
    }

    public void clearTextureData()
    {
        for (TextureAtlasSprite textureatlassprite : this.texturesByName.values())
        {
            textureatlassprite.close();
        }

        if (this.multiTexture)
        {
            for (TextureAtlasSprite textureatlassprite1 : this.texturesByName.values())
            {
                textureatlassprite1.deleteSpriteTexture();

                if (textureatlassprite1.spriteNormal != null)
                {
                    textureatlassprite1.spriteNormal.deleteSpriteTexture();
                }

                if (textureatlassprite1.spriteSpecular != null)
                {
                    textureatlassprite1.spriteSpecular.deleteSpriteTexture();
                }
            }
        }

        this.texturesByName.clear();
        this.animatedTextures.clear();
    }

    public ResourceLocation location()
    {
        return this.location;
    }

    public void updateFilter(TextureAtlas.Preparations pSheetData)
    {
        this.setFilter(false, pSheetData.mipLevel > 0);
    }

    private boolean isAbsoluteLocation(ResourceLocation loc)
    {
        String s = loc.getPath();
        return this.isAbsoluteLocationPath(s);
    }

    private boolean isAbsoluteLocationPath(String resPath)
    {
        String s = resPath.toLowerCase();
        return s.startsWith("optifine/");
    }

    public TextureAtlasSprite getRegisteredSprite(String name)
    {
        ResourceLocation resourcelocation = new ResourceLocation(name);
        return this.getRegisteredSprite(resourcelocation);
    }

    public TextureAtlasSprite getRegisteredSprite(ResourceLocation loc)
    {
        return this.mapRegisteredSprites.get(loc);
    }

    public TextureAtlasSprite getUploadedSprite(String name)
    {
        ResourceLocation resourcelocation = new ResourceLocation(name);
        return this.getUploadedSprite(resourcelocation);
    }

    public TextureAtlasSprite getUploadedSprite(ResourceLocation loc)
    {
        return this.texturesByName.get(loc);
    }

    private boolean isAnimationEnabled(TextureAtlasSprite ts)
    {
        if (!this.terrain)
        {
            return true;
        }
        else if (ts != TextureUtils.iconWaterStill && ts != TextureUtils.iconWaterFlow)
        {
            if (ts != TextureUtils.iconLavaStill && ts != TextureUtils.iconLavaFlow)
            {
                if (ts != TextureUtils.iconFireLayer0 && ts != TextureUtils.iconFireLayer1)
                {
                    if (ts != TextureUtils.iconSoulFireLayer0 && ts != TextureUtils.iconSoulFireLayer1)
                    {
                        if (ts != TextureUtils.iconCampFire && ts != TextureUtils.iconCampFireLogLit)
                        {
                            if (ts != TextureUtils.iconSoulCampFire && ts != TextureUtils.iconSoulCampFireLogLit)
                            {
                                return ts == TextureUtils.iconPortal ? Config.isAnimatedPortal() : Config.isAnimatedTerrain();
                            }
                            else
                            {
                                return Config.isAnimatedFire();
                            }
                        }
                        else
                        {
                            return Config.isAnimatedFire();
                        }
                    }
                    else
                    {
                        return Config.isAnimatedFire();
                    }
                }
                else
                {
                    return Config.isAnimatedFire();
                }
            }
            else
            {
                return Config.isAnimatedLava();
            }
        }
        else
        {
            return Config.isAnimatedWater();
        }
    }

    private static void uploadMipmapsSingle(TextureAtlasSprite tas)
    {
        TextureAtlasSprite textureatlassprite = tas.spriteSingle;

        if (textureatlassprite != null)
        {
            textureatlassprite.setAnimationIndex(tas.getAnimationIndex());
            tas.bindSpriteTexture();

            try
            {
                textureatlassprite.uploadFirstFrame();
            }
            catch (Exception exception)
            {
                Config.dbg("Error uploading sprite single: " + textureatlassprite + ", parent: " + tas);
                exception.printStackTrace();
            }
        }
    }

    private static int updateAnimationSingle(TextureAtlasSprite tas)
    {
        TextureAtlasSprite textureatlassprite = tas.spriteSingle;

        if (textureatlassprite != null)
        {
            tas.bindSpriteTexture();
            textureatlassprite.updateAnimation();

            if (textureatlassprite.isAnimationActive())
            {
                return 1;
            }
        }

        return 0;
    }

    public int getCountRegisteredSprites()
    {
        return this.counterIndexInMap.getValue();
    }

    private int detectMaxMipmapLevel(Set<ResourceLocation> setSpriteLocations, ResourceManager rm)
    {
        int i = this.detectMinimumSpriteSize(setSpriteLocations, rm, 20);

        if (i < 16)
        {
            i = 16;
        }

        i = Mth.smallestEncompassingPowerOfTwo(i);

        if (i > 16)
        {
            Config.log("Sprite size: " + i);
        }

        int j = Mth.log2(i);

        if (j < 4)
        {
            j = 4;
        }

        return j;
    }

    private int detectMinimumSpriteSize(Set<ResourceLocation> setSpriteLocations, ResourceManager rm, int percentScale)
    {
        Map map = new HashMap();

        for (ResourceLocation resourcelocation : setSpriteLocations)
        {
            ResourceLocation resourcelocation1 = this.getResourceLocation(resourcelocation);

            try
            {
                Resource resource = rm.getResource(resourcelocation1);

                if (resource != null)
                {
                    InputStream inputstream = resource.getInputStream();

                    if (inputstream != null)
                    {
                        Dimension dimension = TextureUtils.getImageSize(inputstream, "png");
                        inputstream.close();

                        if (dimension != null)
                        {
                            int i = dimension.width;
                            int j = Mth.smallestEncompassingPowerOfTwo(i);

                            if (!map.containsKey(j))
                            {
                                map.put(j, 1);
                            }
                            else
                            {
                                int k = (int) map.get(j);
                                map.put(j, k + 1);
                            }
                        }
                    }
                }
            }
            catch (Exception exception)
            {
            }
        }

        int l = 0;
        Set set = map.keySet();
        Set set1 = new TreeSet(set);

        for (int j1 : (Set<Integer>)(Set<?>)set1)
        {
            int l1 = (int) map.get(j1);
            l += l1;
        }

        int i1 = 16;
        int k1 = 0;
        int i2 = l * percentScale / 100;

        for (int j2 : (Set<Integer>)(Set<?>)set1)
        {
            int k2 = (int) map.get(j2);
            k1 += k2;

            if (j2 > i1)
            {
                i1 = j2;
            }

            if (k1 > i2)
            {
                return i1;
            }
        }

        return i1;
    }

    private static int getMinSpriteSize(int mipmapLevels)
    {
        int i = 1 << mipmapLevels;

        if (i < 8)
        {
            i = 8;
        }

        return i;
    }

    private static TextureAtlasSprite.Info fixSpriteSize(TextureAtlasSprite.Info info, int minSpriteSize)
    {
        if (info.width() >= minSpriteSize && info.height() >= minSpriteSize)
        {
            return info;
        }
        else
        {
            int i = Math.max(info.width(), minSpriteSize);
            int j = Math.max(info.height(), minSpriteSize);
            return new TextureAtlasSprite.Info(info.name(), i, j, info.getSpriteAnimationMetadata());
        }
    }

    public boolean isTextureBound()
    {
        int i = GlStateManager.getBoundTexture();
        int j = this.getId();
        return i == j;
    }

    private void updateIconGrid(int sheetWidth, int sheetHeight)
    {
        this.iconGridCountX = -1;
        this.iconGridCountY = -1;
        this.iconGrid = null;

        if (this.iconGridSize > 0)
        {
            this.iconGridCountX = sheetWidth / this.iconGridSize;
            this.iconGridCountY = sheetHeight / this.iconGridSize;
            this.iconGrid = new TextureAtlasSprite[this.iconGridCountX * this.iconGridCountY];
            this.iconGridSizeU = 1.0D / (double)this.iconGridCountX;
            this.iconGridSizeV = 1.0D / (double)this.iconGridCountY;

            for (TextureAtlasSprite textureatlassprite : this.texturesByName.values())
            {
                double d0 = 0.5D / (double)sheetWidth;
                double d1 = 0.5D / (double)sheetHeight;
                double d2 = (double)Math.min(textureatlassprite.getU0(), textureatlassprite.getU1()) + d0;
                double d3 = (double)Math.min(textureatlassprite.getV0(), textureatlassprite.getV1()) + d1;
                double d4 = (double)Math.max(textureatlassprite.getU0(), textureatlassprite.getU1()) - d0;
                double d5 = (double)Math.max(textureatlassprite.getV0(), textureatlassprite.getV1()) - d1;
                int i = (int)(d2 / this.iconGridSizeU);
                int j = (int)(d3 / this.iconGridSizeV);
                int k = (int)(d4 / this.iconGridSizeU);
                int l = (int)(d5 / this.iconGridSizeV);

                for (int i1 = i; i1 <= k; ++i1)
                {
                    if (i1 >= 0 && i1 < this.iconGridCountX)
                    {
                        for (int j1 = j; j1 <= l; ++j1)
                        {
                            if (j1 >= 0 && j1 < this.iconGridCountX)
                            {
                                int k1 = j1 * this.iconGridCountX + i1;
                                this.iconGrid[k1] = textureatlassprite;
                            }
                            else
                            {
                                Config.warn("Invalid grid V: " + j1 + ", icon: " + textureatlassprite.getName());
                            }
                        }
                    }
                    else
                    {
                        Config.warn("Invalid grid U: " + i1 + ", icon: " + textureatlassprite.getName());
                    }
                }
            }
        }
    }

    public TextureAtlasSprite getIconByUV(double u, double v)
    {
        if (this.iconGrid == null)
        {
            return null;
        }
        else
        {
            int i = (int)(u / this.iconGridSizeU);
            int j = (int)(v / this.iconGridSizeV);
            int k = j * this.iconGridCountX + i;
            return k >= 0 && k <= this.iconGrid.length ? this.iconGrid[k] : null;
        }
    }

    public int getCountAnimations()
    {
        return this.animatedTextures.size();
    }

    public int getCountAnimationsActive()
    {
        return this.countAnimationsActive;
    }

    public TextureAtlasSprite registerSprite(ResourceLocation location)
    {
        if (location == null)
        {
            throw new IllegalArgumentException("Location cannot be null!");
        }
        else
        {
            TextureAtlasSprite textureatlassprite = this.mapRegisteredSprites.get(location);

            if (textureatlassprite != null)
            {
                return textureatlassprite;
            }
            else
            {
                this.sprites.add(location);
                textureatlassprite = new TextureAtlasSprite(location);
                this.mapRegisteredSprites.put(location, textureatlassprite);
                textureatlassprite.updateIndexInMap(this.counterIndexInMap);
                return textureatlassprite;
            }
        }
    }

    public Collection<TextureAtlasSprite> getRegisteredSprites()
    {
        return Collections.unmodifiableCollection(this.mapRegisteredSprites.values());
    }

    public boolean isTerrain()
    {
        return this.terrain;
    }

    public CounterInt getCounterIndexInMap()
    {
        return this.counterIndexInMap;
    }

    private void onSpriteMissing(ResourceLocation loc)
    {
        TextureAtlasSprite textureatlassprite = this.mapRegisteredSprites.get(loc);

        if (textureatlassprite != null)
        {
            this.mapMissingSprites.put(loc, textureatlassprite);
        }
    }

    private static <T> Set<T> newHashSet(Set<T> set1, Set<T> set2)
    {
        Set<T> set = new HashSet<>();
        set.addAll(set1);
        set.addAll(set2);
        return set;
    }

    public int getMipmapLevel()
    {
        return this.mipmapLevel;
    }

    public boolean isMipmaps()
    {
        return this.mipmapLevel > 0;
    }

    public ITextureFormat getTextureFormat()
    {
        return this.textureFormat;
    }

    public IColorBlender getShadersColorBlender(ShadersTextureType typeIn)
    {
        if (typeIn == null)
        {
            return null;
        }
        else
        {
            return (IColorBlender)(this.textureFormat != null ? this.textureFormat.getColorBlender(typeIn) : new ColorBlenderLinear());
        }
    }

    public boolean isTextureBlend(ShadersTextureType typeIn)
    {
        if (typeIn == null)
        {
            return true;
        }
        else
        {
            return this.textureFormat != null ? this.textureFormat.isTextureBlend(typeIn) : true;
        }
    }

    public boolean isNormalBlend()
    {
        return this.isTextureBlend(ShadersTextureType.NORMAL);
    }

    public boolean isSpecularBlend()
    {
        return this.isTextureBlend(ShadersTextureType.SPECULAR);
    }

    public String toString()
    {
        return "" + this.location;
    }

    public static class Preparations
    {
        final Set<ResourceLocation> sprites;
        final int width;
        final int height;
        final int mipLevel;
        final List<TextureAtlasSprite> regions;

        public Preparations(Set<ResourceLocation> p_118337_, int p_118338_, int p_118339_, int p_118340_, List<TextureAtlasSprite> p_118341_)
        {
            this.sprites = p_118337_;
            this.width = p_118338_;
            this.height = p_118339_;
            this.mipLevel = p_118340_;
            this.regions = p_118341_;
        }
    }
}
