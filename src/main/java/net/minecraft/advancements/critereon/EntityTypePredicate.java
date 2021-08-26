package net.minecraft.advancements.critereon;

import com.google.common.base.Joiner;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.SerializationTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.EntityType;

public abstract class EntityTypePredicate
{
    public static final EntityTypePredicate ANY = new EntityTypePredicate()
    {
        public boolean matches(EntityType<?> pType)
        {
            return true;
        }
        public JsonElement serializeToJson()
        {
            return JsonNull.INSTANCE;
        }
    };
    private static final Joiner COMMA_JOINER = Joiner.on(", ");

    public abstract boolean matches(EntityType<?> pType);

    public abstract JsonElement serializeToJson();

    public static EntityTypePredicate fromJson(@Nullable JsonElement pElement)
    {
        if (pElement != null && !pElement.isJsonNull())
        {
            String s = GsonHelper.convertToString(pElement, "type");

            if (s.startsWith("#"))
            {
                ResourceLocation resourcelocation1 = new ResourceLocation(s.substring(1));
                return new EntityTypePredicate.TagPredicate(SerializationTags.getInstance().getTagOrThrow(Registry.ENTITY_TYPE_REGISTRY, resourcelocation1, (p_37646_) ->
                {
                    return new JsonSyntaxException("Unknown entity tag '" + p_37646_ + "'");
                }));
            }
            else
            {
                ResourceLocation resourcelocation = new ResourceLocation(s);
                EntityType<?> entitytype = Registry.ENTITY_TYPE.getOptional(resourcelocation).orElseThrow(() ->
                {
                    return new JsonSyntaxException("Unknown entity type '" + resourcelocation + "', valid types are: " + COMMA_JOINER.join(Registry.ENTITY_TYPE.keySet()));
                });
                return new EntityTypePredicate.TypePredicate(entitytype);
            }
        }
        else
        {
            return ANY;
        }
    }

    public static EntityTypePredicate of(EntityType<?> pTag)
    {
        return new EntityTypePredicate.TypePredicate(pTag);
    }

    public static EntityTypePredicate of(Tag < EntityType<? >> pTag)
    {
        return new EntityTypePredicate.TagPredicate(pTag);
    }

    static class TagPredicate extends EntityTypePredicate
    {
        private final Tag < EntityType<? >> tag;

        public TagPredicate(Tag < EntityType<? >> p_37655_)
        {
            this.tag = p_37655_;
        }

        public boolean matches(EntityType<?> pType)
        {
            return pType.is(this.tag);
        }

        public JsonElement serializeToJson()
        {
            return new JsonPrimitive("#" + SerializationTags.getInstance(). < EntityType<?>, IllegalStateException > getIdOrThrow(Registry.ENTITY_TYPE_REGISTRY, this.tag, () ->
            {
                return new IllegalStateException("Unknown entity type tag");
            }));
        }
    }

    static class TypePredicate extends EntityTypePredicate
    {
        private final EntityType<?> type;

        public TypePredicate(EntityType<?> p_37661_)
        {
            this.type = p_37661_;
        }

        public boolean matches(EntityType<?> pType)
        {
            return this.type == pType;
        }

        public JsonElement serializeToJson()
        {
            return new JsonPrimitive(Registry.ENTITY_TYPE.getKey(this.type).toString());
        }
    }
}
