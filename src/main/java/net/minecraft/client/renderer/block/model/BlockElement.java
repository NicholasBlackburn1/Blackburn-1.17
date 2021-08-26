package net.minecraft.client.renderer.block.model;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.math.Vector3f;
import java.lang.reflect.Type;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;

public class BlockElement
{
    private static final boolean DEFAULT_RESCALE = false;
    private static final float MIN_EXTENT = -16.0F;
    private static final float MAX_EXTENT = 32.0F;
    public final Vector3f from;
    public final Vector3f to;
    public final Map<Direction, BlockElementFace> faces;
    public final BlockElementRotation rotation;
    public final boolean shade;

    public BlockElement(Vector3f p_111314_, Vector3f p_111315_, Map<Direction, BlockElementFace> p_111316_, @Nullable BlockElementRotation p_111317_, boolean p_111318_)
    {
        this.from = p_111314_;
        this.to = p_111315_;
        this.faces = p_111316_;
        this.rotation = p_111317_;
        this.shade = p_111318_;
        this.fillUvs();
    }

    private void fillUvs()
    {
        for (Entry<Direction, BlockElementFace> entry : this.faces.entrySet())
        {
            float[] afloat = this.uvsByFace(entry.getKey());
            (entry.getValue()).uv.m_111394_(afloat);
        }
    }

    private float[] uvsByFace(Direction pFacing)
    {
        switch (pFacing)
        {
            case DOWN:
                return new float[] {this.from.x(), 16.0F - this.to.z(), this.to.x(), 16.0F - this.from.z()};
            case UP:
                return new float[] {this.from.x(), this.from.z(), this.to.x(), this.to.z()};
            case NORTH:
            default:
                return new float[] {16.0F - this.to.x(), 16.0F - this.to.y(), 16.0F - this.from.x(), 16.0F - this.from.y()};
            case SOUTH:
                return new float[] {this.from.x(), 16.0F - this.to.y(), this.to.x(), 16.0F - this.from.y()};
            case WEST:
                return new float[] {this.from.z(), 16.0F - this.to.y(), this.to.z(), 16.0F - this.from.y()};
            case EAST:
                return new float[] {16.0F - this.to.z(), 16.0F - this.to.y(), 16.0F - this.from.z(), 16.0F - this.from.y()};
        }
    }

    protected static class Deserializer implements JsonDeserializer<BlockElement>
    {
        private static final boolean DEFAULT_SHADE = true;

        public BlockElement deserialize(JsonElement p_111329_, Type p_111330_, JsonDeserializationContext p_111331_) throws JsonParseException
        {
            JsonObject jsonobject = p_111329_.getAsJsonObject();
            Vector3f vector3f = this.getFrom(jsonobject);
            Vector3f vector3f1 = this.getTo(jsonobject);
            BlockElementRotation blockelementrotation = this.getRotation(jsonobject);
            Map<Direction, BlockElementFace> map = this.getFaces(p_111331_, jsonobject);

            if (jsonobject.has("shade") && !GsonHelper.isBooleanValue(jsonobject, "shade"))
            {
                throw new JsonParseException("Expected shade to be a Boolean");
            }
            else
            {
                boolean flag = GsonHelper.getAsBoolean(jsonobject, "shade", true);
                return new BlockElement(vector3f, vector3f1, map, blockelementrotation, flag);
            }
        }

        @Nullable
        private BlockElementRotation getRotation(JsonObject pObject)
        {
            BlockElementRotation blockelementrotation = null;

            if (pObject.has("rotation"))
            {
                JsonObject jsonobject = GsonHelper.getAsJsonObject(pObject, "rotation");
                Vector3f vector3f = this.getVector3f(jsonobject, "origin");
                vector3f.mul(0.0625F);
                Direction.Axis direction$axis = this.getAxis(jsonobject);
                float f = this.getAngle(jsonobject);
                boolean flag = GsonHelper.getAsBoolean(jsonobject, "rescale", false);
                blockelementrotation = new BlockElementRotation(vector3f, direction$axis, f, flag);
            }

            return blockelementrotation;
        }

