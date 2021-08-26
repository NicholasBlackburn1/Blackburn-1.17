package net.minecraft.advancements.critereon;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.Tag;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Team;

public class EntityPredicate
{
    public static final EntityPredicate ANY = new EntityPredicate(EntityTypePredicate.ANY, DistancePredicate.ANY, LocationPredicate.ANY, LocationPredicate.ANY, MobEffectsPredicate.ANY, NbtPredicate.ANY, EntityFlagsPredicate.ANY, EntityEquipmentPredicate.ANY, PlayerPredicate.ANY, FishingHookPredicate.ANY, LighthingBoltPredicate.ANY, (String)null, (ResourceLocation)null);
    private final EntityTypePredicate entityType;
    private final DistancePredicate distanceToPlayer;
    private final LocationPredicate location;
    private final LocationPredicate steppingOnLocation;
    private final MobEffectsPredicate effects;
    private final NbtPredicate nbt;
    private final EntityFlagsPredicate flags;
    private final EntityEquipmentPredicate equipment;
    private final PlayerPredicate player;
    private final FishingHookPredicate fishingHook;
    private final LighthingBoltPredicate lighthingBolt;
    private final EntityPredicate vehicle;
    private final EntityPredicate passenger;
    private final EntityPredicate targetedEntity;
    @Nullable
    private final String team;
    @Nullable
    private final ResourceLocation catType;

    private EntityPredicate(EntityTypePredicate p_150306_, DistancePredicate p_150307_, LocationPredicate p_150308_, LocationPredicate p_150309_, MobEffectsPredicate p_150310_, NbtPredicate p_150311_, EntityFlagsPredicate p_150312_, EntityEquipmentPredicate p_150313_, PlayerPredicate p_150314_, FishingHookPredicate p_150315_, LighthingBoltPredicate p_150316_, @Nullable String p_150317_, @Nullable ResourceLocation p_150318_)
    {
        this.entityType = p_150306_;
        this.distanceToPlayer = p_150307_;
        this.location = p_150308_;
        this.steppingOnLocation = p_150309_;
        this.effects = p_150310_;
        this.nbt = p_150311_;
        this.flags = p_150312_;
        this.equipment = p_150313_;
        this.player = p_150314_;
        this.fishingHook = p_150315_;
        this.lighthingBolt = p_150316_;
        this.passenger = this;
        this.vehicle = this;
        this.targetedEntity = this;
        this.team = p_150317_;
        this.catType = p_150318_;
    }

    EntityPredicate(EntityTypePredicate p_150289_, DistancePredicate p_150290_, LocationPredicate p_150291_, LocationPredicate p_150292_, MobEffectsPredicate p_150293_, NbtPredicate p_150294_, EntityFlagsPredicate p_150295_, EntityEquipmentPredicate p_150296_, PlayerPredicate p_150297_, FishingHookPredicate p_150298_, LighthingBoltPredicate p_150299_, EntityPredicate p_150300_, EntityPredicate p_150301_, EntityPredicate p_150302_, @Nullable String p_150303_, @Nullable ResourceLocation p_150304_)
    {
        this.entityType = p_150289_;
        this.distanceToPlayer = p_150290_;
        this.location = p_150291_;
        this.steppingOnLocation = p_150292_;
        this.effects = p_150293_;
        this.nbt = p_150294_;
        this.flags = p_150295_;
        this.equipment = p_150296_;
        this.player = p_150297_;
        this.fishingHook = p_150298_;
        this.lighthingBolt = p_150299_;
        this.vehicle = p_150300_;
        this.passenger = p_150301_;
        this.targetedEntity = p_150302_;
        this.team = p_150303_;
        this.catType = p_150304_;
    }

    public boolean matches(ServerPlayer pLevel, @Nullable Entity pVector)
    {
        return this.matches(pLevel.getLevel(), pLevel.position(), pVector);
    }

