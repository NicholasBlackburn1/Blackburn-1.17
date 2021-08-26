package net.minecraft.advancements.critereon;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.phys.Vec3;

public class DamageSourcePredicate
{
    public static final DamageSourcePredicate ANY = DamageSourcePredicate.Builder.damageType().build();
    private final Boolean isProjectile;
    private final Boolean isExplosion;
    private final Boolean bypassesArmor;
    private final Boolean bypassesInvulnerability;
    private final Boolean bypassesMagic;
    private final Boolean isFire;
    private final Boolean isMagic;
    private final Boolean isLightning;
    private final EntityPredicate directEntity;
    private final EntityPredicate sourceEntity;

    public DamageSourcePredicate(@Nullable Boolean p_25433_, @Nullable Boolean p_25434_, @Nullable Boolean p_25435_, @Nullable Boolean p_25436_, @Nullable Boolean p_25437_, @Nullable Boolean p_25438_, @Nullable Boolean p_25439_, @Nullable Boolean p_25440_, EntityPredicate p_25441_, EntityPredicate p_25442_)
    {
        this.isProjectile = p_25433_;
        this.isExplosion = p_25434_;
        this.bypassesArmor = p_25435_;
        this.bypassesInvulnerability = p_25436_;
        this.bypassesMagic = p_25437_;
        this.isFire = p_25438_;
        this.isMagic = p_25439_;
        this.isLightning = p_25440_;
        this.directEntity = p_25441_;
        this.sourceEntity = p_25442_;
    }

    public boolean matches(ServerPlayer pLevel, DamageSource pVector)
    {
        return this.matches(pLevel.getLevel(), pLevel.position(), pVector);
    }

    public boolean matches(ServerLevel pLevel, Vec3 pVector, DamageSource pSource)
    {
        if (this == ANY)
        {
            return true;
        }
        else if (this.isProjectile != null && this.isProjectile != pSource.isProjectile())
        {
            return false;
        }
        else if (this.isExplosion != null && this.isExplosion != pSource.isExplosion())
        {
            return false;
        }
        else if (this.bypassesArmor != null && this.bypassesArmor != pSource.isBypassArmor())
        {
            return false;
        }
        else if (this.bypassesInvulnerability != null && this.bypassesInvulnerability != pSource.isBypassInvul())
        {
            return false;
        }
        else if (this.bypassesMagic != null && this.bypassesMagic != pSource.isBypassMagic())
        {
            return false;
        }
        else if (this.isFire != null && this.isFire != pSource.isFire())
        {
            return false;
        }
        else if (this.isMagic != null && this.isMagic != pSource.isMagic())
        {
            return false;
        }
        else if (this.isLightning != null && this.isLightning != (pSource == DamageSource.LIGHTNING_BOLT))
        {
            return false;
        }
        else if (!this.directEntity.matches(pLevel, pVector, pSource.getDirectEntity()))
        {
            return false;
        }
        else
        {
            return this.sourceEntity.matches(pLevel, pVector, pSource.getEntity());
        }
    }

    public static DamageSourcePredicate fromJson(@Nullable JsonElement pElement)
    {
        if (pElement != null && !pElement.isJsonNull())
        {
            JsonObject jsonobject = GsonHelper.convertToJsonObject(pElement, "damage type");
            Boolean obool = getOptionalBoolean(jsonobject, "is_projectile");
            Boolean obool1 = getOptionalBoolean(jsonobject, "is_explosion");
            Boolean obool2 = getOptionalBoolean(jsonobject, "bypasses_armor");
            Boolean obool3 = getOptionalBoolean(jsonobject, "bypasses_invulnerability");
            Boolean obool4 = getOptionalBoolean(jsonobject, "bypasses_magic");
            Boolean obool5 = getOptionalBoolean(jsonobject, "is_fire");
            Boolean obool6 = getOptionalBoolean(jsonobject, "is_magic");
            Boolean obool7 = getOptionalBoolean(jsonobject, "is_lightning");
            EntityPredicate entitypredicate = EntityPredicate.fromJson(jsonobject.get("direct_entity"));
            EntityPredicate entitypredicate1 = EntityPredicate.fromJson(jsonobject.get("source_entity"));
            return new DamageSourcePredicate(obool, obool1, obool2, obool3, obool4, obool5, obool6, obool7, entitypredicate, entitypredicate1);
        }
        else
        {
            return ANY;
        }
    }

