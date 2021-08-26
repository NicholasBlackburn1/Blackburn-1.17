package net.minecraft.resources;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.lang.reflect.Type;
import javax.annotation.Nullable;
import net.minecraft.ResourceLocationException;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.GsonHelper;
import org.apache.commons.lang3.StringUtils;

public class ResourceLocation implements Comparable<ResourceLocation>
{
    public static final Codec<ResourceLocation> CODEC = Codec.STRING.comapFlatMap(ResourceLocation::read, ResourceLocation::toString).stable();
    private static final SimpleCommandExceptionType ERROR_INVALID = new SimpleCommandExceptionType(new TranslatableComponent("argument.id.invalid"));
    public static final char NAMESPACE_SEPARATOR = ':';
    public static final String DEFAULT_NAMESPACE = "minecraft";
    public static final String REALMS_NAMESPACE = "realms";
    protected final String namespace;
    protected final String path;

    protected ResourceLocation(String[] p_135814_)
    {
        this.namespace = StringUtils.isEmpty(p_135814_[0]) ? "minecraft" : p_135814_[0];
        this.path = p_135814_[1];

        if (this.path.equals("DUMMY"))
        {
            if (!isValidNamespace(this.namespace))
            {
                throw new ResourceLocationException("Non [a-z0-9_.-] character in namespace of location: " + this.namespace + ":" + this.path);
            }
            else if (!isValidPath(this.path))
            {
                throw new ResourceLocationException("Non [a-z0-9/._-] character in path of location: " + this.namespace + ":" + this.path);
            }
        }
    }

    public ResourceLocation(String p_135809_)
    {
        this(decompose(p_135809_, ':'));
    }

    public ResourceLocation(String p_135811_, String p_135812_)
    {
        this(new String[] {p_135811_, p_135812_});
    }

    public static ResourceLocation of(String pResourceName, char pSplitOn)
    {
        return new ResourceLocation(decompose(pResourceName, pSplitOn));
    }

    @Nullable
    public static ResourceLocation tryParse(String pString)
    {
        try
        {
            return new ResourceLocation(pString);
        }
        catch (ResourceLocationException resourcelocationexception)
        {
            return null;
        }
    }

    protected static String[] decompose(String pResourceName, char pSplitOn)
    {
        String[] astring = new String[] {"minecraft", pResourceName};
        int i = pResourceName.indexOf(pSplitOn);

        if (i >= 0)
        {
            astring[1] = pResourceName.substring(i + 1, pResourceName.length());

            if (i >= 1)
            {
                astring[0] = pResourceName.substring(0, i);
            }
        }

        return astring;
    }

    private static DataResult<ResourceLocation> read(String pReader)
    {
        try
        {
            return DataResult.success(new ResourceLocation(pReader));
        }
        catch (ResourceLocationException resourcelocationexception)
        {
            return DataResult.error("Not a valid resource location: " + pReader + " " + resourcelocationexception.getMessage());
        }
    }

    public String getPath()
    {
        return this.path;
    }

    public String getNamespace()
    {
        return this.namespace;
    }

    public String toString()
    {
        return this.namespace + ":" + this.path;
    }

    public boolean equals(Object p_135846_)
    {
        if (this == p_135846_)
        {
            return true;
        }
        else if (!(p_135846_ instanceof ResourceLocation))
        {
            return false;
        }
        else
        {
            ResourceLocation resourcelocation = (ResourceLocation)p_135846_;
            return this.namespace.equals(resourcelocation.namespace) && this.path.equals(resourcelocation.path);
        }
    }

    public int hashCode()
    {
        return 31 * this.namespace.hashCode() + this.path.hashCode();
    }

    public int compareTo(ResourceLocation p_135826_)
    {
        int i = this.path.compareTo(p_135826_.path);

        if (i == 0)
        {
            i = this.namespace.compareTo(p_135826_.namespace);
        }

        return i;
    }

    public String toDebugFileName()
    {
        return this.toString().replace('/', '_').replace(':', '_');
    }

    public static ResourceLocation read(StringReader pReader) throws CommandSyntaxException
    {
        int i = pReader.getCursor();

        while (pReader.canRead() && isAllowedInResourceLocation(pReader.peek()))
        {
            pReader.skip();
        }

        String s = pReader.getString().substring(i, pReader.getCursor());

        try
        {
            return new ResourceLocation(s);
        }
        catch (ResourceLocationException resourcelocationexception)
        {
            pReader.setCursor(i);
            throw ERROR_INVALID.createWithContext(pReader);
        }
    }

    public static boolean isAllowedInResourceLocation(char pCharValue)
    {
        return pCharValue >= '0' && pCharValue <= '9' || pCharValue >= 'a' && pCharValue <= 'z' || pCharValue == '_' || pCharValue == ':' || pCharValue == '/' || pCharValue == '.' || pCharValue == '-';
    }

    private static boolean isValidPath(String pPath)
    {
        for (int i = 0; i < pPath.length(); ++i)
        {
            if (!validPathChar(pPath.charAt(i)))
            {
                return false;
            }
        }

        return true;
    }

    private static boolean isValidNamespace(String pNamespace)
    {
        for (int i = 0; i < pNamespace.length(); ++i)
        {
            if (!validNamespaceChar(pNamespace.charAt(i)))
            {
                return false;
            }
        }

        return true;
    }

    public static boolean validPathChar(char pCharValue)
    {
        return pCharValue == '_' || pCharValue == '-' || pCharValue >= 'a' && pCharValue <= 'z' || pCharValue >= '0' && pCharValue <= '9' || pCharValue == '/' || pCharValue == '.';
    }

    private static boolean validNamespaceChar(char pCharValue)
    {
        return pCharValue == '_' || pCharValue == '-' || pCharValue >= 'a' && pCharValue <= 'z' || pCharValue >= '0' && pCharValue <= '9' || pCharValue == '.';
    }

    public static boolean isValidResourceLocation(String pResourceName)
    {
        String[] astring = decompose(pResourceName, ':');
        return isValidNamespace(StringUtils.isEmpty(astring[0]) ? "minecraft" : astring[0]) && isValidPath(astring[1]);
    }

    public int compareNamespaced(ResourceLocation o)
    {
        int i = this.namespace.compareTo(o.namespace);
        return i != 0 ? i : this.path.compareTo(o.path);
    }

    public static class Serializer implements JsonDeserializer<ResourceLocation>, JsonSerializer<ResourceLocation>
    {
        public ResourceLocation deserialize(JsonElement p_135851_, Type p_135852_, JsonDeserializationContext p_135853_) throws JsonParseException
        {
            return new ResourceLocation(GsonHelper.convertToString(p_135851_, "location"));
        }

        public JsonElement serialize(ResourceLocation p_135855_, Type p_135856_, JsonSerializationContext p_135857_)
        {
            return new JsonPrimitive(p_135855_.toString());
        }
    }
}