    public boolean matches(ServerLevel pLevel, @Nullable Vec3 pVector, @Nullable Entity pEntity)
    {
        if (this == ANY)
        {
            return true;
        }
        else if (pEntity == null)
        {
            return false;
        }
        else if (!this.entityType.matches(pEntity.getType()))
        {
            return false;
        }
        else
        {
            if (pVector == null)
            {
                if (this.distanceToPlayer != DistancePredicate.ANY)
                {
                    return false;
                }
            }
            else if (!this.distanceToPlayer.matches(pVector.x, pVector.y, pVector.z, pEntity.getX(), pEntity.getY(), pEntity.getZ()))
            {
                return false;
            }

            if (!this.location.matches(pLevel, pEntity.getX(), pEntity.getY(), pEntity.getZ()))
            {
                return false;
            }
            else
            {
                if (this.steppingOnLocation != LocationPredicate.ANY)
                {
                    Vec3 vec3 = Vec3.atCenterOf(pEntity.getOnPos());

                    if (!this.steppingOnLocation.matches(pLevel, vec3.x(), vec3.y(), vec3.z()))
                    {
                        return false;
                    }
                }

                if (!this.effects.matches(pEntity))
                {
                    return false;
                }
                else if (!this.nbt.matches(pEntity))
                {
                    return false;
                }
                else if (!this.flags.matches(pEntity))
                {
                    return false;
                }
                else if (!this.equipment.matches(pEntity))
                {
                    return false;
                }
                else if (!this.player.matches(pEntity))
                {
                    return false;
                }
                else if (!this.fishingHook.matches(pEntity))
                {
                    return false;
                }
                else if (!this.lighthingBolt.matches(pEntity, pLevel, pVector))
                {
                    return false;
                }
                else if (!this.vehicle.matches(pLevel, pVector, pEntity.getVehicle()))
                {
                    return false;
                }
                else if (this.passenger != ANY && pEntity.getPassengers().stream().noneMatch((p_150322_) ->
            {
                return this.passenger.matches(pLevel, pVector, p_150322_);
                }))
                {
                    return false;
                }
                else if (!this.targetedEntity.matches(pLevel, pVector, pEntity instanceof Mob ? ((Mob)pEntity).getTarget() : null))
                {
                    return false;
                }
                else
                {
                    if (this.team != null)
                    {
                        Team team = pEntity.getTeam();

                        if (team == null || !this.team.equals(team.getName()))
                        {
                            return false;
                        }
                    }

                    return this.catType == null || pEntity instanceof Cat && ((Cat)pEntity).getResourceLocation().equals(this.catType);
                }
            }
        }
    }

    public static EntityPredicate fromJson(@Nullable JsonElement pElement)
    {
        if (pElement != null && !pElement.isJsonNull())
        {
            JsonObject jsonobject = GsonHelper.convertToJsonObject(pElement, "entity");
            EntityTypePredicate entitytypepredicate = EntityTypePredicate.fromJson(jsonobject.get("type"));
            DistancePredicate distancepredicate = DistancePredicate.fromJson(jsonobject.get("distance"));
            LocationPredicate locationpredicate = LocationPredicate.fromJson(jsonobject.get("location"));
            LocationPredicate locationpredicate1 = LocationPredicate.fromJson(jsonobject.get("stepping_on"));
            MobEffectsPredicate mobeffectspredicate = MobEffectsPredicate.fromJson(jsonobject.get("effects"));
            NbtPredicate nbtpredicate = NbtPredicate.fromJson(jsonobject.get("nbt"));
            EntityFlagsPredicate entityflagspredicate = EntityFlagsPredicate.fromJson(jsonobject.get("flags"));
            EntityEquipmentPredicate entityequipmentpredicate = EntityEquipmentPredicate.fromJson(jsonobject.get("equipment"));
            PlayerPredicate playerpredicate = PlayerPredicate.fromJson(jsonobject.get("player"));
            FishingHookPredicate fishinghookpredicate = FishingHookPredicate.fromJson(jsonobject.get("fishing_hook"));
            EntityPredicate entitypredicate = fromJson(jsonobject.get("vehicle"));
            EntityPredicate entitypredicate1 = fromJson(jsonobject.get("passenger"));
            EntityPredicate entitypredicate2 = fromJson(jsonobject.get("targeted_entity"));
            LighthingBoltPredicate lighthingboltpredicate = LighthingBoltPredicate.fromJson(jsonobject.get("lightning_bolt"));
            String s = GsonHelper.getAsString(jsonobject, "team", (String)null);
            ResourceLocation resourcelocation = jsonobject.has("catType") ? new ResourceLocation(GsonHelper.getAsString(jsonobject, "catType")) : null;
            return (new EntityPredicate.Builder()).entityType(entitytypepredicate).distance(distancepredicate).located(locationpredicate).steppingOn(locationpredicate1).effects(mobeffectspredicate).nbt(nbtpredicate).flags(entityflagspredicate).equipment(entityequipmentpredicate).player(playerpredicate).fishingHook(fishinghookpredicate).lighthingBolt(lighthingboltpredicate).team(s).vehicle(entitypredicate).passenger(entitypredicate1).targetedEntity(entitypredicate2).catType(resourcelocation).build();
        }
        else
        {
            return ANY;
        }
    }

