package net.minecraft.client.resources;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.hash.Hashing;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.InsecureTextureException;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import com.mojang.authlib.properties.Property;
import com.mojang.blaze3d.systems.RenderSystem;
import java.io.File;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.HttpTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;

public class SkinManager
{
    public static final String PROPERTY_TEXTURES = "textures";
    private final TextureManager textureManager;
    private final File skinsDirectory;
    private final MinecraftSessionService sessionService;
    private final LoadingCache<String, Map<Type, MinecraftProfileTexture>> insecureSkinCache;

    public SkinManager(TextureManager p_118812_, File p_118813_, final MinecraftSessionService p_118814_)
    {
        this.textureManager = p_118812_;
        this.skinsDirectory = p_118813_;
        this.sessionService = p_118814_;
        this.insecureSkinCache = CacheBuilder.newBuilder().expireAfterAccess(15L, TimeUnit.SECONDS).build(new CacheLoader<String, Map<Type, MinecraftProfileTexture>>()
        {
            public Map<Type, MinecraftProfileTexture> load(String p_118853_)
            {
                GameProfile gameprofile = new GameProfile((UUID)null, "dummy_mcdummyface");
                gameprofile.getProperties().put("textures", new Property("textures", p_118853_, ""));

                try
                {
                    return p_118814_.getTextures(gameprofile, false);
                }
                catch (Throwable throwable)
                {
                    return ImmutableMap.of();
                }
            }
        });
    }

    public ResourceLocation registerTexture(MinecraftProfileTexture pProfileTexture, Type pTextureType)
    {
        return this.registerTexture(pProfileTexture, pTextureType, (SkinManager.SkinTextureCallback)null);
    }

    private ResourceLocation registerTexture(MinecraftProfileTexture pProfileTexture, Type pTextureType, @Nullable SkinManager.SkinTextureCallback p_118831_)
    {
        String s = Hashing.sha1().hashUnencodedChars(pProfileTexture.getHash()).toString();
        ResourceLocation resourcelocation = new ResourceLocation("skins/" + s);
        AbstractTexture abstracttexture = this.textureManager.getTexture(resourcelocation, MissingTextureAtlasSprite.getTexture());

        if (abstracttexture == MissingTextureAtlasSprite.getTexture())
        {
            File file1 = new File(this.skinsDirectory, s.length() > 2 ? s.substring(0, 2) : "xx");
            File file2 = new File(file1, s);
            HttpTexture httptexture = new HttpTexture(file2, pProfileTexture.getUrl(), DefaultPlayerSkin.getDefaultSkin(), pTextureType == Type.SKIN, () ->
            {
                if (p_118831_ != null)
                {
                    p_118831_.onSkinTextureAvailable(pTextureType, resourcelocation, pProfileTexture);
                }
            });
            this.textureManager.register(resourcelocation, httptexture);
        }
        else if (p_118831_ != null)
        {
            p_118831_.onSkinTextureAvailable(pTextureType, resourcelocation, pProfileTexture);
        }

        return resourcelocation;
    }

    public void registerSkins(GameProfile pProfile, SkinManager.SkinTextureCallback pSkinAvailableCallback, boolean pRequireSecure)
    {
        Runnable runnable = () ->
        {
            Map<Type, MinecraftProfileTexture> map = Maps.newHashMap();

            try {
                map.putAll(this.sessionService.getTextures(pProfile, pRequireSecure));
            }
            catch (InsecureTextureException insecuretextureexception1)
            {
            }

            if (map.isEmpty())
            {
                pProfile.getProperties().clear();

                if (pProfile.getId().equals(Minecraft.getInstance().getUser().getGameProfile().getId()))
                {
                    pProfile.getProperties().putAll(Minecraft.getInstance().getProfileProperties());
                    map.putAll(this.sessionService.getTextures(pProfile, false));
                }
                else
                {
                    this.sessionService.fillProfileProperties(pProfile, pRequireSecure);

                    try
                    {
                        map.putAll(this.sessionService.getTextures(pProfile, pRequireSecure));
                    }
                    catch (InsecureTextureException insecuretextureexception)
                    {
                    }
                }
            }

            Minecraft.getInstance().execute(() -> {
                RenderSystem.recordRenderCall(() -> {
                    ImmutableList.of(Type.SKIN, Type.CAPE).forEach((p_174848_) -> {
                        if (map.containsKey(p_174848_))
                        {
                            this.registerTexture(map.get(p_174848_), p_174848_, pSkinAvailableCallback);
                        }
                    });
                });
            });
        };
        Util.backgroundExecutor().execute(runnable);
    }

    public Map<Type, MinecraftProfileTexture> getInsecureSkinInformation(GameProfile pProfile)
    {
        Property property = Iterables.getFirst(pProfile.getProperties().get("textures"), (Property)null);
        return (Map<Type, MinecraftProfileTexture>)(property == null ? ImmutableMap.of() : this.insecureSkinCache.getUnchecked(property.getValue()));
    }

    public interface SkinTextureCallback
    {
        void onSkinTextureAvailable(Type p_118857_, ResourceLocation p_118858_, MinecraftProfileTexture p_118859_);
    }
}
