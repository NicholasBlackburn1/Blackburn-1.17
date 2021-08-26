package net.minecraft.client.renderer.block.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import java.lang.reflect.Type;
import net.minecraft.util.GsonHelper;

public class ItemTransform
{
    public static final ItemTransform NO_TRANSFORM = new ItemTransform(new Vector3f(), new Vector3f(), new Vector3f(1.0F, 1.0F, 1.0F));
    public final Vector3f rotation;
    public final Vector3f translation;
    public final Vector3f scale;

    public ItemTransform(Vector3f p_111760_, Vector3f p_111761_, Vector3f p_111762_)
    {
        this.rotation = p_111760_.copy();
        this.translation = p_111761_.copy();
        this.scale = p_111762_.copy();
    }

    public void apply(boolean pLeftHand, PoseStack pMatrixStack)
    {
        if (this != NO_TRANSFORM)
        {
            float f = this.rotation.x();
            float f1 = this.rotation.y();
            float f2 = this.rotation.z();

            if (pLeftHand)
            {
                f1 = -f1;
                f2 = -f2;
            }

            int i = pLeftHand ? -1 : 1;
            pMatrixStack.translate((double)((float)i * this.translation.x()), (double)this.translation.y(), (double)this.translation.z());
            pMatrixStack.mulPose(new Quaternion(f, f1, f2, true));
            pMatrixStack.scale(this.scale.x(), this.scale.y(), this.scale.z());
        }
    }

    public boolean equals(Object p_111767_)
    {
        if (this == p_111767_)
        {
            return true;
        }
        else if (this.getClass() != p_111767_.getClass())
        {
            return false;
        }
        else
        {
            ItemTransform itemtransform = (ItemTransform)p_111767_;
            return this.rotation.equals(itemtransform.rotation) && this.scale.equals(itemtransform.scale) && this.translation.equals(itemtransform.translation);
        }
    }

    public int hashCode()
    {
        int i = this.rotation.hashCode();
        i = 31 * i + this.translation.hashCode();
        return 31 * i + this.scale.hashCode();
    }

    protected static class Deserializer implements JsonDeserializer<ItemTransform>
    {
        private static final Vector3f DEFAULT_ROTATION = new Vector3f(0.0F, 0.0F, 0.0F);
        private static final Vector3f DEFAULT_TRANSLATION = new Vector3f(0.0F, 0.0F, 0.0F);
        private static final Vector3f DEFAULT_SCALE = new Vector3f(1.0F, 1.0F, 1.0F);
        public static final float MAX_TRANSLATION = 5.0F;
        public static final float MAX_SCALE = 4.0F;

        public ItemTransform deserialize(JsonElement p_111775_, Type p_111776_, JsonDeserializationContext p_111777_) throws JsonParseException
        {
            JsonObject jsonobject = p_111775_.getAsJsonObject();
            Vector3f vector3f = this.getVector3f(jsonobject, "rotation", DEFAULT_ROTATION);
            Vector3f vector3f1 = this.getVector3f(jsonobject, "translation", DEFAULT_TRANSLATION);
            vector3f1.mul(0.0625F);
            vector3f1.clamp(-5.0F, 5.0F);
            Vector3f vector3f2 = this.getVector3f(jsonobject, "scale", DEFAULT_SCALE);
            vector3f2.clamp(-4.0F, 4.0F);
            return new ItemTransform(vector3f, vector3f1, vector3f2);
        }

        private Vector3f getVector3f(JsonObject pJson, String pKey, Vector3f pFallback)
        {
            if (!pJson.has(pKey))
            {
                return pFallback;
            }
            else
            {
                JsonArray jsonarray = GsonHelper.getAsJsonArray(pJson, pKey);

                if (jsonarray.size() != 3)
                {
                    throw new JsonParseException("Expected 3 " + pKey + " values, found: " + jsonarray.size());
                }
                else
                {
                    float[] afloat = new float[3];

                    for (int i = 0; i < afloat.length; ++i)
                    {
                        afloat[i] = GsonHelper.convertToFloat(jsonarray.get(i), pKey + "[" + i + "]");
                    }

                    return new Vector3f(afloat[0], afloat[1], afloat[2]);
                }
            }
        }
    }
}
