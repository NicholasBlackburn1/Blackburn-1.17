package com.mojang.realmsclient.util;

import com.google.common.collect.Maps;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.util.UUIDTypeAdapter;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsTextureManager {
   private static final Map<String, RealmsTextureManager.RealmsTexture> TEXTURES = Maps.newHashMap();
   static final Map<String, Boolean> SKIN_FETCH_STATUS = Maps.newHashMap();
   static final Map<String, String> FETCHED_SKINS = Maps.newHashMap();
   static final Logger LOGGER = LogManager.getLogger();
   private static final ResourceLocation TEMPLATE_ICON_LOCATION = new ResourceLocation("textures/gui/presets/isles.png");

   public static void bindWorldTemplate(String p_90191_, @Nullable String p_90192_) {
      if (p_90192_ == null) {
         RenderSystem.setShaderTexture(0, TEMPLATE_ICON_LOCATION);
      } else {
         int i = getTextureId(p_90191_, p_90192_);
         RenderSystem.setShaderTexture(0, i);
      }
   }

   public static void withBoundFace(String p_90188_, Runnable p_90189_) {
      bindFace(p_90188_);
      p_90189_.run();
   }

   private static void bindDefaultFace(UUID p_90194_) {
      RenderSystem.setShaderTexture(0, DefaultPlayerSkin.getDefaultSkin(p_90194_));
   }

   private static void bindFace(final String p_90186_) {
      UUID uuid = UUIDTypeAdapter.fromString(p_90186_);
      if (TEXTURES.containsKey(p_90186_)) {
         int j = (TEXTURES.get(p_90186_)).textureId;
         RenderSystem.setShaderTexture(0, j);
      } else if (SKIN_FETCH_STATUS.containsKey(p_90186_)) {
         if (!SKIN_FETCH_STATUS.get(p_90186_)) {
            bindDefaultFace(uuid);
         } else if (FETCHED_SKINS.containsKey(p_90186_)) {
            int i = getTextureId(p_90186_, FETCHED_SKINS.get(p_90186_));
            RenderSystem.setShaderTexture(0, i);
         } else {
            bindDefaultFace(uuid);
         }

      } else {
         SKIN_FETCH_STATUS.put(p_90186_, false);
         bindDefaultFace(uuid);
         Thread thread = new Thread("Realms Texture Downloader") {
            public void run() {
               Map<Type, MinecraftProfileTexture> map = RealmsUtil.getTextures(p_90186_);
               if (map.containsKey(Type.SKIN)) {
                  MinecraftProfileTexture minecraftprofiletexture = map.get(Type.SKIN);
                  String s = minecraftprofiletexture.getUrl();
                  HttpURLConnection httpurlconnection = null;
                  RealmsTextureManager.LOGGER.debug("Downloading http texture from {}", (Object)s);

                  try {
                     httpurlconnection = (HttpURLConnection)(new URL(s)).openConnection(Minecraft.getInstance().getProxy());
                     httpurlconnection.setDoInput(true);
                     httpurlconnection.setDoOutput(false);
                     httpurlconnection.connect();
                     if (httpurlconnection.getResponseCode() / 100 == 2) {
                        BufferedImage bufferedimage;
                        try {
                           bufferedimage = ImageIO.read(httpurlconnection.getInputStream());
                        } catch (Exception exception) {
                           RealmsTextureManager.SKIN_FETCH_STATUS.remove(p_90186_);
                           return;
                        } finally {
                           IOUtils.closeQuietly(httpurlconnection.getInputStream());
                        }

                        bufferedimage = (new SkinProcessor()).process(bufferedimage);
                        ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
                        ImageIO.write(bufferedimage, "png", bytearrayoutputstream);
                        RealmsTextureManager.FETCHED_SKINS.put(p_90186_, (new Base64()).encodeToString(bytearrayoutputstream.toByteArray()));
                        RealmsTextureManager.SKIN_FETCH_STATUS.put(p_90186_, true);
                        return;
                     }

                     RealmsTextureManager.SKIN_FETCH_STATUS.remove(p_90186_);
                  } catch (Exception exception1) {
                     RealmsTextureManager.LOGGER.error("Couldn't download http texture", (Throwable)exception1);
                     RealmsTextureManager.SKIN_FETCH_STATUS.remove(p_90186_);
                     return;
                  } finally {
                     if (httpurlconnection != null) {
                        httpurlconnection.disconnect();
                     }

                  }

               } else {
                  RealmsTextureManager.SKIN_FETCH_STATUS.put(p_90186_, true);
               }
            }
         };
         thread.setDaemon(true);
         thread.start();
      }
   }

   private static int getTextureId(String p_90197_, String p_90198_) {
      RealmsTextureManager.RealmsTexture realmstexturemanager$realmstexture = TEXTURES.get(p_90197_);
      if (realmstexturemanager$realmstexture != null && realmstexturemanager$realmstexture.image.equals(p_90198_)) {
         return realmstexturemanager$realmstexture.textureId;
      } else {
         int i;
         if (realmstexturemanager$realmstexture != null) {
            i = realmstexturemanager$realmstexture.textureId;
         } else {
            i = GlStateManager._genTexture();
         }

         IntBuffer intbuffer = null;
         int j = 0;
         int k = 0;

         try {
            InputStream inputstream = new ByteArrayInputStream((new Base64()).decode(p_90198_));

            BufferedImage bufferedimage;
            try {
               bufferedimage = ImageIO.read(inputstream);
            } finally {
               IOUtils.closeQuietly(inputstream);
            }

            j = bufferedimage.getWidth();
            k = bufferedimage.getHeight();
            int[] aint = new int[j * k];
            bufferedimage.getRGB(0, 0, j, k, aint, 0, j);
            intbuffer = ByteBuffer.allocateDirect(4 * j * k).order(ByteOrder.nativeOrder()).asIntBuffer();
            intbuffer.put(aint);
            intbuffer.flip();
         } catch (IOException ioexception) {
            ioexception.printStackTrace();
         }

         RenderSystem.activeTexture(33984);
         RenderSystem.bindTextureForSetup(i);
         TextureUtil.initTexture(intbuffer, j, k);
         TEXTURES.put(p_90197_, new RealmsTextureManager.RealmsTexture(p_90198_, i));
         return i;
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class RealmsTexture {
      final String image;
      final int textureId;

      public RealmsTexture(String p_90208_, int p_90209_) {
         this.image = p_90208_;
         this.textureId = p_90209_;
      }
   }
}