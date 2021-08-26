package net.minecraft.client.player;

import com.google.common.hash.Hashing;
import com.mojang.authlib.GameProfile;
import java.io.File;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.HttpTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.ShoulderRidingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.optifine.Config;
import net.optifine.player.CapeUtils;
import net.optifine.player.PlayerConfigurations;
import net.optifine.reflect.Reflector;

public abstract class AbstractClientPlayer extends Player
{
    private static final String SKIN_URL_TEMPLATE = "http://skins.minecraft.net/MinecraftSkins/%s.png";
    public static final int SKIN_HEAD_U = 8;
    public static final int SKIN_HEAD_V = 8;
    public static final int SKIN_HEAD_WIDTH = 8;
    public static final int SKIN_HEAD_HEIGHT = 8;
    public static final int SKIN_HAT_U = 40;
    public static final int SKIN_HAT_V = 8;
    public static final int SKIN_HAT_WIDTH = 8;
    public static final int SKIN_HAT_HEIGHT = 8;
    public static final int SKIN_TEX_WIDTH = 64;
    public static final int SKIN_TEX_HEIGHT = 64;
    private PlayerInfo playerInfo;
    public float elytraRotX;
    public float elytraRotY;
    public float elytraRotZ;
    public final ClientLevel clientLevel;
    private ResourceLocation locationOfCape = null;
    private long reloadCapeTimeMs = 0L;
    private boolean elytraOfCape = false;
    private String nameClear = null;
    public ShoulderRidingEntity entityShoulderLeft;
    public ShoulderRidingEntity entityShoulderRight;
    public float capeRotateX;
    public float capeRotateY;
    public float capeRotateZ;
    private static final ResourceLocation TEXTURE_ELYTRA = new ResourceLocation("textures/entity/elytra.png");

    public AbstractClientPlayer(ClientLevel p_108548_, GameProfile p_108549_)
    {
        super(p_108548_, p_108548_.getSharedSpawnPos(), p_108548_.getSharedSpawnAngle(), p_108549_);
        this.clientLevel = p_108548_;
        this.nameClear = p_108549_.getName();

        if (this.nameClear != null && !this.nameClear.isEmpty())
        {
            this.nameClear = StringUtil.stripColor(this.nameClear);
        }

        CapeUtils.downloadCape(this);
        PlayerConfigurations.getPlayerConfiguration(this);
    }

    public boolean isSpectator()
    {
        PlayerInfo playerinfo = Minecraft.getInstance().getConnection().getPlayerInfo(this.getGameProfile().getId());
        return playerinfo != null && playerinfo.getGameMode() == GameType.SPECTATOR;
    }

    public boolean isCreative()
    {
        PlayerInfo playerinfo = Minecraft.getInstance().getConnection().getPlayerInfo(this.getGameProfile().getId());
        return playerinfo != null && playerinfo.getGameMode() == GameType.CREATIVE;
    }

    public boolean isCapeLoaded()
    {
        return this.getPlayerInfo() != null;
    }

    @Nullable
    protected PlayerInfo getPlayerInfo()
    {
        if (this.playerInfo == null)
        {
            this.playerInfo = Minecraft.getInstance().getConnection().getPlayerInfo(this.getUUID());
        }

        return this.playerInfo;
    }

    public boolean isSkinLoaded()
    {
        PlayerInfo playerinfo = this.getPlayerInfo();
        return playerinfo != null && playerinfo.isSkinLoaded();
    }

    public ResourceLocation getSkinTextureLocation()
    {
        PlayerInfo playerinfo = this.getPlayerInfo();
        return playerinfo == null ? DefaultPlayerSkin.getDefaultSkin(this.getUUID()) : playerinfo.getSkinLocation();
    }

    @Nullable
    public ResourceLocation getCloakTextureLocation()
    {
        if (!Config.isShowCapes())
        {
            return null;
        }
        else
        {
            if (this.reloadCapeTimeMs != 0L && System.currentTimeMillis() > this.reloadCapeTimeMs)
            {
                CapeUtils.reloadCape(this);
                this.reloadCapeTimeMs = 0L;
            }

            if (this.locationOfCape != null)
            {
                return this.locationOfCape;
            }
            else
            {
                PlayerInfo playerinfo = this.getPlayerInfo();
                return playerinfo == null ? null : playerinfo.getCapeLocation();
            }
        }
    }

