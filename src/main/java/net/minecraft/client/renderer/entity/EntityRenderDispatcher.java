package net.minecraft.client.renderer.entity;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.optifine.Config;
import net.optifine.DynamicLights;
import net.optifine.EmissiveTextures;
import net.optifine.entity.model.CustomEntityModels;
import net.optifine.player.PlayerItemsLayer;
import net.optifine.reflect.Reflector;
import net.optifine.reflect.ReflectorForge;
import net.optifine.shaders.Shaders;

public class EntityRenderDispatcher implements ResourceManagerReloadListener
{
    private static final RenderType SHADOW_RENDER_TYPE = RenderType.entityShadow(new ResourceLocation("textures/misc/shadow.png"));
    private Map <EntityType, EntityRenderer> renderers = ImmutableMap.of();
    private Map <String, EntityRenderer> playerRenderers = ImmutableMap.of();
    public final TextureManager textureManager;
    private Level level;
    public Camera camera;
    private Quaternion cameraOrientation;
    public Entity crosshairPickEntity;
    private final ItemRenderer itemRenderer;
    private final Font font;
    public final Options options;
    private final EntityModelSet entityModels;
    private boolean shouldRenderShadow = true;
    private boolean renderHitBoxes;
    public EntityRenderer renderRender = null;
    public Entity renderEntity = null;
    private EntityRendererProvider.Context context = null;

    public <E extends Entity> int getPackedLightCoords(E pEntity, float pPartialTicks)
    {
        int i = this.getRenderer(pEntity).getPackedLightCoords(pEntity, pPartialTicks);

        if (Config.isDynamicLights())
        {
            i = DynamicLights.getCombinedLight(pEntity, i);
        }

        return i;
    }

    public EntityRenderDispatcher(TextureManager p_173998_, ItemRenderer p_173999_, Font p_174000_, Options p_174001_, EntityModelSet p_174002_)
    {
        this.textureManager = p_173998_;
        this.itemRenderer = p_173999_;
        this.font = p_174000_;
        this.options = p_174001_;
        this.entityModels = p_174002_;
    }

    public <T extends Entity> EntityRenderer <? super T > getRenderer(T pEntity)
    {
        if (pEntity instanceof AbstractClientPlayer)
        {
            String s = ((AbstractClientPlayer)pEntity).getModelName();
            EntityRenderer <? extends Player > entityrenderer = this.playerRenderers.get(s);
            return entityrenderer != null ? (EntityRenderer <? super T >)entityrenderer : (EntityRenderer <? super T >)this.playerRenderers.get("default");
        }
        else
        {
            return (EntityRenderer <? super T >)this.renderers.get(pEntity.getType());
        }
    }

    public void prepare(Level pLevel, Camera pActiveRenderInfo, Entity pEntity)
    {
        this.level = pLevel;
        this.camera = pActiveRenderInfo;
        this.cameraOrientation = pActiveRenderInfo.rotation();
        this.crosshairPickEntity = pEntity;
    }

    public void overrideCameraOrientation(Quaternion pQuaternion)
    {
        this.cameraOrientation = pQuaternion;
    }

    public void setRenderShadow(boolean pRenderShadow)
    {
        this.shouldRenderShadow = pRenderShadow;
    }

    public void setRenderHitBoxes(boolean pDebugBoundingBox)
    {
        this.renderHitBoxes = pDebugBoundingBox;
    }

    public boolean shouldRenderHitBoxes()
    {
        return this.renderHitBoxes;
    }

    public <E extends Entity> boolean shouldRender(E pEntity, Frustum pFrustum, double pCamX, double p_114401_, double pCamY)
    {
        EntityRenderer <? super E > entityrenderer = this.getRenderer(pEntity);
        return entityrenderer.shouldRender(pEntity, pFrustum, pCamX, p_114401_, pCamY);
    }