    public JsonElement serializeToJson()
    {
        if (this == ANY)
        {
            return JsonNull.INSTANCE;
        }
        else
        {
            JsonObject jsonobject = new JsonObject();
            jsonobject.add("type", this.entityType.serializeToJson());
            jsonobject.add("distance", this.distanceToPlayer.serializeToJson());
            jsonobject.add("location", this.location.serializeToJson());
            jsonobject.add("stepping_on", this.steppingOnLocation.serializeToJson());
            jsonobject.add("effects", this.effects.serializeToJson());
            jsonobject.add("nbt", this.nbt.serializeToJson());
            jsonobject.add("flags", this.flags.serializeToJson());
            jsonobject.add("equipment", this.equipment.serializeToJson());
            jsonobject.add("player", this.player.serializeToJson());
            jsonobject.add("fishing_hook", this.fishingHook.serializeToJson());
            jsonobject.add("lightning_bolt", this.lighthingBolt.serializeToJson());
            jsonobject.add("vehicle", this.vehicle.serializeToJson());
            jsonobject.add("passenger", this.passenger.serializeToJson());
            jsonobject.add("targeted_entity", this.targetedEntity.serializeToJson());
            jsonobject.addProperty("team", this.team);

            if (this.catType != null)
            {
                jsonobject.addProperty("catType", this.catType.toString());
            }

            return jsonobject;
        }
    }

    public static LootContext createContext(ServerPlayer pPlayer, Entity pEntity)
    {
        return (new LootContext.Builder(pPlayer.getLevel())).withParameter(LootContextParams.THIS_ENTITY, pEntity).withParameter(LootContextParams.ORIGIN, pPlayer.position()).withRandom(pPlayer.getRandom()).create(LootContextParamSets.ADVANCEMENT_ENTITY);
    }

    public static class Builder
    {
        private EntityTypePredicate entityType = EntityTypePredicate.ANY;
        private DistancePredicate distanceToPlayer = DistancePredicate.ANY;
        private LocationPredicate location = LocationPredicate.ANY;
        private LocationPredicate steppingOnLocation = LocationPredicate.ANY;
        private MobEffectsPredicate effects = MobEffectsPredicate.ANY;
        private NbtPredicate nbt = NbtPredicate.ANY;
        private EntityFlagsPredicate flags = EntityFlagsPredicate.ANY;
        private EntityEquipmentPredicate equipment = EntityEquipmentPredicate.ANY;
        private PlayerPredicate player = PlayerPredicate.ANY;
        private FishingHookPredicate fishingHook = FishingHookPredicate.ANY;
        private LighthingBoltPredicate lighthingBolt = LighthingBoltPredicate.ANY;
        private EntityPredicate vehicle = EntityPredicate.ANY;
        private EntityPredicate passenger = EntityPredicate.ANY;
        private EntityPredicate targetedEntity = EntityPredicate.ANY;
        private String team;
        private ResourceLocation catType;

        public static EntityPredicate.Builder entity()
        {
            return new EntityPredicate.Builder();
        }

        public EntityPredicate.Builder of(EntityType<?> pCatType)
        {
            this.entityType = EntityTypePredicate.of(pCatType);
            return this;
        }

        public EntityPredicate.Builder of(Tag < EntityType<? >> pCatType)
        {
            this.entityType = EntityTypePredicate.of(pCatType);
            return this;
        }

        public EntityPredicate.Builder of(ResourceLocation pCatType)
        {
            this.catType = pCatType;
            return this;
        }

        public EntityPredicate.Builder entityType(EntityTypePredicate pType)
        {
            this.entityType = pType;
            return this;
        }

        public EntityPredicate.Builder distance(DistancePredicate pDistance)
        {
            this.distanceToPlayer = pDistance;
            return this;
        }

        public EntityPredicate.Builder located(LocationPredicate pLocation)
        {
            this.location = pLocation;
            return this;
        }

        public EntityPredicate.Builder steppingOn(LocationPredicate p_150331_)
        {
            this.steppingOnLocation = p_150331_;
            return this;
        }

        public EntityPredicate.Builder effects(MobEffectsPredicate pEffects)
        {
            this.effects = pEffects;
            return this;
        }

        public EntityPredicate.Builder nbt(NbtPredicate pNbt)
        {
            this.nbt = pNbt;
            return this;
        }

        public EntityPredicate.Builder flags(EntityFlagsPredicate pFlags)
        {
            this.flags = pFlags;
            return this;
        }

        public EntityPredicate.Builder equipment(EntityEquipmentPredicate pEquipment)
        {
            this.equipment = pEquipment;
            return this;
        }

        public EntityPredicate.Builder player(PlayerPredicate pPlayer)
        {
            this.player = pPlayer;
            return this;
        }

        public EntityPredicate.Builder fishingHook(FishingHookPredicate pFishing)
        {
            this.fishingHook = pFishing;
            return this;
        }

