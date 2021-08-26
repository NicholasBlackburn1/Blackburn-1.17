package net.minecraft.world.level.storage.loot;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class LootContext
{
    private final Random random;
    private final float luck;
    private final ServerLevel level;
    private final Function<ResourceLocation, LootTable> lootTables;
    private final Set<LootTable> visitedTables = Sets.newLinkedHashSet();
    private final Function<ResourceLocation, LootItemCondition> conditions;
    private final Set<LootItemCondition> visitedConditions = Sets.newLinkedHashSet();
    private final Map < LootContextParam<?>, Object > params;
    private final Map<ResourceLocation, LootContext.DynamicDrop> dynamicDrops;

    LootContext(Random p_78917_, float p_78918_, ServerLevel p_78919_, Function<ResourceLocation, LootTable> p_78920_, Function<ResourceLocation, LootItemCondition> p_78921_, Map < LootContextParam<?>, Object > p_78922_, Map<ResourceLocation, LootContext.DynamicDrop> p_78923_)
    {
        this.random = p_78917_;
        this.luck = p_78918_;
        this.level = p_78919_;
        this.lootTables = p_78920_;
        this.conditions = p_78921_;
        this.params = ImmutableMap.copyOf(p_78922_);
        this.dynamicDrops = ImmutableMap.copyOf(p_78923_);
    }

    public boolean hasParam(LootContextParam<?> pParameter)
    {
        return this.params.containsKey(pParameter);
    }

    public <T> T getParam(LootContextParam<T> p_165125_)
    {
        T t = (T)this.params.get(p_165125_);

        if (t == null)
        {
            throw new NoSuchElementException(p_165125_.getName().toString());
        }
        else
        {
            return t;
        }
    }

    public void addDynamicDrops(ResourceLocation pName, Consumer<ItemStack> pConsumer)
    {
        LootContext.DynamicDrop lootcontext$dynamicdrop = this.dynamicDrops.get(pName);

        if (lootcontext$dynamicdrop != null)
        {
            lootcontext$dynamicdrop.add(this, pConsumer);
        }
    }

    @Nullable
    public <T> T getParamOrNull(LootContextParam<T> pParameter)
    {
        return (T)this.params.get(pParameter);
    }

    public boolean addVisitedTable(LootTable pLootTable)
    {
        return this.visitedTables.add(pLootTable);
    }

    public void removeVisitedTable(LootTable pLootTable)
    {
        this.visitedTables.remove(pLootTable);
    }

    public boolean addVisitedCondition(LootItemCondition pCondition)
    {
        return this.visitedConditions.add(pCondition);
    }

    public void removeVisitedCondition(LootItemCondition pCondition)
    {
        this.visitedConditions.remove(pCondition);
    }

    public LootTable getLootTable(ResourceLocation pTableId)
    {
        return this.lootTables.apply(pTableId);
    }

    public LootItemCondition getCondition(ResourceLocation pConditionId)
    {
        return this.conditions.apply(pConditionId);
    }

    public Random getRandom()
    {
        return this.random;
    }

    public float getLuck()
    {
        return this.luck;
    }

    public ServerLevel getLevel()
    {
        return this.level;
    }

    public static class Builder
    {
        private final ServerLevel level;
        private final Map < LootContextParam<?>, Object > params = Maps.newIdentityHashMap();
        private final Map<ResourceLocation, LootContext.DynamicDrop> dynamicDrops = Maps.newHashMap();
        private Random random;
        private float luck;

        public Builder(ServerLevel p_78961_)
        {
            this.level = p_78961_;
        }

        public LootContext.Builder withRandom(Random pRandom)
        {
            this.random = pRandom;
            return this;
        }

        public LootContext.Builder withOptionalRandomSeed(long pSeed)
        {
            if (pSeed != 0L)
            {
                this.random = new Random(pSeed);
            }

            return this;
        }

        public LootContext.Builder withOptionalRandomSeed(long pSeed, Random p_78969_)
        {
            if (pSeed == 0L)
            {
                this.random = p_78969_;
            }
            else
            {
                this.random = new Random(pSeed);
            }

            return this;
        }

        public LootContext.Builder withLuck(float pLuck)
        {
            this.luck = pLuck;
            return this;
        }

        public <T> LootContext.Builder withParameter(LootContextParam<T> pParameter, T pValue)
        {
            this.params.put(pParameter, pValue);
            return this;
        }

        public <T> LootContext.Builder withOptionalParameter(LootContextParam<T> pParameter, @Nullable T pValue)
        {
            if (pValue == null)
            {
                this.params.remove(pParameter);
            }
            else
            {
                this.params.put(pParameter, pValue);
            }

            return this;
        }

        public LootContext.Builder withDynamicDrop(ResourceLocation p_78980_, LootContext.DynamicDrop p_78981_)
        {
            LootContext.DynamicDrop lootcontext$dynamicdrop = this.dynamicDrops.put(p_78980_, p_78981_);

            if (lootcontext$dynamicdrop != null)
            {
                throw new IllegalStateException("Duplicated dynamic drop '" + this.dynamicDrops + "'");
            }
            else
            {
                return this;
            }
        }

        public ServerLevel getLevel()
        {
            return this.level;
        }

        public <T> T getParameter(LootContextParam<T> pParameter)
        {
            T t = (T)this.params.get(pParameter);

            if (t == null)
            {
                throw new IllegalArgumentException("No parameter " + pParameter);
            }
            else
            {
                return t;
            }
        }

        @Nullable
        public <T> T getOptionalParameter(LootContextParam<T> pParameter)
        {
            return (T)this.params.get(pParameter);
        }

        public LootContext create(LootContextParamSet pParameterSet)
        {
            Set < LootContextParam<? >> set = Sets.difference(this.params.keySet(), pParameterSet.getAllowed());

            if (!set.isEmpty())
            {
                throw new IllegalArgumentException("Parameters not allowed in this parameter set: " + set);
            }
            else
            {
                Set < LootContextParam<? >> set1 = Sets.difference(pParameterSet.getRequired(), this.params.keySet());

                if (!set1.isEmpty())
                {
                    throw new IllegalArgumentException("Missing required parameters: " + set1);
                }
                else
                {
                    Random random = this.random;

                    if (random == null)
                    {
                        random = new Random();
                    }

                    MinecraftServer minecraftserver = this.level.getServer();
                    return new LootContext(random, this.luck, this.level, minecraftserver.getLootTables()::get, minecraftserver.getPredicateManager()::get, this.params, this.dynamicDrops);
                }
            }
        }
    }

    @FunctionalInterface
    public interface DynamicDrop
    {
        void add(LootContext p_78988_, Consumer<ItemStack> p_78989_);
    }

    public static enum EntityTarget
    {
        THIS("this", LootContextParams.THIS_ENTITY),
        KILLER("killer", LootContextParams.KILLER_ENTITY),
        DIRECT_KILLER("direct_killer", LootContextParams.DIRECT_KILLER_ENTITY),
        KILLER_PLAYER("killer_player", LootContextParams.LAST_DAMAGE_PLAYER);

        final String name;
        private final LootContextParam <? extends Entity > param;

        private EntityTarget(String p_79001_, LootContextParam <? extends Entity > p_79002_)
        {
            this.name = p_79001_;
            this.param = p_79002_;
        }

        public LootContextParam <? extends Entity > getParam()
        {
            return this.param;
        }

        public static LootContext.EntityTarget getByName(String pType)
        {
            for (LootContext.EntityTarget lootcontext$entitytarget : values())
            {
                if (lootcontext$entitytarget.name.equals(pType))
                {
                    return lootcontext$entitytarget;
                }
            }

            throw new IllegalArgumentException("Invalid entity target " + pType);
        }

        public static class Serializer extends TypeAdapter<LootContext.EntityTarget> {
            public void write(JsonWriter p_79015_, LootContext.EntityTarget p_79016_) throws IOException {
                p_79015_.value(p_79016_.name);
            }

            public LootContext.EntityTarget read(JsonReader p_79013_) throws IOException {
                return LootContext.EntityTarget.getByName(p_79013_.nextString());
            }
        }
    }
}
