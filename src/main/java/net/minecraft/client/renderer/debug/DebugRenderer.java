package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import com.mojang.math.Transformation;
import java.util.Optional;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.blackburn.Const;

public class DebugRenderer
{
  

    private boolean renderChunkborder;

    public DebugRenderer(Minecraft p_113433_)
    {
 
    
    }

    public void clear()
    {
        
        
    }

    public boolean switchRenderChunkborder()
    {
        this.renderChunkborder = !this.renderChunkborder;
        return this.renderChunkborder;
    }

    public void render(PoseStack pMatrixStack, MultiBufferSource.BufferSource pBuffer, double pCamX, double p_113461_, double pCamY)
    {
        

    }

    public static Optional<Entity> getTargetedEntity(@Nullable Entity pEntity, int pDistance)
    {
        if (pEntity == null)
        {
            return Optional.empty();
        }
        else
        {
            Vec3 vec3 = pEntity.getEyePosition();
            Vec3 vec31 = pEntity.getViewVector(1.0F).scale((double)pDistance);
            Vec3 vec32 = vec3.add(vec31);
            AABB aabb = pEntity.getBoundingBox().expandTowards(vec31).inflate(1.0D);
            int i = pDistance * pDistance;
            Predicate<Entity> predicate = (p_113447_) ->
            {
                return !p_113447_.isSpectator() && p_113447_.isPickable();
            };
            EntityHitResult entityhitresult = ProjectileUtil.getEntityHitResult(pEntity, vec3, vec32, aabb, predicate, (double)i);

            if (entityhitresult == null)
            {
                return Optional.empty();
            }
            else
            {
                return vec3.distanceToSqr(entityhitresult.getLocation()) > (double)i ? Optional.empty() : Optional.of(entityhitresult.getEntity());
            }
        }
    }

    public static void renderFilledBox(BlockPos p_113471_, BlockPos p_113472_, float p_113473_, float p_113474_, float p_113475_, float p_113476_)
    {
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();

        if (camera.isInitialized())
        {
            Vec3 vec3 = camera.getPosition().reverse();
            AABB aabb = (new AABB(p_113471_, p_113472_)).move(vec3);
            renderFilledBox(aabb, p_113473_, p_113474_, p_113475_, p_113476_);
        }
    }

    public static void renderFilledBox(BlockPos p_113464_, float p_113465_, float p_113466_, float p_113467_, float p_113468_, float p_113469_)
    {
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();

        if (camera.isInitialized())
        {
            Vec3 vec3 = camera.getPosition().reverse();
            AABB aabb = (new AABB(p_113464_)).move(vec3).inflate((double)p_113465_);
            renderFilledBox(aabb, p_113466_, p_113467_, p_113468_, p_113469_);
        }
    }

    public static void renderFilledBox(AABB p_113452_, float p_113453_, float p_113454_, float p_113455_, float p_113456_)
    {
        renderFilledBox(p_113452_.minX, p_113452_.minY, p_113452_.minZ, p_113452_.maxX, p_113452_.maxY, p_113452_.maxZ, p_113453_, p_113454_, p_113455_, p_113456_);
    }

    public static void renderFilledBox(double p_113436_, double p_113437_, double p_113438_, double p_113439_, double p_113440_, double p_113441_, float p_113442_, float p_113443_, float p_113444_, float p_113445_)
    {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferbuilder.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        LevelRenderer.addChainedFilledBoxVertices(bufferbuilder, p_113436_, p_113437_, p_113438_, p_113439_, p_113440_, p_113441_, p_113442_, p_113443_, p_113444_, p_113445_);
        tesselator.end();
    }

    public static void renderFloatingText(String pText, int p_113502_, int p_113503_, int p_113504_, int p_113505_)
    {
        renderFloatingText(pText, (double)p_113502_ + 0.5D, (double)p_113503_ + 0.5D, (double)p_113504_ + 0.5D, p_113505_);
    }

    public static void renderFloatingText(String pText, double p_113479_, double p_113480_, double p_113481_, int p_113482_)
    {
        renderFloatingText(pText, p_113479_, p_113480_, p_113481_, p_113482_, 0.02F);
    }

    public static void renderFloatingText(String pText, double p_113485_, double p_113486_, double p_113487_, int p_113488_, float p_113489_)
    {
        renderFloatingText(pText, p_113485_, p_113486_, p_113487_, p_113488_, p_113489_, true, 0.0F, false);
    }

    public static void renderFloatingText(String pText, double p_113492_, double p_113493_, double p_113494_, int p_113495_, float p_113496_, boolean p_113497_, float pColor, boolean p_113499_)
    {
        Minecraft minecraft = Minecraft.getInstance();
        Camera camera = minecraft.gameRenderer.getMainCamera();

        if (camera.isInitialized() && minecraft.getEntityRenderDispatcher().options != null)
        {
            Font font = minecraft.font;
            double d0 = camera.getPosition().x;
            double d1 = camera.getPosition().y;
            double d2 = camera.getPosition().z;
            PoseStack posestack = RenderSystem.getModelViewStack();
            posestack.pushPose();
            posestack.translate((double)((float)(p_113492_ - d0)), (double)((float)(p_113493_ - d1) + 0.07F), (double)((float)(p_113494_ - d2)));
            posestack.mulPoseMatrix(new Matrix4f(camera.rotation()));
            posestack.scale(p_113496_, -p_113496_, p_113496_);
            RenderSystem.enableTexture();

            if (p_113499_)
            {
                RenderSystem.disableDepthTest();
            }
            else
            {
                RenderSystem.enableDepthTest();
            }

            RenderSystem.depthMask(true);
            posestack.scale(-1.0F, 1.0F, 1.0F);
            RenderSystem.applyModelViewMatrix();
            float f = p_113497_ ? (float)(-font.width(pText)) / 2.0F : 0.0F;
            f = f - pColor / p_113496_;
            MultiBufferSource.BufferSource multibuffersource$buffersource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
            font.drawInBatch(pText, f, 0.0F, p_113495_, false, Transformation.identity().getMatrix(), multibuffersource$buffersource, p_113499_, 0, 15728880);
            multibuffersource$buffersource.endBatch();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.enableDepthTest();
            posestack.popPose();
            RenderSystem.applyModelViewMatrix();
        }
    }

    public interface SimpleDebugRenderer
    {
        void render(PoseStack pMatrixStack, MultiBufferSource pBuffer, double pCamX, double p_113510_, double pCamY);

    default void clear()
        {
        }
    }
}
