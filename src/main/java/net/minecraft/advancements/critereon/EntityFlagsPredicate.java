package net.minecraft.advancements.critereon;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class EntityFlagsPredicate
{
    public static final EntityFlagsPredicate ANY = (new EntityFlagsPredicate.Builder()).build();
    @Nullable
    private final Boolean isOnFire;
    @Nullable
    private final Boolean isCrouching;
    @Nullable
    private final Boolean isSprinting;
    @Nullable
    private final Boolean isSwimming;
    @Nullable
    private final Boolean isBaby;

    public EntityFlagsPredicate(@Nullable Boolean p_33690_, @Nullable Boolean p_33691_, @Nullable Boolean p_33692_, @Nullable Boolean p_33693_, @Nullable Boolean p_33694_)
    {
        this.isOnFire = p_33690_;
        this.isCrouching = p_33691_;
        this.isSprinting = p_33692_;
        this.isSwimming = p_33693_;
        this.isBaby = p_33694_;
    }

    public boolean matches(Entity pEntity)
    {
        if (this.isOnFire != null && pEntity.isOnFire() != this.isOnFire)
        {
            return false;
        }
        else if (this.isCrouching != null && pEntity.isCrouching() != this.isCrouching)
        {
            return false;
        }
        else if (this.isSprinting != null && pEntity.isSprinting() != this.isSprinting)
        {
            return false;
        }
        else if (this.isSwimming != null && pEntity.isSwimming() != this.isSwimming)
        {
            return false;
        }
        else
        {
            return this.isBaby == null || !(pEntity instanceof LivingEntity) || ((LivingEntity)pEntity).isBaby() == this.isBaby;
        }
    }

    @Nullable
    private static Boolean getOptionalBoolean(JsonObject pJsonObject, String pName)
    {
        return pJsonObject.has(pName) ? GsonHelper.getAsBoolean(pJsonObject, pName) : null;
    }

    public static EntityFlagsPredicate fromJson(@Nullable JsonElement pElement)
    {
        if (pElement != null && !pElement.isJsonNull())
        {
            JsonObject jsonobject = GsonHelper.convertToJsonObject(pElement, "entity flags");
            Boolean obool = getOptionalBoolean(jsonobject, "is_on_fire");
            Boolean obool1 = getOptionalBoolean(jsonobject, "is_sneaking");
            Boolean obool2 = getOptionalBoolean(jsonobject, "is_sprinting");
            Boolean obool3 = getOptionalBoolean(jsonobject, "is_swimming");
            Boolean obool4 = getOptionalBoolean(jsonobject, "is_baby");
            return new EntityFlagsPredicate(obool, obool1, obool2, obool3, obool4);
        }
        else
        {
            return ANY;
        }
    }

    private void addOptionalBoolean(JsonObject pJsonObject, String pName, @Nullable Boolean pBool)
    {
        if (pBool != null)
        {
            pJsonObject.addProperty(pName, pBool);
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
            this.addOptionalBoolean(jsonobject, "is_on_fire", this.isOnFire);
            this.addOptionalBoolean(jsonobject, "is_sneaking", this.isCrouching);
            this.addOptionalBoolean(jsonobject, "is_sprinting", this.isSprinting);
            this.addOptionalBoolean(jsonobject, "is_swimming", this.isSwimming);
            this.addOptionalBoolean(jsonobject, "is_baby", this.isBaby);
            return jsonobject;
        }
    }

    public static class Builder
    {
        @Nullable
        private Boolean isOnFire;
        @Nullable
        private Boolean isCrouching;
        @Nullable
        private Boolean isSprinting;
        @Nullable
        private Boolean isSwimming;
        @Nullable
        private Boolean isBaby;

        public static EntityFlagsPredicate.Builder flags()
        {
            return new EntityFlagsPredicate.Builder();
        }

        public EntityFlagsPredicate.Builder setOnFire(@Nullable Boolean pOnFire)
        {
            this.isOnFire = pOnFire;
            return this;
        }

        public EntityFlagsPredicate.Builder setCrouching(@Nullable Boolean p_150058_)
        {
            this.isCrouching = p_150058_;
            return this;
        }

        public EntityFlagsPredicate.Builder setSprinting(@Nullable Boolean p_150060_)
        {
            this.isSprinting = p_150060_;
            return this;
        }

        public EntityFlagsPredicate.Builder setSwimming(@Nullable Boolean p_150062_)
        {
            this.isSwimming = p_150062_;
            return this;
        }

        public EntityFlagsPredicate.Builder setIsBaby(@Nullable Boolean pBaby)
        {
            this.isBaby = pBaby;
            return this;
        }

        public EntityFlagsPredicate build()
        {
            return new EntityFlagsPredicate(this.isOnFire, this.isCrouching, this.isSprinting, this.isSwimming, this.isBaby);
        }
    }
}