    public boolean isElytraLoaded()
    {
        return this.getPlayerInfo() != null;
    }

    @Nullable
    public ResourceLocation getElytraTextureLocation()
    {
        PlayerInfo playerinfo = this.getPlayerInfo();
        return playerinfo == null ? null : playerinfo.getElytraLocation();
    }

    public static void registerSkinTexture(ResourceLocation p_172522_, String p_172523_)
    {
        TextureManager texturemanager = Minecraft.getInstance().getTextureManager();
        AbstractTexture abstracttexture = texturemanager.getTexture(p_172522_, MissingTextureAtlasSprite.getTexture());

        if (abstracttexture == MissingTextureAtlasSprite.getTexture())
        {
            AbstractTexture abstracttexture1 = new HttpTexture((File)null, String.format("http://skins.minecraft.net/MinecraftSkins/%s.png", StringUtil.stripColor(p_172523_)), DefaultPlayerSkin.getDefaultSkin(createPlayerUUID(p_172523_)), true, (Runnable)null);
            texturemanager.register(p_172522_, abstracttexture1);
        }
    }

    public static ResourceLocation getSkinLocation(String pUsername)
    {
        return new ResourceLocation("skins/" + Hashing.sha1().hashUnencodedChars(StringUtil.stripColor(pUsername)));
    }

    public String getModelName()
    {
        PlayerInfo playerinfo = this.getPlayerInfo();
        return playerinfo == null ? DefaultPlayerSkin.getSkinModelName(this.getUUID()) : playerinfo.getModelName();
    }

    public float getFieldOfViewModifier()
    {
        float f = 1.0F;

        if (this.getAbilities().flying)
        {
            f *= 1.1F;
        }

        f = (float)((double)f * ((this.getAttributeValue(Attributes.MOVEMENT_SPEED) / (double)this.getAbilities().getWalkingSpeed() + 1.0D) / 2.0D));

        if (this.getAbilities().getWalkingSpeed() == 0.0F || Float.isNaN(f) || Float.isInfinite(f))
        {
            f = 1.0F;
        }

        ItemStack itemstack = this.getUseItem();

        if (this.isUsingItem())
        {
            if (itemstack.getItem() instanceof BowItem)
            {
                int i = this.getTicksUsingItem();
                float f1 = (float)i / 20.0F;

                if (f1 > 1.0F)
                {
                    f1 = 1.0F;
                }
                else
                {
                    f1 = f1 * f1;
                }

                f *= 1.0F - f1 * 0.15F;
            }
            else if (Minecraft.getInstance().options.getCameraType().isFirstPerson() && this.isScoping())
            {
                return 0.1F;
            }
        }

        return Reflector.ForgeHooksClient_getOffsetFOV.exists() ? Reflector.callFloat(Reflector.ForgeHooksClient_getOffsetFOV, this, f) : Mth.lerp(Minecraft.getInstance().options.fovEffectScale, 1.0F, f);
    }

    public String getNameClear()
    {
        return this.nameClear;
    }

    public ResourceLocation getLocationOfCape()
    {
        return this.locationOfCape;
    }

    public void setLocationOfCape(ResourceLocation locationOfCape)
    {
        this.locationOfCape = locationOfCape;
    }

    public boolean hasElytraCape()
    {
        ResourceLocation resourcelocation = this.getCloakTextureLocation();

        if (resourcelocation == null)
        {
            return false;
        }
        else
        {
            return resourcelocation == this.locationOfCape ? this.elytraOfCape : true;
        }
    }

    public void setElytraOfCape(boolean elytraOfCape)
    {
        this.elytraOfCape = elytraOfCape;
    }

    public boolean isElytraOfCape()
    {
        return this.elytraOfCape;
    }

    public long getReloadCapeTimeMs()
    {
        return this.reloadCapeTimeMs;
    }

    public void setReloadCapeTimeMs(long reloadCapeTimeMs)
    {
        this.reloadCapeTimeMs = reloadCapeTimeMs;
    }
}