    public <E extends Entity> void render(E pEntity, double pX, double p_114387_, double pY, float p_114389_, float pZ, PoseStack p_114391_, MultiBufferSource pRotationYaw, int pPartialTicks)
    {
        if (this.camera != null)
        {
            EntityRenderer <? super E > entityrenderer = this.getRenderer(pEntity);

            try
            {
                Vec3 vec3 = entityrenderer.getRenderOffset(pEntity, pZ);
                double d2 = pX + vec3.x();
                double d3 = p_114387_ + vec3.y();
                double d0 = pY + vec3.z();
                p_114391_.pushPose();
                p_114391_.translate(d2, d3, d0);

                if (CustomEntityModels.isActive())
                {
                    this.renderRender = entityrenderer;
                    this.renderEntity = pEntity;
                }

                if (EmissiveTextures.isActive())
                {
                    EmissiveTextures.beginRender();
                }

                entityrenderer.render(pEntity, p_114389_, pZ, p_114391_, pRotationYaw, pPartialTicks);

                if (EmissiveTextures.isActive())
                {
                    if (EmissiveTextures.hasEmissive())
                    {
                        EmissiveTextures.beginRenderEmissive();
                        entityrenderer.render(pEntity, p_114389_, pZ, p_114391_, pRotationYaw, LightTexture.MAX_BRIGHTNESS);
                        EmissiveTextures.endRenderEmissive();
                    }

                    EmissiveTextures.endRender();
                }

                if (CustomEntityModels.isActive())
                {
                    this.renderRender = null;
                    this.renderEntity = null;
                }

                if (pEntity.displayFireAnimation())
                {
                    this.renderFlame(p_114391_, pRotationYaw, pEntity);
                }

                p_114391_.translate(-vec3.x(), -vec3.y(), -vec3.z());

                if (this.options.entityShadows && this.shouldRenderShadow && entityrenderer.shadowRadius > 0.0F && !pEntity.isInvisible())
                {
                    double d1 = this.distanceToSqr(pEntity.getX(), pEntity.getY(), pEntity.getZ());
                    float f = (float)((1.0D - d1 / 256.0D) * (double)entityrenderer.shadowStrength);

                    if (f > 0.0F)
                    {
                        renderShadow(p_114391_, pRotationYaw, pEntity, f, pZ, this.level, entityrenderer.shadowRadius);
                    }
                }

                if (this.renderHitBoxes && !pEntity.isInvisible() && !Minecraft.getInstance().showOnlyReducedInfo())
                {
                    renderHitbox(p_114391_, pRotationYaw.getBuffer(RenderType.lines()), pEntity, pZ);
                }

                p_114391_.popPose();
            }
            catch (Throwable throwable1)
            {
                CrashReport crashreport = CrashReport.forThrowable(throwable1, "Rendering entity in world");
                CrashReportCategory crashreportcategory = crashreport.addCategory("Entity being rendered");
                pEntity.fillCrashReportCategory(crashreportcategory);
                CrashReportCategory crashreportcategory1 = crashreport.addCategory("Renderer details");
                crashreportcategory1.setDetail("Assigned renderer", entityrenderer);
                crashreportcategory1.setDetail("Location", CrashReportCategory.formatLocation(this.level, pX, p_114387_, pY));
                crashreportcategory1.setDetail("Rotation", p_114389_);
                crashreportcategory1.setDetail("Delta", pZ);
                throw new ReportedException(crashreport);
            }
        }
    }

