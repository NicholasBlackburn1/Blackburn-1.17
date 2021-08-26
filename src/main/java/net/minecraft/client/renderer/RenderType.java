package net.minecraft.client.renderer;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.BiFunction;
import java.util.function.Function;
import net.minecraft.Util;
import net.minecraft.client.renderer.blockentity.TheEndPortalRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.optifine.Config;
import net.optifine.EmissiveTextures;
import net.optifine.RandomEntities;
import net.optifine.SmartAnimations;
import net.optifine.render.RenderStateManager;
import net.optifine.render.RenderUtils;
import net.optifine.shaders.Shaders;
import net.optifine.shaders.ShadersRender;
import net.optifine.util.CompoundKey;

public abstract class RenderType extends RenderStateShard
{
    private static final int BYTES_IN_INT = 4;
    private static final int MEGABYTE = 1048576;
    public static final int BIG_BUFFER_SIZE = 2097152;
    public static final int MEDIUM_BUFFER_SIZE = 262144;
    public static final int SMALL_BUFFER_SIZE = 131072;
    public static final int TRANSIENT_BUFFER_SIZE = 256;
    private static final RenderType SOLID = create("solid", DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 2097152, true, false, RenderType.CompositeState.builder().setLightmapState(LIGHTMAP).setShaderState(RENDERTYPE_SOLID_SHADER).setTextureState(BLOCK_SHEET_MIPPED).createCompositeState(true));
    private static final RenderType CUTOUT_MIPPED = create("cutout_mipped", DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 131072, true, false, RenderType.CompositeState.builder().setLightmapState(LIGHTMAP).setShaderState(RENDERTYPE_CUTOUT_MIPPED_SHADER).setTextureState(BLOCK_SHEET_MIPPED).createCompositeState(true));
    private static final RenderType CUTOUT = create("cutout", DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 131072, true, false, RenderType.CompositeState.builder().setLightmapState(LIGHTMAP).setShaderState(RENDERTYPE_CUTOUT_SHADER).setTextureState(BLOCK_SHEET).createCompositeState(true));
    private static final RenderType TRANSLUCENT = create("translucent", DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 2097152, true, true, translucentState(RENDERTYPE_TRANSLUCENT_SHADER));
    private static final RenderType TRANSLUCENT_MOVING_BLOCK = create("translucent_moving_block", DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 262144, false, true, translucentMovingBlockState());
    private static final RenderType TRANSLUCENT_NO_CRUMBLING = create("translucent_no_crumbling", DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 262144, false, true, translucentState(RENDERTYPE_TRANSLUCENT_NO_CRUMBLING_SHADER));
    private static final Function<ResourceLocation, RenderType> ARMOR_CUTOUT_NO_CULL = Util.memoize((p_316266_0_) ->
    {
        RenderType.CompositeState rendertype$compositestate = RenderType.CompositeState.builder().setShaderState(RENDERTYPE_ARMOR_CUTOUT_NO_CULL_SHADER).setTextureState(new RenderStateShard.TextureStateShard(p_316266_0_, false, false)).setTransparencyState(NO_TRANSPARENCY).setCullState(NO_CULL).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).setLayeringState(VIEW_OFFSET_Z_LAYERING).createCompositeState(true);
        return create("armor_cutout_no_cull", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, true, false, rendertype$compositestate);
    });
    private static final Function<ResourceLocation, RenderType> ENTITY_SOLID = Util.memoize((p_316264_0_) ->
    {
        RenderType.CompositeState rendertype$compositestate = RenderType.CompositeState.builder().setShaderState(RENDERTYPE_ENTITY_SOLID_SHADER).setTextureState(new RenderStateShard.TextureStateShard(p_316264_0_, false, false)).setTransparencyState(NO_TRANSPARENCY).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).createCompositeState(true);
        return create("entity_solid", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, true, false, rendertype$compositestate);
    });
    private static final Function<ResourceLocation, RenderType> ENTITY_CUTOUT = Util.memoize((p_316262_0_) ->
    {
        RenderType.CompositeState rendertype$compositestate = RenderType.CompositeState.builder().setShaderState(RENDERTYPE_ENTITY_CUTOUT_SHADER).setTextureState(new RenderStateShard.TextureStateShard(p_316262_0_, false, false)).setTransparencyState(NO_TRANSPARENCY).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).createCompositeState(true);
        return create("entity_cutout", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, true, false, rendertype$compositestate);
    });
    private static final BiFunction<ResourceLocation, Boolean, RenderType> ENTITY_CUTOUT_NO_CULL = Util.memoize((p_316277_0_, p_316277_1_) ->
    {
        RenderType.CompositeState rendertype$compositestate = RenderType.CompositeState.builder().setShaderState(RENDERTYPE_ENTITY_CUTOUT_NO_CULL_SHADER).setTextureState(new RenderStateShard.TextureStateShard(p_316277_0_, false, false)).setTransparencyState(NO_TRANSPARENCY).setCullState(NO_CULL).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).createCompositeState(p_316277_1_);
        return create("entity_cutout_no_cull", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, true, false, rendertype$compositestate);
    });
    private static final BiFunction<ResourceLocation, Boolean, RenderType> ENTITY_CUTOUT_NO_CULL_Z_OFFSET = Util.memoize((p_316274_0_, p_316274_1_) ->
    {
        RenderType.CompositeState rendertype$compositestate = RenderType.CompositeState.builder().setShaderState(RENDERTYPE_ENTITY_CUTOUT_NO_CULL_Z_OFFSET_SHADER).setTextureState(new RenderStateShard.TextureStateShard(p_316274_0_, false, false)).setTransparencyState(NO_TRANSPARENCY).setCullState(NO_CULL).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).setLayeringState(VIEW_OFFSET_Z_LAYERING).createCompositeState(p_316274_1_);
        return create("entity_cutout_no_cull_z_offset", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, true, false, rendertype$compositestate);
    });
    private static final Function<ResourceLocation, RenderType> ITEM_ENTITY_TRANSLUCENT_CULL = Util.memoize((p_316260_0_) ->
    {
        RenderType.CompositeState rendertype$compositestate = RenderType.CompositeState.builder().setShaderState(RENDERTYPE_ITEM_ENTITY_TRANSLUCENT_CULL_SHADER).setTextureState(new RenderStateShard.TextureStateShard(p_316260_0_, false, false)).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setOutputState(ITEM_ENTITY_TARGET).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE).createCompositeState(true);
        return create("item_entity_translucent_cull", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, true, true, rendertype$compositestate);
    });
    private static final Function<ResourceLocation, RenderType> ENTITY_TRANSLUCENT_CULL = Util.memoize((p_316258_0_) ->
    {
        RenderType.CompositeState rendertype$compositestate = RenderType.CompositeState.builder().setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_CULL_SHADER).setTextureState(new RenderStateShard.TextureStateShard(p_316258_0_, false, false)).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).createCompositeState(true);
        return create("entity_translucent_cull", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, true, true, rendertype$compositestate);
    });
    private static final BiFunction<ResourceLocation, Boolean, RenderType> ENTITY_TRANSLUCENT = Util.memoize((p_316271_0_, p_316271_1_) ->
    {
        RenderType.CompositeState rendertype$compositestate = RenderType.CompositeState.builder().setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_SHADER).setTextureState(new RenderStateShard.TextureStateShard(p_316271_0_, false, false)).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setCullState(NO_CULL).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).createCompositeState(p_316271_1_);
        return create("entity_translucent", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, true, true, rendertype$compositestate);
    });
    private static final Function<ResourceLocation, RenderType> ENTITY_SMOOTH_CUTOUT = Util.memoize((p_316256_0_) ->
    {
        RenderType.CompositeState rendertype$compositestate = RenderType.CompositeState.builder().setShaderState(RENDERTYPE_ENTITY_SMOOTH_CUTOUT_SHADER).setTextureState(new RenderStateShard.TextureStateShard(p_316256_0_, false, false)).setCullState(NO_CULL).setLightmapState(LIGHTMAP).createCompositeState(true);
        return create("entity_smooth_cutout", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, rendertype$compositestate);
    });
    private static final BiFunction<ResourceLocation, Boolean, RenderType> BEACON_BEAM = Util.memoize((p_316268_0_, p_316268_1_) ->
    {
        RenderType.CompositeState rendertype$compositestate = RenderType.CompositeState.builder().setShaderState(RENDERTYPE_BEACON_BEAM_SHADER).setTextureState(new RenderStateShard.TextureStateShard(p_316268_0_, false, false)).setTransparencyState(p_316268_1_ ? TRANSLUCENT_TRANSPARENCY : NO_TRANSPARENCY).setWriteMaskState(p_316268_1_ ? COLOR_WRITE : COLOR_DEPTH_WRITE).createCompositeState(false);
        return create("beacon_beam", DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 256, false, true, rendertype$compositestate);
    });
    private static final Function<ResourceLocation, RenderType> ENTITY_DECAL = Util.memoize((p_316254_0_) ->
    {
        RenderType.CompositeState rendertype$compositestate = RenderType.CompositeState.builder().setShaderState(RENDERTYPE_ENTITY_DECAL_SHADER).setTextureState(new RenderStateShard.TextureStateShard(p_316254_0_, false, false)).setDepthTestState(EQUAL_DEPTH_TEST).setCullState(NO_CULL).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).createCompositeState(false);
        return create("entity_decal", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, rendertype$compositestate);
    });
    private static final Function<ResourceLocation, RenderType> ENTITY_NO_OUTLINE = Util.memoize((p_316252_0_) ->
    {
        RenderType.CompositeState rendertype$compositestate = RenderType.CompositeState.builder().setShaderState(RENDERTYPE_ENTITY_NO_OUTLINE_SHADER).setTextureState(new RenderStateShard.TextureStateShard(p_316252_0_, false, false)).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setCullState(NO_CULL).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).setWriteMaskState(COLOR_WRITE).createCompositeState(false);
        return create("entity_no_outline", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false, true, rendertype$compositestate);
    });
    private static final Function<ResourceLocation, RenderType> ENTITY_SHADOW = Util.memoize((p_316250_0_) ->
    {
        RenderType.CompositeState rendertype$compositestate = RenderType.CompositeState.builder().setShaderState(RENDERTYPE_ENTITY_SHADOW_SHADER).setTextureState(new RenderStateShard.TextureStateShard(p_316250_0_, false, false)).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setCullState(CULL).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).setWriteMaskState(COLOR_WRITE).setDepthTestState(LEQUAL_DEPTH_TEST).setLayeringState(VIEW_OFFSET_Z_LAYERING).createCompositeState(false);
        return create("entity_shadow", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false, false, rendertype$compositestate);
    });
    private static final Function<ResourceLocation, RenderType> DRAGON_EXPLOSION_ALPHA = Util.memoize((p_316248_0_) ->
    {
        RenderType.CompositeState rendertype$compositestate = RenderType.CompositeState.builder().setShaderState(RENDERTYPE_ENTITY_ALPHA_SHADER).setTextureState(new RenderStateShard.TextureStateShard(p_316248_0_, false, false)).setCullState(NO_CULL).createCompositeState(true);
        return create("entity_alpha", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, rendertype$compositestate);
    });
    private static final Function<ResourceLocation, RenderType> EYES = Util.memoize((p_316246_0_) ->
    {
        RenderStateShard.TextureStateShard renderstateshard$texturestateshard = new RenderStateShard.TextureStateShard(p_316246_0_, false, false);
        return create("eyes", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false, true, RenderType.CompositeState.builder().setShaderState(RENDERTYPE_EYES_SHADER).setTextureState(renderstateshard$texturestateshard).setTransparencyState(ADDITIVE_TRANSPARENCY).setWriteMaskState(COLOR_WRITE).createCompositeState(false));
    });
    private static final RenderType LEASH = create("leash", DefaultVertexFormat.POSITION_COLOR_LIGHTMAP, VertexFormat.Mode.TRIANGLE_STRIP, 256, RenderType.CompositeState.builder().setShaderState(RENDERTYPE_LEASH_SHADER).setTextureState(NO_TEXTURE).setCullState(NO_CULL).setLightmapState(LIGHTMAP).createCompositeState(false));
    private static final RenderType WATER_MASK = create("water_mask", DefaultVertexFormat.POSITION, VertexFormat.Mode.QUADS, 256, RenderType.CompositeState.builder().setShaderState(RENDERTYPE_WATER_MASK_SHADER).setTextureState(NO_TEXTURE).setWriteMaskState(DEPTH_WRITE).createCompositeState(false));
    private static final RenderType ARMOR_GLINT = create("armor_glint", DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS, 256, RenderType.CompositeState.builder().setShaderState(RENDERTYPE_ARMOR_GLINT_SHADER).setTextureState(new RenderStateShard.TextureStateShard(ItemRenderer.ENCHANT_GLINT_LOCATION, true, false)).setWriteMaskState(COLOR_WRITE).setCullState(NO_CULL).setDepthTestState(EQUAL_DEPTH_TEST).setTransparencyState(GLINT_TRANSPARENCY).setTexturingState(GLINT_TEXTURING).setLayeringState(VIEW_OFFSET_Z_LAYERING).createCompositeState(false));
    private static final RenderType ARMOR_ENTITY_GLINT = create("armor_entity_glint", DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS, 256, RenderType.CompositeState.builder().setShaderState(RENDERTYPE_ARMOR_ENTITY_GLINT_SHADER).setTextureState(new RenderStateShard.TextureStateShard(ItemRenderer.ENCHANT_GLINT_LOCATION, true, false)).setWriteMaskState(COLOR_WRITE).setCullState(NO_CULL).setDepthTestState(EQUAL_DEPTH_TEST).setTransparencyState(GLINT_TRANSPARENCY).setTexturingState(ENTITY_GLINT_TEXTURING).setLayeringState(VIEW_OFFSET_Z_LAYERING).createCompositeState(false));
    private static final RenderType GLINT_TRANSLUCENT = create("glint_translucent", DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS, 256, RenderType.CompositeState.builder().setShaderState(RENDERTYPE_GLINT_TRANSLUCENT_SHADER).setTextureState(new RenderStateShard.TextureStateShard(ItemRenderer.ENCHANT_GLINT_LOCATION, true, false)).setWriteMaskState(COLOR_WRITE).setCullState(NO_CULL).setDepthTestState(EQUAL_DEPTH_TEST).setTransparencyState(GLINT_TRANSPARENCY).setTexturingState(GLINT_TEXTURING).setOutputState(ITEM_ENTITY_TARGET).createCompositeState(false));
    private static final RenderType GLINT = create("glint", DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS, 256, RenderType.CompositeState.builder().setShaderState(RENDERTYPE_GLINT_SHADER).setTextureState(new RenderStateShard.TextureStateShard(ItemRenderer.ENCHANT_GLINT_LOCATION, true, false)).setWriteMaskState(COLOR_WRITE).setCullState(NO_CULL).setDepthTestState(EQUAL_DEPTH_TEST).setTransparencyState(GLINT_TRANSPARENCY).setTexturingState(GLINT_TEXTURING).createCompositeState(false));
    private static final RenderType GLINT_DIRECT = create("glint_direct", DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS, 256, RenderType.CompositeState.builder().setShaderState(RENDERTYPE_GLINT_DIRECT_SHADER).setTextureState(new RenderStateShard.TextureStateShard(ItemRenderer.ENCHANT_GLINT_LOCATION, true, false)).setWriteMaskState(COLOR_WRITE).setCullState(NO_CULL).setDepthTestState(EQUAL_DEPTH_TEST).setTransparencyState(GLINT_TRANSPARENCY).setTexturingState(GLINT_TEXTURING).createCompositeState(false));
    private static final RenderType ENTITY_GLINT = create("entity_glint", DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS, 256, RenderType.CompositeState.builder().setShaderState(RENDERTYPE_ENTITY_GLINT_SHADER).setTextureState(new RenderStateShard.TextureStateShard(ItemRenderer.ENCHANT_GLINT_LOCATION, true, false)).setWriteMaskState(COLOR_WRITE).setCullState(NO_CULL).setDepthTestState(EQUAL_DEPTH_TEST).setTransparencyState(GLINT_TRANSPARENCY).setOutputState(ITEM_ENTITY_TARGET).setTexturingState(ENTITY_GLINT_TEXTURING).createCompositeState(false));
    private static final RenderType ENTITY_GLINT_DIRECT = create("entity_glint_direct", DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS, 256, RenderType.CompositeState.builder().setShaderState(RENDERTYPE_ENTITY_GLINT_DIRECT_SHADER).setTextureState(new RenderStateShard.TextureStateShard(ItemRenderer.ENCHANT_GLINT_LOCATION, true, false)).setWriteMaskState(COLOR_WRITE).setCullState(NO_CULL).setDepthTestState(EQUAL_DEPTH_TEST).setTransparencyState(GLINT_TRANSPARENCY).setTexturingState(ENTITY_GLINT_TEXTURING).createCompositeState(false));
    private static final Function<ResourceLocation, RenderType> CRUMBLING = Util.memoize((p_316244_0_) ->
    {
        RenderStateShard.TextureStateShard renderstateshard$texturestateshard = new RenderStateShard.TextureStateShard(p_316244_0_, false, false);
        return create("crumbling", DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 256, false, true, RenderType.CompositeState.builder().setShaderState(RENDERTYPE_CRUMBLING_SHADER).setTextureState(renderstateshard$texturestateshard).setTransparencyState(CRUMBLING_TRANSPARENCY).setWriteMaskState(COLOR_WRITE).setLayeringState(POLYGON_OFFSET_LAYERING).createCompositeState(false));
    });
    private static final Function<ResourceLocation, RenderType> TEXT = Util.memoize((p_316242_0_) ->
    {
        return create("text", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS, 256, false, false, RenderType.CompositeState.builder().setShaderState(RENDERTYPE_TEXT_SHADER).setTextureState(new RenderStateShard.TextureStateShard(p_316242_0_, false, false)).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setLightmapState(LIGHTMAP).createCompositeState(false));
    });
    private static final Function<ResourceLocation, RenderType> TEXT_INTENSITY = Util.memoize((p_316240_0_) ->
    {
        return create("text_intensity", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS, 256, false, false, RenderType.CompositeState.builder().setShaderState(RENDERTYPE_TEXT_INTENSITY_SHADER).setTextureState(new RenderStateShard.TextureStateShard(p_316240_0_, false, false)).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setLightmapState(LIGHTMAP).createCompositeState(false));
    });
    private static final Function<ResourceLocation, RenderType> TEXT_POLYGON_OFFSET = Util.memoize((p_316294_0_) ->
    {
        return create("text_polygon_offset", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS, 256, false, false, RenderType.CompositeState.builder().setShaderState(RENDERTYPE_TEXT_SHADER).setTextureState(new RenderStateShard.TextureStateShard(p_316294_0_, false, false)).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setLightmapState(LIGHTMAP).setLayeringState(POLYGON_OFFSET_LAYERING).createCompositeState(false));
    });
    private static final Function<ResourceLocation, RenderType> TEXT_INTENSITY_POLYGON_OFFSET = Util.memoize((p_316292_0_) ->
    {
        return create("text_intensity_polygon_offset", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS, 256, false, false, RenderType.CompositeState.builder().setShaderState(RENDERTYPE_TEXT_INTENSITY_SHADER).setTextureState(new RenderStateShard.TextureStateShard(p_316292_0_, false, false)).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setLightmapState(LIGHTMAP).setLayeringState(POLYGON_OFFSET_LAYERING).createCompositeState(false));
    });
    private static final Function<ResourceLocation, RenderType> TEXT_SEE_THROUGH = Util.memoize((p_316290_0_) ->
    {
        return create("text_see_through", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS, 256, false, false, RenderType.CompositeState.builder().setShaderState(RENDERTYPE_TEXT_SEE_THROUGH_SHADER).setTextureState(new RenderStateShard.TextureStateShard(p_316290_0_, false, false)).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setLightmapState(LIGHTMAP).setDepthTestState(NO_DEPTH_TEST).setWriteMaskState(COLOR_WRITE).createCompositeState(false));
    });
    private static final Function<ResourceLocation, RenderType> TEXT_INTENSITY_SEE_THROUGH = Util.memoize((p_316288_0_) ->
    {
        return create("text_intensity_see_through", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS, 256, false, false, RenderType.CompositeState.builder().setShaderState(RENDERTYPE_TEXT_INTENSITY_SEE_THROUGH_SHADER).setTextureState(new RenderStateShard.TextureStateShard(p_316288_0_, false, false)).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setLightmapState(LIGHTMAP).setDepthTestState(NO_DEPTH_TEST).setWriteMaskState(COLOR_WRITE).createCompositeState(false));
    });
    private static final RenderType LIGHTNING = create("lightning", DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 256, false, true, RenderType.CompositeState.builder().setShaderState(RENDERTYPE_LIGHTNING_SHADER).setWriteMaskState(COLOR_DEPTH_WRITE).setTransparencyState(LIGHTNING_TRANSPARENCY).setOutputState(WEATHER_TARGET).createCompositeState(false));
    private static final RenderType TRIPWIRE = create("tripwire", DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 262144, true, true, tripwireState());
    private static final RenderType END_PORTAL = create("end_portal", DefaultVertexFormat.POSITION, VertexFormat.Mode.QUADS, 256, false, false, RenderType.CompositeState.builder().setShaderState(RENDERTYPE_END_PORTAL_SHADER).setTextureState(RenderStateShard.MultiTextureStateShard.builder().add(TheEndPortalRenderer.END_SKY_LOCATION, false, false).add(TheEndPortalRenderer.END_PORTAL_LOCATION, false, false).build()).createCompositeState(false));
    private static final RenderType END_GATEWAY = create("end_gateway", DefaultVertexFormat.POSITION, VertexFormat.Mode.QUADS, 256, false, false, RenderType.CompositeState.builder().setShaderState(RENDERTYPE_END_GATEWAY_SHADER).setTextureState(RenderStateShard.MultiTextureStateShard.builder().add(TheEndPortalRenderer.END_SKY_LOCATION, false, false).add(TheEndPortalRenderer.END_PORTAL_LOCATION, false, false).build()).createCompositeState(false));
    public static final RenderType.CompositeRenderType LINES = create("lines", DefaultVertexFormat.POSITION_COLOR_NORMAL, VertexFormat.Mode.LINES, 256, RenderType.CompositeState.builder().setShaderState(RENDERTYPE_LINES_SHADER).setLineState(new RenderStateShard.LineStateShard(OptionalDouble.empty())).setLayeringState(VIEW_OFFSET_Z_LAYERING).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setOutputState(ITEM_ENTITY_TARGET).setWriteMaskState(COLOR_DEPTH_WRITE).setCullState(NO_CULL).createCompositeState(false));
    public static final RenderType.CompositeRenderType LINE_STRIP = create("line_strip", DefaultVertexFormat.POSITION_COLOR_NORMAL, VertexFormat.Mode.LINE_STRIP, 256, RenderType.CompositeState.builder().setShaderState(RENDERTYPE_LINES_SHADER).setLineState(new RenderStateShard.LineStateShard(OptionalDouble.empty())).setLayeringState(VIEW_OFFSET_Z_LAYERING).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setOutputState(ITEM_ENTITY_TARGET).setWriteMaskState(COLOR_DEPTH_WRITE).setCullState(NO_CULL).createCompositeState(false));
    private final VertexFormat format;
    private final VertexFormat.Mode mode;
    private final int bufferSize;
    private final boolean affectsCrumbling;
    private final boolean sortOnUpload;
    private final Optional<RenderType> asOptional;
    private int id = -1;
    public static final RenderType[] CHUNK_RENDER_TYPES = getChunkRenderTypesArray();
    private static Map<CompoundKey, RenderType> RENDER_TYPES;

    public int ordinal()
    {
        return this.id;
    }

    public boolean isNeedsSorting()
    {
        return this.sortOnUpload;
    }

    private static RenderType[] getChunkRenderTypesArray()
    {
        RenderType[] arendertype = chunkBufferLayers().toArray(new RenderType[0]);
        RenderType rendertype;

        for (int i = 0; i < arendertype.length; rendertype.id = i++)
        {
            rendertype = arendertype[i];
        }

        return arendertype;
    }

    public static RenderType solid()
    {
        return SOLID;
    }

    public static RenderType cutoutMipped()
    {
        return CUTOUT_MIPPED;
    }

    public static RenderType cutout()
    {
        return CUTOUT;
    }

    private static RenderType.CompositeState translucentState(RenderStateShard.ShaderStateShard p_173208_)
    {
        return RenderType.CompositeState.builder().setLightmapState(LIGHTMAP).setShaderState(p_173208_).setTextureState(BLOCK_SHEET_MIPPED).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setOutputState(TRANSLUCENT_TARGET).createCompositeState(true);
    }

    public static RenderType translucent()
    {
        return TRANSLUCENT;
    }

    private static RenderType.CompositeState translucentMovingBlockState()
    {
        return RenderType.CompositeState.builder().setLightmapState(LIGHTMAP).setShaderState(RENDERTYPE_TRANSLUCENT_MOVING_BLOCK_SHADER).setTextureState(BLOCK_SHEET_MIPPED).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setOutputState(ITEM_ENTITY_TARGET).createCompositeState(true);
    }

    public static RenderType translucentMovingBlock()
    {
        return TRANSLUCENT_MOVING_BLOCK;
    }

    public static RenderType translucentNoCrumbling()
    {
        return TRANSLUCENT_NO_CRUMBLING;
    }

    public static RenderType armorCutoutNoCull(ResourceLocation pLocation)
    {
        pLocation = getCustomTexture(pLocation);
        return ARMOR_CUTOUT_NO_CULL.apply(pLocation);
    }

    public static RenderType entitySolid(ResourceLocation pLocation)
    {
        pLocation = getCustomTexture(pLocation);
        return ENTITY_SOLID.apply(pLocation);
    }

    public static RenderType entityCutout(ResourceLocation pLocation)
    {
        pLocation = getCustomTexture(pLocation);
        return ENTITY_CUTOUT.apply(pLocation);
    }

    public static RenderType entityCutoutNoCull(ResourceLocation pLocation, boolean p_110445_)
    {
        pLocation = getCustomTexture(pLocation);
        return ENTITY_CUTOUT_NO_CULL.apply(pLocation, p_110445_);
    }

    public static RenderType entityCutoutNoCull(ResourceLocation pLocation)
    {
        return entityCutoutNoCull(pLocation, true);
    }

    public static RenderType entityCutoutNoCullZOffset(ResourceLocation pLocation, boolean p_110450_)
    {
        pLocation = getCustomTexture(pLocation);
        return ENTITY_CUTOUT_NO_CULL_Z_OFFSET.apply(pLocation, p_110450_);
    }

    public static RenderType entityCutoutNoCullZOffset(ResourceLocation pLocation)
    {
        return entityCutoutNoCullZOffset(pLocation, true);
    }

    public static RenderType itemEntityTranslucentCull(ResourceLocation pLocation)
    {
        pLocation = getCustomTexture(pLocation);
        return ITEM_ENTITY_TRANSLUCENT_CULL.apply(pLocation);
    }

    public static RenderType entityTranslucentCull(ResourceLocation pLocation)
    {
        pLocation = getCustomTexture(pLocation);
        return ENTITY_TRANSLUCENT_CULL.apply(pLocation);
    }

    public static RenderType entityTranslucent(ResourceLocation pLocation, boolean p_110456_)
    {
        pLocation = getCustomTexture(pLocation);
        return ENTITY_TRANSLUCENT.apply(pLocation, p_110456_);
    }

    public static RenderType entityTranslucent(ResourceLocation pLocation)
    {
        return entityTranslucent(pLocation, true);
    }

    public static RenderType entitySmoothCutout(ResourceLocation pLocation)
    {
        pLocation = getCustomTexture(pLocation);
        return ENTITY_SMOOTH_CUTOUT.apply(pLocation);
    }

    public static RenderType beaconBeam(ResourceLocation pLocation, boolean pColorFlag)
    {
        pLocation = getCustomTexture(pLocation);
        return BEACON_BEAM.apply(pLocation, pColorFlag);
    }

    public static RenderType entityDecal(ResourceLocation pLocation)
    {
        pLocation = getCustomTexture(pLocation);
        return ENTITY_DECAL.apply(pLocation);
    }

    public static RenderType entityNoOutline(ResourceLocation pLocation)
    {
        pLocation = getCustomTexture(pLocation);
        return ENTITY_NO_OUTLINE.apply(pLocation);
    }

    public static RenderType entityShadow(ResourceLocation pLocation)
    {
        pLocation = getCustomTexture(pLocation);
        return ENTITY_SHADOW.apply(pLocation);
    }

    public static RenderType dragonExplosionAlpha(ResourceLocation p_173236_)
    {
        p_173236_ = getCustomTexture(p_173236_);
        return DRAGON_EXPLOSION_ALPHA.apply(p_173236_);
    }

    public static RenderType eyes(ResourceLocation pLocation)
    {
        pLocation = getCustomTexture(pLocation);
        return EYES.apply(pLocation);
    }

    public static RenderType energySwirl(ResourceLocation pLocation, float pU, float pV)
    {
        pLocation = getCustomTexture(pLocation);
        return create("energy_swirl", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false, true, RenderType.CompositeState.builder().setShaderState(RENDERTYPE_ENERGY_SWIRL_SHADER).setTextureState(new RenderStateShard.TextureStateShard(pLocation, false, false)).setTexturingState(new RenderStateShard.OffsetTexturingStateShard(pU, pV)).setTransparencyState(ADDITIVE_TRANSPARENCY).setCullState(NO_CULL).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).createCompositeState(false));
    }

    public static RenderType leash()
    {
        return LEASH;
    }

    public static RenderType waterMask()
    {
        return WATER_MASK;
    }

    public static RenderType outline(ResourceLocation pLocation)
    {
        pLocation = getCustomTexture(pLocation);
        return RenderType.CompositeRenderType.OUTLINE.apply(pLocation, NO_CULL);
    }

    public static RenderType armorGlint()
    {
        return ARMOR_GLINT;
    }

    public static RenderType armorEntityGlint()
    {
        return ARMOR_ENTITY_GLINT;
    }

    public static RenderType glintTranslucent()
    {
        return GLINT_TRANSLUCENT;
    }

    public static RenderType glint()
    {
        return GLINT;
    }

    public static RenderType glintDirect()
    {
        return GLINT_DIRECT;
    }

    public static RenderType entityGlint()
    {
        return ENTITY_GLINT;
    }

    public static RenderType entityGlintDirect()
    {
        return ENTITY_GLINT_DIRECT;
    }

    public static RenderType crumbling(ResourceLocation pLocation)
    {
        pLocation = getCustomTexture(pLocation);
        return CRUMBLING.apply(pLocation);
    }

    public static RenderType text(ResourceLocation pLocation)
    {
        return TEXT.apply(pLocation);
    }

    public static RenderType textIntensity(ResourceLocation p_173238_)
    {
        return TEXT_INTENSITY.apply(p_173238_);
    }

    public static RenderType textPolygonOffset(ResourceLocation p_181445_)
    {
        return TEXT_POLYGON_OFFSET.apply(p_181445_);
    }

    public static RenderType textIntensityPolygonOffset(ResourceLocation p_181447_)
    {
        return TEXT_INTENSITY_POLYGON_OFFSET.apply(p_181447_);
    }

    public static RenderType textSeeThrough(ResourceLocation pLocation)
    {
        return TEXT_SEE_THROUGH.apply(pLocation);
    }

    public static RenderType textIntensitySeeThrough(ResourceLocation p_173241_)
    {
        return TEXT_INTENSITY_SEE_THROUGH.apply(p_173241_);
    }

    public static RenderType lightning()
    {
        return LIGHTNING;
    }

    private static RenderType.CompositeState tripwireState()
    {
        return RenderType.CompositeState.builder().setLightmapState(LIGHTMAP).setShaderState(RENDERTYPE_TRIPWIRE_SHADER).setTextureState(BLOCK_SHEET_MIPPED).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setOutputState(WEATHER_TARGET).createCompositeState(true);
    }

    public static RenderType tripwire()
    {
        return TRIPWIRE;
    }

    public static RenderType endPortal()
    {
        return END_PORTAL;
    }

    public static RenderType endGateway()
    {
        return END_GATEWAY;
    }

    public static RenderType lines()
    {
        return LINES;
    }

    public static RenderType lineStrip()
    {
        return LINE_STRIP;
    }

    public RenderType(String p_173178_, VertexFormat p_173179_, VertexFormat.Mode p_173180_, int p_173181_, boolean p_173182_, boolean p_173183_, Runnable p_173184_, Runnable p_173185_)
    {
        super(p_173178_, p_173184_, p_173185_);
        this.format = p_173179_;
        this.mode = p_173180_;
        this.bufferSize = p_173181_;
        this.affectsCrumbling = p_173182_;
        this.sortOnUpload = p_173183_;
        this.asOptional = Optional.of(this);
    }

    static RenderType.CompositeRenderType create(String p_173210_, VertexFormat p_173211_, VertexFormat.Mode p_173212_, int p_173213_, RenderType.CompositeState p_173214_)
    {
        return create(p_173210_, p_173211_, p_173212_, p_173213_, false, false, p_173214_);
    }

    static RenderType.CompositeRenderType create(String p_173216_, VertexFormat p_173217_, VertexFormat.Mode p_173218_, int p_173219_, boolean p_173220_, boolean p_173221_, RenderType.CompositeState p_173222_)
    {
        return new RenderType.CompositeRenderType(p_173216_, p_173217_, p_173218_, p_173219_, p_173220_, p_173221_, p_173222_);
    }

    public void end(BufferBuilder pBuffer, int pCameraX, int pCameraY, int pCameraZ)
    {
        if (pBuffer.building())
        {
            if (this.sortOnUpload)
            {
                pBuffer.setQuadSortOrigin((float)pCameraX, (float)pCameraY, (float)pCameraZ);
            }

            if (pBuffer.animatedSprites != null)
            {
                SmartAnimations.spritesRendered(pBuffer.animatedSprites);
            }

            pBuffer.end();
            this.setupRenderState();

            if (Config.isShaders())
            {
                RenderUtils.setFlushRenderBuffers(false);
                Shaders.pushProgram();
                ShadersRender.preRender(this, pBuffer);
            }

            BufferUploader.end(pBuffer);

            if (Config.isShaders())
            {
                ShadersRender.postRender(this, pBuffer);
                Shaders.popProgram();
                RenderUtils.setFlushRenderBuffers(true);
            }

            this.clearRenderState();
        }
    }

    public String toString()
    {
        return this.name;
    }

    public static List<RenderType> chunkBufferLayers()
    {
        return ImmutableList.of(solid(), cutoutMipped(), cutout(), translucent(), tripwire());
    }

    public int bufferSize()
    {
        return this.bufferSize;
    }

    public VertexFormat format()
    {
        return this.format;
    }

    public VertexFormat.Mode mode()
    {
        return this.mode;
    }

    public Optional<RenderType> outline()
    {
        return Optional.empty();
    }

    public boolean isOutline()
    {
        return false;
    }

    public boolean affectsCrumbling()
    {
        return this.affectsCrumbling;
    }

    public Optional<RenderType> asOptional()
    {
        return this.asOptional;
    }

    public static ResourceLocation getCustomTexture(ResourceLocation locationIn)
    {
        if (Config.isRandomEntities())
        {
            locationIn = RandomEntities.getTextureLocation(locationIn);
        }

        if (EmissiveTextures.isActive())
        {
            locationIn = EmissiveTextures.getEmissiveTexture(locationIn);
        }

        return locationIn;
    }

    public boolean isEntitySolid()
    {
        return this.getName().equals("entity_solid");
    }

    public static int getCountRenderStates()
    {
        return LINES.state.states.size();
    }

    public ResourceLocation getTextureLocation()
    {
        return null;
    }

    public boolean isGlint()
    {
        return this.getTextureLocation() == ItemRenderer.ENCHANT_GLINT_LOCATION;
    }

    static final class CompositeRenderType extends RenderType
    {
        static final BiFunction<ResourceLocation, RenderStateShard.CullStateShard, RenderType> OUTLINE = Util.memoize((p_316303_0_, p_316303_1_) ->
        {
            return RenderType.create("outline", DefaultVertexFormat.POSITION_COLOR_TEX, VertexFormat.Mode.QUADS, 256, RenderType.CompositeState.builder().setShaderState(RENDERTYPE_OUTLINE_SHADER).setTextureState(new RenderStateShard.TextureStateShard(p_316303_0_, false, false)).setCullState(p_316303_1_).setDepthTestState(NO_DEPTH_TEST).setOutputState(OUTLINE_TARGET).createCompositeState(RenderType.OutlineProperty.IS_OUTLINE));
        });
        private final RenderType.CompositeState state;
        private final Optional<RenderType> outline;
        private final boolean isOutline;
        private Map<ResourceLocation, RenderType.CompositeRenderType> mapTextured = new HashMap<>();

        CompositeRenderType(String p_173258_, VertexFormat p_173259_, VertexFormat.Mode p_173260_, int p_173261_, boolean p_173262_, boolean p_173263_, RenderType.CompositeState p_173264_)
        {
            super(p_173258_, p_173259_, p_173260_, p_173261_, p_173262_, p_173263_, () ->
            {
                RenderStateManager.setupRenderStates(p_173264_.states);
            }, () ->
            {
                RenderStateManager.clearRenderStates(p_173264_.states);
            });
            this.state = p_173264_;
            this.outline = p_173264_.outlineProperty == RenderType.OutlineProperty.AFFECTS_OUTLINE ? p_173264_.textureState.cutoutTexture().map((locationIn) ->
            {
                return OUTLINE.apply(locationIn, p_173264_.cullState);
            }) : Optional.empty();
            this.isOutline = p_173264_.outlineProperty == RenderType.OutlineProperty.IS_OUTLINE;
        }

        public Optional<RenderType> outline()
        {
            return this.outline;
        }

        public boolean isOutline()
        {
            return this.isOutline;
        }

        protected final RenderType.CompositeState state()
        {
            return this.state;
        }

        public String toString()
        {
            return "" + this.name + ":RenderType[" + this.name + ":" + this.state + "]";
        }

        public RenderType.CompositeRenderType getTextured(ResourceLocation textureLocation)
        {
            if (textureLocation == null)
            {
                return this;
            }
            else
            {
                Optional<ResourceLocation> optional = this.state.textureState.cutoutTexture();

                if (!optional.isPresent())
                {
                    return this;
                }
                else
                {
                    ResourceLocation resourcelocation = optional.get();

                    if (resourcelocation == null)
                    {
                        return this;
                    }
                    else if (textureLocation.equals(resourcelocation))
                    {
                        return this;
                    }
                    else
                    {
                        RenderType.CompositeRenderType rendertype$compositerendertype = this.mapTextured.get(textureLocation);

                        if (rendertype$compositerendertype == null)
                        {
                            RenderType.CompositeState.CompositeStateBuilder rendertype$compositestate$compositestatebuilder = this.state.getCopyBuilder();
                            rendertype$compositestate$compositestatebuilder.setTextureState(new RenderStateShard.TextureStateShard(textureLocation, this.state.textureState.isBlur(), this.state.textureState.isMipmap()));
                            RenderType.CompositeState rendertype$compositestate = rendertype$compositestate$compositestatebuilder.createCompositeState(this.isOutline);
                            rendertype$compositerendertype = create(this.name, this.format(), this.mode(), this.bufferSize(), this.affectsCrumbling(), this.isNeedsSorting(), rendertype$compositestate);
                            this.mapTextured.put(textureLocation, rendertype$compositerendertype);
                        }

                        return rendertype$compositerendertype;
                    }
                }
            }
        }

        public ResourceLocation getTextureLocation()
        {
            Optional<ResourceLocation> optional = this.state.textureState.cutoutTexture();
            return !optional.isPresent() ? null : optional.get();
        }
    }

    protected static final class CompositeState
    {
        final RenderStateShard.EmptyTextureStateShard textureState;
        private final RenderStateShard.ShaderStateShard shaderState;
        private final RenderStateShard.TransparencyStateShard transparencyState;
        private final RenderStateShard.DepthTestStateShard depthTestState;
        final RenderStateShard.CullStateShard cullState;
        private final RenderStateShard.LightmapStateShard lightmapState;
        private final RenderStateShard.OverlayStateShard overlayState;
        private final RenderStateShard.LayeringStateShard layeringState;
        private final RenderStateShard.OutputStateShard outputState;
        private final RenderStateShard.TexturingStateShard texturingState;
        private final RenderStateShard.WriteMaskStateShard writeMaskState;
        private final RenderStateShard.LineStateShard lineState;
        final RenderType.OutlineProperty outlineProperty;
        final ImmutableList<RenderStateShard> states;

        CompositeState(RenderStateShard.EmptyTextureStateShard p_173276_, RenderStateShard.ShaderStateShard p_173277_, RenderStateShard.TransparencyStateShard p_173278_, RenderStateShard.DepthTestStateShard p_173279_, RenderStateShard.CullStateShard p_173280_, RenderStateShard.LightmapStateShard p_173281_, RenderStateShard.OverlayStateShard p_173282_, RenderStateShard.LayeringStateShard p_173283_, RenderStateShard.OutputStateShard p_173284_, RenderStateShard.TexturingStateShard p_173285_, RenderStateShard.WriteMaskStateShard p_173286_, RenderStateShard.LineStateShard p_173287_, RenderType.OutlineProperty p_173288_)
        {
            this.textureState = p_173276_;
            this.shaderState = p_173277_;
            this.transparencyState = p_173278_;
            this.depthTestState = p_173279_;
            this.cullState = p_173280_;
            this.lightmapState = p_173281_;
            this.overlayState = p_173282_;
            this.layeringState = p_173283_;
            this.outputState = p_173284_;
            this.texturingState = p_173285_;
            this.writeMaskState = p_173286_;
            this.lineState = p_173287_;
            this.outlineProperty = p_173288_;
            this.states = ImmutableList.of(this.textureState, this.shaderState, this.transparencyState, this.depthTestState, this.cullState, this.lightmapState, this.overlayState, this.layeringState, this.outputState, this.texturingState, this.writeMaskState, this.lineState);
        }

        public String toString()
        {
            return "CompositeState[" + this.states + ", outlineProperty=" + this.outlineProperty + "]";
        }

        public static RenderType.CompositeState.CompositeStateBuilder builder()
        {
            return new RenderType.CompositeState.CompositeStateBuilder();
        }

        public RenderType.CompositeState.CompositeStateBuilder getCopyBuilder()
        {
            RenderType.CompositeState.CompositeStateBuilder rendertype$compositestate$compositestatebuilder = new RenderType.CompositeState.CompositeStateBuilder();
            rendertype$compositestate$compositestatebuilder.setTextureState(this.textureState);
            rendertype$compositestate$compositestatebuilder.setShaderState(this.shaderState);
            rendertype$compositestate$compositestatebuilder.setTransparencyState(this.transparencyState);
            rendertype$compositestate$compositestatebuilder.setDepthTestState(this.depthTestState);
            rendertype$compositestate$compositestatebuilder.setCullState(this.cullState);
            rendertype$compositestate$compositestatebuilder.setLightmapState(this.lightmapState);
            rendertype$compositestate$compositestatebuilder.setOverlayState(this.overlayState);
            rendertype$compositestate$compositestatebuilder.setLayeringState(this.layeringState);
            rendertype$compositestate$compositestatebuilder.setOutputState(this.outputState);
            rendertype$compositestate$compositestatebuilder.setTexturingState(this.texturingState);
            rendertype$compositestate$compositestatebuilder.setWriteMaskState(this.writeMaskState);
            rendertype$compositestate$compositestatebuilder.setLineState(this.lineState);
            return rendertype$compositestate$compositestatebuilder;
        }

        public static class CompositeStateBuilder
        {
            private RenderStateShard.EmptyTextureStateShard textureState = RenderStateShard.NO_TEXTURE;
            private RenderStateShard.ShaderStateShard shaderState = RenderStateShard.NO_SHADER;
            private RenderStateShard.TransparencyStateShard transparencyState = RenderStateShard.NO_TRANSPARENCY;
            private RenderStateShard.DepthTestStateShard depthTestState = RenderStateShard.LEQUAL_DEPTH_TEST;
            private RenderStateShard.CullStateShard cullState = RenderStateShard.CULL;
            private RenderStateShard.LightmapStateShard lightmapState = RenderStateShard.NO_LIGHTMAP;
            private RenderStateShard.OverlayStateShard overlayState = RenderStateShard.NO_OVERLAY;
            private RenderStateShard.LayeringStateShard layeringState = RenderStateShard.NO_LAYERING;
            private RenderStateShard.OutputStateShard outputState = RenderStateShard.MAIN_TARGET;
            private RenderStateShard.TexturingStateShard texturingState = RenderStateShard.DEFAULT_TEXTURING;
            private RenderStateShard.WriteMaskStateShard writeMaskState = RenderStateShard.COLOR_DEPTH_WRITE;
            private RenderStateShard.LineStateShard lineState = RenderStateShard.DEFAULT_LINE;

            CompositeStateBuilder()
            {
            }

            public RenderType.CompositeState.CompositeStateBuilder setTextureState(RenderStateShard.EmptyTextureStateShard p_173291_)
            {
                this.textureState = p_173291_;
                return this;
            }

            public RenderType.CompositeState.CompositeStateBuilder setShaderState(RenderStateShard.ShaderStateShard p_173293_)
            {
                this.shaderState = p_173293_;
                return this;
            }

            public RenderType.CompositeState.CompositeStateBuilder setTransparencyState(RenderStateShard.TransparencyStateShard pTransparency)
            {
                this.transparencyState = pTransparency;
                return this;
            }

            public RenderType.CompositeState.CompositeStateBuilder setDepthTestState(RenderStateShard.DepthTestStateShard pDepthTest)
            {
                this.depthTestState = pDepthTest;
                return this;
            }

            public RenderType.CompositeState.CompositeStateBuilder setCullState(RenderStateShard.CullStateShard pCull)
            {
                this.cullState = pCull;
                return this;
            }

            public RenderType.CompositeState.CompositeStateBuilder setLightmapState(RenderStateShard.LightmapStateShard pLightmap)
            {
                this.lightmapState = pLightmap;
                return this;
            }

            public RenderType.CompositeState.CompositeStateBuilder setOverlayState(RenderStateShard.OverlayStateShard pOverlay)
            {
                this.overlayState = pOverlay;
                return this;
            }

            public RenderType.CompositeState.CompositeStateBuilder setLayeringState(RenderStateShard.LayeringStateShard pLayer)
            {
                this.layeringState = pLayer;
                return this;
            }

            public RenderType.CompositeState.CompositeStateBuilder setOutputState(RenderStateShard.OutputStateShard pTarget)
            {
                this.outputState = pTarget;
                return this;
            }

            public RenderType.CompositeState.CompositeStateBuilder setTexturingState(RenderStateShard.TexturingStateShard pTexturing)
            {
                this.texturingState = pTexturing;
                return this;
            }

            public RenderType.CompositeState.CompositeStateBuilder setWriteMaskState(RenderStateShard.WriteMaskStateShard pWriteMask)
            {
                this.writeMaskState = pWriteMask;
                return this;
            }

            public RenderType.CompositeState.CompositeStateBuilder setLineState(RenderStateShard.LineStateShard pLine)
            {
                this.lineState = pLine;
                return this;
            }

            public RenderType.CompositeState createCompositeState(boolean pOutlineState)
            {
                return this.createCompositeState(pOutlineState ? RenderType.OutlineProperty.AFFECTS_OUTLINE : RenderType.OutlineProperty.NONE);
            }

            public RenderType.CompositeState createCompositeState(RenderType.OutlineProperty pOutlineState)
            {
                return new RenderType.CompositeState(this.textureState, this.shaderState, this.transparencyState, this.depthTestState, this.cullState, this.lightmapState, this.overlayState, this.layeringState, this.outputState, this.texturingState, this.writeMaskState, this.lineState, pOutlineState);
            }
        }
    }

    static enum OutlineProperty
    {
        NONE("none"),
        IS_OUTLINE("is_outline"),
        AFFECTS_OUTLINE("affects_outline");

        private final String name;

        private OutlineProperty(String p_110702_)
        {
            this.name = p_110702_;
        }

        public String toString()
        {
            return this.name;
        }
    }
}
