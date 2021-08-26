package net.minecraft.client.resources;

import java.util.UUID;
import net.minecraft.resources.ResourceLocation;

public class DefaultPlayerSkin
{
    private static final ResourceLocation STEVE_SKIN_LOCATION = new ResourceLocation("textures/entity/steve.png");
    private static final ResourceLocation ALEX_SKIN_LOCATION = new ResourceLocation("textures/entity/alex.png");
    private static final String STEVE_MODEL = "default";
    private static final String ALEX_MODEL = "slim";

    public static ResourceLocation getDefaultSkin()
    {
        return STEVE_SKIN_LOCATION;
    }

    public static ResourceLocation getDefaultSkin(UUID p_118628_)
    {
        return isAlexDefault(p_118628_) ? ALEX_SKIN_LOCATION : STEVE_SKIN_LOCATION;
    }

    public static String getSkinModelName(UUID pPlayerUUID)
    {
        return isAlexDefault(pPlayerUUID) ? "slim" : "default";
    }

    private static boolean isAlexDefault(UUID pPlayerUUID)
    {
        return (pPlayerUUID.hashCode() & 1) == 1;
    }
}