    private static void renderHitbox(PoseStack p_114442_, VertexConsumer pMatrixStack, Entity pBuffer, float pEntity)
    {
        if (!Shaders.isShadowPass)
        {
            AABB aabb = pBuffer.getBoundingBox().move(-pBuffer.getX(), -pBuffer.getY(), -pBuffer.getZ());
            LevelRenderer.renderLineBox(p_114442_, pMatrixStack, aabb, 1.0F, 1.0F, 1.0F, 1.0F);
            boolean flag = pBuffer instanceof EnderDragon;

            if (Reflector.IForgeEntity_isMultipartEntity.exists() && Reflector.IForgeEntity_getParts.exists())
            {
                flag = Reflector.callBoolean(pBuffer, Reflector.IForgeEntity_isMultipartEntity);
            }

            if (flag)
            {
                double d0 = -Mth.lerp((double)pEntity, pBuffer.xOld, pBuffer.getX());
                double d1 = -Mth.lerp((double)pEntity, pBuffer.yOld, pBuffer.getY());
                double d2 = -Mth.lerp((double)pEntity, pBuffer.zOld, pBuffer.getZ());
                Entity[] aentity = (Entity[])(Reflector.IForgeEntity_getParts.exists() ? (Entity[])Reflector.call(pBuffer, Reflector.IForgeEntity_getParts) : ((EnderDragon)pBuffer).getSubEntities());

                for (Entity entity : aentity)
                {
                    p_114442_.pushPose();
                    double d3 = d0 + Mth.lerp((double)pEntity, entity.xOld, entity.getX());
                    double d4 = d1 + Mth.lerp((double)pEntity, entity.yOld, entity.getY());
                    double d5 = d2 + Mth.lerp((double)pEntity, entity.zOld, entity.getZ());
                    p_114442_.translate(d3, d4, d5);
                    LevelRenderer.renderLineBox(p_114442_, pMatrixStack, entity.getBoundingBox().move(-entity.getX(), -entity.getY(), -entity.getZ()), 0.25F, 1.0F, 0.0F, 1.0F);
                    p_114442_.popPose();
                }
            }

            if (pBuffer instanceof LivingEntity)
            {
                float f = 0.01F;
                LevelRenderer.renderLineBox(p_114442_, pMatrixStack, aabb.minX, (double)(pBuffer.getEyeHeight() - 0.01F), aabb.minZ, aabb.maxX, (double)(pBuffer.getEyeHeight() + 0.01F), aabb.maxZ, 1.0F, 0.0F, 0.0F, 1.0F);
            }

            Vec3 vec3 = pBuffer.getViewVector(pEntity);
            Matrix4f matrix4f = p_114442_.last().pose();
            Matrix3f matrix3f = p_114442_.last().normal();
            pMatrixStack.vertex(matrix4f, 0.0F, pBuffer.getEyeHeight(), 0.0F).color(0, 0, 255, 255).normal(matrix3f, (float)vec3.x, (float)vec3.y, (float)vec3.z).endVertex();
            pMatrixStack.vertex(matrix4f, (float)(vec3.x * 2.0D), (float)((double)pBuffer.getEyeHeight() + vec3.y * 2.0D), (float)(vec3.z * 2.0D)).color(0, 0, 255, 255).normal(matrix3f, (float)vec3.x, (float)vec3.y, (float)vec3.z).endVertex();
        }
    }

    private void renderFlame(PoseStack pMatrixStack, MultiBufferSource pBuffer, Entity pEntity)
    {
        TextureAtlasSprite textureatlassprite = ModelBakery.FIRE_0.sprite();
        TextureAtlasSprite textureatlassprite1 = ModelBakery.FIRE_1.sprite();
        pMatrixStack.pushPose();
        float f = pEntity.getBbWidth() * 1.4F;
        pMatrixStack.scale(f, f, f);
        float f1 = 0.5F;
        float f2 = 0.0F;
        float f3 = pEntity.getBbHeight() / f;
        float f4 = 0.0F;
        pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(-this.camera.getYRot()));
        pMatrixStack.translate(0.0D, 0.0D, (double)(-0.3F + (float)((int)f3) * 0.02F));
        float f5 = 0.0F;
        int i = 0;
        VertexConsumer vertexconsumer = pBuffer.getBuffer(Sheets.cutoutBlockSheet());

        if (Config.isMultiTexture())
        {
            vertexconsumer.setRenderBlocks(true);
        }