        private float getAngle(JsonObject pObject)
        {
            float f = GsonHelper.getAsFloat(pObject, "angle");

            if (f != 0.0F && Mth.abs(f) != 22.5F && Mth.abs(f) != 45.0F)
            {
                throw new JsonParseException("Invalid rotation " + f + " found, only -45/-22.5/0/22.5/45 allowed");
            }
            else
            {
                return f;
            }
        }

        private Direction.Axis getAxis(JsonObject pObject)
        {
            String s = GsonHelper.getAsString(pObject, "axis");
            Direction.Axis direction$axis = Direction.Axis.byName(s.toLowerCase(Locale.ROOT));

            if (direction$axis == null)
            {
                throw new JsonParseException("Invalid rotation axis: " + s);
            }
            else
            {
                return direction$axis;
            }
        }

        private Map<Direction, BlockElementFace> getFaces(JsonDeserializationContext pDeserializationContext, JsonObject pObject)
        {
            Map<Direction, BlockElementFace> map = this.filterNullFromFaces(pDeserializationContext, pObject);

            if (map.isEmpty())
            {
                throw new JsonParseException("Expected between 1 and 6 unique faces, got 0");
            }
            else
            {
                return map;
            }
        }

        private Map<Direction, BlockElementFace> filterNullFromFaces(JsonDeserializationContext pDeserializationContext, JsonObject pObject)
        {
            Map<Direction, BlockElementFace> map = Maps.newEnumMap(Direction.class);
            JsonObject jsonobject = GsonHelper.getAsJsonObject(pObject, "faces");

            for (Entry<String, JsonElement> entry : jsonobject.entrySet())
            {
                Direction direction = this.getFacing(entry.getKey());
                map.put(direction, pDeserializationContext.deserialize(entry.getValue(), BlockElementFace.class));
            }

            return map;
        }

        private Direction getFacing(String pName)
        {
            Direction direction = Direction.byName(pName);

            if (direction == null)
            {
                throw new JsonParseException("Unknown facing: " + pName);
            }
            else
            {
                return direction;
            }
        }

        private Vector3f getTo(JsonObject pJson)
        {
            Vector3f vector3f = this.getVector3f(pJson, "to");

            if (!(vector3f.x() < -16.0F) && !(vector3f.y() < -16.0F) && !(vector3f.z() < -16.0F) && !(vector3f.x() > 32.0F) && !(vector3f.y() > 32.0F) && !(vector3f.z() > 32.0F))
            {
                return vector3f;
            }
            else
            {
                throw new JsonParseException("'to' specifier exceeds the allowed boundaries: " + vector3f);
            }
        }

        private Vector3f getFrom(JsonObject pJson)
        {
            Vector3f vector3f = this.getVector3f(pJson, "from");

            if (!(vector3f.x() < -16.0F) && !(vector3f.y() < -16.0F) && !(vector3f.z() < -16.0F) && !(vector3f.x() > 32.0F) && !(vector3f.y() > 32.0F) && !(vector3f.z() > 32.0F))
            {
                return vector3f;
            }
            else
            {
                throw new JsonParseException("'from' specifier exceeds the allowed boundaries: " + vector3f);
            }
        }

        private Vector3f getVector3f(JsonObject pJson, String pName)
        {
            JsonArray jsonarray = GsonHelper.getAsJsonArray(pJson, pName);

            if (jsonarray.size() != 3)
            {
                throw new JsonParseException("Expected 3 " + pName + " values, found: " + jsonarray.size());
            }
            else
            {
                float[] afloat = new float[3];

                for (int i = 0; i < afloat.length; ++i)
                {
                    afloat[i] = GsonHelper.convertToFloat(jsonarray.get(i), pName + "[" + i + "]");
                }

                return new Vector3f(afloat[0], afloat[1], afloat[2]);
            }
        }
    }
}
