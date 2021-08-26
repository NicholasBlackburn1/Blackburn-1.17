package net.minecraft.client.renderer;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import net.optifine.Config;
import net.optifine.CustomColors;
import net.optifine.shaders.Shaders;
import net.optifine.util.TextureUtils;

public class LightTexture implements AutoCloseable
{
    public static final int FULL_BRIGHT = 15728880;
    public static final int FULL_SKY = 15728640;
    public static final int FULL_BLOCK = 240;
    private final DynamicTexture lightTexture;
    private final NativeImage lightPixels;
    private final ResourceLocation lightTextureLocation;
    private boolean updateLightTexture;
    private float blockLightRedFlicker;
    private final GameRenderer renderer;
    private final Minecraft minecraft;
    private boolean allowed = true;
    private boolean custom = false;
    private Vector3f tempVector = new Vector3f();
    public static final int MAX_BRIGHTNESS = pack(15, 15);

    public LightTexture(GameRenderer p_109878_, Minecraft p_109879_)
    {
        this.renderer = p_109878_;
        this.minecraft = p_109879_;
        this.lightTexture = new DynamicTexture(16, 16, false);
        this.lightTextureLocation = this.minecraft.getTextureManager().register("light_map", this.lightTexture);
        this.lightPixels = this.lightTexture.getPixels();

        for (int i = 0; i < 16; ++i)
        {
            for (int j = 0; j < 16; ++j)
            {
                this.lightPixels.setPixelRGBA(j, i, -1);
            }
        }

        this.lightTexture.upload();
    }

    public void close()
    {
        this.lightTexture.close();
    }

    public void tick()
    {
        this.blockLightRedFlicker = (float)((double)this.blockLightRedFlicker + (Math.random() - Math.random()) * Math.random() * Math.random() * 0.1D);
        this.blockLightRedFlicker = (float)((double)this.blockLightRedFlicker * 0.9D);
        this.updateLightTexture = true;
    }

    public void turnOffLightLayer()
    {
        RenderSystem.setShaderTexture(2, 0);

        if (Config.isShaders())
        {
            Shaders.disableLightmap();
        }
    }

    public void turnOnLightLayer()
    {
        if (!this.allowed)
        {
            RenderSystem.setShaderTexture(2, TextureUtils.WHITE_TEXTURE_LOCATION);
            this.minecraft.getTextureManager().bindForSetup(TextureUtils.WHITE_TEXTURE_LOCATION);
        }
        else
        {
            RenderSystem.setShaderTexture(2, this.lightTextureLocation);
            this.minecraft.getTextureManager().bindForSetup(this.lightTextureLocation);
        }

        RenderSystem.texParameter(3553, 10241, 9729);
        RenderSystem.texParameter(3553, 10240, 9729);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        if (Config.isShaders())
        {
            Shaders.enableLightmap();
        }
    }

