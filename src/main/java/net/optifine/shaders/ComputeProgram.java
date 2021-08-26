package net.optifine.shaders;

import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec2;

public class ComputeProgram
{
    private final String name;
    private final ProgramStage programStage;
    private int id;
    private int ref;
    private Vec3i localSize;
    private Vec3i workGroups;
    private Vec2 workGroupsRender;
    private int compositeMipmapSetting;

    public ComputeProgram(String name, ProgramStage programStage)
    {
        this.name = name;
        this.programStage = programStage;
    }

    public void resetProperties()
    {
    }

    public void resetId()
    {
        this.id = 0;
        this.ref = 0;
    }

    public void resetConfiguration()
    {
        this.localSize = null;
        this.workGroups = null;
        this.workGroupsRender = null;
    }

    public String getName()
    {
        return this.name;
    }

    public ProgramStage getProgramStage()
    {
        return this.programStage;
    }

    public int getId()
    {
        return this.id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public int getRef()
    {
        return this.ref;
    }

    public void setRef(int ref)
    {
        this.ref = ref;
    }

    public Vec3i getLocalSize()
    {
        return this.localSize;
    }

    public void setLocalSize(Vec3i localSize)
    {
        this.localSize = localSize;
    }

    public Vec3i getWorkGroups()
    {
        return this.workGroups;
    }

    public void setWorkGroups(Vec3i workGroups)
    {
        this.workGroups = workGroups;
    }

    public Vec2 getWorkGroupsRender()
    {
        return this.workGroupsRender;
    }

    public void setWorkGroupsRender(Vec2 workGroupsRender)
    {
        this.workGroupsRender = workGroupsRender;
    }

    public int getCompositeMipmapSetting()
    {
        return this.compositeMipmapSetting;
    }

    public void setCompositeMipmapSetting(int compositeMipmapSetting)
    {
        this.compositeMipmapSetting = compositeMipmapSetting;
    }

    public boolean hasCompositeMipmaps()
    {
        return this.compositeMipmapSetting != 0;
    }

    public String toString()
    {
        return "name: " + this.name + ", id: " + this.id;
    }
}
