package net.minecraft.client.renderer.entity;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.Map;
import net.minecraft.client.model.SquidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.optifine.player.PlayerItemsLayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EntityRenderers
{
    private static final Logger LOGGER = LogManager.getLogger();
    public static final String DEFAULT_PLAYER_MODEL = "default";
    private static final Map < EntityType<?>, EntityRendererProvider<? >> PROVIDERS = Maps.newHashMap();
    private static final Map<String, EntityRendererProvider<AbstractClientPlayer>> PLAYER_PROVIDERS = ImmutableMap.of("default", (p_208872_0_) ->
    {
        PlayerRenderer playerrenderer = new PlayerRenderer(p_208872_0_, false);
        playerrenderer.addLayer(new PlayerItemsLayer(playerrenderer));
        return playerrenderer;
    }, "slim", (p_208870_0_) ->
    {
        PlayerRenderer playerrenderer = new PlayerRenderer(p_208870_0_, true);
        playerrenderer.addLayer(new PlayerItemsLayer(playerrenderer));
        return playerrenderer;
    });

    private static <T extends Entity> void register(EntityType <? extends T > p_174037_, EntityRendererProvider<T> p_174038_)
    {
        PROVIDERS.put(p_174037_, p_174038_);
    }

    public static Map<EntityType, EntityRenderer> createEntityRenderers(EntityRendererProvider.Context p_174050_)
    {
        Builder<EntityType, EntityRenderer> builder = ImmutableMap.builder();
        PROVIDERS.forEach((p_208814_2_, p_208814_3_) ->
        {
            try {
                builder.put(p_208814_2_, p_208814_3_.create(p_174050_));
            }
            catch (Exception exception)
            {
                throw new IllegalArgumentException("Failed to create model for " + Registry.ENTITY_TYPE.getKey(p_208814_2_), exception);
            }
        });
        return builder.build();
    }

    public static Map<String, EntityRenderer> createPlayerRenderers(EntityRendererProvider.Context p_174052_)
    {
        Builder <String, EntityRenderer> builder = ImmutableMap.builder();
        PLAYER_PROVIDERS.forEach((p_208819_2_, p_208819_3_) ->
        {
            try {
                builder.put(p_208819_2_, p_208819_3_.create(p_174052_));
            }
            catch (Exception exception)
            {
                throw new IllegalArgumentException("Failed to create player model for " + p_208819_2_, exception);
            }
        });
        return builder.build();
    }

    public static boolean validateRegistrations()
    {
        boolean flag = true;

        for (EntityType<?> entitytype : Registry.ENTITY_TYPE)
        {
            if (entitytype != EntityType.PLAYER && !PROVIDERS.containsKey(entitytype))
            {
                LOGGER.warn("No renderer registered for {}", (Object)Registry.ENTITY_TYPE.getKey(entitytype));
                flag = false;
            }
        }

        return !flag;
    }

    static
    {
        register(EntityType.AREA_EFFECT_CLOUD, NoopRenderer::new);
        register(EntityType.ARMOR_STAND, ArmorStandRenderer::new);
        register(EntityType.ARROW, TippableArrowRenderer::new);
        register(EntityType.AXOLOTL, AxolotlRenderer::new);
        register(EntityType.BAT, BatRenderer::new);
        register(EntityType.BEE, BeeRenderer::new);
        register(EntityType.BLAZE, BlazeRenderer::new);
        register(EntityType.BOAT, BoatRenderer::new);
        register(EntityType.CAT, CatRenderer::new);
        register(EntityType.CAVE_SPIDER, CaveSpiderRenderer::new);
        register(EntityType.CHEST_MINECART, (p_208868_0_) ->
        {
            return new MinecartRenderer<>(p_208868_0_, ModelLayers.CHEST_MINECART);
        });
        register(EntityType.CHICKEN, ChickenRenderer::new);
        register(EntityType.COD, CodRenderer::new);
        register(EntityType.COMMAND_BLOCK_MINECART, (p_208866_0_) ->
        {
            return new MinecartRenderer<>(p_208866_0_, ModelLayers.COMMAND_BLOCK_MINECART);
        });
        register(EntityType.COW, CowRenderer::new);
        register(EntityType.CREEPER, CreeperRenderer::new);
        register(EntityType.DOLPHIN, DolphinRenderer::new);
        register(EntityType.DONKEY, (p_208864_0_) ->
        {
            return new ChestedHorseRenderer<>(p_208864_0_, 0.87F, ModelLayers.DONKEY);
        });
        register(EntityType.DRAGON_FIREBALL, DragonFireballRenderer::new);
        register(EntityType.DROWNED, DrownedRenderer::new);
        register(EntityType.EGG, ThrownItemRenderer::new);
        register(EntityType.ELDER_GUARDIAN, ElderGuardianRenderer::new);
        register(EntityType.ENDERMAN, EndermanRenderer::new);
        register(EntityType.ENDERMITE, EndermiteRenderer::new);
        register(EntityType.ENDER_DRAGON, EnderDragonRenderer::new);
        register(EntityType.ENDER_PEARL, ThrownItemRenderer::new);
        register(EntityType.END_CRYSTAL, EndCrystalRenderer::new);
        register(EntityType.EVOKER, EvokerRenderer::new);
        register(EntityType.EVOKER_FANGS, EvokerFangsRenderer::new);
        register(EntityType.EXPERIENCE_BOTTLE, ThrownItemRenderer::new);
        register(EntityType.EXPERIENCE_ORB, ExperienceOrbRenderer::new);
        register(EntityType.EYE_OF_ENDER, (p_208862_0_) ->
        {
            return new ThrownItemRenderer<>(p_208862_0_, 1.0F, true);
        });
        register(EntityType.FALLING_BLOCK, FallingBlockRenderer::new);
        register(EntityType.FIREBALL, (p_208860_0_) ->
        {
            return new ThrownItemRenderer<>(p_208860_0_, 3.0F, true);
        });
        register(EntityType.FIREWORK_ROCKET, FireworkEntityRenderer::new);
        register(EntityType.FISHING_BOBBER, FishingHookRenderer::new);
        register(EntityType.FOX, FoxRenderer::new);
        register(EntityType.FURNACE_MINECART, (p_208858_0_) ->
        {
            return new MinecartRenderer<>(p_208858_0_, ModelLayers.FURNACE_MINECART);
        });
        register(EntityType.GHAST, GhastRenderer::new);
        register(EntityType.GIANT, (p_208856_0_) ->
        {
            return new GiantMobRenderer(p_208856_0_, 6.0F);
        });
        register(EntityType.GLOW_ITEM_FRAME, ItemFrameRenderer::new);
        register(EntityType.GLOW_SQUID, (p_208854_0_) ->
        {
            return new GlowSquidRenderer(p_208854_0_, new SquidModel<>(p_208854_0_.bakeLayer(ModelLayers.GLOW_SQUID)));
        });
        register(EntityType.GOAT, GoatRenderer::new);
        register(EntityType.GUARDIAN, GuardianRenderer::new);
        register(EntityType.HOGLIN, HoglinRenderer::new);
        register(EntityType.HOPPER_MINECART, (p_208852_0_) ->
        {
            return new MinecartRenderer<>(p_208852_0_, ModelLayers.HOPPER_MINECART);
        });
        register(EntityType.HORSE, HorseRenderer::new);
        register(EntityType.HUSK, HuskRenderer::new);
        register(EntityType.ILLUSIONER, IllusionerRenderer::new);
        register(EntityType.IRON_GOLEM, IronGolemRenderer::new);
        register(EntityType.ITEM, ItemEntityRenderer::new);
        register(EntityType.ITEM_FRAME, ItemFrameRenderer::new);
        register(EntityType.LEASH_KNOT, LeashKnotRenderer::new);
        register(EntityType.LIGHTNING_BOLT, LightningBoltRenderer::new);
        register(EntityType.LLAMA, (p_208850_0_) ->
        {
            return new LlamaRenderer(p_208850_0_, ModelLayers.LLAMA);
        });
        register(EntityType.LLAMA_SPIT, LlamaSpitRenderer::new);
        register(EntityType.MAGMA_CUBE, MagmaCubeRenderer::new);
        register(EntityType.MARKER, NoopRenderer::new);
        register(EntityType.MINECART, (p_208848_0_) ->
        {
            return new MinecartRenderer<>(p_208848_0_, ModelLayers.MINECART);
        });
        register(EntityType.MOOSHROOM, MushroomCowRenderer::new);
        register(EntityType.MULE, (p_208846_0_) ->
        {
            return new ChestedHorseRenderer<>(p_208846_0_, 0.92F, ModelLayers.MULE);
        });
        register(EntityType.OCELOT, OcelotRenderer::new);
        register(EntityType.PAINTING, PaintingRenderer::new);
        register(EntityType.PANDA, PandaRenderer::new);
        register(EntityType.PARROT, ParrotRenderer::new);
        register(EntityType.PHANTOM, PhantomRenderer::new);
        register(EntityType.PIG, PigRenderer::new);
        register(EntityType.PIGLIN, (p_208844_0_) ->
        {
            return new PiglinRenderer(p_208844_0_, ModelLayers.PIGLIN, ModelLayers.PIGLIN_INNER_ARMOR, ModelLayers.PIGLIN_OUTER_ARMOR, false);
        });
        register(EntityType.PIGLIN_BRUTE, (p_208842_0_) ->
        {
            return new PiglinRenderer(p_208842_0_, ModelLayers.PIGLIN_BRUTE, ModelLayers.PIGLIN_BRUTE_INNER_ARMOR, ModelLayers.PIGLIN_BRUTE_OUTER_ARMOR, false);
        });
        register(EntityType.PILLAGER, PillagerRenderer::new);
        register(EntityType.POLAR_BEAR, PolarBearRenderer::new);
        register(EntityType.POTION, ThrownItemRenderer::new);
        register(EntityType.PUFFERFISH, PufferfishRenderer::new);
        register(EntityType.RABBIT, RabbitRenderer::new);
        register(EntityType.RAVAGER, RavagerRenderer::new);
        register(EntityType.SALMON, SalmonRenderer::new);
        register(EntityType.SHEEP, SheepRenderer::new);
        register(EntityType.SHULKER, ShulkerRenderer::new);
        register(EntityType.SHULKER_BULLET, ShulkerBulletRenderer::new);
        register(EntityType.SILVERFISH, SilverfishRenderer::new);
        register(EntityType.SKELETON, SkeletonRenderer::new);
        register(EntityType.SKELETON_HORSE, (p_208840_0_) ->
        {
            return new UndeadHorseRenderer(p_208840_0_, ModelLayers.SKELETON_HORSE);
        });
        register(EntityType.SLIME, SlimeRenderer::new);
        register(EntityType.SMALL_FIREBALL, (p_208838_0_) ->
        {
            return new ThrownItemRenderer<>(p_208838_0_, 0.75F, true);
        });
        register(EntityType.SNOWBALL, ThrownItemRenderer::new);
        register(EntityType.SNOW_GOLEM, SnowGolemRenderer::new);
        register(EntityType.SPAWNER_MINECART, (p_208836_0_) ->
        {
            return new MinecartRenderer<>(p_208836_0_, ModelLayers.SPAWNER_MINECART);
        });
        register(EntityType.SPECTRAL_ARROW, SpectralArrowRenderer::new);
        register(EntityType.SPIDER, SpiderRenderer::new);
        register(EntityType.SQUID, (p_208834_0_) ->
        {
            return new SquidRenderer<>(p_208834_0_, new SquidModel<>(p_208834_0_.bakeLayer(ModelLayers.SQUID)));
        });
        register(EntityType.STRAY, StrayRenderer::new);
        register(EntityType.STRIDER, StriderRenderer::new);
        register(EntityType.TNT, TntRenderer::new);
        register(EntityType.TNT_MINECART, TntMinecartRenderer::new);
        register(EntityType.TRADER_LLAMA, (p_208832_0_) ->
        {
            return new LlamaRenderer(p_208832_0_, ModelLayers.TRADER_LLAMA);
        });
        register(EntityType.TRIDENT, ThrownTridentRenderer::new);
        register(EntityType.TROPICAL_FISH, TropicalFishRenderer::new);
        register(EntityType.TURTLE, TurtleRenderer::new);
        register(EntityType.VEX, VexRenderer::new);
        register(EntityType.VILLAGER, VillagerRenderer::new);
        register(EntityType.VINDICATOR, VindicatorRenderer::new);
        register(EntityType.WANDERING_TRADER, WanderingTraderRenderer::new);
        register(EntityType.WITCH, WitchRenderer::new);
        register(EntityType.WITHER, WitherBossRenderer::new);
        register(EntityType.WITHER_SKELETON, WitherSkeletonRenderer::new);
        register(EntityType.WITHER_SKULL, WitherSkullRenderer::new);
        register(EntityType.WOLF, WolfRenderer::new);
        register(EntityType.ZOGLIN, ZoglinRenderer::new);
        register(EntityType.ZOMBIE, ZombieRenderer::new);
        register(EntityType.ZOMBIE_HORSE, (p_208830_0_) ->
        {
            return new UndeadHorseRenderer(p_208830_0_, ModelLayers.ZOMBIE_HORSE);
        });
        register(EntityType.ZOMBIE_VILLAGER, ZombieVillagerRenderer::new);
        register(EntityType.ZOMBIFIED_PIGLIN, (p_208828_0_) ->
        {
            return new PiglinRenderer(p_208828_0_, ModelLayers.ZOMBIFIED_PIGLIN, ModelLayers.ZOMBIFIED_PIGLIN_INNER_ARMOR, ModelLayers.ZOMBIFIED_PIGLIN_OUTER_ARMOR, true);
        });
    }
}