        for (PoseStack.Pose posestack$pose = pMatrixStack.last(); f3 > 0.0F; ++i)
        {
            TextureAtlasSprite textureatlassprite2 = i % 2 == 0 ? textureatlassprite : textureatlassprite1;
            vertexconsumer.setSprite(textureatlassprite2);
            float f6 = textureatlassprite2.getU0();
            float f7 = textureatlassprite2.getV0();
            float f8 = textureatlassprite2.getU1();
            float f9 = textureatlassprite2.getV1();

            if (i / 2 % 2 == 0)
            {
                float f10 = f8;
                f8 = f6;
                f6 = f10;
            }

            fireVertex(posestack$pose, vertexconsumer, f1 - 0.0F, 0.0F - f4, f5, f8, f9);
            fireVertex(posestack$pose, vertexconsumer, -f1 - 0.0F, 0.0F - f4, f5, f6, f9);
            fireVertex(posestack$pose, vertexconsumer, -f1 - 0.0F, 1.4F - f4, f5, f6, f7);
            fireVertex(posestack$pose, vertexconsumer, f1 - 0.0F, 1.4F - f4, f5, f8, f7);
            f3 -= 0.45F;
            f4 -= 0.45F;
            f1 *= 0.9F;
            f5 += 0.03F;
        }

