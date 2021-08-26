package net.optifine.shaders;

import com.mojang.math.Matrix4f;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.phys.AABB;

public class ClippingHelperDummy extends Frustum
{
    public ClippingHelperDummy()
    {
        super(new Matrix4f(), new Matrix4f());
    }

    public boolean isVisible(AABB pAabb)
    {
        return true;
    }
}