        public EntityPredicate.Builder lighthingBolt(LighthingBoltPredicate p_150327_)
        {
            this.lighthingBolt = p_150327_;
            return this;
        }

        public EntityPredicate.Builder vehicle(EntityPredicate pMount)
        {
            this.vehicle = pMount;
            return this;
        }

        public EntityPredicate.Builder passenger(EntityPredicate p_150329_)
        {
            this.passenger = p_150329_;
            return this;
        }

        public EntityPredicate.Builder targetedEntity(EntityPredicate pTarget)
        {
            this.targetedEntity = pTarget;
            return this;
        }

        public EntityPredicate.Builder team(@Nullable String pTeam)
        {
            this.team = pTeam;
            return this;
        }

        public EntityPredicate.Builder catType(@Nullable ResourceLocation pCatType)
        {
            this.catType = pCatType;
            return this;
        }

        public EntityPredicate build()
        {
            return new EntityPredicate(this.entityType, this.distanceToPlayer, this.location, this.steppingOnLocation, this.effects, this.nbt, this.flags, this.equipment, this.player, this.fishingHook, this.lighthingBolt, this.vehicle, this.passenger, this.targetedEntity, this.team, this.catType);
        }
    }

    public static class Composite
    {
        public static final EntityPredicate.Composite ANY = new EntityPredicate.Composite(new LootItemCondition[0]);
        private final LootItemCondition[] conditions;
        private final Predicate<LootContext> compositePredicates;

        private Composite(LootItemCondition[] p_36672_)
        {
            this.conditions = p_36672_;
            this.compositePredicates = LootItemConditions.m_81834_(p_36672_);
        }

        public static EntityPredicate.Composite m_36690_(LootItemCondition... p_36691_)
        {
            return new EntityPredicate.Composite(p_36691_);
        }

        public static EntityPredicate.Composite fromJson(JsonObject pJsonObject, String pName, DeserializationContext pConditions)
        {
            JsonElement jsonelement = pJsonObject.get(pName);
            return fromElement(pName, pConditions, jsonelement);
        }

        public static EntityPredicate.Composite[] fromJsonArray(JsonObject pJsonObject, String pName, DeserializationContext pConditions)
        {
            JsonElement jsonelement = pJsonObject.get(pName);

            if (jsonelement != null && !jsonelement.isJsonNull())
            {
                JsonArray jsonarray = GsonHelper.convertToJsonArray(jsonelement, pName);
                EntityPredicate.Composite[] aentitypredicate$composite = new EntityPredicate.Composite[jsonarray.size()];

                for (int i = 0; i < jsonarray.size(); ++i)
                {
                    aentitypredicate$composite[i] = fromElement(pName + "[" + i + "]", pConditions, jsonarray.get(i));
                }

                return aentitypredicate$composite;
            }
            else
            {
                return new EntityPredicate.Composite[0];
            }
        }

        private static EntityPredicate.Composite fromElement(String pName, DeserializationContext pConditions, @Nullable JsonElement pElement)
        {
            if (pElement != null && pElement.isJsonArray())
            {
                LootItemCondition[] alootitemcondition = pConditions.deserializeConditions(pElement.getAsJsonArray(), pConditions.getAdvancementId() + "/" + pName, LootContextParamSets.ADVANCEMENT_ENTITY);
                return new EntityPredicate.Composite(alootitemcondition);
            }
            else
            {
                EntityPredicate entitypredicate = EntityPredicate.fromJson(pElement);
                return wrap(entitypredicate);
            }
        }

        public static EntityPredicate.Composite wrap(EntityPredicate pEntityCondition)
        {
            if (pEntityCondition == EntityPredicate.ANY)
            {
                return ANY;
            }
            else
            {
                LootItemCondition lootitemcondition = LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, pEntityCondition).build();
                return new EntityPredicate.Composite(new LootItemCondition[] {lootitemcondition});
            }
        }

        public boolean matches(LootContext pContext)
        {
            return this.compositePredicates.test(pContext);
        }

        public JsonElement toJson(SerializationContext pSerializer)
        {
            return (JsonElement)(this.conditions.length == 0 ? JsonNull.INSTANCE : pSerializer.m_64772_(this.conditions));
        }

        public static JsonElement m_36687_(EntityPredicate.Composite[] p_36688_, SerializationContext p_36689_)
        {
            if (p_36688_.length == 0)
            {
                return JsonNull.INSTANCE;
            }
            else
            {
                JsonArray jsonarray = new JsonArray();

                for (EntityPredicate.Composite entitypredicate$composite : p_36688_)
                {
                    jsonarray.add(entitypredicate$composite.toJson(p_36689_));
                }

                return jsonarray;
            }
        }
    }
}
