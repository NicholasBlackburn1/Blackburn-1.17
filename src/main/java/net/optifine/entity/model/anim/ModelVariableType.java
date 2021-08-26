package net.optifine.entity.model.anim;

import net.minecraft.client.model.geom.ModelPart;
import net.optifine.Config;

public enum ModelVariableType
{
    POS_X("tx"),
    POS_Y("ty"),
    POS_Z("tz"),
    ANGLE_X("rx"),
    ANGLE_Y("ry"),
    ANGLE_Z("rz"),
    SCALE_X("sx"),
    SCALE_Y("sy"),
    SCALE_Z("sz");

    private String name;
    public static ModelVariableType[] VALUES = values();

    private ModelVariableType(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return this.name;
    }

    public float getFloat(ModelPart mr)
    {
        switch (this)
        {
            case POS_X:
                return mr.x;

            case POS_Y:
                return mr.y;

            case POS_Z:
                return mr.z;

            case ANGLE_X:
                return mr.xRot;

            case ANGLE_Y:
                return mr.yRot;

            case ANGLE_Z:
                return mr.zRot;

            case SCALE_X:
                return mr.scaleX;

            case SCALE_Y:
                return mr.scaleY;

            case SCALE_Z:
                return mr.scaleZ;

            default:
                Config.warn("GetFloat not supported for: " + this);
                return 0.0F;
        }
    }

    public void setFloat(ModelPart mr, float val)
    {
        switch (this)
        {
            case POS_X:
                mr.x = val;
                return;

            case POS_Y:
                mr.y = val;
                return;

            case POS_Z:
                mr.z = val;
                return;

            case ANGLE_X:
                mr.xRot = val;
                return;

            case ANGLE_Y:
                mr.yRot = val;
                return;

            case ANGLE_Z:
                mr.zRot = val;
                return;

            case SCALE_X:
                mr.scaleX = val;
                return;

            case SCALE_Y:
                mr.scaleY = val;
                return;

            case SCALE_Z:
                mr.scaleZ = val;
                return;

            default:
                Config.warn("SetFloat not supported for: " + this);
        }
    }

    public static ModelVariableType parse(String str)
    {
        for (int i = 0; i < VALUES.length; ++i)
        {
            ModelVariableType modelvariabletype = VALUES[i];

            if (modelvariabletype.getName().equals(str))
            {
                return modelvariabletype;
            }
        }

        return null;
    }
}
