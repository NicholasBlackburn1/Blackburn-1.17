package net.minecraft.client.renderer.blockentity;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Camera;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.HitResult;
import net.optifine.EmissiveTextures;

public class BlockEntityRenderDispatcher implements ResourceManagerReloadListener
{
    private Map < BlockEntityType<?>, BlockEntityRenderer<? >> renderers = ImmutableMap.of();
    private final Font font;
    private final EntityModelSet entityModelSet;
    public Level level;
    public Camera camera;
    public HitResult cameraHitResult;
    private final Supplier<BlockRenderDispatcher> blockRenderDispatcher;
    public static BlockEntity tileEntityRendered;
    private BlockEntityRendererProvider.Context context;

    public BlockEntityRenderDispatcher(Font p_173559_, EntityModelSet p_173560_, Supplier<BlockRenderDispatcher> p_173561_)
    {
        this.font = p_173559_;
        this.entityModelSet = p_173560_;
        this.blockRenderDispatcher = p_173561_;
    }

    @Nullable
    public <E extends BlockEntity> BlockEntityRenderer<E> getRenderer(E pBlockEntity)
    {
        return (BlockEntityRenderer<E>)this.renderers.get(pBlockEntity.getType());
    }

    public void prepare(Level p_173565_, Camera p_173566_, HitResult p_173567_)
    {
        if (this.level != p_173565_)
        {
            this.setLevel(p_173565_);
        }

        this.camera = p_173566_;
        this.cameraHitResult = p_173567_;
    }

    public <E extends BlockEntity> void render(E pBlockEntity, float pPartialTicks, PoseStack pMatrixStack, MultiBufferSource pBuffer)
    {
        BlockEntityRenderer<E> blockentityrenderer = this.getRenderer(pBlockEntity);

        if (blockentityrenderer != null && pBlockEntity.hasLevel() && pBlockEntity.getType().isValid(pBlockEntity.getBlockState()) && blockentityrenderer.shouldRender(pBlockEntity, this.camera.getPosition()))
        {
            tryRender(pBlockEntity, () ->
            {
                setupAndRender(blockentityrenderer, pBlockEntity, pPartialTicks, pMatrixStack, pBuffer);
            });
        }
    }

    private static <T extends BlockEntity> void setupAndRender(BlockEntityRenderer<T> pRenderer, T pBlockEntity, float pPartialTicks, PoseStack pMatrixStack, MultiBufferSource pBuffer)
    {
        Level level = pBlockEntity.getLevel();
        int i;

        if (level != null)
        {
            i = LevelRenderer.getLightColor(level, pBlockEntity.getBlockPos());
        }
        else
        {
            i = 15728880;
        }

        tileEntityRendered = pBlockEntity;

        if (EmissiveTextures.isActive())
        {
            EmissiveTextures.beginRender();
        }

        pRenderer.render(pBlockEntity, pPartialTicks, pMatrixStack, pBuffer, i, OverlayTexture.NO_OVERLAY);

        if (EmissiveTextures.isActive())
        {
            if (EmissiveTextures.hasEmissive())
            {
                EmissiveTextures.beginRenderEmissive();
                pRenderer.render(pBlockEntity, pPartialTicks, pMatrixStack, pBuffer, LightTexture.MAX_BRIGHTNESS, OverlayTexture.NO_OVERLAY);
                EmissiveTextures.endRenderEmissive();
            }

            EmissiveTextures.endRender();
        }

        tileEntityRendered = null;
    }

    public <E extends BlockEntity> boolean renderItem(E pBlockEntity, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pCombinedLight, int pCombinedOverlay)
    {
        BlockEntityRenderer<E> blockentityrenderer = this.getRenderer(pBlockEntity);

        if (blockentityrenderer == null)
        {
            return true;
        }
        else
        {
            tryRender(pBlockEntity, () ->
            {
                tileEntityRendered = pBlockEntity;
                blockentityrenderer.render(pBlockEntity, 0.0F, pMatrixStack, pBuffer, pCombinedLight, pCombinedOverlay);
                tileEntityRendered = null;
            });
            return false;
        }
    }

    private static void tryRender(BlockEntity pBlockEntity, Runnable pRunnable)
    {
        try
        {
            pRunnable.run();
        }
        catch (Throwable throwable)
        {
            CrashReport crashreport = CrashReport.forThrowable(throwable, "Rendering Block Entity");
            CrashReportCategory crashreportcategory = crashreport.addCategory("Block Entity Details");
            pBlockEntity.fillCrashReportCategory(crashreportcategory);
            throw new ReportedException(crashreport);
        }
    }

    public void setLevel(@Nullable Level pLevel)
    {
        this.level = pLevel;

        if (pLevel == null)
        {
            this.camera = null;
        }
    }

    public void onResourceManagerReload(ResourceManager p_173563_)
    {
        BlockEntityRendererProvider.Context blockentityrendererprovider$context = new BlockEntityRendererProvider.Context(this, this.blockRenderDispatcher.get(), this.entityModelSet, this.font);
        this.context = blockentityrendererprovider$context;
        this.renderers = BlockEntityRenderers.createEntityRenderers(blockentityrendererprovider$context);
    }

    public BlockEntityRenderer getRenderer(BlockEntityType type)
    {
        return this.renderers.get(type);
    }

    public BlockEntityRendererProvider.Context getContext()
    {
        return this.context;
    }

    public Map getBlockEntityRenderMap()
    {
        if (this.renderers instanceof ImmutableMap)
        {
            this.renderers = new HashMap<>(this.renderers);
        }

        return this.renderers;
    }

    public synchronized <T extends BlockEntity> void setSpecialRendererInternal(BlockEntityType<T> tileEntityType, BlockEntityRenderer <? super T > specialRenderer)
    {
        this.renderers.put(tileEntityType, specialRenderer);
    }
}