        pMatrixStack.popPose();
    }

    private static void fireVertex(PoseStack.Pose pMatrixEntry, VertexConsumer pBuffer, float pX, float pY, float pZ, float pTexU, float pTexV)
    {
        pBuffer.vertex(pMatrixEntry.pose(), pX, pY, pZ).color(255, 255, 255, 255).uv(pTexU, pTexV).overlayCoords(0, 10).uv2(240).normal(pMatrixEntry.normal(), 0.0F, 1.0F, 0.0F).endVertex();
    }

    private static void renderShadow(PoseStack pMatrixStack, MultiBufferSource pBuffer, Entity pEntity, float pWeight, float pPartialTicks, LevelReader pLevel, float pSize)
    {
        if (!Config.isShaders() || !Shaders.shouldSkipDefaultShadow)
        {
            float f = pSize;

            if (pEntity instanceof Mob)
            {
                Mob mob = (Mob)pEntity;

                if (mob.isBaby())
                {
                    f = pSize * 0.5F;
                }
            }

            double d2 = Mth.lerp((double)pPartialTicks, pEntity.xOld, pEntity.getX());
            double d0 = Mth.lerp((double)pPartialTicks, pEntity.yOld, pEntity.getY());
            double d1 = Mth.lerp((double)pPartialTicks, pEntity.zOld, pEntity.getZ());
            int i = Mth.floor(d2 - (double)f);
            int j = Mth.floor(d2 + (double)f);
            int k = Mth.floor(d0 - (double)f);
            int l = Mth.floor(d0);
            int i1 = Mth.floor(d1 - (double)f);
            int j1 = Mth.floor(d1 + (double)f);
            PoseStack.Pose posestack$pose = pMatrixStack.last();
            VertexConsumer vertexconsumer = pBuffer.getBuffer(SHADOW_RENDER_TYPE);

            for (BlockPos blockpos : BlockPos.betweenClosed(new BlockPos(i, k, i1), new BlockPos(j, l, j1)))
            {
                renderBlockShadow(posestack$pose, vertexconsumer, pLevel, blockpos, d2, d0, d1, f, pWeight);
            }
        }
    }

    private static void renderBlockShadow(PoseStack.Pose pMatrixEntry, VertexConsumer pBuffer, LevelReader pLevel, BlockPos pBlockPos, double pX, double p_114437_, double pY, float p_114439_, float pZ)
    {
        BlockPos blockpos = pBlockPos.below();
        BlockState blockstate = pLevel.getBlockState(blockpos);

        if (blockstate.getRenderShape() != RenderShape.INVISIBLE && pLevel.getMaxLocalRawBrightness(pBlockPos) > 3 && blockstate.isCollisionShapeFullBlock(pLevel, blockpos))
        {
            VoxelShape voxelshape = blockstate.getShape(pLevel, pBlockPos.below());

            if (!voxelshape.isEmpty())
            {
                float f = (float)(((double)pZ - (p_114437_ - (double)pBlockPos.getY()) / 2.0D) * 0.5D * (double)pLevel.getBrightness(pBlockPos));

                if (f >= 0.0F)
                {
                    if (f > 1.0F)
                    {
                        f = 1.0F;
                    }

                    AABB aabb = voxelshape.bounds();
                    double d0 = (double)pBlockPos.getX() + aabb.minX;
                    double d1 = (double)pBlockPos.getX() + aabb.maxX;
                    double d2 = (double)pBlockPos.getY() + aabb.minY;
                    double d3 = (double)pBlockPos.getZ() + aabb.minZ;
                    double d4 = (double)pBlockPos.getZ() + aabb.maxZ;
                    float f1 = (float)(d0 - pX);
                    float f2 = (float)(d1 - pX);
                    float f3 = (float)(d2 - p_114437_);
                    float f4 = (float)(d3 - pY);
                    float f5 = (float)(d4 - pY);
                    float f6 = -f1 / 2.0F / p_114439_ + 0.5F;
                    float f7 = -f2 / 2.0F / p_114439_ + 0.5F;
                    float f8 = -f4 / 2.0F / p_114439_ + 0.5F;
                    float f9 = -f5 / 2.0F / p_114439_ + 0.5F;
                    shadowVertex(pMatrixEntry, pBuffer, f, f1, f3, f4, f6, f8);
                    shadowVertex(pMatrixEntry, pBuffer, f, f1, f3, f5, f6, f9);
                    shadowVertex(pMatrixEntry, pBuffer, f, f2, f3, f5, f7, f9);
                    shadowVertex(pMatrixEntry, pBuffer, f, f2, f3, f4, f7, f8);
                }
            }
        }
    }

    private static void shadowVertex(PoseStack.Pose pMatrixEntry, VertexConsumer pBuffer, float pAlpha, float pX, float pY, float pZ, float pTexU, float pTexV)
    {
        pBuffer.vertex(pMatrixEntry.pose(), pX, pY, pZ).color(1.0F, 1.0F, 1.0F, pAlpha).uv(pTexU, pTexV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(pMatrixEntry.normal(), 0.0F, 1.0F, 0.0F).endVertex();
    }

    public void setLevel(@Nullable Level pLevel)
    {
        this.level = pLevel;

        if (pLevel == null)
        {
            this.camera = null;
        }
    }

    public double distanceToSqr(Entity pX)
    {
        return this.camera.getPosition().distanceToSqr(pX.position());
    }

    public double distanceToSqr(double pX, double p_114380_, double pY)
    {
        return this.camera.getPosition().distanceToSqr(pX, p_114380_, pY);
    }

    public Quaternion cameraOrientation()
    {
        return this.cameraOrientation;
    }

    public void onResourceManagerReload(ResourceManager p_174004_)
    {
        EntityRendererProvider.Context entityrendererprovider$context = new EntityRendererProvider.Context(this, this.itemRenderer, p_174004_, this.entityModels, this.font);
        this.renderers = EntityRenderers.createEntityRenderers(entityrendererprovider$context);
        this.playerRenderers = EntityRenderers.createPlayerRenderers(entityrendererprovider$context);
        this.context = entityrendererprovider$context;
        registerPlayerItems(this.playerRenderers);
        ReflectorForge.postModLoaderEvent(Reflector.EntityRenderersEvent_AddLayers_Constructor, this.renderers, this.playerRenderers);
    }

    private static void registerPlayerItems(Map<String, EntityRenderer> renderPlayerMap)
    {
        boolean flag = false;

        for (EntityRenderer entityrenderer : renderPlayerMap.values())
        {
            if (entityrenderer instanceof PlayerRenderer)
            {
                PlayerRenderer playerrenderer = (PlayerRenderer)entityrenderer;
                playerrenderer.addLayer(new PlayerItemsLayer(playerrenderer));
                flag = true;
            }
        }

        if (!flag)
        {
            Config.warn("PlayerItemsLayer not registered");
        }
    }

    public Map<EntityType, EntityRenderer> getEntityRenderMap()
    {
        if (this.renderers instanceof ImmutableMap)
        {
            this.renderers = new HashMap<>(this.renderers);
        }

        return this.renderers;
    }

    public EntityRendererProvider.Context getContext()
    {
        return this.context;
    }

    public Map<String, EntityRenderer> getSkinMap()
    {
        return Collections.unmodifiableMap(this.playerRenderers);
    }
}
