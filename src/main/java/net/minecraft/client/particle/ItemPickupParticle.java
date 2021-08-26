package net.minecraft.client.particle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;
import net.optifine.Config;
import net.optifine.shaders.Program;
import net.optifine.shaders.Shaders;

public class ItemPickupParticle extends Particle
{
    private static final int LIFE_TIME = 3;
    private final RenderBuffers renderBuffers;
    private final Entity itemEntity;
    private final Entity target;
    private int life;
    private final EntityRenderDispatcher entityRenderDispatcher;

    public ItemPickupParticle(EntityRenderDispatcher p_107023_, RenderBuffers p_107024_, ClientLevel p_107025_, Entity p_107026_, Entity p_107027_)
    {
        this(p_107023_, p_107024_, p_107025_, p_107026_, p_107027_, p_107026_.getDeltaMovement());
    }

    private ItemPickupParticle(EntityRenderDispatcher p_107029_, RenderBuffers p_107030_, ClientLevel p_107031_, Entity p_107032_, Entity p_107033_, Vec3 p_107034_)
    {
        super(p_107031_, p_107032_.getX(), p_107032_.getY(), p_107032_.getZ(), p_107034_.x, p_107034_.y, p_107034_.z);
        this.renderBuffers = p_107030_;
        this.itemEntity = this.getSafeCopy(p_107032_);
        this.target = p_107033_;
        this.entityRenderDispatcher = p_107029_;
    }

    private Entity getSafeCopy(Entity pEntity)
    {
        return (Entity)(!(pEntity instanceof ItemEntity) ? pEntity : ((ItemEntity)pEntity).copy());
    }

    public ParticleRenderType getRenderType()
    {
        return ParticleRenderType.CUSTOM;
    }

    public void render(VertexConsumer pBuffer, Camera pRenderInfo, float pPartialTicks)
    {
        Program program = null;

        if (Config.isShaders())
        {
            program = Shaders.activeProgram;
            Shaders.nextEntity(this.itemEntity);
        }

        float f = ((float)this.life + pPartialTicks) / 3.0F;
        f = f * f;
        double d0 = Mth.lerp((double)pPartialTicks, this.target.xOld, this.target.getX());
        double d1 = Mth.lerp((double)pPartialTicks, this.target.yOld, this.target.getY()) + 0.5D;
        double d2 = Mth.lerp((double)pPartialTicks, this.target.zOld, this.target.getZ());
        double d3 = Mth.lerp((double)f, this.itemEntity.getX(), d0);
        double d4 = Mth.lerp((double)f, this.itemEntity.getY(), d1);
        double d5 = Mth.lerp((double)f, this.itemEntity.getZ(), d2);
        MultiBufferSource.BufferSource multibuffersource$buffersource = this.renderBuffers.bufferSource();
        Vec3 vec3 = pRenderInfo.getPosition();
        this.entityRenderDispatcher.render(this.itemEntity, d3 - vec3.x(), d4 - vec3.y(), d5 - vec3.z(), this.itemEntity.getYRot(), pPartialTicks, new PoseStack(), multibuffersource$buffersource, this.entityRenderDispatcher.getPackedLightCoords(this.itemEntity, pPartialTicks));
        multibuffersource$buffersource.endBatch();

        if (Config.isShaders())
        {
            Shaders.setEntityId((Entity)null);
            Shaders.useProgram(program);
        }
    }

    public void tick()
    {
        ++this.life;

        if (this.life == 3)
        {
            this.remove();
        }
    }
}
