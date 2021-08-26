package net.minecraft.advancements.critereon;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.util.List;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;

public class StatePropertiesPredicate
{
    public static final StatePropertiesPredicate ANY = new StatePropertiesPredicate(ImmutableList.of());
    private final List<StatePropertiesPredicate.PropertyMatcher> properties;

    private static StatePropertiesPredicate.PropertyMatcher fromJson(String pElement, JsonElement p_67688_)
    {
        if (p_67688_.isJsonPrimitive())
        {
            String s2 = p_67688_.getAsString();
            return new StatePropertiesPredicate.ExactPropertyMatcher(pElement, s2);
        }
        else
        {
            JsonObject jsonobject = GsonHelper.convertToJsonObject(p_67688_, "value");
            String s = jsonobject.has("min") ? getStringOrNull(jsonobject.get("min")) : null;
            String s1 = jsonobject.has("max") ? getStringOrNull(jsonobject.get("max")) : null;
            return (StatePropertiesPredicate.PropertyMatcher)(s != null && s.equals(s1) ? new StatePropertiesPredicate.ExactPropertyMatcher(pElement, s) : new StatePropertiesPredicate.RangedPropertyMatcher(pElement, s, s1));
        }
    }

    @Nullable
    private static String getStringOrNull(JsonElement pElement)
    {
        return pElement.isJsonNull() ? null : pElement.getAsString();
    }

    StatePropertiesPredicate(List<StatePropertiesPredicate.PropertyMatcher> p_67662_)
    {
        this.properties = ImmutableList.copyOf(p_67662_);
    }

    public < S extends StateHolder <? , S >> boolean matches(StateDefinition <? , S > pState, S p_67671_)
    {
        for (StatePropertiesPredicate.PropertyMatcher statepropertiespredicate$propertymatcher : this.properties)
        {
            if (!statepropertiespredicate$propertymatcher.match(pState, p_67671_))
            {
                return false;
            }
        }

        return true;
    }

    public boolean matches(BlockState pState)
    {
        return this.matches(pState.getBlock().getStateDefinition(), pState);
    }

    public boolean matches(FluidState pState)
    {
        return this.matches(pState.getType().getStateDefinition(), pState);
    }

    public void checkState(StateDefinition <? , ? > pProperties, Consumer<String> pStringConsumer)
    {
        this.properties.forEach((p_67678_) ->
        {
            p_67678_.checkState(pProperties, pStringConsumer);
        });
    }

    public static StatePropertiesPredicate fromJson(@Nullable JsonElement pElement)
    {
        if (pElement != null && !pElement.isJsonNull())
        {
            JsonObject jsonobject = GsonHelper.convertToJsonObject(pElement, "properties");
            List<StatePropertiesPredicate.PropertyMatcher> list = Lists.newArrayList();

            for (Entry<String, JsonElement> entry : jsonobject.entrySet())
            {
                list.add(fromJson(entry.getKey(), entry.getValue()));
            }

            return new StatePropertiesPredicate(list);
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

            if (!this.properties.isEmpty())
            {
                this.properties.forEach((p_67683_) ->
                {
                    jsonobject.add(p_67683_.getName(), p_67683_.toJson());
                });
            }

            return jsonobject;
        }
    }

    public static class Builder
    {
        private final List<StatePropertiesPredicate.PropertyMatcher> matchers = Lists.newArrayList();

        private Builder()
        {
        }

        public static StatePropertiesPredicate.Builder properties()
        {
            return new StatePropertiesPredicate.Builder();
        }

        public StatePropertiesPredicate.Builder hasProperty(Property<?> pIntProp, String pValue)
        {
            this.matchers.add(new StatePropertiesPredicate.ExactPropertyMatcher(pIntProp.getName(), pValue));
            return this;
        }

        public StatePropertiesPredicate.Builder hasProperty(Property<Integer> pIntProp, int pValue)
        {
            return this.hasProperty(pIntProp, Integer.toString(pValue));
        }

        public StatePropertiesPredicate.Builder hasProperty(Property<Boolean> pIntProp, boolean pValue)
        {
            return this.hasProperty(pIntProp, Boolean.toString(pValue));
        }

        public <T extends Comparable<T> & StringRepresentable> StatePropertiesPredicate.Builder hasProperty(Property<T> pIntProp, T pValue)
        {
            return this.hasProperty(pIntProp, pValue.getSerializedName());
        }

        public StatePropertiesPredicate build()
        {
            return new StatePropertiesPredicate(this.matchers);
        }
    }

    static class ExactPropertyMatcher extends StatePropertiesPredicate.PropertyMatcher
    {
        private final String value;

        public ExactPropertyMatcher(String p_67709_, String p_67710_)
        {
            super(p_67709_);
            this.value = p_67710_;
        }

        protected <T extends Comparable<T>> boolean match(StateHolder <? , ? > pProperties, Property<T> pPropertyTarget)
        {
            T t = pProperties.getValue(pPropertyTarget);
            Optional<T> optional = pPropertyTarget.getValue(this.value);
            return optional.isPresent() && t.compareTo(optional.get()) == 0;
        }

        public JsonElement toJson()
        {
            return new JsonPrimitive(this.value);
        }
    }

    abstract static class PropertyMatcher
    {
        private final String name;

        public PropertyMatcher(String p_67717_)
        {
            this.name = p_67717_;
        }

        public < S extends StateHolder <? , S >> boolean match(StateDefinition <? , S > pProperties, S pPropertyToMatch)
        {
            Property<?> property = pProperties.getProperty(this.name);
            return property == null ? false : this.match(pPropertyToMatch, property);
        }

        protected abstract <T extends Comparable<T>> boolean match(StateHolder <? , ? > pProperties, Property<T> pPropertyToMatch);

        public abstract JsonElement toJson();

        public String getName()
        {
            return this.name;
        }

        public void checkState(StateDefinition <? , ? > pProperties, Consumer<String> pPropertyConsumer)
        {
            Property<?> property = pProperties.getProperty(this.name);

            if (property == null)
            {
                pPropertyConsumer.accept(this.name);
            }
        }
    }

    static class RangedPropertyMatcher extends StatePropertiesPredicate.PropertyMatcher
    {
        @Nullable
        private final String minValue;
        @Nullable
        private final String maxValue;

        public RangedPropertyMatcher(String p_67730_, @Nullable String p_67731_, @Nullable String p_67732_)
        {
            super(p_67730_);
            this.minValue = p_67731_;
            this.maxValue = p_67732_;
        }

        protected <T extends Comparable<T>> boolean match(StateHolder <? , ? > pProperties, Property<T> pPropertyTarget)
        {
            T t = pProperties.getValue(pPropertyTarget);

            if (this.minValue != null)
            {
                Optional<T> optional = pPropertyTarget.getValue(this.minValue);

                if (!optional.isPresent() || t.compareTo(optional.get()) < 0)
                {
                    return false;
                }
            }

            if (this.maxValue != null)
            {
                Optional<T> optional1 = pPropertyTarget.getValue(this.maxValue);

                if (!optional1.isPresent() || t.compareTo(optional1.get()) > 0)
                {
                    return false;
                }
            }

            return true;
        }

        public JsonElement toJson()
        {
            JsonObject jsonobject = new JsonObject();

            if (this.minValue != null)
            {
                jsonobject.addProperty("min", this.minValue);
            }

            if (this.maxValue != null)
            {
                jsonobject.addProperty("max", this.maxValue);
            }

            return jsonobject;
        }
    }
}
