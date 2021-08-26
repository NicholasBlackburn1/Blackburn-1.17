package net.minecraft.world.entity.ai;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Supplier;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.ExpirableValue;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.entity.schedule.Schedule;
import org.apache.commons.lang3.mutable.MutableObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Brain<E extends LivingEntity>
{
    static final Logger LOGGER = LogManager.getLogger();
    private final Supplier<Codec<Brain<E>>> codec;
    private static final int SCHEDULE_UPDATE_DELAY = 20;
    private final Map < MemoryModuleType<?>, Optional <? extends ExpirableValue<? >>> memories = Maps.newHashMap();
    private final Map < SensorType <? extends Sensor <? super E >> , Sensor <? super E >> sensors = Maps.newLinkedHashMap();
    private final Map < Integer, Map < Activity, Set < Behavior <? super E >>> > availableBehaviorsByPriority = Maps.newTreeMap();
    private Schedule schedule = Schedule.EMPTY;
    private final Map < Activity, Set < Pair < MemoryModuleType<?>, MemoryStatus >>> activityRequirements = Maps.newHashMap();
    private final Map < Activity, Set < MemoryModuleType<? >>> activityMemoriesToEraseWhenStopped = Maps.newHashMap();
    private Set<Activity> coreActivities = Sets.newHashSet();
    private final Set<Activity> activeActivities = Sets.newHashSet();
    private Activity defaultActivity = Activity.IDLE;
    private long lastScheduleUpdate = -9999L;

    public static <E extends LivingEntity> Brain.Provider<E> provider(Collection <? extends MemoryModuleType<? >> pMemoryTypes, Collection <? extends SensorType <? extends Sensor <? super E >>> pSensorTypes)
    {
        return new Brain.Provider<>(pMemoryTypes, pSensorTypes);
    }

    public static <E extends LivingEntity> Codec<Brain<E>> codec(final Collection <? extends MemoryModuleType<? >> pMemoryTypes, final Collection <? extends SensorType <? extends Sensor <? super E >>> pSensorTypes)
    {
        final MutableObject<Codec<Brain<E>>> mutableobject = new MutableObject<>();
        mutableobject.setValue((new MapCodec<Brain<E>>()
        {
            public <T> Stream<T> keys(DynamicOps<T> p_22029_)
            {
                return pMemoryTypes.stream().flatMap((p_22020_) ->
                {
                    return Util.toStream(p_22020_.getCodec().map((p_147346_) -> {
                        return Registry.MEMORY_MODULE_TYPE.getKey(p_22020_);
                    }));
                }).map((p_22018_) ->
                {
                    return p_22029_.createString(p_22018_.toString());
                });
            }
            public <T> DataResult<Brain<E>> decode(DynamicOps<T> p_22022_, MapLike<T> p_22023_)
            {
                MutableObject < DataResult < Builder < Brain.MemoryValue<? >>> > mutableobject1 = new MutableObject<>(DataResult.success(ImmutableList.builder()));
                p_22023_.entries().forEach((p_22015_) ->
                {
                    DataResult < MemoryModuleType<? >> dataresult = Registry.MEMORY_MODULE_TYPE.parse(p_22022_, p_22015_.getFirst());
                    DataResult <? extends Brain.MemoryValue<? >> dataresult1 = dataresult.flatMap((p_147350_) -> {
                        return this.captureRead(p_147350_, p_22022_, (T)p_22015_.getSecond());
                    });
                    mutableobject1.setValue(mutableobject1.getValue().apply2(Builder::add, dataresult1));
                });
                ImmutableList < Brain.MemoryValue<? >> immutablelist = mutableobject1.getValue().resultOrPartial(Brain.LOGGER::error).map(Builder::build).orElseGet(ImmutableList::of);
                return DataResult.success(new Brain<>(pMemoryTypes, pSensorTypes, immutablelist, mutableobject::getValue));
            }
            private <T, U> DataResult<Brain.MemoryValue<U>> captureRead(MemoryModuleType<U> pMemoryType, DynamicOps<T> pOps, T pInput)
            {
                return pMemoryType.getCodec().map(DataResult::success).orElseGet(() ->
                {
                    return DataResult.error("No codec for memory: " + pMemoryType);
                }).flatMap((p_22011_) ->
                {
                    return p_22011_.parse(pOps, pInput);
                }).map((p_21992_) ->
                {
                    return new Brain.MemoryValue<>(pMemoryType, Optional.of(p_21992_));
                });
            }
            public <T> RecordBuilder<T> encode(Brain<E> p_21985_, DynamicOps<T> p_21986_, RecordBuilder<T> p_21987_)
            {
                p_21985_.memories().forEach((p_22007_) ->
                {
                    p_22007_.serialize(p_21986_, p_21987_);
                });
                return p_21987_;
            }
        }).fieldOf("memories").codec());
        return mutableobject.getValue();
    }

    public Brain(Collection <? extends MemoryModuleType<? >> p_21855_, Collection <? extends SensorType <? extends Sensor <? super E >>> p_21856_, ImmutableList < Brain.MemoryValue<? >> p_21857_, Supplier<Codec<Brain<E>>> p_21858_)
    {
        this.codec = p_21858_;

        for (MemoryModuleType<?> memorymoduletype : p_21855_)
        {
            this.memories.put(memorymoduletype, Optional.empty());
        }

        for (SensorType <? extends Sensor <? super E >> sensortype : p_21856_)
        {
            this.sensors.put(sensortype, sensortype.create());
        }

        for (Sensor <? super E > sensor : this.sensors.values())
        {
            for (MemoryModuleType<?> memorymoduletype1 : sensor.requires())
            {
                this.memories.put(memorymoduletype1, Optional.empty());
            }
        }

        for (Brain.MemoryValue<?> memoryvalue : p_21857_)
        {
            memoryvalue.setMemoryInternal(this);
        }
    }

    public <T> DataResult<T> serializeStart(DynamicOps<T> pOps)
    {
        return this.codec.get().encodeStart(pOps, this);
    }

    Stream < Brain.MemoryValue<? >> memories()
    {
        return this.memories.entrySet().stream().map((p_21929_) ->
        {
            return Brain.MemoryValue.createUnchecked(p_21929_.getKey(), p_21929_.getValue());
        });
    }

    public boolean hasMemoryValue(MemoryModuleType<?> pType)
    {
        return this.checkMemory(pType, MemoryStatus.VALUE_PRESENT);
    }

    public <U> void eraseMemory(MemoryModuleType<U> pType)
    {
        this.setMemory(pType, Optional.empty());
    }

    public <U> void setMemory(MemoryModuleType<U> pMemoryType, @Nullable U pMemory)
    {
        this.setMemory(pMemoryType, Optional.ofNullable(pMemory));
    }

    public <U> void setMemoryWithExpiry(MemoryModuleType<U> pMemoryType, U pMemory, long pTimesToLive)
    {
        this.setMemoryInternal(pMemoryType, Optional.of(ExpirableValue.of(pMemory, pTimesToLive)));
    }

    public <U> void setMemory(MemoryModuleType<U> pMemoryType, Optional <? extends U > pMemory)
    {
        this.setMemoryInternal(pMemoryType, pMemory.map(ExpirableValue::of));
    }

    <U> void setMemoryInternal(MemoryModuleType<U> pMemoryType, Optional <? extends ExpirableValue<? >> pMemory)
    {
        if (this.memories.containsKey(pMemoryType))
        {
            if (pMemory.isPresent() && this.isEmptyCollection(pMemory.get().getValue()))
            {
                this.eraseMemory(pMemoryType);
            }
            else
            {
                this.memories.put(pMemoryType, pMemory);
            }
        }
    }

    public <U> Optional<U> getMemory(MemoryModuleType<U> pType)
    {
        return (Optional<U>)this.memories.get(pType).map(ExpirableValue::getValue);
    }

    public <U> long getTimeUntilExpiry(MemoryModuleType<U> p_147342_)
    {
        Optional <? extends ExpirableValue<? >> optional = this.memories.get(p_147342_);
        return optional.map(ExpirableValue::getTimeToLive).orElse(0L);
    }

    @Deprecated
    @VisibleForDebug
    public Map < MemoryModuleType<?>, Optional <? extends ExpirableValue<? >>> getMemories()
    {
        return this.memories;
    }

    public <U> boolean isMemoryValue(MemoryModuleType<U> pMemoryType, U pMemory)
    {
        return !this.hasMemoryValue(pMemoryType) ? false : this.getMemory(pMemoryType).filter((p_21922_) ->
        {
            return p_21922_.equals(pMemory);
        }).isPresent();
    }

    public boolean checkMemory(MemoryModuleType<?> pMemoryType, MemoryStatus pMemoryStatus)
    {
        Optional <? extends ExpirableValue<? >> optional = this.memories.get(pMemoryType);

        if (optional == null)
        {
            return false;
        }
        else
        {
            return pMemoryStatus == MemoryStatus.REGISTERED || pMemoryStatus == MemoryStatus.VALUE_PRESENT && optional.isPresent() || pMemoryStatus == MemoryStatus.VALUE_ABSENT && !optional.isPresent();
        }
    }

    public Schedule getSchedule()
    {
        return this.schedule;
    }

    public void setSchedule(Schedule pNewSchedule)
    {
        this.schedule = pNewSchedule;
    }

    public void setCoreActivities(Set<Activity> pNewActivities)
    {
        this.coreActivities = pNewActivities;
    }

    @Deprecated
    @VisibleForDebug
    public Set<Activity> getActiveActivities()
    {
        return this.activeActivities;
    }

    @Deprecated
    @VisibleForDebug
    public List < Behavior <? super E >> getRunningBehaviors()
    {
        List < Behavior <? super E >> list = new ObjectArrayList<>();

        for (Map < Activity, Set < Behavior <? super E >>> map : this.availableBehaviorsByPriority.values())
        {
            for (Set < Behavior <? super E >> set : map.values())
            {
                for (Behavior <? super E > behavior : set)
                {
                    if (behavior.getStatus() == Behavior.Status.RUNNING)
                    {
                        list.add(behavior);
                    }
                }
            }
        }

        return list;
    }

    public void useDefaultActivity()
    {
        this.setActiveActivity(this.defaultActivity);
    }

    public Optional<Activity> getActiveNonCoreActivity()
    {
        for (Activity activity : this.activeActivities)
        {
            if (!this.coreActivities.contains(activity))
            {
                return Optional.of(activity);
            }
        }

        return Optional.empty();
    }

    public void setActiveActivityIfPossible(Activity pActivity)
    {
        if (this.activityRequirementsAreMet(pActivity))
        {
            this.setActiveActivity(pActivity);
        }
        else
        {
            this.useDefaultActivity();
        }
    }

    private void setActiveActivity(Activity pActivity)
    {
        if (!this.isActive(pActivity))
        {
            this.eraseMemoriesForOtherActivitesThan(pActivity);
            this.activeActivities.clear();
            this.activeActivities.addAll(this.coreActivities);
            this.activeActivities.add(pActivity);
        }
    }

    private void eraseMemoriesForOtherActivitesThan(Activity pActivity)
    {
        for (Activity activity : this.activeActivities)
        {
            if (activity != pActivity)
            {
                Set < MemoryModuleType<? >> set = this.activityMemoriesToEraseWhenStopped.get(activity);

                if (set != null)
                {
                    for (MemoryModuleType<?> memorymoduletype : set)
                    {
                        this.eraseMemory(memorymoduletype);
                    }
                }
            }
        }
    }

    public void updateActivityFromSchedule(long pDayTime, long p_21864_)
    {
        if (p_21864_ - this.lastScheduleUpdate > 20L)
        {
            this.lastScheduleUpdate = p_21864_;
            Activity activity = this.getSchedule().getActivityAt((int)(pDayTime % 24000L));

            if (!this.activeActivities.contains(activity))
            {
                this.setActiveActivityIfPossible(activity);
            }
        }
    }

    public void setActiveActivityToFirstValid(List<Activity> pActivities)
    {
        for (Activity activity : pActivities)
        {
            if (this.activityRequirementsAreMet(activity))
            {
                this.setActiveActivity(activity);
                break;
            }
        }
    }

    public void setDefaultActivity(Activity pNewFallbackActivity)
    {
        this.defaultActivity = pNewFallbackActivity;
    }

    public void addActivity(Activity pActivity, int pPriorityStart, ImmutableList <? extends Behavior <? super E >> pTasks)
    {
        this.addActivity(pActivity, this.createPriorityPairs(pPriorityStart, pTasks));
    }

    public void addActivityAndRemoveMemoryWhenStopped(Activity pActivity, int pPriorityStart, ImmutableList <? extends Behavior <? super E >> pTasks, MemoryModuleType<?> pMemoryType)
    {
        Set < Pair < MemoryModuleType<?>, MemoryStatus >> set = ImmutableSet.of(Pair.of(pMemoryType, MemoryStatus.VALUE_PRESENT));
        Set < MemoryModuleType<? >> set1 = ImmutableSet.of(pMemoryType);
        this.addActivityAndRemoveMemoriesWhenStopped(pActivity, this.createPriorityPairs(pPriorityStart, pTasks), set, set1);
    }

    public void addActivity(Activity pActivity, ImmutableList <? extends Pair < Integer, ? extends Behavior <? super E >>> pPriorityStart)
    {
        this.addActivityAndRemoveMemoriesWhenStopped(pActivity, pPriorityStart, ImmutableSet.of(), Sets.newHashSet());
    }

    public void addActivityWithConditions(Activity pActivity, ImmutableList <? extends Pair < Integer, ? extends Behavior <? super E >>> pTasks, Set < Pair < MemoryModuleType<?>, MemoryStatus >> pMemoryStatuses)
    {
        this.addActivityAndRemoveMemoriesWhenStopped(pActivity, pTasks, pMemoryStatuses, Sets.newHashSet());
    }

    public void addActivityAndRemoveMemoriesWhenStopped(Activity pActivity, ImmutableList <? extends Pair < Integer, ? extends Behavior <? super E >>> pTasks, Set < Pair < MemoryModuleType<?>, MemoryStatus >> pMemorieStatuses, Set < MemoryModuleType<? >> pMemoryTypes)
    {
        this.activityRequirements.put(pActivity, pMemorieStatuses);

        if (!pMemoryTypes.isEmpty())
        {
            this.activityMemoriesToEraseWhenStopped.put(pActivity, pMemoryTypes);
        }

        for (Pair < Integer, ? extends Behavior <? super E >> pair : pTasks)
        {
            this.availableBehaviorsByPriority.computeIfAbsent(pair.getFirst(), (p_21917_) ->
            {
                return Maps.newHashMap();
            }).computeIfAbsent(pActivity, (p_21972_) ->
            {
                return Sets.newLinkedHashSet();
            }).add(pair.getSecond());
        }
    }

    @VisibleForTesting
    public void removeAllBehaviors()
    {
        this.availableBehaviorsByPriority.clear();
    }

    public boolean isActive(Activity pActivity)
    {
        return this.activeActivities.contains(pActivity);
    }

    public Brain<E> copyWithoutBehaviors()
    {
        Brain<E> brain = new Brain<>(this.memories.keySet(), this.sensors.keySet(), ImmutableList.of(), this.codec);

        for (Entry < MemoryModuleType<?>, Optional <? extends ExpirableValue<? >>> entry : this.memories.entrySet())
        {
            MemoryModuleType<?> memorymoduletype = entry.getKey();

            if (entry.getValue().isPresent())
            {
                brain.memories.put(memorymoduletype, entry.getValue());
            }
        }

        return brain;
    }

    public void tick(ServerLevel pLevel, E pEntity)
    {
        this.forgetOutdatedMemories();
        this.tickSensors(pLevel, pEntity);
        this.startEachNonRunningBehavior(pLevel, pEntity);
        this.tickEachRunningBehavior(pLevel, pEntity);
    }

    private void tickSensors(ServerLevel pLevel, E pBrainHolder)
    {
        for (Sensor <? super E > sensor : this.sensors.values())
        {
            sensor.tick(pLevel, pBrainHolder);
        }
    }

    private void forgetOutdatedMemories()
    {
        for (Entry < MemoryModuleType<?>, Optional <? extends ExpirableValue<? >>> entry : this.memories.entrySet())
        {
            if (entry.getValue().isPresent())
            {
                ExpirableValue<?> expirablevalue = entry.getValue().get();
                expirablevalue.tick();

                if (expirablevalue.hasExpired())
                {
                    this.eraseMemory(entry.getKey());
                }
            }
        }
    }

    public void stopAll(ServerLevel pLevel, E pOwner)
    {
        long i = pOwner.level.getGameTime();

        for (Behavior <? super E > behavior : this.getRunningBehaviors())
        {
            behavior.doStop(pLevel, pOwner, i);
        }
    }

    private void startEachNonRunningBehavior(ServerLevel pLevel, E pEntity)
    {
        long i = pLevel.getGameTime();

        for (Map < Activity, Set < Behavior <? super E >>> map : this.availableBehaviorsByPriority.values())
        {
            for (Entry < Activity, Set < Behavior <? super E >>> entry : map.entrySet())
            {
                Activity activity = entry.getKey();

                if (this.activeActivities.contains(activity))
                {
                    for (Behavior <? super E > behavior : entry.getValue())
                    {
                        if (behavior.getStatus() == Behavior.Status.STOPPED)
                        {
                            behavior.tryStart(pLevel, pEntity, i);
                        }
                    }
                }
            }
        }
    }

    private void tickEachRunningBehavior(ServerLevel pLevel, E pEntity)
    {
        long i = pLevel.getGameTime();

        for (Behavior <? super E > behavior : this.getRunningBehaviors())
        {
            behavior.tickOrStop(pLevel, pEntity, i);
        }
    }

    private boolean activityRequirementsAreMet(Activity pActivity)
    {
        if (!this.activityRequirements.containsKey(pActivity))
        {
            return false;
        }
        else
        {
            for (Pair < MemoryModuleType<?>, MemoryStatus > pair : this.activityRequirements.get(pActivity))
            {
                MemoryModuleType<?> memorymoduletype = pair.getFirst();
                MemoryStatus memorystatus = pair.getSecond();

                if (!this.checkMemory(memorymoduletype, memorystatus))
                {
                    return false;
                }
            }

            return true;
        }
    }

    private boolean isEmptyCollection(Object pCollection)
    {
        return pCollection instanceof Collection && ((Collection)pCollection).isEmpty();
    }

    ImmutableList <? extends Pair < Integer, ? extends Behavior <? super E >>> createPriorityPairs(int pPriorityStart, ImmutableList <? extends Behavior <? super E >> pTasks)
    {
        int i = pPriorityStart;
        Builder < Pair < Integer, ? extends Behavior <? super E >>> builder = ImmutableList.builder();

        for (Behavior <? super E > behavior : pTasks)
        {
            builder.add(Pair.of(i++, behavior));
        }

        return builder.build();
    }

    static final class MemoryValue<U>
    {
        private final MemoryModuleType<U> type;
        private final Optional <? extends ExpirableValue<U >> value;

        static <U> Brain.MemoryValue<U> createUnchecked(MemoryModuleType<U> pMemoryType, Optional <? extends ExpirableValue<? >> pMemory)
        {
            return new Brain.MemoryValue<U>(pMemoryType, (Optional <? extends ExpirableValue<U >>)pMemory);
        }

        MemoryValue(MemoryModuleType<U> p_22033_, Optional <? extends ExpirableValue<U >> p_22034_)
        {
            this.type = p_22033_;
            this.value = p_22034_;
        }

        void setMemoryInternal(Brain<?> pBrain)
        {
            pBrain.setMemoryInternal(this.type, this.value);
        }

        public <T> void serialize(DynamicOps<T> pOps, RecordBuilder<T> pBuilder)
        {
            this.type.getCodec().ifPresent((p_22053_) ->
            {
                this.value.ifPresent((p_147355_) -> {
                    pBuilder.add(Registry.MEMORY_MODULE_TYPE.encodeStart(pOps, this.type), p_22053_.encodeStart(pOps, p_147355_));
                });
            });
        }
    }

    public static final class Provider<E extends LivingEntity>
    {
        private final Collection <? extends MemoryModuleType<? >> memoryTypes;
        private final Collection <? extends SensorType <? extends Sensor <? super E >>> sensorTypes;
        private final Codec<Brain<E>> codec;

        Provider(Collection <? extends MemoryModuleType<? >> p_22066_, Collection <? extends SensorType <? extends Sensor <? super E >>> p_22067_)
        {
            this.memoryTypes = p_22066_;
            this.sensorTypes = p_22067_;
            this.codec = Brain.codec(p_22066_, p_22067_);
        }

        public Brain<E> makeBrain(Dynamic<?> pOps)
        {
            return this.codec.parse(pOps).resultOrPartial(Brain.LOGGER::error).orElseGet(() ->
            {
                return new Brain<>(this.memoryTypes, this.sensorTypes, ImmutableList.of(), () -> {
                    return this.codec;
                });
            });
        }
    }
}
