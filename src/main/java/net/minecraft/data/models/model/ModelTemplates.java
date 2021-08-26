package net.minecraft.data.models.model;

import java.util.Optional;
import java.util.stream.IntStream;
import net.minecraft.resources.ResourceLocation;

public class ModelTemplates
{
    public static final ModelTemplate CUBE = m_125723_("cube", TextureSlot.PARTICLE, TextureSlot.NORTH, TextureSlot.SOUTH, TextureSlot.EAST, TextureSlot.WEST, TextureSlot.UP, TextureSlot.DOWN);
    public static final ModelTemplate CUBE_DIRECTIONAL = m_125723_("cube_directional", TextureSlot.PARTICLE, TextureSlot.NORTH, TextureSlot.SOUTH, TextureSlot.EAST, TextureSlot.WEST, TextureSlot.UP, TextureSlot.DOWN);
    public static final ModelTemplate CUBE_ALL = m_125723_("cube_all", TextureSlot.ALL);
    public static final ModelTemplate CUBE_MIRRORED_ALL = m_125719_("cube_mirrored_all", "_mirrored", TextureSlot.ALL);
    public static final ModelTemplate CUBE_COLUMN = m_125723_("cube_column", TextureSlot.END, TextureSlot.SIDE);
    public static final ModelTemplate CUBE_COLUMN_HORIZONTAL = m_125719_("cube_column_horizontal", "_horizontal", TextureSlot.END, TextureSlot.SIDE);
    public static final ModelTemplate CUBE_COLUMN_MIRRORED = m_125719_("cube_column_mirrored", "_mirrored", TextureSlot.END, TextureSlot.SIDE);
    public static final ModelTemplate CUBE_TOP = m_125723_("cube_top", TextureSlot.TOP, TextureSlot.SIDE);
    public static final ModelTemplate CUBE_BOTTOM_TOP = m_125723_("cube_bottom_top", TextureSlot.TOP, TextureSlot.BOTTOM, TextureSlot.SIDE);
    public static final ModelTemplate CUBE_ORIENTABLE = m_125723_("orientable", TextureSlot.TOP, TextureSlot.FRONT, TextureSlot.SIDE);
    public static final ModelTemplate CUBE_ORIENTABLE_TOP_BOTTOM = m_125723_("orientable_with_bottom", TextureSlot.TOP, TextureSlot.BOTTOM, TextureSlot.SIDE, TextureSlot.FRONT);
    public static final ModelTemplate CUBE_ORIENTABLE_VERTICAL = m_125719_("orientable_vertical", "_vertical", TextureSlot.FRONT, TextureSlot.SIDE);
    public static final ModelTemplate BUTTON = m_125723_("button", TextureSlot.TEXTURE);
    public static final ModelTemplate BUTTON_PRESSED = m_125719_("button_pressed", "_pressed", TextureSlot.TEXTURE);
    public static final ModelTemplate BUTTON_INVENTORY = m_125719_("button_inventory", "_inventory", TextureSlot.TEXTURE);
    public static final ModelTemplate DOOR_BOTTOM = m_125719_("door_bottom", "_bottom", TextureSlot.TOP, TextureSlot.BOTTOM);
    public static final ModelTemplate DOOR_BOTTOM_HINGE = m_125719_("door_bottom_rh", "_bottom_hinge", TextureSlot.TOP, TextureSlot.BOTTOM);
    public static final ModelTemplate DOOR_TOP = m_125719_("door_top", "_top", TextureSlot.TOP, TextureSlot.BOTTOM);
    public static final ModelTemplate DOOR_TOP_HINGE = m_125719_("door_top_rh", "_top_hinge", TextureSlot.TOP, TextureSlot.BOTTOM);
    public static final ModelTemplate FENCE_POST = m_125719_("fence_post", "_post", TextureSlot.TEXTURE);
    public static final ModelTemplate FENCE_SIDE = m_125719_("fence_side", "_side", TextureSlot.TEXTURE);
    public static final ModelTemplate FENCE_INVENTORY = m_125719_("fence_inventory", "_inventory", TextureSlot.TEXTURE);
    public static final ModelTemplate WALL_POST = m_125719_("template_wall_post", "_post", TextureSlot.WALL);
    public static final ModelTemplate WALL_LOW_SIDE = m_125719_("template_wall_side", "_side", TextureSlot.WALL);
    public static final ModelTemplate WALL_TALL_SIDE = m_125719_("template_wall_side_tall", "_side_tall", TextureSlot.WALL);
    public static final ModelTemplate WALL_INVENTORY = m_125719_("wall_inventory", "_inventory", TextureSlot.WALL);
    public static final ModelTemplate FENCE_GATE_CLOSED = m_125723_("template_fence_gate", TextureSlot.TEXTURE);
    public static final ModelTemplate FENCE_GATE_OPEN = m_125719_("template_fence_gate_open", "_open", TextureSlot.TEXTURE);
    public static final ModelTemplate FENCE_GATE_WALL_CLOSED = m_125719_("template_fence_gate_wall", "_wall", TextureSlot.TEXTURE);
    public static final ModelTemplate FENCE_GATE_WALL_OPEN = m_125719_("template_fence_gate_wall_open", "_wall_open", TextureSlot.TEXTURE);
    public static final ModelTemplate PRESSURE_PLATE_UP = m_125723_("pressure_plate_up", TextureSlot.TEXTURE);
    public static final ModelTemplate PRESSURE_PLATE_DOWN = m_125719_("pressure_plate_down", "_down", TextureSlot.TEXTURE);
    public static final ModelTemplate PARTICLE_ONLY = m_125726_(TextureSlot.PARTICLE);
    public static final ModelTemplate SLAB_BOTTOM = m_125723_("slab", TextureSlot.BOTTOM, TextureSlot.TOP, TextureSlot.SIDE);
    public static final ModelTemplate SLAB_TOP = m_125719_("slab_top", "_top", TextureSlot.BOTTOM, TextureSlot.TOP, TextureSlot.SIDE);
    public static final ModelTemplate LEAVES = m_125723_("leaves", TextureSlot.ALL);
    public static final ModelTemplate STAIRS_STRAIGHT = m_125723_("stairs", TextureSlot.BOTTOM, TextureSlot.TOP, TextureSlot.SIDE);
    public static final ModelTemplate STAIRS_INNER = m_125719_("inner_stairs", "_inner", TextureSlot.BOTTOM, TextureSlot.TOP, TextureSlot.SIDE);
    public static final ModelTemplate STAIRS_OUTER = m_125719_("outer_stairs", "_outer", TextureSlot.BOTTOM, TextureSlot.TOP, TextureSlot.SIDE);
    public static final ModelTemplate TRAPDOOR_TOP = m_125719_("template_trapdoor_top", "_top", TextureSlot.TEXTURE);
    public static final ModelTemplate TRAPDOOR_BOTTOM = m_125719_("template_trapdoor_bottom", "_bottom", TextureSlot.TEXTURE);
    public static final ModelTemplate TRAPDOOR_OPEN = m_125719_("template_trapdoor_open", "_open", TextureSlot.TEXTURE);
    public static final ModelTemplate ORIENTABLE_TRAPDOOR_TOP = m_125719_("template_orientable_trapdoor_top", "_top", TextureSlot.TEXTURE);
    public static final ModelTemplate ORIENTABLE_TRAPDOOR_BOTTOM = m_125719_("template_orientable_trapdoor_bottom", "_bottom", TextureSlot.TEXTURE);
    public static final ModelTemplate ORIENTABLE_TRAPDOOR_OPEN = m_125719_("template_orientable_trapdoor_open", "_open", TextureSlot.TEXTURE);
    public static final ModelTemplate POINTED_DRIPSTONE = m_125723_("pointed_dripstone", TextureSlot.CROSS);
    public static final ModelTemplate CROSS = m_125723_("cross", TextureSlot.CROSS);
    public static final ModelTemplate TINTED_CROSS = m_125723_("tinted_cross", TextureSlot.CROSS);
    public static final ModelTemplate FLOWER_POT_CROSS = m_125723_("flower_pot_cross", TextureSlot.PLANT);
    public static final ModelTemplate TINTED_FLOWER_POT_CROSS = m_125723_("tinted_flower_pot_cross", TextureSlot.PLANT);
    public static final ModelTemplate RAIL_FLAT = m_125723_("rail_flat", TextureSlot.RAIL);
    public static final ModelTemplate RAIL_CURVED = m_125719_("rail_curved", "_corner", TextureSlot.RAIL);
    public static final ModelTemplate RAIL_RAISED_NE = m_125719_("template_rail_raised_ne", "_raised_ne", TextureSlot.RAIL);
    public static final ModelTemplate RAIL_RAISED_SW = m_125719_("template_rail_raised_sw", "_raised_sw", TextureSlot.RAIL);
    public static final ModelTemplate CARPET = m_125723_("carpet", TextureSlot.WOOL);
    public static final ModelTemplate CORAL_FAN = m_125723_("coral_fan", TextureSlot.FAN);
    public static final ModelTemplate CORAL_WALL_FAN = m_125723_("coral_wall_fan", TextureSlot.FAN);
    public static final ModelTemplate GLAZED_TERRACOTTA = m_125723_("template_glazed_terracotta", TextureSlot.PATTERN);
    public static final ModelTemplate CHORUS_FLOWER = m_125723_("template_chorus_flower", TextureSlot.TEXTURE);
    public static final ModelTemplate DAYLIGHT_DETECTOR = m_125723_("template_daylight_detector", TextureSlot.TOP, TextureSlot.SIDE);
    public static final ModelTemplate STAINED_GLASS_PANE_NOSIDE = m_125719_("template_glass_pane_noside", "_noside", TextureSlot.PANE);
    public static final ModelTemplate STAINED_GLASS_PANE_NOSIDE_ALT = m_125719_("template_glass_pane_noside_alt", "_noside_alt", TextureSlot.PANE);
    public static final ModelTemplate STAINED_GLASS_PANE_POST = m_125719_("template_glass_pane_post", "_post", TextureSlot.PANE, TextureSlot.EDGE);
    public static final ModelTemplate STAINED_GLASS_PANE_SIDE = m_125719_("template_glass_pane_side", "_side", TextureSlot.PANE, TextureSlot.EDGE);
    public static final ModelTemplate STAINED_GLASS_PANE_SIDE_ALT = m_125719_("template_glass_pane_side_alt", "_side_alt", TextureSlot.PANE, TextureSlot.EDGE);
    public static final ModelTemplate COMMAND_BLOCK = m_125723_("template_command_block", TextureSlot.FRONT, TextureSlot.BACK, TextureSlot.SIDE);
    public static final ModelTemplate ANVIL = m_125723_("template_anvil", TextureSlot.TOP);
    public static final ModelTemplate[] STEMS = IntStream.range(0, 8).mapToObj((p_125729_) ->
    {
        return m_125719_("stem_growth" + p_125729_, "_stage" + p_125729_, TextureSlot.STEM);
    }).toArray((p_125718_) ->
    {
        return new ModelTemplate[p_125718_];
    });
    public static final ModelTemplate ATTACHED_STEM = m_125723_("stem_fruit", TextureSlot.STEM, TextureSlot.UPPER_STEM);
    public static final ModelTemplate CROP = m_125723_("crop", TextureSlot.CROP);
    public static final ModelTemplate FARMLAND = m_125723_("template_farmland", TextureSlot.DIRT, TextureSlot.TOP);
    public static final ModelTemplate FIRE_FLOOR = m_125723_("template_fire_floor", TextureSlot.FIRE);
    public static final ModelTemplate FIRE_SIDE = m_125723_("template_fire_side", TextureSlot.FIRE);
    public static final ModelTemplate FIRE_SIDE_ALT = m_125723_("template_fire_side_alt", TextureSlot.FIRE);
    public static final ModelTemplate FIRE_UP = m_125723_("template_fire_up", TextureSlot.FIRE);
    public static final ModelTemplate FIRE_UP_ALT = m_125723_("template_fire_up_alt", TextureSlot.FIRE);
    public static final ModelTemplate CAMPFIRE = m_125723_("template_campfire", TextureSlot.FIRE, TextureSlot.LIT_LOG);
    public static final ModelTemplate LANTERN = m_125723_("template_lantern", TextureSlot.LANTERN);
    public static final ModelTemplate HANGING_LANTERN = m_125719_("template_hanging_lantern", "_hanging", TextureSlot.LANTERN);
    public static final ModelTemplate TORCH = m_125723_("template_torch", TextureSlot.TORCH);
    public static final ModelTemplate WALL_TORCH = m_125723_("template_torch_wall", TextureSlot.TORCH);
    public static final ModelTemplate PISTON = m_125723_("template_piston", TextureSlot.PLATFORM, TextureSlot.BOTTOM, TextureSlot.SIDE);
    public static final ModelTemplate PISTON_HEAD = m_125723_("template_piston_head", TextureSlot.PLATFORM, TextureSlot.SIDE, TextureSlot.UNSTICKY);
    public static final ModelTemplate PISTON_HEAD_SHORT = m_125723_("template_piston_head_short", TextureSlot.PLATFORM, TextureSlot.SIDE, TextureSlot.UNSTICKY);
    public static final ModelTemplate SEAGRASS = m_125723_("template_seagrass", TextureSlot.TEXTURE);
    public static final ModelTemplate TURTLE_EGG = m_125723_("template_turtle_egg", TextureSlot.ALL);
    public static final ModelTemplate TWO_TURTLE_EGGS = m_125723_("template_two_turtle_eggs", TextureSlot.ALL);
    public static final ModelTemplate THREE_TURTLE_EGGS = m_125723_("template_three_turtle_eggs", TextureSlot.ALL);
    public static final ModelTemplate FOUR_TURTLE_EGGS = m_125723_("template_four_turtle_eggs", TextureSlot.ALL);
    public static final ModelTemplate SINGLE_FACE = m_125723_("template_single_face", TextureSlot.TEXTURE);
    public static final ModelTemplate CAULDRON_LEVEL1 = m_125723_("template_cauldron_level1", TextureSlot.CONTENT, TextureSlot.INSIDE, TextureSlot.PARTICLE, TextureSlot.TOP, TextureSlot.BOTTOM, TextureSlot.SIDE);
    public static final ModelTemplate CAULDRON_LEVEL2 = m_125723_("template_cauldron_level2", TextureSlot.CONTENT, TextureSlot.INSIDE, TextureSlot.PARTICLE, TextureSlot.TOP, TextureSlot.BOTTOM, TextureSlot.SIDE);
    public static final ModelTemplate CAULDRON_FULL = m_125723_("template_cauldron_full", TextureSlot.CONTENT, TextureSlot.INSIDE, TextureSlot.PARTICLE, TextureSlot.TOP, TextureSlot.BOTTOM, TextureSlot.SIDE);
    public static final ModelTemplate AZALEA = m_125723_("template_azalea", TextureSlot.TOP, TextureSlot.SIDE);
    public static final ModelTemplate POTTED_AZALEA = m_125723_("template_potted_azalea_bush", TextureSlot.TOP, TextureSlot.SIDE);
    public static final ModelTemplate FLAT_ITEM = m_125730_("generated", TextureSlot.LAYER0);
    public static final ModelTemplate FLAT_HANDHELD_ITEM = m_125730_("handheld", TextureSlot.LAYER0);
    public static final ModelTemplate FLAT_HANDHELD_ROD_ITEM = m_125730_("handheld_rod", TextureSlot.LAYER0);
    public static final ModelTemplate SHULKER_BOX_INVENTORY = m_125730_("template_shulker_box", TextureSlot.PARTICLE);
    public static final ModelTemplate BED_INVENTORY = m_125730_("template_bed", TextureSlot.PARTICLE);
    public static final ModelTemplate BANNER_INVENTORY = m_125730_("template_banner");
    public static final ModelTemplate SKULL_INVENTORY = m_125730_("template_skull");
    public static final ModelTemplate CANDLE = m_125723_("template_candle", TextureSlot.ALL, TextureSlot.PARTICLE);
    public static final ModelTemplate TWO_CANDLES = m_125723_("template_two_candles", TextureSlot.ALL, TextureSlot.PARTICLE);
    public static final ModelTemplate THREE_CANDLES = m_125723_("template_three_candles", TextureSlot.ALL, TextureSlot.PARTICLE);
    public static final ModelTemplate FOUR_CANDLES = m_125723_("template_four_candles", TextureSlot.ALL, TextureSlot.PARTICLE);
    public static final ModelTemplate CANDLE_CAKE = m_125723_("template_cake_with_candle", TextureSlot.CANDLE, TextureSlot.BOTTOM, TextureSlot.SIDE, TextureSlot.TOP, TextureSlot.PARTICLE);

    private static ModelTemplate m_125726_(TextureSlot... p_125727_)
    {
        return new ModelTemplate(Optional.empty(), Optional.empty(), p_125727_);
    }

    private static ModelTemplate m_125723_(String p_125724_, TextureSlot... p_125725_)
    {
        return new ModelTemplate(Optional.of(new ResourceLocation("minecraft", "block/" + p_125724_)), Optional.empty(), p_125725_);
    }

    private static ModelTemplate m_125730_(String p_125731_, TextureSlot... p_125732_)
    {
        return new ModelTemplate(Optional.of(new ResourceLocation("minecraft", "item/" + p_125731_)), Optional.empty(), p_125732_);
    }

    private static ModelTemplate m_125719_(String p_125720_, String p_125721_, TextureSlot... p_125722_)
    {
        return new ModelTemplate(Optional.of(new ResourceLocation("minecraft", "block/" + p_125720_)), Optional.of(p_125721_), p_125722_);
    }
}