    @Nullable
    private static Boolean getOptionalBoolean(JsonObject pObject, String pMemberName)
    {
        return pObject.has(pMemberName) ? GsonHelper.getAsBoolean(pObject, pMemberName) : null;
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
            this.addOptionally(jsonobject, "is_projectile", this.isProjectile);
            this.addOptionally(jsonobject, "is_explosion", this.isExplosion);
            this.addOptionally(jsonobject, "bypasses_armor", this.bypassesArmor);
            this.addOptionally(jsonobject, "bypasses_invulnerability", this.bypassesInvulnerability);
            this.addOptionally(jsonobject, "bypasses_magic", this.bypassesMagic);
            this.addOptionally(jsonobject, "is_fire", this.isFire);
            this.addOptionally(jsonobject, "is_magic", this.isMagic);
            this.addOptionally(jsonobject, "is_lightning", this.isLightning);
            jsonobject.add("direct_entity", this.directEntity.serializeToJson());
            jsonobject.add("source_entity", this.sourceEntity.serializeToJson());
            return jsonobject;
        }
    }

    private void addOptionally(JsonObject pObj, String pKey, @Nullable Boolean pValue)
    {
        if (pValue != null)
        {
            pObj.addProperty(pKey, pValue);
        }
    }

    public static class Builder
    {
        private Boolean isProjectile;
        private Boolean isExplosion;
        private Boolean bypassesArmor;
        private Boolean bypassesInvulnerability;
        private Boolean bypassesMagic;
        private Boolean isFire;
        private Boolean isMagic;
        private Boolean isLightning;
        private EntityPredicate directEntity = EntityPredicate.ANY;
        private EntityPredicate sourceEntity = EntityPredicate.ANY;

        public static DamageSourcePredicate.Builder damageType()
        {
            return new DamageSourcePredicate.Builder();
        }

        public DamageSourcePredicate.Builder isProjectile(Boolean pIsProjectile)
        {
            this.isProjectile = pIsProjectile;
            return this;
        }

        public DamageSourcePredicate.Builder isExplosion(Boolean p_148236_)
        {
            this.isExplosion = p_148236_;
            return this;
        }

        public DamageSourcePredicate.Builder bypassesArmor(Boolean p_148238_)
        {
            this.bypassesArmor = p_148238_;
            return this;
        }

        public DamageSourcePredicate.Builder bypassesInvulnerability(Boolean p_148240_)
        {
            this.bypassesInvulnerability = p_148240_;
            return this;
        }

        public DamageSourcePredicate.Builder bypassesMagic(Boolean p_148242_)
        {
            this.bypassesMagic = p_148242_;
            return this;
        }

        public DamageSourcePredicate.Builder isFire(Boolean p_148244_)
        {
            this.isFire = p_148244_;
            return this;
        }

        public DamageSourcePredicate.Builder isMagic(Boolean p_148246_)
        {
            this.isMagic = p_148246_;
            return this;
        }

        public DamageSourcePredicate.Builder isLightning(Boolean pIsLightning)
        {
            this.isLightning = pIsLightning;
            return this;
        }

        public DamageSourcePredicate.Builder direct(EntityPredicate pDirectEntity)
        {
            this.directEntity = pDirectEntity;
            return this;
        }

        public DamageSourcePredicate.Builder direct(EntityPredicate.Builder pDirectEntity)
        {
            this.directEntity = pDirectEntity.build();
            return this;
        }

        public DamageSourcePredicate.Builder source(EntityPredicate p_148234_)
        {
            this.sourceEntity = p_148234_;
            return this;
        }

        public DamageSourcePredicate.Builder source(EntityPredicate.Builder p_148232_)
        {
            this.sourceEntity = p_148232_.build();
            return this;
        }

        public DamageSourcePredicate build()
        {
            return new DamageSourcePredicate(this.isProjectile, this.isExplosion, this.bypassesArmor, this.bypassesInvulnerability, this.bypassesMagic, this.isFire, this.isMagic, this.isLightning, this.directEntity, this.sourceEntity);
        }
    }
}
