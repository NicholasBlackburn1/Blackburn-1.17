package net.minecraft.client.renderer.block.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;

public class ItemTransforms
{
    public static final ItemTransforms NO_TRANSFORMS = new ItemTransforms();
    public final ItemTransform thirdPersonLeftHand;
    public final ItemTransform thirdPersonRightHand;
    public final ItemTransform firstPersonLeftHand;
    public final ItemTransform firstPersonRightHand;
    public final ItemTransform head;
    public final ItemTransform gui;
    public final ItemTransform ground;
    public final ItemTransform fixed;

    private ItemTransforms()
    {
        this(ItemTransform.NO_TRANSFORM, ItemTransform.NO_TRANSFORM, ItemTransform.NO_TRANSFORM, ItemTransform.NO_TRANSFORM, ItemTransform.NO_TRANSFORM, ItemTransform.NO_TRANSFORM, ItemTransform.NO_TRANSFORM, ItemTransform.NO_TRANSFORM);
    }

    public ItemTransforms(ItemTransforms p_111807_)
    {
        this.thirdPersonLeftHand = p_111807_.thirdPersonLeftHand;
        this.thirdPersonRightHand = p_111807_.thirdPersonRightHand;
        this.firstPersonLeftHand = p_111807_.firstPersonLeftHand;
        this.firstPersonRightHand = p_111807_.firstPersonRightHand;
        this.head = p_111807_.head;
        this.gui = p_111807_.gui;
        this.ground = p_111807_.ground;
        this.fixed = p_111807_.fixed;
    }

    public ItemTransforms(ItemTransform p_111798_, ItemTransform p_111799_, ItemTransform p_111800_, ItemTransform p_111801_, ItemTransform p_111802_, ItemTransform p_111803_, ItemTransform p_111804_, ItemTransform p_111805_)
    {
        this.thirdPersonLeftHand = p_111798_;
        this.thirdPersonRightHand = p_111799_;
        this.firstPersonLeftHand = p_111800_;
        this.firstPersonRightHand = p_111801_;
        this.head = p_111802_;
        this.gui = p_111803_;
        this.ground = p_111804_;
        this.fixed = p_111805_;
    }

    public ItemTransform getTransform(ItemTransforms.TransformType pType)
    {
        switch (pType)
        {
            case THIRD_PERSON_LEFT_HAND:
                return this.thirdPersonLeftHand;

            case THIRD_PERSON_RIGHT_HAND:
                return this.thirdPersonRightHand;

            case FIRST_PERSON_LEFT_HAND:
                return this.firstPersonLeftHand;

            case FIRST_PERSON_RIGHT_HAND:
                return this.firstPersonRightHand;

            case HEAD:
                return this.head;

            case GUI:
                return this.gui;

            case GROUND:
                return this.ground;

            case FIXED:
                return this.fixed;

            default:
                return ItemTransform.NO_TRANSFORM;
        }
    }

    public boolean hasTransform(ItemTransforms.TransformType pType)
    {
        return this.getTransform(pType) != ItemTransform.NO_TRANSFORM;
    }

    protected static class Deserializer implements JsonDeserializer<ItemTransforms>
    {
        public ItemTransforms deserialize(JsonElement p_111820_, Type p_111821_, JsonDeserializationContext p_111822_) throws JsonParseException
        {
            JsonObject jsonobject = p_111820_.getAsJsonObject();
            ItemTransform itemtransform = this.getTransform(p_111822_, jsonobject, "thirdperson_righthand");
            ItemTransform itemtransform1 = this.getTransform(p_111822_, jsonobject, "thirdperson_lefthand");

            if (itemtransform1 == ItemTransform.NO_TRANSFORM)
            {
                itemtransform1 = itemtransform;
            }

            ItemTransform itemtransform2 = this.getTransform(p_111822_, jsonobject, "firstperson_righthand");
            ItemTransform itemtransform3 = this.getTransform(p_111822_, jsonobject, "firstperson_lefthand");

            if (itemtransform3 == ItemTransform.NO_TRANSFORM)
            {
                itemtransform3 = itemtransform2;
            }

            ItemTransform itemtransform4 = this.getTransform(p_111822_, jsonobject, "head");
            ItemTransform itemtransform5 = this.getTransform(p_111822_, jsonobject, "gui");
            ItemTransform itemtransform6 = this.getTransform(p_111822_, jsonobject, "ground");
            ItemTransform itemtransform7 = this.getTransform(p_111822_, jsonobject, "fixed");
            return new ItemTransforms(itemtransform1, itemtransform, itemtransform3, itemtransform2, itemtransform4, itemtransform5, itemtransform6, itemtransform7);
        }

        private ItemTransform getTransform(JsonDeserializationContext pContext, JsonObject pJson, String pName)
        {
            return pJson.has(pName) ? pContext.deserialize(pJson.get(pName), ItemTransform.class) : ItemTransform.NO_TRANSFORM;
        }
    }

    public static enum TransformType
    {
        NONE,
        THIRD_PERSON_LEFT_HAND,
        THIRD_PERSON_RIGHT_HAND,
        FIRST_PERSON_LEFT_HAND,
        FIRST_PERSON_RIGHT_HAND,
        HEAD,
        GUI,
        GROUND,
        FIXED;

        public boolean firstPerson()
        {
            return this == FIRST_PERSON_LEFT_HAND || this == FIRST_PERSON_RIGHT_HAND;
        }
    }
}
