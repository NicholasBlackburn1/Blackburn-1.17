package net.minecraft.advancements;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.apache.commons.lang3.ArrayUtils;

public class Advancement
{
    private final Advancement parent;
    private final DisplayInfo display;
    private final AdvancementRewards rewards;
    private final ResourceLocation id;
    private final Map<String, Criterion> criteria;
    private final String[][] requirements;
    private final Set<Advancement> children = Sets.newLinkedHashSet();
    private final Component chatComponent;

    public Advancement(ResourceLocation p_138307_, @Nullable Advancement p_138308_, @Nullable DisplayInfo p_138309_, AdvancementRewards p_138310_, Map<String, Criterion> p_138311_, String[][] p_138312_)
    {
        this.id = p_138307_;
        this.display = p_138309_;
        this.criteria = ImmutableMap.copyOf(p_138311_);
        this.parent = p_138308_;
        this.rewards = p_138310_;
        this.requirements = p_138312_;

        if (p_138308_ != null)
        {
            p_138308_.addChild(this);
        }

        if (p_138309_ == null)
        {
            this.chatComponent = new TextComponent(p_138307_.toString());
        }
        else
        {
            Component component = p_138309_.getTitle();
            ChatFormatting chatformatting = p_138309_.getFrame().getChatColor();
            Component component1 = ComponentUtils.mergeStyles(component.copy(), Style.EMPTY.withColor(chatformatting)).append("\n").append(p_138309_.getDescription());
            Component component2 = component.copy().withStyle((p_138316_) ->
            {
                return p_138316_.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, component1));
            });
            this.chatComponent = ComponentUtils.wrapInSquareBrackets(component2).withStyle(chatformatting);
        }
    }

    public Advancement.Builder deconstruct()
    {
        return new Advancement.Builder(this.parent == null ? null : this.parent.getId(), this.display, this.rewards, this.criteria, this.requirements);
    }

    @Nullable
    public Advancement getParent()
    {
        return this.parent;
    }

    @Nullable
    public DisplayInfo getDisplay()
    {
        return this.display;
    }

    public AdvancementRewards getRewards()
    {
        return this.rewards;
    }

    public String toString()
    {
        return "SimpleAdvancement{id=" + this.getId() + ", parent=" + (this.parent == null ? "null" : this.parent.getId()) + ", display=" + this.display + ", rewards=" + this.rewards + ", criteria=" + this.criteria + ", requirements=" + Arrays.deepToString(this.requirements) + "}";
    }

    public Iterable<Advancement> getChildren()
    {
        return this.children;
    }

    public Map<String, Criterion> getCriteria()
    {
        return this.criteria;
    }

    public int getMaxCriteraRequired()
    {
        return this.requirements.length;
    }

    public void addChild(Advancement pAdvancement)
    {
        this.children.add(pAdvancement);
    }

    public ResourceLocation getId()
    {
        return this.id;
    }

    public boolean equals(Object p_138324_)
    {
        if (this == p_138324_)
        {
            return true;
        }
        else if (!(p_138324_ instanceof Advancement))
        {
            return false;
        }
        else
        {
            Advancement advancement = (Advancement)p_138324_;
            return this.id.equals(advancement.id);
        }
    }

    public int hashCode()
    {
        return this.id.hashCode();
    }

    public String[][] getRequirements()
    {
        return this.requirements;
    }

    public Component getChatComponent()
    {
        return this.chatComponent;
    }

    public static class Builder
    {
        private ResourceLocation parentId;
        private Advancement parent;
        private DisplayInfo display;
        private AdvancementRewards rewards = AdvancementRewards.EMPTY;
        private Map<String, Criterion> criteria = Maps.newLinkedHashMap();
        private String[][] requirements;
        private RequirementsStrategy requirementsStrategy = RequirementsStrategy.AND;

        Builder(@Nullable ResourceLocation p_138341_, @Nullable DisplayInfo p_138342_, AdvancementRewards p_138343_, Map<String, Criterion> p_138344_, String[][] p_138345_)
        {
            this.parentId = p_138341_;
            this.display = p_138342_;
            this.rewards = p_138343_;
            this.criteria = p_138344_;
            this.requirements = p_138345_;
        }

        private Builder()
        {
        }

        public static Advancement.Builder advancement()
        {
            return new Advancement.Builder();
        }

        public Advancement.Builder parent(Advancement pParent)
        {
            this.parent = pParent;
            return this;
        }

        public Advancement.Builder parent(ResourceLocation pParent)
        {
            this.parentId = pParent;
            return this;
        }

        public Advancement.Builder display(ItemStack pDisplay, Component p_138364_, Component p_138365_, @Nullable ResourceLocation p_138366_, FrameType p_138367_, boolean p_138368_, boolean p_138369_, boolean p_138370_)
        {
            return this.display(new DisplayInfo(pDisplay, p_138364_, p_138365_, p_138366_, p_138367_, p_138368_, p_138369_, p_138370_));
        }

        public Advancement.Builder display(ItemLike pDisplay, Component p_138373_, Component p_138374_, @Nullable ResourceLocation p_138375_, FrameType p_138376_, boolean p_138377_, boolean p_138378_, boolean p_138379_)
        {
            return this.display(new DisplayInfo(new ItemStack(pDisplay.asItem()), p_138373_, p_138374_, p_138375_, p_138376_, p_138377_, p_138378_, p_138379_));
        }

        public Advancement.Builder display(DisplayInfo pDisplay)
        {
            this.display = pDisplay;
            return this;
        }

        public Advancement.Builder rewards(AdvancementRewards.Builder pRewardsBuilder)
        {
            return this.rewards(pRewardsBuilder.build());
        }

        public Advancement.Builder rewards(AdvancementRewards pRewardsBuilder)
        {
            this.rewards = pRewardsBuilder;
            return this;
        }

        public Advancement.Builder addCriterion(String pKey, CriterionTriggerInstance pCriterion)
        {
            return this.addCriterion(pKey, new Criterion(pCriterion));
        }

        public Advancement.Builder addCriterion(String pKey, Criterion pCriterion)
        {
            if (this.criteria.containsKey(pKey))
            {
                throw new IllegalArgumentException("Duplicate criterion " + pKey);
            }
            else
            {
                this.criteria.put(pKey, pCriterion);
                return this;
            }
        }

        public Advancement.Builder requirements(RequirementsStrategy pStrategy)
        {
            this.requirementsStrategy = pStrategy;
            return this;
        }

        public Advancement.Builder m_143951_(String[][] p_143952_)
        {
            this.requirements = p_143952_;
            return this;
        }

        public boolean canBuild(Function<ResourceLocation, Advancement> pLookup)
        {
            if (this.parentId == null)
            {
                return true;
            }
            else
            {
                if (this.parent == null)
                {
                    this.parent = pLookup.apply(this.parentId);
                }

                return this.parent != null;
            }
        }

        public Advancement build(ResourceLocation pId)
        {
            if (!this.canBuild((p_138407_) ->
        {
            return null;
        }))
            {
                throw new IllegalStateException("Tried to build incomplete advancement!");
            }
            else
            {
                if (this.requirements == null)
                {
                    this.requirements = this.requirementsStrategy.createRequirements(this.criteria.keySet());
                }

                return new Advancement(pId, this.parent, this.display, this.rewards, this.criteria, this.requirements);
            }
        }

        public Advancement save(Consumer<Advancement> pConsumer, String pId)
        {
            Advancement advancement = this.build(new ResourceLocation(pId));
            pConsumer.accept(advancement);
            return advancement;
        }

        public JsonObject serializeToJson()
        {
            if (this.requirements == null)
            {
                this.requirements = this.requirementsStrategy.createRequirements(this.criteria.keySet());
            }

            JsonObject jsonobject = new JsonObject();

            if (this.parent != null)
            {
                jsonobject.addProperty("parent", this.parent.getId().toString());
            }
            else if (this.parentId != null)
            {
                jsonobject.addProperty("parent", this.parentId.toString());
            }

            if (this.display != null)
            {
                jsonobject.add("display", this.display.serializeToJson());
            }

            jsonobject.add("rewards", this.rewards.serializeToJson());
            JsonObject jsonobject1 = new JsonObject();

            for (Entry<String, Criterion> entry : this.criteria.entrySet())
            {
                jsonobject1.add(entry.getKey(), entry.getValue().serializeToJson());
            }

            jsonobject.add("criteria", jsonobject1);
            JsonArray jsonarray1 = new JsonArray();

            for (String[] astring : this.requirements)
            {
                JsonArray jsonarray = new JsonArray();

                for (String s : astring)
                {
                    jsonarray.add(s);
                }

                jsonarray1.add(jsonarray);
            }

            jsonobject.add("requirements", jsonarray1);
            return jsonobject;
        }

        public void serializeToNetwork(FriendlyByteBuf pBuf)
        {
            if (this.parentId == null)
            {
                pBuf.writeBoolean(false);
            }
            else
            {
                pBuf.writeBoolean(true);
                pBuf.writeResourceLocation(this.parentId);
            }

            if (this.display == null)
            {
                pBuf.writeBoolean(false);
            }
            else
            {
                pBuf.writeBoolean(true);
                this.display.serializeToNetwork(pBuf);
            }

            Criterion.serializeToNetwork(this.criteria, pBuf);
            pBuf.writeVarInt(this.requirements.length);

            for (String[] astring : this.requirements)
            {
                pBuf.writeVarInt(astring.length);

                for (String s : astring)
                {
                    pBuf.writeUtf(s);
                }
            }
        }

        public String toString()
        {
            return "Task Advancement{parentId=" + this.parentId + ", display=" + this.display + ", rewards=" + this.rewards + ", criteria=" + this.criteria + ", requirements=" + Arrays.deepToString(this.requirements) + "}";
        }

        public static Advancement.Builder fromJson(JsonObject pJson, DeserializationContext pConditionParser)
        {
            ResourceLocation resourcelocation = pJson.has("parent") ? new ResourceLocation(GsonHelper.getAsString(pJson, "parent")) : null;
            DisplayInfo displayinfo = pJson.has("display") ? DisplayInfo.fromJson(GsonHelper.getAsJsonObject(pJson, "display")) : null;
            AdvancementRewards advancementrewards = pJson.has("rewards") ? AdvancementRewards.deserialize(GsonHelper.getAsJsonObject(pJson, "rewards")) : AdvancementRewards.EMPTY;
            Map<String, Criterion> map = Criterion.criteriaFromJson(GsonHelper.getAsJsonObject(pJson, "criteria"), pConditionParser);

            if (map.isEmpty())
            {
                throw new JsonSyntaxException("Advancement criteria cannot be empty");
            }
            else
            {
                JsonArray jsonarray = GsonHelper.getAsJsonArray(pJson, "requirements", new JsonArray());
                String[][] astring = new String[jsonarray.size()][];

                for (int i = 0; i < jsonarray.size(); ++i)
                {
                    JsonArray jsonarray1 = GsonHelper.convertToJsonArray(jsonarray.get(i), "requirements[" + i + "]");
                    astring[i] = new String[jsonarray1.size()];

                    for (int j = 0; j < jsonarray1.size(); ++j)
                    {
                        astring[i][j] = GsonHelper.convertToString(jsonarray1.get(j), "requirements[" + i + "][" + j + "]");
                    }
                }

                if (astring.length == 0)
                {
                    astring = new String[map.size()][];
                    int k = 0;

                    for (String s2 : map.keySet())
                    {
                        astring[k++] = new String[] {s2};
                    }
                }

                for (String[] astring1 : astring)
                {
                    if (astring1.length == 0 && map.isEmpty())
                    {
                        throw new JsonSyntaxException("Requirement entry cannot be empty");
                    }

                    for (String s : astring1)
                    {
                        if (!map.containsKey(s))
                        {
                            throw new JsonSyntaxException("Unknown required criterion '" + s + "'");
                        }
                    }
                }

                for (String s1 : map.keySet())
                {
                    boolean flag = false;

                    for (String[] astring2 : astring)
                    {
                        if (ArrayUtils.contains(astring2, s1))
                        {
                            flag = true;
                            break;
                        }
                    }

                    if (!flag)
                    {
                        throw new JsonSyntaxException("Criterion '" + s1 + "' isn't a requirement for completion. This isn't supported behaviour, all criteria must be required.");
                    }
                }

                return new Advancement.Builder(resourcelocation, displayinfo, advancementrewards, map, astring);
            }
        }

        public static Advancement.Builder fromNetwork(FriendlyByteBuf pBuf)
        {
            ResourceLocation resourcelocation = pBuf.readBoolean() ? pBuf.readResourceLocation() : null;
            DisplayInfo displayinfo = pBuf.readBoolean() ? DisplayInfo.fromNetwork(pBuf) : null;
            Map<String, Criterion> map = Criterion.criteriaFromNetwork(pBuf);
            String[][] astring = new String[pBuf.readVarInt()][];

            for (int i = 0; i < astring.length; ++i)
            {
                astring[i] = new String[pBuf.readVarInt()];

                for (int j = 0; j < astring[i].length; ++j)
                {
                    astring[i][j] = pBuf.readUtf();
                }
            }

            return new Advancement.Builder(resourcelocation, displayinfo, AdvancementRewards.EMPTY, map, astring);
        }

        public Map<String, Criterion> getCriteria()
        {
            return this.criteria;
        }
    }
}