    public void updateLightTexture(float pPartialTicks)
    {
        if (this.updateLightTexture)
        {
            this.updateLightTexture = false;
            this.minecraft.getProfiler().push("lightTex");
            ClientLevel clientlevel = this.minecraft.level;

            if (clientlevel != null)
            {
                this.custom = false;

                if (Config.isCustomColors())
                {
                    boolean flag = this.minecraft.player.hasEffect(MobEffects.NIGHT_VISION) || this.minecraft.player.hasEffect(MobEffects.CONDUIT_POWER);

                    if (CustomColors.updateLightmap(clientlevel, this.blockLightRedFlicker, this.lightPixels, flag, pPartialTicks))
                    {
                        this.lightTexture.upload();
                        this.updateLightTexture = false;
                        this.minecraft.getProfiler().pop();
                        this.custom = true;
                        return;
                    }
                }

                float f9 = clientlevel.getSkyDarken(1.0F);
                float f;

                if (clientlevel.getSkyFlashTime() > 0)
                {
                    f = 1.0F;
                }
                else
                {
                    f = f9 * 0.95F + 0.05F;
                }

                float f1 = this.minecraft.player.getWaterVision();
                float f2;

                if (this.minecraft.player.hasEffect(MobEffects.NIGHT_VISION))
                {
                    f2 = GameRenderer.getNightVisionScale(this.minecraft.player, pPartialTicks);
                }
                else if (f1 > 0.0F && this.minecraft.player.hasEffect(MobEffects.CONDUIT_POWER))
                {
                    f2 = f1;
                }
                else
                {
                    f2 = 0.0F;
                }

                Vector3f vector3f = new Vector3f(f9, f9, 1.0F);
                vector3f.lerp(new Vector3f(1.0F, 1.0F, 1.0F), 0.35F);
                float f3 = this.blockLightRedFlicker + 1.5F;
                Vector3f vector3f1 = new Vector3f();

                for (int i = 0; i < 16; ++i)
                {
                    for (int j = 0; j < 16; ++j)
                    {
                        float f4 = this.getBrightness(clientlevel, i) * f;
                        float f5 = this.getBrightness(clientlevel, j) * f3;
                        float f6 = f5 * ((f5 * 0.6F + 0.4F) * 0.6F + 0.4F);
                        float f7 = f5 * (f5 * f5 * 0.6F + 0.4F);
                        vector3f1.set(f5, f6, f7);

                        if (clientlevel.effects().forceBrightLightmap())
                        {
                            vector3f1.lerp(this.getTempVector3f(0.99F, 1.12F, 1.0F), 0.25F);
                        }
                        else
                        {
                            Vector3f vector3f2 = this.getTempCopy(vector3f);
                            vector3f2.mul(f4);
                            vector3f1.add(vector3f2);
                            vector3f1.lerp(this.getTempVector3f(0.75F, 0.75F, 0.75F), 0.04F);

                            if (this.renderer.getDarkenWorldAmount(pPartialTicks) > 0.0F)
                            {
                                float f8 = this.renderer.getDarkenWorldAmount(pPartialTicks);
                                Vector3f vector3f3 = this.getTempCopy(vector3f1);
                                vector3f3.mul(0.7F, 0.6F, 0.6F);
                                vector3f1.lerp(vector3f3, f8);
                            }
                        }

                        vector3f1.clamp(0.0F, 1.0F);

                        if (f2 > 0.0F)
                        {
                            float f10 = Math.max(vector3f1.x(), Math.max(vector3f1.y(), vector3f1.z()));

                            if (f10 < 1.0F)
                            {
                                float f12 = 1.0F / f10;
                                Vector3f vector3f5 = this.getTempCopy(vector3f1);
                                vector3f5.mul(f12);
                                vector3f1.lerp(vector3f5, f2);
                            }
                        }

                        float f11 = (float)this.minecraft.options.gamma;
                        Vector3f vector3f4 = this.getTempCopy(vector3f1);
                        vector3f4.map(this::notGamma);
                        vector3f1.lerp(vector3f4, f11);
                        vector3f1.lerp(this.getTempVector3f(0.75F, 0.75F, 0.75F), 0.04F);
                        vector3f1.clamp(0.0F, 1.0F);
                        vector3f1.mul(255.0F);
                        int j1 = 255;
                        int k = (int)vector3f1.x();
                        int l = (int)vector3f1.y();
                        int i1 = (int)vector3f1.z();
                        this.lightPixels.setPixelRGBA(j, i, -16777216 | i1 << 16 | l << 8 | k);
                    }
                }

                this.lightTexture.upload();
                this.minecraft.getProfiler().pop();
            }
        }
    }

    private float notGamma(float pValue)
    {
        float f = 1.0F - pValue;
        return 1.0F - f * f * f * f;
    }

    private float getBrightness(Level pLevel, int pLightLevel)
    {
        return pLevel.dimensionType().brightness(pLightLevel);
    }

    public static int pack(int pBlockLight, int pSkyLight)
    {
        return pBlockLight << 4 | pSkyLight << 20;
    }

    public static int block(int pPackedLight)
    {
        return (pPackedLight & 65535) >> 4;
    }

    public static int sky(int pPackedLight)
    {
        return pPackedLight >> 20 & 65535;
    }

    private Vector3f getTempVector3f(float x, float y, float z)
    {
        this.tempVector.set(x, y, z);
        return this.tempVector;
    }

    private Vector3f getTempCopy(Vector3f vec)
    {
        this.tempVector.set(vec.x(), vec.y(), vec.z());
        return this.tempVector;
    }

    public boolean isAllowed()
    {
        return this.allowed;
    }

    public void setAllowed(boolean allowed)
    {
        this.allowed = allowed;
    }

    public boolean isCustom()
    {
        return this.custom;
    }
}
