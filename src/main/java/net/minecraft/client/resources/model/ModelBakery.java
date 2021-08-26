package net.minecraft.client.resources.model;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Transformation;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.BlockModelDefinition;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.block.model.ItemModelGenerator;
import net.minecraft.client.renderer.block.model.multipart.MultiPart;
import net.minecraft.client.renderer.block.model.multipart.Selector;
import net.minecraft.client.renderer.blockentity.BellRenderer;
import net.minecraft.client.renderer.blockentity.ConduitRenderer;
import net.minecraft.client.renderer.blockentity.EnchantTableRenderer;
import net.minecraft.client.renderer.texture.AtlasSet;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.optifine.reflect.Reflector;
import net.optifine.util.StrUtils;
import net.optifine.util.TextureUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ModelBakery
{
    public static final Material FIRE_0 = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("block/fire_0"));
    public static final Material FIRE_1 = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("block/fire_1"));
    public static final Material LAVA_FLOW = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("block/lava_flow"));
    public static final Material WATER_FLOW = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("block/water_flow"));
    public static final Material WATER_OVERLAY = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("block/water_overlay"));
    public static final Material BANNER_BASE = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("entity/banner_base"));
    public static final Material SHIELD_BASE = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("entity/shield_base"));
    public static final Material NO_PATTERN_SHIELD = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("entity/shield_base_nopattern"));
    public static final int DESTROY_STAGE_COUNT = 10;
    public static final List<ResourceLocation> DESTROY_STAGES = IntStream.range(0, 10).mapToObj((indexIn) ->
    {
        return new ResourceLocation("block/destroy_stage_" + indexIn);
    }).collect(Collectors.toList());
    public static final List<ResourceLocation> BREAKING_LOCATIONS = DESTROY_STAGES.stream().map((locationIn) ->
    {
        return new ResourceLocation("textures/" + locationIn.getPath() + ".png");
    }).collect(Collectors.toList());
    public static final List<RenderType> DESTROY_TYPES = BREAKING_LOCATIONS.stream().map(RenderType::crumbling).collect(Collectors.toList());
    private static final Set<Material> UNREFERENCED_TEXTURES = Util.make(Sets.newHashSet(), (setIn) ->
    {
        setIn.add(WATER_FLOW);
        setIn.add(LAVA_FLOW);
        setIn.add(WATER_OVERLAY);
        setIn.add(FIRE_0);
        setIn.add(FIRE_1);
        setIn.add(BellRenderer.BELL_RESOURCE_LOCATION);
        setIn.add(ConduitRenderer.SHELL_TEXTURE);
        setIn.add(ConduitRenderer.ACTIVE_SHELL_TEXTURE);
        setIn.add(ConduitRenderer.WIND_TEXTURE);
        setIn.add(ConduitRenderer.VERTICAL_WIND_TEXTURE);
        setIn.add(ConduitRenderer.OPEN_EYE_TEXTURE);
        setIn.add(ConduitRenderer.CLOSED_EYE_TEXTURE);
        setIn.add(EnchantTableRenderer.BOOK_LOCATION);
        setIn.add(BANNER_BASE);
        setIn.add(SHIELD_BASE);
        setIn.add(NO_PATTERN_SHIELD);

        for (ResourceLocation resourcelocation : DESTROY_STAGES)
        {
            setIn.add(new Material(TextureAtlas.LOCATION_BLOCKS, resourcelocation));
        }

        setIn.add(new Material(TextureAtlas.LOCATION_BLOCKS, InventoryMenu.EMPTY_ARMOR_SLOT_HELMET));
        setIn.add(new Material(TextureAtlas.LOCATION_BLOCKS, InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE));
        setIn.add(new Material(TextureAtlas.LOCATION_BLOCKS, InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS));
        setIn.add(new Material(TextureAtlas.LOCATION_BLOCKS, InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS));
        setIn.add(new Material(TextureAtlas.LOCATION_BLOCKS, InventoryMenu.EMPTY_ARMOR_SLOT_SHIELD));
        Sheets.getAllMaterials(setIn::add);
    });
    static final int SINGLETON_MODEL_GROUP = -1;
    private static final int INVISIBLE_MODEL_GROUP = 0;
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String BUILTIN_SLASH = "builtin/";
    private static final String BUILTIN_SLASH_GENERATED = "builtin/generated";
    private static final String BUILTIN_BLOCK_ENTITY = "builtin/entity";
    private static final String MISSING_MODEL_NAME = "missing";
    public static final ModelResourceLocation MISSING_MODEL_LOCATION = new ModelResourceLocation("builtin/missing", "missing");
    private static final String MISSING_MODEL_LOCATION_STRING = MISSING_MODEL_LOCATION.toString();
    @VisibleForTesting
    public static final String MISSING_MODEL_MESH = ("{    'textures': {       'particle': '" + MissingTextureAtlasSprite.getLocation().getPath() + "',       'missingno': '" + MissingTextureAtlasSprite.getLocation().getPath() + "'    },    'elements': [         {  'from': [ 0, 0, 0 ],            'to': [ 16, 16, 16 ],            'faces': {                'down':  { 'uv': [ 0, 0, 16, 16 ], 'cullface': 'down',  'texture': '#missingno' },                'up':    { 'uv': [ 0, 0, 16, 16 ], 'cullface': 'up',    'texture': '#missingno' },                'north': { 'uv': [ 0, 0, 16, 16 ], 'cullface': 'north', 'texture': '#missingno' },                'south': { 'uv': [ 0, 0, 16, 16 ], 'cullface': 'south', 'texture': '#missingno' },                'west':  { 'uv': [ 0, 0, 16, 16 ], 'cullface': 'west',  'texture': '#missingno' },                'east':  { 'uv': [ 0, 0, 16, 16 ], 'cullface': 'east',  'texture': '#missingno' }            }        }    ]}").replace('\'', '"');
    private static final Map<String, String> BUILTIN_MODELS = Maps.newHashMap(ImmutableMap.of("missing", MISSING_MODEL_MESH));
    private static final Splitter COMMA_SPLITTER = Splitter.on(',');
    private static final Splitter EQUAL_SPLITTER = Splitter.on('=').limit(2);
    public static final BlockModel GENERATION_MARKER = Util.make(BlockModel.fromString("{\"gui_light\": \"front\"}"), (modelIn) ->
    {
        modelIn.name = "generation marker";
    });
    public static final BlockModel BLOCK_ENTITY_MARKER = Util.make(BlockModel.fromString("{\"gui_light\": \"side\"}"), (modelIn) ->
    {
        modelIn.name = "block entity marker";
    });
    private static final StateDefinition<Block, BlockState> ITEM_FRAME_FAKE_DEFINITION = (new StateDefinition.Builder<Block, BlockState>(Blocks.AIR)).m_61104_(BooleanProperty.create("map")).create(Block::defaultBlockState, BlockState::new);
    private static final ItemModelGenerator ITEM_MODEL_GENERATOR = new ItemModelGenerator();
    private static final Map<ResourceLocation, StateDefinition<Block, BlockState>> STATIC_DEFINITIONS = ImmutableMap.of(new ResourceLocation("item_frame"), ITEM_FRAME_FAKE_DEFINITION, new ResourceLocation("glow_item_frame"), ITEM_FRAME_FAKE_DEFINITION);
    private final ResourceManager resourceManager;
    @Nullable
    private AtlasSet atlasSet;
    private final BlockColors blockColors;
    private final Set<ResourceLocation> loadingStack = Sets.newHashSet();
    private final BlockModelDefinition.Context context = new BlockModelDefinition.Context();
    private final Map<ResourceLocation, UnbakedModel> unbakedCache = Maps.newHashMap();
    private final Map<Triple<ResourceLocation, Transformation, Boolean>, BakedModel> bakedCache = Maps.newHashMap();
    private final Map<ResourceLocation, UnbakedModel> topLevelModels = Maps.newHashMap();
    private final Map<ResourceLocation, BakedModel> bakedTopLevelModels = Maps.newHashMap();
    private Map<ResourceLocation, Pair<TextureAtlas, TextureAtlas.Preparations>> atlasPreparations;
    private int nextModelGroup = 1;
    private final Object2IntMap<BlockState> modelGroups = Util.make(new Object2IntOpenHashMap<>(), (mapIn) ->
    {
        mapIn.defaultReturnValue(-1);
    });
    public Map<ResourceLocation, UnbakedModel> mapUnbakedModels;

    public ModelBakery(ResourceManager p_119247_, BlockColors p_119248_, ProfilerFiller p_119249_, int p_119250_)
    {
        this(p_119247_, p_119248_, true);
        this.processLoading(p_119249_, p_119250_);
    }

    protected ModelBakery(ResourceManager resourceManagerIn, BlockColors blockColorsIn, boolean vanillaBakery)
    {
        this.resourceManager = resourceManagerIn;
        this.blockColors = blockColorsIn;
    }

    protected void processLoading(ProfilerFiller profilerIn, int maxMipmapLevel)
    {
        Reflector.ModelLoaderRegistry_onModelLoadingStart.callVoid();
        profilerIn.push("missing_model");

        try
        {
            this.unbakedCache.put(MISSING_MODEL_LOCATION, this.loadBlockModel(MISSING_MODEL_LOCATION));
            this.loadTopLevel(MISSING_MODEL_LOCATION);
        }
        catch (IOException ioexception)
        {
            LOGGER.error("Error loading missing model, should never happen :(", (Throwable)ioexception);
            throw new RuntimeException(ioexception);
        }

        profilerIn.popPush("static_definitions");
        STATIC_DEFINITIONS.forEach((locationIn, definitionIn) ->
        {
            definitionIn.getPossibleStates().forEach((stateIn) -> {
                this.loadTopLevel(BlockModelShaper.stateToModelLocation(locationIn, stateIn));
            });
        });
        profilerIn.popPush("blocks");

        for (Block block : Registry.BLOCK)
        {
            block.getStateDefinition().getPossibleStates().forEach((stateIn) ->
            {
                this.loadTopLevel(BlockModelShaper.stateToModelLocation(stateIn));
            });
        }

        profilerIn.popPush("items");

        for (ResourceLocation resourcelocation : Registry.ITEM.keySet())
        {
            this.loadTopLevel(new ModelResourceLocation(resourcelocation, "inventory"));
        }

        profilerIn.popPush("special");
        this.loadTopLevel(new ModelResourceLocation("minecraft:trident_in_hand#inventory"));
        this.loadTopLevel(new ModelResourceLocation("minecraft:spyglass_in_hand#inventory"));

        for (ResourceLocation resourcelocation1 : this.getSpecialModels())
        {
            this.addModelToCache(resourcelocation1);
        }

        profilerIn.popPush("textures");
        this.mapUnbakedModels = this.unbakedCache;
        TextureUtils.registerCustomModels(this);
        Set<Pair<String, String>> set = Sets.newLinkedHashSet();
        Set<Material> set1 = this.topLevelModels.values().stream().flatMap((modelIn) ->
        {
            return modelIn.getMaterials(this::getModel, set).stream();
        }).collect(Collectors.toSet());
        set1.addAll(UNREFERENCED_TEXTURES);
        Reflector.call(Reflector.ForgeHooksClient_gatherFluidTextures, set1);
        set.stream().filter((pairIn) ->
        {
            return !pairIn.getSecond().equals(MISSING_MODEL_LOCATION_STRING);
        }).forEach((pairIn) ->
        {
            LOGGER.warn("Unable to resolve texture reference: {} in {}", pairIn.getFirst(), pairIn.getSecond());
        });
        Map<ResourceLocation, List<Material>> map = set1.stream().collect(Collectors.groupingBy(Material::atlasLocation));
        profilerIn.popPush("stitching");
        this.atlasPreparations = Maps.newHashMap();

        for (Entry<ResourceLocation, List<Material>> entry : map.entrySet())
        {
            TextureAtlas textureatlas = new TextureAtlas(entry.getKey());
            TextureAtlas.Preparations textureatlas$preparations = textureatlas.prepareToStitch(this.resourceManager, entry.getValue().stream().map(Material::texture), profilerIn, maxMipmapLevel);
            this.atlasPreparations.put(entry.getKey(), Pair.of(textureatlas, textureatlas$preparations));
        }

        profilerIn.pop();
    }

    public AtlasSet uploadTextures(TextureManager pResourceManager, ProfilerFiller pProfiler)
    {
        pProfiler.push("atlas");

        for (Pair<TextureAtlas, TextureAtlas.Preparations> pair : this.atlasPreparations.values())
        {
            TextureAtlas textureatlas = pair.getFirst();
            TextureAtlas.Preparations textureatlas$preparations = pair.getSecond();
            textureatlas.reload(textureatlas$preparations);
            pResourceManager.register(textureatlas.location(), textureatlas);
            pResourceManager.bindForSetup(textureatlas.location());
            textureatlas.updateFilter(textureatlas$preparations);
        }

        this.atlasSet = new AtlasSet(this.atlasPreparations.values().stream().map(Pair::getFirst).collect(Collectors.toList()));
        pProfiler.popPush("baking");
        this.topLevelModels.keySet().forEach((locationIn) ->
        {
            BakedModel bakedmodel = null;

            try {
                bakedmodel = this.bake(locationIn, BlockModelRotation.X0_Y0);
            }
            catch (Exception exception)
            {
                LOGGER.warn("Unable to bake model: '{}': {}", locationIn, exception);
            }

            if (bakedmodel != null)
            {
                this.bakedTopLevelModels.put(locationIn, bakedmodel);
            }
        });
        pProfiler.pop();
        return this.atlasSet;
    }

    private static Predicate<BlockState> predicate(StateDefinition<Block, BlockState> pContainer, String pVariant)
    {
        Map < Property<?>, Comparable<? >> map = Maps.newHashMap();

        for (String s : COMMA_SPLITTER.split(pVariant))
        {
            Iterator<String> iterator = EQUAL_SPLITTER.split(s).iterator();

            if (iterator.hasNext())
            {
                String s1 = iterator.next();
                Property<?> property = pContainer.getProperty(s1);

                if (property != null && iterator.hasNext())
                {
                    String s2 = iterator.next();
                    Comparable<?> comparable = getValueHelper(property, s2);

                    if (comparable == null)
                    {
                        throw new RuntimeException("Unknown value: '" + s2 + "' for blockstate property: '" + s1 + "' " + property.getPossibleValues());
                    }

                    map.put(property, comparable);
                }
                else if (!s1.isEmpty())
                {
                    throw new RuntimeException("Unknown blockstate property: '" + s1 + "'");
                }
            }
        }

        Block block = pContainer.getOwner();
        return (stateIn) ->
        {
            if (stateIn != null && stateIn.is(block))
            {
                for (Entry < Property<?>, Comparable<? >> entry : map.entrySet())
                {
                    if (!Objects.equals(stateIn.getValue(entry.getKey()), entry.getValue()))
                    {
                        return false;
                    }
                }

                return true;
            }
            else {
                return false;
            }
        };
    }

    @Nullable
    static <T extends Comparable<T>> T getValueHelper(Property<T> pProperty, String pValue)
    {
        return pProperty.getValue(pValue).orElse((T)(null));
    }

    public UnbakedModel getModel(ResourceLocation pModelLocation)
    {
        if (this.unbakedCache.containsKey(pModelLocation))
        {
            return this.unbakedCache.get(pModelLocation);
        }
        else if (this.loadingStack.contains(pModelLocation))
        {
            throw new IllegalStateException("Circular reference while loading " + pModelLocation);
        }
        else
        {
            this.loadingStack.add(pModelLocation);
            UnbakedModel unbakedmodel = this.unbakedCache.get(MISSING_MODEL_LOCATION);

            while (!this.loadingStack.isEmpty())
            {
                ResourceLocation resourcelocation = this.loadingStack.iterator().next();

                try
                {
                    if (!this.unbakedCache.containsKey(resourcelocation))
                    {
                        this.loadModel(resourcelocation);
                    }
                }
                catch (ModelBakery.BlockStateDefinitionException modelbakery$blockstatedefinitionexception)
                {
                    LOGGER.warn(modelbakery$blockstatedefinitionexception.getMessage());
                    this.unbakedCache.put(resourcelocation, unbakedmodel);
                }
                catch (Exception exception)
                {
                    LOGGER.warn("Unable to load model: '{}' referenced from: {}: {}", resourcelocation, pModelLocation);
                    LOGGER.warn(exception.getClass().getName() + ": " + exception.getMessage());
                    this.unbakedCache.put(resourcelocation, unbakedmodel);
                }
                finally
                {
                    this.loadingStack.remove(resourcelocation);
                }
            }

            return this.unbakedCache.getOrDefault(pModelLocation, unbakedmodel);
        }
    }

    private void loadModel(ResourceLocation pBlockstateLocation) throws Exception
    {
        if (!(pBlockstateLocation instanceof ModelResourceLocation))
        {
            this.cacheAndQueueDependencies(pBlockstateLocation, this.loadBlockModel(pBlockstateLocation));
        }
        else
        {
            ModelResourceLocation modelresourcelocation = (ModelResourceLocation)pBlockstateLocation;

            if (Objects.equals(modelresourcelocation.getVariant(), "inventory"))
            {
                ResourceLocation resourcelocation2 = new ResourceLocation(pBlockstateLocation.getNamespace(), "item/" + pBlockstateLocation.getPath());
                String s = pBlockstateLocation.getPath();

                if (s.startsWith("optifine/") || s.startsWith("item/"))
                {
                    resourcelocation2 = pBlockstateLocation;
                }

                BlockModel blockmodel = this.loadBlockModel(resourcelocation2);
                this.cacheAndQueueDependencies(modelresourcelocation, blockmodel);
                this.unbakedCache.put(resourcelocation2, blockmodel);
            }
            else
            {
                ResourceLocation resourcelocation = new ResourceLocation(pBlockstateLocation.getNamespace(), pBlockstateLocation.getPath());
                StateDefinition<Block, BlockState> statedefinition = Optional.ofNullable(STATIC_DEFINITIONS.get(resourcelocation)).orElseGet(() ->
                {
                    return Registry.BLOCK.get(resourcelocation).getStateDefinition();
                });
                this.context.setDefinition(statedefinition);
                List < Property<? >> list = ImmutableList.copyOf(this.blockColors.getColoringProperties(statedefinition.getOwner()));
                ImmutableList<BlockState> immutablelist = statedefinition.getPossibleStates();
                Map<ModelResourceLocation, BlockState> map = Maps.newHashMap();
                immutablelist.forEach((stateIn) ->
                {
                    map.put(BlockModelShaper.stateToModelLocation(resourcelocation, stateIn), stateIn);
                });
                Map<BlockState, Pair<UnbakedModel, Supplier<ModelBakery.ModelGroupKey>>> map1 = Maps.newHashMap();
                ResourceLocation resourcelocation1 = new ResourceLocation(pBlockstateLocation.getNamespace(), "blockstates/" + pBlockstateLocation.getPath() + ".json");
                UnbakedModel unbakedmodel = this.unbakedCache.get(MISSING_MODEL_LOCATION);
                ModelBakery.ModelGroupKey modelbakery$modelgroupkey = new ModelBakery.ModelGroupKey(ImmutableList.of(unbakedmodel), ImmutableList.of());
                Pair<UnbakedModel, Supplier<ModelBakery.ModelGroupKey>> pair = Pair.of(unbakedmodel, () ->
                {
                    return modelbakery$modelgroupkey;
                });

                try
                {
                    List<Pair<String, BlockModelDefinition>> list1;

                    try
                    {
                        list1 = this.resourceManager.getResources(resourcelocation1).stream().map((resourceIn) ->
                        {
                            try {
                                InputStream inputstream = resourceIn.getInputStream();

                                Pair<String, BlockModelDefinition> pair2;

                                try {
                                    pair2 = Pair.of(resourceIn.getSourceName(), BlockModelDefinition.fromStream(this.context, new InputStreamReader(inputstream, StandardCharsets.UTF_8)));
                                }
                                catch (Throwable throwable1)
                                {
                                    if (inputstream != null)
                                    {
                                        try
                                        {
                                            inputstream.close();
                                        }
                                        catch (Throwable throwable)
                                        {
                                            throwable1.addSuppressed(throwable);
                                        }
                                    }

                                    throw throwable1;
                                }

                                if (inputstream != null)
                                {
                                    inputstream.close();
                                }

                                return pair2;
                            }
                            catch (Exception exception11)
                            {
                                throw new ModelBakery.BlockStateDefinitionException(String.format("Exception loading blockstate definition: '%s' in resourcepack: '%s': %s", resourceIn.getLocation(), resourceIn.getSourceName(), exception11.getMessage()));
                            }
                        }).collect(Collectors.toList());
                    }
                    catch (IOException ioexception)
                    {
                        LOGGER.warn("Exception loading blockstate definition: {}: {}", resourcelocation1, ioexception);
                        return;
                    }

                    for (Pair<String, BlockModelDefinition> pair1 : list1)
                    {
                        BlockModelDefinition blockmodeldefinition = pair1.getSecond();
                        Map<BlockState, Pair<UnbakedModel, Supplier<ModelBakery.ModelGroupKey>>> map2 = Maps.newIdentityHashMap();
                        MultiPart multipart;

                        if (blockmodeldefinition.isMultiPart())
                        {
                            multipart = blockmodeldefinition.getMultiPart();
                            immutablelist.forEach((stateIn) ->
                            {
                                map2.put(stateIn, Pair.of(multipart, () -> {
                                    return ModelBakery.ModelGroupKey.create(stateIn, multipart, list);
                                }));
                            });
                        }
                        else
                        {
                            multipart = null;
                        }

                        blockmodeldefinition.getVariants().forEach((keyIn, variantIn) ->
                        {
                            try {
                                immutablelist.stream().filter(predicate(statedefinition, keyIn)).forEach((stateIn) -> {
                                    Pair<UnbakedModel, Supplier<ModelBakery.ModelGroupKey>> pair2 = map2.put(stateIn, Pair.of(variantIn, () -> {
                                        return ModelBakery.ModelGroupKey.create(stateIn, variantIn, list);
                                    }));

                                    if (pair2 != null && pair2.getFirst() != multipart)
                                    {
                                        map2.put(stateIn, pair);
                                        throw new RuntimeException("Overlapping definition with: " + (String)blockmodeldefinition.getVariants().entrySet().stream().filter((entityIn) ->
                                        {
                                            return entityIn.getValue() == pair2.getFirst();
                                        }).findFirst().get().getKey());
                                    }
                                });
                            }
                            catch (Exception exception1)
                            {
                                LOGGER.warn("Exception loading blockstate definition: '{}' in resourcepack: '{}' for variant: '{}': {}", resourcelocation1, pair1.getFirst(), keyIn, exception1.getMessage());
                            }
                        });
                        map1.putAll(map2);
                    }

                    return;
                }
                catch (ModelBakery.BlockStateDefinitionException modelbakery$blockstatedefinitionexception)
                {
                    throw modelbakery$blockstatedefinitionexception;
                }
                catch (Exception exception1)
                {
                    throw new ModelBakery.BlockStateDefinitionException(String.format("Exception loading blockstate definition: '%s': %s", resourcelocation1, exception1));
                }
                finally
                {
                    HashMap hashmap = Maps.newHashMap();
                    map.forEach((locationIn, stateIn) ->
                    {
                        Pair<UnbakedModel, Supplier<ModelBakery.ModelGroupKey>> pair2 = map1.get(stateIn);

                        if (pair2 == null)
                        {
                            LOGGER.warn("Exception loading blockstate definition: '{}' missing model for variant: '{}'", resourcelocation1, locationIn);
                            pair2 = pair;
                        }

                        this.cacheAndQueueDependencies(locationIn, pair2.getFirst());

                        try {
                            ModelBakery.ModelGroupKey modelbakery$modelgroupkey1 = pair2.getSecond().get();
                            ((Set) hashmap.computeIfAbsent(modelbakery$modelgroupkey1, (keyIn) -> {
                                return Sets.newIdentityHashSet();
                            })).add(stateIn);
                        }
                        catch (Exception exception11)
                        {
                            LOGGER.warn("Exception evaluating model definition: '{}'", locationIn, exception11);
                        }
                    });
                    hashmap.forEach((keyIn, setIn) ->
                    {
                        Iterator<BlockState> iterator = ((Iterable<BlockState>)setIn).iterator();

                        while (iterator.hasNext())
                        {
                            BlockState blockstate = iterator.next();

                            if (blockstate.getRenderShape() != RenderShape.MODEL)
                            {
                                iterator.remove();
                                this.modelGroups.put(blockstate, 0);
                            }
                        }

                        if (((Set<ResourceLocation>) setIn).size() > 1)
                        {
                            this.registerModelGroup((Iterable<BlockState>) setIn);
                        }
                    });
                }
            }
        }
    }

    private void cacheAndQueueDependencies(ResourceLocation pLocation, UnbakedModel pModel)
    {
        this.unbakedCache.put(pLocation, pModel);
        this.loadingStack.addAll(pModel.getDependencies());
    }

    private void addModelToCache(ResourceLocation locationIn)
    {
        UnbakedModel unbakedmodel = this.getModel(locationIn);
        this.unbakedCache.put(locationIn, unbakedmodel);
        this.topLevelModels.put(locationIn, unbakedmodel);
    }

    public void loadTopLevel(ModelResourceLocation pLocation)
    {
        UnbakedModel unbakedmodel = this.getModel(pLocation);
        this.unbakedCache.put(pLocation, unbakedmodel);
        this.topLevelModels.put(pLocation, unbakedmodel);
    }

    private void registerModelGroup(Iterable<BlockState> pBlockStates)
    {
        int i = this.nextModelGroup++;
        pBlockStates.forEach((stateIn) ->
        {
            this.modelGroups.put(stateIn, i);
        });
    }

    @Nullable
    public BakedModel bake(ResourceLocation pLocation, ModelState pTransform)
    {
        return this.bake(pLocation, pTransform, this.atlasSet::getSprite);
    }

    public BakedModel bake(ResourceLocation locationIn, ModelState transformIn, Function<Material, TextureAtlasSprite> textureGetter)
    {
        Triple<ResourceLocation, Transformation, Boolean> triple = Triple.of(locationIn, transformIn.getRotation(), transformIn.isUvLocked());

        if (this.bakedCache.containsKey(triple))
        {
            return this.bakedCache.get(triple);
        }
        else if (this.atlasSet == null)
        {
            throw new IllegalStateException("bake called too early");
        }
        else
        {
            UnbakedModel unbakedmodel = this.getModel(locationIn);

            if (unbakedmodel instanceof BlockModel)
            {
                BlockModel blockmodel = (BlockModel)unbakedmodel;

                if (blockmodel.getRootModel() == GENERATION_MARKER)
                {
                    if (Reflector.ForgeHooksClient.exists())
                    {
                        return ITEM_MODEL_GENERATOR.generateBlockModel(textureGetter, blockmodel).bake(this, blockmodel, textureGetter, transformIn, locationIn, false);
                    }

                    return ITEM_MODEL_GENERATOR.generateBlockModel(this.atlasSet::getSprite, blockmodel).bake(this, blockmodel, this.atlasSet::getSprite, transformIn, locationIn, false);
                }
            }

            BakedModel bakedmodel = unbakedmodel.bake(this, this.atlasSet::getSprite, transformIn, locationIn);

            if (Reflector.ForgeHooksClient.exists())
            {
                bakedmodel = unbakedmodel.bake(this, textureGetter, transformIn, locationIn);
            }

            this.bakedCache.put(triple, bakedmodel);
            return bakedmodel;
        }
    }

    private BlockModel loadBlockModel(ResourceLocation pLocation) throws IOException
    {
        Reader reader = null;
        Resource resource = null;
        BlockModel basePath;

        try
        {
            String s = pLocation.getPath();
            ResourceLocation resourcelocation = pLocation;

            if ("builtin/generated".equals(s))
            {
                return GENERATION_MARKER;
            }

            if (!"builtin/entity".equals(s))
            {
                if (s.startsWith("builtin/"))
                {
                    String s2 = s.substring("builtin/".length());
                    String s1 = BUILTIN_MODELS.get(s2);

                    if (s1 == null)
                    {
                        throw new FileNotFoundException(pLocation.toString());
                    }

                    reader = new StringReader(s1);
                }
                else
                {
                    resourcelocation = this.getModelLocation(pLocation);
                    resource = this.resourceManager.getResource(resourcelocation);
                    reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
                }

                BlockModel blockmodel = BlockModel.fromStream(reader);
                blockmodel.name = pLocation.toString();
                String s3 = TextureUtils.getBasePath(resourcelocation.getPath());
                fixModelLocations(blockmodel, s3);
                return blockmodel;
            }

            basePath = BLOCK_ENTITY_MARKER;
        }
        finally
        {
            IOUtils.closeQuietly(reader);
            IOUtils.closeQuietly((Closeable)resource);
        }

        return basePath;
    }

    public Map<ResourceLocation, BakedModel> getBakedTopLevelModels()
    {
        return this.bakedTopLevelModels;
    }

    public Object2IntMap<BlockState> getModelGroups()
    {
        return this.modelGroups;
    }

    private ResourceLocation getModelLocation(ResourceLocation location)
    {
        String s = location.getPath();

        if (s.startsWith("optifine/"))
        {
            if (!s.endsWith(".json"))
            {
                location = new ResourceLocation(location.getNamespace(), s + ".json");
            }

            return location;
        }
        else
        {
            return new ResourceLocation(location.getNamespace(), "models/" + location.getPath() + ".json");
        }
    }

    public static void fixModelLocations(BlockModel modelBlock, String basePath)
    {
        ResourceLocation resourcelocation = fixModelLocation(FaceBakery.getParentLocation(modelBlock), basePath);

        if (resourcelocation != FaceBakery.getParentLocation(modelBlock))
        {
            FaceBakery.setParentLocation(modelBlock, resourcelocation);
        }

        if (FaceBakery.getTextures(modelBlock) != null)
        {
            for (Entry<String, Either<Material, String>> entry : FaceBakery.getTextures(modelBlock).entrySet())
            {
                Either<Material, String> either = entry.getValue();
                Optional<Material> optional = either.left();

                if (optional.isPresent())
                {
                    Material material = optional.get();
                    ResourceLocation resourcelocation1 = material.texture();
                    String s = resourcelocation1.getPath();
                    String s1 = fixResourcePath(s, basePath);

                    if (!s1.equals(s))
                    {
                        ResourceLocation resourcelocation2 = new ResourceLocation(resourcelocation1.getNamespace(), s1);
                        Material material1 = new Material(material.atlasLocation(), resourcelocation2);
                        Either<Material, String> either1 = Either.left(material1);
                        entry.setValue(either1);
                    }
                }
            }
        }
    }

    public static ResourceLocation fixModelLocation(ResourceLocation loc, String basePath)
    {
        if (loc != null && basePath != null)
        {
            if (!loc.getNamespace().equals("minecraft"))
            {
                return loc;
            }
            else
            {
                String s = loc.getPath();
                String s1 = fixResourcePath(s, basePath);

                if (s1 != s)
                {
                    loc = new ResourceLocation(loc.getNamespace(), s1);
                }

                return loc;
            }
        }
        else
        {
            return loc;
        }
    }

    private static String fixResourcePath(String path, String basePath)
    {
        path = TextureUtils.fixResourcePath(path, basePath);
        path = StrUtils.removeSuffix(path, ".json");
        return StrUtils.removeSuffix(path, ".png");
    }

    public Set<ResourceLocation> getSpecialModels()
    {
        return Collections.emptySet();
    }

    public AtlasSet getSpriteMap()
    {
        return this.atlasSet;
    }

    static class BlockStateDefinitionException extends RuntimeException
    {
        public BlockStateDefinitionException(String p_119373_)
        {
            super(p_119373_);
        }
    }

    static class ModelGroupKey
    {
        private final List<UnbakedModel> models;
        private final List<Object> coloringValues;

        public ModelGroupKey(List<UnbakedModel> p_119377_, List<Object> p_119378_)
        {
            this.models = p_119377_;
            this.coloringValues = p_119378_;
        }

        public boolean equals(Object p_119395_)
        {
            if (this == p_119395_)
            {
                return true;
            }
            else if (!(p_119395_ instanceof ModelBakery.ModelGroupKey))
            {
                return false;
            }
            else
            {
                ModelBakery.ModelGroupKey modelbakery$modelgroupkey = (ModelBakery.ModelGroupKey)p_119395_;
                return Objects.equals(this.models, modelbakery$modelgroupkey.models) && Objects.equals(this.coloringValues, modelbakery$modelgroupkey.coloringValues);
            }
        }

        public int hashCode()
        {
            return 31 * this.models.hashCode() + this.coloringValues.hashCode();
        }

        public static ModelBakery.ModelGroupKey create(BlockState pBlockState, MultiPart pMultipart, Collection < Property<? >> pProperties)
        {
            StateDefinition<Block, BlockState> statedefinition = pBlockState.getBlock().getStateDefinition();
            List<UnbakedModel> list = pMultipart.getSelectors().stream().filter((selectorIn) ->
            {
                return selectorIn.getPredicate(statedefinition).test(pBlockState);
            }).map(Selector::getVariant).collect(ImmutableList.toImmutableList());
            List<Object> list1 = getColoringValues(pBlockState, pProperties);
            return new ModelBakery.ModelGroupKey(list, list1);
        }

        public static ModelBakery.ModelGroupKey create(BlockState pBlockState, UnbakedModel pMultipart, Collection < Property<? >> pProperties)
        {
            List<Object> list = getColoringValues(pBlockState, pProperties);
            return new ModelBakery.ModelGroupKey(ImmutableList.of(pMultipart), list);
        }

        private static List<Object> getColoringValues(BlockState pBlockState, Collection < Property<? >> pProperties)
        {
            return pProperties.stream().map(pBlockState::getValue).collect(ImmutableList.toImmutableList());
        }
    }
}
