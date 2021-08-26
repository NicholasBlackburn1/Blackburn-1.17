package net.minecraft.world.effect;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;

public class MobEffect
{
    private final Map<Attribute, AttributeModifier> attributeModifiers = Maps.newHashMap();
    private final MobEffectCategory category;
    private final int color;
    @Nullable
    private String descriptionId;

    @Nullable
    public static MobEffect byId(int pPotionID)
    {
        return Registry.MOB_EFFECT.byId(pPotionID);
    }

    public static int getId(MobEffect pPotion)
    {
        return Registry.MOB_EFFECT.getId(pPotion);
    }

    protected MobEffect(MobEffectCategory p_19451_, int p_19452_)
    {
        this.category = p_19451_;
        this.color = p_19452_;
    }

    public void applyEffectTick(LivingEntity pLivingEntity, int pAmplifier)
    {
        if (this == MobEffects.REGENERATION)
        {
            if (pLivingEntity.getHealth() < pLivingEntity.getMaxHealth())
            {
                pLivingEntity.heal(1.0F);
            }
        }
        else if (this == MobEffects.POISON)
        {
            if (pLivingEntity.getHealth() > 1.0F)
            {
                pLivingEntity.hurt(DamageSource.MAGIC, 1.0F);
            }
        }
        else if (this == MobEffects.WITHER)
        {
            pLivingEntity.hurt(DamageSource.WITHER, 1.0F);
        }
        else if (this == MobEffects.HUNGER && pLivingEntity instanceof Player)
        {
            ((Player)pLivingEntity).causeFoodExhaustion(0.005F * (float)(pAmplifier + 1));
        }
        else if (this == MobEffects.SATURATION && pLivingEntity instanceof Player)
        {
            if (!pLivingEntity.level.isClientSide)
            {
                ((Player)pLivingEntity).getFoodData().eat(pAmplifier + 1, 1.0F);
            }
        }
        else if ((this != MobEffects.HEAL || pLivingEntity.isInvertedHealAndHarm()) && (this != MobEffects.HARM || !pLivingEntity.isInvertedHealAndHarm()))
        {
            if (this == MobEffects.HARM && !pLivingEntity.isInvertedHealAndHarm() || this == MobEffects.HEAL && pLivingEntity.isInvertedHealAndHarm())
            {
                pLivingEntity.hurt(DamageSource.MAGIC, (float)(6 << pAmplifier));
            }
        }
        else
        {
            pLivingEntity.heal((float)Math.max(4 << pAmplifier, 0));
        }
    }

    public void applyInstantenousEffect(@Nullable Entity pSource, @Nullable Entity pIndirectSource, LivingEntity pLivingEntity, int pAmplifier, double pHealth)
    {
        if ((this != MobEffects.HEAL || pLivingEntity.isInvertedHealAndHarm()) && (this != MobEffects.HARM || !pLivingEntity.isInvertedHealAndHarm()))
        {
            if (this == MobEffects.HARM && !pLivingEntity.isInvertedHealAndHarm() || this == MobEffects.HEAL && pLivingEntity.isInvertedHealAndHarm())
            {
                int j = (int)(pHealth * (double)(6 << pAmplifier) + 0.5D);

                if (pSource == null)
                {
                    pLivingEntity.hurt(DamageSource.MAGIC, (float)j);
                }
                else
                {
                    pLivingEntity.hurt(DamageSource.indirectMagic(pSource, pIndirectSource), (float)j);
                }
            }
            else
            {
                this.applyEffectTick(pLivingEntity, pAmplifier);
            }
        }
        else
        {
            int i = (int)(pHealth * (double)(4 << pAmplifier) + 0.5D);
            pLivingEntity.heal((float)i);
        }
    }

    public boolean isDurationEffectTick(int pDuration, int pAmplifier)
    {
        if (this == MobEffects.REGENERATION)
        {
            int k = 50 >> pAmplifier;

            if (k > 0)
            {
                return pDuration % k == 0;
            }
            else
            {
                return true;
            }
        }
        else if (this == MobEffects.POISON)
        {
            int j = 25 >> pAmplifier;

            if (j > 0)
            {
                return pDuration % j == 0;
            }
            else
            {
                return true;
            }
        }
        else if (this == MobEffects.WITHER)
        {
            int i = 40 >> pAmplifier;

            if (i > 0)
            {
                return pDuration % i == 0;
            }
            else
            {
                return true;
            }
        }
        else
        {
            return this == MobEffects.HUNGER;
        }
    }

    public boolean isInstantenous()
    {
        return false;
    }

    protected String getOrCreateDescriptionId()
    {
        if (this.descriptionId == null)
        {
            this.descriptionId = Util.makeDescriptionId("effect", Registry.MOB_EFFECT.getKey(this));
        }

        return this.descriptionId;
    }

    public String getDescriptionId()
    {
        return this.getOrCreateDescriptionId();
    }

    public Component getDisplayName()
    {
        return new TranslatableComponent(this.getDescriptionId());
    }

    public MobEffectCategory getCategory()
    {
        return this.category;
    }

    public int getColor()
    {
        return this.color;
    }

    public MobEffect addAttributeModifier(Attribute pAttribute, String pUuid, double pAmount, AttributeModifier.Operation p_19476_)
    {
        AttributeModifier attributemodifier = new AttributeModifier(UUID.fromString(pUuid), this::getDescriptionId, pAmount, p_19476_);
        this.attributeModifiers.put(pAttribute, attributemodifier);
        return this;
    }

    public Map<Attribute, AttributeModifier> getAttributeModifiers()
    {
        return this.attributeModifiers;
    }

    public void removeAttributeModifiers(LivingEntity pLivingEntity, AttributeMap pAttributeMap, int pAmplifier)
    {
        for (Entry<Attribute, AttributeModifier> entry : this.attributeModifiers.entrySet())
        {
            AttributeInstance attributeinstance = pAttributeMap.getInstance(entry.getKey());

            if (attributeinstance != null)
            {
                attributeinstance.removeModifier(entry.getValue());
            }
        }
    }

    public void addAttributeModifiers(LivingEntity pLivingEntity, AttributeMap pAttributeMap, int pAmplifier)
    {
        for (Entry<Attribute, AttributeModifier> entry : this.attributeModifiers.entrySet())
        {
            AttributeInstance attributeinstance = pAttributeMap.getInstance(entry.getKey());

            if (attributeinstance != null)
            {
                AttributeModifier attributemodifier = entry.getValue();
                attributeinstance.removeModifier(attributemodifier);
                attributeinstance.addPermanentModifier(new AttributeModifier(attributemodifier.getId(), this.getDescriptionId() + " " + pAmplifier, this.getAttributeModifierValue(pAmplifier, attributemodifier), attributemodifier.getOperation()));
            }
        }
    }

    public double getAttributeModifierValue(int pAmplifier, AttributeModifier pModifier)
    {
        return pModifier.getAmount() * (double)(pAmplifier + 1);
    }

    public boolean isBeneficial()
    {
        return this.category == MobEffectCategory.BENEFICIAL;
    }
}
