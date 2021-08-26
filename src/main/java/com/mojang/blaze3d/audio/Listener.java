package com.mojang.blaze3d.audio;

import com.mojang.math.Vector3f;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.openal.AL10;

public class Listener
{
    private float gain = 1.0F;
    private Vec3 position = Vec3.ZERO;

    public void setListenerPosition(Vec3 pPos)
    {
        this.position = pPos;
        AL10.alListener3f(4100, (float)pPos.x, (float)pPos.y, (float)pPos.z);
    }

    public Vec3 getListenerPosition()
    {
        return this.position;
    }

    public void setListenerOrientation(Vector3f pClientViewVector, Vector3f pViewVectorRaised)
    {
        AL10.alListenerfv(4111, new float[] {pClientViewVector.x(), pClientViewVector.y(), pClientViewVector.z(), pViewVectorRaised.x(), pViewVectorRaised.y(), pViewVectorRaised.z()});
    }

    public void setGain(float pGain)
    {
        AL10.alListenerf(4106, pGain);
        this.gain = pGain;
    }

    public float getGain()
    {
        return this.gain;
    }

    public void reset()
    {
        this.setListenerPosition(Vec3.ZERO);
        this.setListenerOrientation(Vector3f.ZN, Vector3f.YP);
    }
}
