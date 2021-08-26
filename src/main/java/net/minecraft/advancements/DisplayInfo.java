package net.minecraft.advancements;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class DisplayInfo
{
    private final Component title;
    private final Component description;
    private final ItemStack icon;
    private final ResourceLocation background;
    private final FrameType frame;
    private final boolean showToast;
    private final boolean announceChat;
    private final boolean hidden;
    private float x;
    private float y;

    public DisplayInfo(ItemStack p_14969_, Component p_14970_, Component p_14971_, @Nullable ResourceLocation p_14972_, FrameType p_14973_, boolean p_14974_, boolean p_14975_, boolean p_14976_)
    {
        this.title = p_14970_;
        this.description = p_14971_;
        this.icon = p_14969_;
        this.background = p_14972_;
        this.frame = p_14973_;
        this.showToast = p_14974_;
        this.announceChat = p_14975_;
        this.hidden = p_14976_;
    }

    public void setLocation(float pX, float pY)
    {
        this.x = pX;
        this.y = pY;
    }

    public Component getTitle()
    {
        return this.title;
    }

    public Component getDescription()
    {
        return this.description;
    }

    public ItemStack getIcon()
    {
        return this.icon;
    }

    @Nullable
    public ResourceLocation getBackground()
    {
        return this.background;
    }

    public FrameType getFrame()
    {
        return this.frame;
    }

    public float getX()
    {
        return this.x;
    }

    public float getY()
    {
        return this.y;
    }

    public boolean shouldShowToast()
    {
        return this.showToast;
    }

    public boolean shouldAnnounceChat()
    {
        return this.announceChat;
    }

    public boolean isHidden()
    {
        return this.hidden;
    }

    public static DisplayInfo fromJson(JsonObject pObject)
    {
        Component component = Component.Serializer.fromJson(pObject.get("title"));
        Component component1 = Component.Serializer.fromJson(pObject.get("description"));

        if (component != null && component1 != null)
        {
            ItemStack itemstack = getIcon(GsonHelper.getAsJsonObject(pObject, "icon"));
            ResourceLocation resourcelocation = pObject.has("background") ? new ResourceLocation(GsonHelper.getAsString(pObject, "background")) : null;
            FrameType frametype = pObject.has("frame") ? FrameType.byName(GsonHelper.getAsString(pObject, "frame")) : FrameType.TASK;
            boolean flag = GsonHelper.getAsBoolean(pObject, "show_toast", true);
            boolean flag1 = GsonHelper.getAsBoolean(pObject, "announce_to_chat", true);
            boolean flag2 = GsonHelper.getAsBoolean(pObject, "hidden", false);
            return new DisplayInfo(itemstack, component, component1, resourcelocation, frametype, flag, flag1, flag2);
        }
        else
        {
            throw new JsonSyntaxException("Both title and description must be set");
        }
    }

    private static ItemStack getIcon(JsonObject pObject)
    {
        if (!pObject.has("item"))
        {
            throw new JsonSyntaxException("Unsupported icon type, currently only items are supported (add 'item' key)");
        }
        else
        {
            Item item = GsonHelper.getAsItem(pObject, "item");

            if (pObject.has("data"))
            {
                throw new JsonParseException("Disallowed data tag found");
            }
            else
            {
                ItemStack itemstack = new ItemStack(item);

                if (pObject.has("nbt"))
                {
                    try
                    {
                        CompoundTag compoundtag = TagParser.parseTag(GsonHelper.convertToString(pObject.get("nbt"), "nbt"));
                        itemstack.setTag(compoundtag);
                    }
                    catch (CommandSyntaxException commandsyntaxexception)
                    {
                        throw new JsonSyntaxException("Invalid nbt tag: " + commandsyntaxexception.getMessage());
                    }
                }

                return itemstack;
            }
        }
    }

    public void serializeToNetwork(FriendlyByteBuf pBuf)
    {
        pBuf.writeComponent(this.title);
        pBuf.writeComponent(this.description);
        pBuf.writeItem(this.icon);
        pBuf.writeEnum(this.frame);
        int i = 0;

        if (this.background != null)
        {
            i |= 1;
        }

        if (this.showToast)
        {
            i |= 2;
        }

        if (this.hidden)
        {
            i |= 4;
        }

        pBuf.writeInt(i);

        if (this.background != null)
        {
            pBuf.writeResourceLocation(this.background);
        }

        pBuf.writeFloat(this.x);
        pBuf.writeFloat(this.y);
    }

    public static DisplayInfo fromNetwork(FriendlyByteBuf pBuf)
    {
        Component component = pBuf.readComponent();
        Component component1 = pBuf.readComponent();
        ItemStack itemstack = pBuf.readItem();
        FrameType frametype = pBuf.readEnum(FrameType.class);
        int i = pBuf.readInt();
        ResourceLocation resourcelocation = (i & 1) != 0 ? pBuf.readResourceLocation() : null;
        boolean flag = (i & 2) != 0;
        boolean flag1 = (i & 4) != 0;
        DisplayInfo displayinfo = new DisplayInfo(itemstack, component, component1, resourcelocation, frametype, flag, false, flag1);
        displayinfo.setLocation(pBuf.readFloat(), pBuf.readFloat());
        return displayinfo;
    }

    public JsonElement serializeToJson()
    {
        JsonObject jsonobject = new JsonObject();
        jsonobject.add("icon", this.serializeIcon());
        jsonobject.add("title", Component.Serializer.toJsonTree(this.title));
        jsonobject.add("description", Component.Serializer.toJsonTree(this.description));
        jsonobject.addProperty("frame", this.frame.getName());
        jsonobject.addProperty("show_toast", this.showToast);
        jsonobject.addProperty("announce_to_chat", this.announceChat);
        jsonobject.addProperty("hidden", this.hidden);

        if (this.background != null)
        {
            jsonobject.addProperty("background", this.background.toString());
        }

        return jsonobject;
    }

    private JsonObject serializeIcon()
    {
        JsonObject jsonobject = new JsonObject();
        jsonobject.addProperty("item", Registry.ITEM.getKey(this.icon.getItem()).toString());

        if (this.icon.hasTag())
        {
            jsonobject.addProperty("nbt", this.icon.getTag().toString());
        }

        return jsonobject;
    }
}
