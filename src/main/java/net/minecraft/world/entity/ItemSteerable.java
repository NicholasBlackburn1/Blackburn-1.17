package net.minecraft.world.entity;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public interface ItemSteerable
{
    boolean boost();

    void travelWithInput(Vec3 pTravelVec);

    float getSteeringSpeed();

default boolean travel(Mob pMount, ItemBasedSteering pHelper, Vec3 p_20857_)
    {
        if (!pMount.isAlive())
        {
            return false;
        }
        else
        {
            Entity entity = pMount.getFirstPassenger();

            if (pMount.isVehicle() && pMount.canBeControlledByRider() && entity instanceof Player)
            {
                pMount.setYRot(entity.getYRot());
                pMount.yRotO = pMount.getYRot();
                pMount.setXRot(entity.getXRot() * 0.5F);
                pMount.setRot(pMount.getYRot(), pMount.getXRot());
                pMount.yBodyRot = pMount.getYRot();
                pMount.yHeadRot = pMount.getYRot();
                pMount.maxUpStep = 1.0F;
                pMount.flyingSpeed = pMount.getSpeed() * 0.1F;

                if (pHelper.boosting && pHelper.boostTime++ > pHelper.boostTimeTotal)
                {
                    pHelper.boosting = false;
                }

                if (pMount.isControlledByLocalInstance())
                {
                    float f = this.getSteeringSpeed();

                    if (pHelper.boosting)
                    {
                        f += f * 1.15F * Mth.sin((float)pHelper.boostTime / (float)pHelper.boostTimeTotal * (float)Math.PI);
                    }

                    pMount.setSpeed(f);
                    this.travelWithInput(new Vec3(0.0D, 0.0D, 1.0D));
                    pMount.lerpSteps = 0;
                }
                else
                {
                    pMount.calculateEntityAnimation(pMount, false);
                    pMount.setDeltaMovement(Vec3.ZERO);
                }

                pMount.tryCheckInsideBlocks();
                return true;
            }
            else
            {
                pMount.maxUpStep = 0.5F;
                pMount.flyingSpeed = 0.02F;
                this.travelWithInput(p_20857_);
                return false;
            }
        }
    }
}
