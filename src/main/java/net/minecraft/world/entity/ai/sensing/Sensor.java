package net.minecraft.world.entity.ai.sensing;

import java.util.Random;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

public abstract class Sensor<E extends LivingEntity>
{
    private static final Random RANDOM = new Random();
    private static final int DEFAULT_SCAN_RATE = 20;
    protected static final int TARGETING_RANGE = 16;
    private static final TargetingConditions TARGET_CONDITIONS = TargetingConditions.forNonCombat().range(16.0D);
    private static final TargetingConditions TARGET_CONDITIONS_IGNORE_INVISIBILITY_TESTING = TargetingConditions.forNonCombat().range(16.0D).ignoreInvisibilityTesting();
    private static final TargetingConditions ATTACK_TARGET_CONDITIONS = TargetingConditions.forCombat().range(16.0D);
    private static final TargetingConditions ATTACK_TARGET_CONDITIONS_IGNORE_INVISIBILITY_TESTING = TargetingConditions.forCombat().range(16.0D).ignoreInvisibilityTesting();
    private static final TargetingConditions f_182375_ = TargetingConditions.forCombat().range(16.0D).ignoreLineOfSight();
    private static final TargetingConditions f_182376_ = TargetingConditions.forCombat().range(16.0D).ignoreLineOfSight().ignoreInvisibilityTesting();
    private final int scanRate;
    private long timeToTick;

    public Sensor(int p_26800_)
    {
        this.scanRate = p_26800_;
        this.timeToTick = (long)RANDOM.nextInt(p_26800_);
    }

    public Sensor()
    {
        this(20);
    }

    public final void tick(ServerLevel pLevel, E pEntity)
    {
        if (--this.timeToTick <= 0L)
        {
            this.timeToTick = (long)this.scanRate;
            this.doTick(pLevel, pEntity);
        }
    }

    protected abstract void doTick(ServerLevel pLevel, E pEntity);

    public abstract Set < MemoryModuleType<? >> requires();

    protected static boolean isEntityTargetable(LivingEntity pLivingEntity, LivingEntity pTarget)
    {
        return pLivingEntity.getBrain().isMemoryValue(MemoryModuleType.ATTACK_TARGET, pTarget) ? TARGET_CONDITIONS_IGNORE_INVISIBILITY_TESTING.test(pLivingEntity, pTarget) : TARGET_CONDITIONS.test(pLivingEntity, pTarget);
    }

    public static boolean isEntityAttackable(LivingEntity p_148313_, LivingEntity p_148314_)
    {
        return p_148313_.getBrain().isMemoryValue(MemoryModuleType.ATTACK_TARGET, p_148314_) ? ATTACK_TARGET_CONDITIONS_IGNORE_INVISIBILITY_TESTING.test(p_148313_, p_148314_) : ATTACK_TARGET_CONDITIONS.test(p_148313_, p_148314_);
    }

    public static boolean m_182377_(LivingEntity p_182378_, LivingEntity p_182379_)
    {
        return p_182378_.getBrain().isMemoryValue(MemoryModuleType.ATTACK_TARGET, p_182379_) ? f_182376_.test(p_182378_, p_182379_) : f_182375_.test(p_182378_, p_182379_);
    }
}
