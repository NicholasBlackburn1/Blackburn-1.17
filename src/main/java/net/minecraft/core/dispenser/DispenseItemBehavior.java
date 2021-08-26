package net.minecraft.core.dispenser;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Saddleable;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.entity.projectile.SpectralArrow;
import net.minecraft.world.entity.projectile.ThrownEgg;
import net.minecraft.world.entity.projectile.ThrownExperienceBottle;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.DispensibleContainerItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.CandleCakeBlock;
import net.minecraft.world.level.block.CarvedPumpkinBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.WitherSkullBlock;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public interface DispenseItemBehavior
{
    Logger LOGGER = LogManager.getLogger();
    DispenseItemBehavior NOOP = (p_123400_, p_123401_) ->
    {
        return p_123401_;
    };

    ItemStack dispense(BlockSource p_123403_, ItemStack p_123404_);

    static void bootStrap()
    {
        DispenserBlock.registerBehavior(Items.ARROW, new AbstractProjectileDispenseBehavior()
        {
            protected Projectile getProjectile(Level pLevel, Position pPosition, ItemStack pStack)
            {
                Arrow arrow = new Arrow(pLevel, pPosition.x(), pPosition.y(), pPosition.z());
                arrow.pickup = AbstractArrow.Pickup.ALLOWED;
                return arrow;
            }
        });
        DispenserBlock.registerBehavior(Items.TIPPED_ARROW, new AbstractProjectileDispenseBehavior()
        {
            protected Projectile getProjectile(Level pLevel, Position pPosition, ItemStack pStack)
            {
                Arrow arrow = new Arrow(pLevel, pPosition.x(), pPosition.y(), pPosition.z());
                arrow.setEffectsFromItem(pStack);
                arrow.pickup = AbstractArrow.Pickup.ALLOWED;
                return arrow;
            }
        });
        DispenserBlock.registerBehavior(Items.SPECTRAL_ARROW, new AbstractProjectileDispenseBehavior()
        {
            protected Projectile getProjectile(Level pLevel, Position pPosition, ItemStack pStack)
            {
                AbstractArrow abstractarrow = new SpectralArrow(pLevel, pPosition.x(), pPosition.y(), pPosition.z());
                abstractarrow.pickup = AbstractArrow.Pickup.ALLOWED;
                return abstractarrow;
            }
        });
        DispenserBlock.registerBehavior(Items.EGG, new AbstractProjectileDispenseBehavior()
        {
            protected Projectile getProjectile(Level pLevel, Position pPosition, ItemStack pStack)
            {
                return Util.make(new ThrownEgg(pLevel, pPosition.x(), pPosition.y(), pPosition.z()), (p_123466_) ->
                {
                    p_123466_.setItem(pStack);
                });
            }
        });
        DispenserBlock.registerBehavior(Items.SNOWBALL, new AbstractProjectileDispenseBehavior()
        {
            protected Projectile getProjectile(Level pLevel, Position pPosition, ItemStack pStack)
            {
                return Util.make(new Snowball(pLevel, pPosition.x(), pPosition.y(), pPosition.z()), (p_123474_) ->
                {
                    p_123474_.setItem(pStack);
                });
            }
        });
        DispenserBlock.registerBehavior(Items.EXPERIENCE_BOTTLE, new AbstractProjectileDispenseBehavior()
        {
            protected Projectile getProjectile(Level pLevel, Position pPosition, ItemStack pStack)
            {
                return Util.make(new ThrownExperienceBottle(pLevel, pPosition.x(), pPosition.y(), pPosition.z()), (p_123483_) ->
                {
                    p_123483_.setItem(pStack);
                });
            }
            protected float getUncertainty()
            {
                return super.getUncertainty() * 0.5F;
            }
            protected float getPower()
            {
                return super.getPower() * 1.25F;
            }
        });
        DispenserBlock.registerBehavior(Items.SPLASH_POTION, new DispenseItemBehavior()
        {
            public ItemStack dispense(BlockSource p_123491_, ItemStack p_123492_)
            {
                return (new AbstractProjectileDispenseBehavior()
                {
                    protected Projectile getProjectile(Level pLevel, Position pPosition, ItemStack pStack)
                    {
                        return Util.make(new ThrownPotion(pLevel, pPosition.x(), pPosition.y(), pPosition.z()), (p_123499_) ->
                        {
                            p_123499_.setItem(pStack);
                        });
                    }
                    protected float getUncertainty()
                    {
                        return super.getUncertainty() * 0.5F;
                    }
                    protected float getPower()
                    {
                        return super.getPower() * 1.25F;
                    }
                }).dispense(p_123491_, p_123492_);
            }
        });
        DispenserBlock.registerBehavior(Items.LINGERING_POTION, new DispenseItemBehavior()
        {
            public ItemStack dispense(BlockSource p_123507_, ItemStack p_123508_)
            {
                return (new AbstractProjectileDispenseBehavior()
                {
                    protected Projectile getProjectile(Level pLevel, Position pPosition, ItemStack pStack)
                    {
                        return Util.make(new ThrownPotion(pLevel, pPosition.x(), pPosition.y(), pPosition.z()), (p_123515_) ->
                        {
                            p_123515_.setItem(pStack);
                        });
                    }
                    protected float getUncertainty()
                    {
                        return super.getUncertainty() * 0.5F;
                    }
                    protected float getPower()
                    {
                        return super.getPower() * 1.25F;
                    }
                }).dispense(p_123507_, p_123508_);
            }
        });
        DefaultDispenseItemBehavior defaultdispenseitembehavior = new DefaultDispenseItemBehavior()
        {
            public ItemStack execute(BlockSource pSource, ItemStack pStack)
            {
                Direction direction = pSource.getBlockState().getValue(DispenserBlock.FACING);
                EntityType<?> entitytype = ((SpawnEggItem)pStack.getItem()).getType(pStack.getTag());

                try
                {
                    entitytype.spawn(pSource.getLevel(), pStack, (Player)null, pSource.getPos().relative(direction), MobSpawnType.DISPENSER, direction != Direction.UP, false);
                }
                catch (Exception exception)
                {
                    LOGGER.error("Error while dispensing spawn egg from dispenser at {}", pSource.getPos(), exception);
                    return ItemStack.EMPTY;
                }

                pStack.shrink(1);
                pSource.getLevel().gameEvent(GameEvent.ENTITY_PLACE, pSource.getPos());
                return pStack;
            }
        };

        for (SpawnEggItem spawneggitem : SpawnEggItem.eggs())
        {
            DispenserBlock.registerBehavior(spawneggitem, defaultdispenseitembehavior);
        }

        DispenserBlock.registerBehavior(Items.ARMOR_STAND, new DefaultDispenseItemBehavior()
        {
            public ItemStack execute(BlockSource pSource, ItemStack pStack)
            {
                Direction direction = pSource.getBlockState().getValue(DispenserBlock.FACING);
                BlockPos blockpos = pSource.getPos().relative(direction);
                Level level = pSource.getLevel();
                ArmorStand armorstand = new ArmorStand(level, (double)blockpos.getX() + 0.5D, (double)blockpos.getY(), (double)blockpos.getZ() + 0.5D);
                EntityType.updateCustomEntityTag(level, (Player)null, armorstand, pStack.getTag());
                armorstand.setYRot(direction.toYRot());
                level.addFreshEntity(armorstand);
                pStack.shrink(1);
                return pStack;
            }
        });
        DispenserBlock.registerBehavior(Items.SADDLE, new OptionalDispenseItemBehavior()
        {
            public ItemStack execute(BlockSource pSource, ItemStack pStack)
            {
                BlockPos blockpos = pSource.getPos().relative(pSource.getBlockState().getValue(DispenserBlock.FACING));
                List<LivingEntity> list = pSource.getLevel().getEntitiesOfClass(LivingEntity.class, new AABB(blockpos), (p_123527_) ->
                {
                    if (!(p_123527_ instanceof Saddleable))
                    {
                        return false;
                    }
                    else {
                        Saddleable saddleable = (Saddleable)p_123527_;
                        return !saddleable.isSaddled() && saddleable.isSaddleable();
                    }
                });

                if (!list.isEmpty())
                {
                    ((Saddleable)list.get(0)).equipSaddle(SoundSource.BLOCKS);
                    pStack.shrink(1);
                    this.setSuccess(true);
                    return pStack;
                }
                else
                {
                    return super.execute(pSource, pStack);
                }
            }
        });
        DefaultDispenseItemBehavior defaultdispenseitembehavior1 = new OptionalDispenseItemBehavior()
        {
            protected ItemStack execute(BlockSource pSource, ItemStack pStack)
            {
                BlockPos blockpos = pSource.getPos().relative(pSource.getBlockState().getValue(DispenserBlock.FACING));

                for (AbstractHorse abstracthorse : pSource.getLevel().getEntitiesOfClass(AbstractHorse.class, new AABB(blockpos), (p_123533_) ->
            {
                return p_123533_.isAlive() && p_123533_.canWearArmor();
                }))
                {
                    if (abstracthorse.isArmor(pStack) && !abstracthorse.isWearingArmor() && abstracthorse.isTamed())
                    {
                        abstracthorse.getSlot(401).set(pStack.split(1));
                        this.setSuccess(true);
                        return pStack;
                    }
                }
                return super.execute(pSource, pStack);
            }
        };
        DispenserBlock.registerBehavior(Items.LEATHER_HORSE_ARMOR, defaultdispenseitembehavior1);
        DispenserBlock.registerBehavior(Items.IRON_HORSE_ARMOR, defaultdispenseitembehavior1);
        DispenserBlock.registerBehavior(Items.GOLDEN_HORSE_ARMOR, defaultdispenseitembehavior1);
        DispenserBlock.registerBehavior(Items.DIAMOND_HORSE_ARMOR, defaultdispenseitembehavior1);
        DispenserBlock.registerBehavior(Items.WHITE_CARPET, defaultdispenseitembehavior1);
        DispenserBlock.registerBehavior(Items.ORANGE_CARPET, defaultdispenseitembehavior1);
        DispenserBlock.registerBehavior(Items.CYAN_CARPET, defaultdispenseitembehavior1);
        DispenserBlock.registerBehavior(Items.BLUE_CARPET, defaultdispenseitembehavior1);
        DispenserBlock.registerBehavior(Items.BROWN_CARPET, defaultdispenseitembehavior1);
        DispenserBlock.registerBehavior(Items.BLACK_CARPET, defaultdispenseitembehavior1);
        DispenserBlock.registerBehavior(Items.GRAY_CARPET, defaultdispenseitembehavior1);
        DispenserBlock.registerBehavior(Items.GREEN_CARPET, defaultdispenseitembehavior1);
        DispenserBlock.registerBehavior(Items.LIGHT_BLUE_CARPET, defaultdispenseitembehavior1);
        DispenserBlock.registerBehavior(Items.LIGHT_GRAY_CARPET, defaultdispenseitembehavior1);
        DispenserBlock.registerBehavior(Items.LIME_CARPET, defaultdispenseitembehavior1);
        DispenserBlock.registerBehavior(Items.MAGENTA_CARPET, defaultdispenseitembehavior1);
        DispenserBlock.registerBehavior(Items.PINK_CARPET, defaultdispenseitembehavior1);
        DispenserBlock.registerBehavior(Items.PURPLE_CARPET, defaultdispenseitembehavior1);
        DispenserBlock.registerBehavior(Items.RED_CARPET, defaultdispenseitembehavior1);
        DispenserBlock.registerBehavior(Items.YELLOW_CARPET, defaultdispenseitembehavior1);
        DispenserBlock.registerBehavior(Items.CHEST, new OptionalDispenseItemBehavior()
        {
            public ItemStack execute(BlockSource pSource, ItemStack pStack)
            {
                BlockPos blockpos = pSource.getPos().relative(pSource.getBlockState().getValue(DispenserBlock.FACING));

                for (AbstractChestedHorse abstractchestedhorse : pSource.getLevel().getEntitiesOfClass(AbstractChestedHorse.class, new AABB(blockpos), (p_123539_) ->
            {
                return p_123539_.isAlive() && !p_123539_.hasChest();
                }))
                {
                    if (abstractchestedhorse.isTamed() && abstractchestedhorse.getSlot(499).set(pStack))
                    {
                        pStack.shrink(1);
                        this.setSuccess(true);
                        return pStack;
                    }
                }
                return super.execute(pSource, pStack);
            }
        });
        DispenserBlock.registerBehavior(Items.FIREWORK_ROCKET, new DefaultDispenseItemBehavior()
        {
            public ItemStack execute(BlockSource pSource, ItemStack pStack)
            {
                Direction direction = pSource.getBlockState().getValue(DispenserBlock.FACING);
                FireworkRocketEntity fireworkrocketentity = new FireworkRocketEntity(pSource.getLevel(), pStack, pSource.x(), pSource.y(), pSource.x(), true);
                DispenseItemBehavior.setEntityPokingOutOfBlock(pSource, fireworkrocketentity, direction);
                fireworkrocketentity.shoot((double)direction.getStepX(), (double)direction.getStepY(), (double)direction.getStepZ(), 0.5F, 1.0F);
                pSource.getLevel().addFreshEntity(fireworkrocketentity);
                pStack.shrink(1);
                return pStack;
            }
            protected void playSound(BlockSource pSource)
            {
                pSource.getLevel().levelEvent(1004, pSource.getPos(), 0);
            }
        });
        DispenserBlock.registerBehavior(Items.FIRE_CHARGE, new DefaultDispenseItemBehavior()
        {
            public ItemStack execute(BlockSource pSource, ItemStack pStack)
            {
                Direction direction = pSource.getBlockState().getValue(DispenserBlock.FACING);
                Position position = DispenserBlock.getDispensePosition(pSource);
                double d0 = position.x() + (double)((float)direction.getStepX() * 0.3F);
                double d1 = position.y() + (double)((float)direction.getStepY() * 0.3F);
                double d2 = position.z() + (double)((float)direction.getStepZ() * 0.3F);
                Level level = pSource.getLevel();
                Random random = level.random;
                double d3 = random.nextGaussian() * 0.05D + (double)direction.getStepX();
                double d4 = random.nextGaussian() * 0.05D + (double)direction.getStepY();
                double d5 = random.nextGaussian() * 0.05D + (double)direction.getStepZ();
                SmallFireball smallfireball = new SmallFireball(level, d0, d1, d2, d3, d4, d5);
                level.addFreshEntity(Util.make(smallfireball, (p_123552_) ->
                {
                    p_123552_.setItem(pStack);
                }));
                pStack.shrink(1);
                return pStack;
            }
            protected void playSound(BlockSource pSource)
            {
                pSource.getLevel().levelEvent(1018, pSource.getPos(), 0);
            }
        });
        DispenserBlock.registerBehavior(Items.OAK_BOAT, new BoatDispenseItemBehavior(Boat.Type.OAK));
        DispenserBlock.registerBehavior(Items.SPRUCE_BOAT, new BoatDispenseItemBehavior(Boat.Type.SPRUCE));
        DispenserBlock.registerBehavior(Items.BIRCH_BOAT, new BoatDispenseItemBehavior(Boat.Type.BIRCH));
        DispenserBlock.registerBehavior(Items.JUNGLE_BOAT, new BoatDispenseItemBehavior(Boat.Type.JUNGLE));
        DispenserBlock.registerBehavior(Items.DARK_OAK_BOAT, new BoatDispenseItemBehavior(Boat.Type.DARK_OAK));
        DispenserBlock.registerBehavior(Items.ACACIA_BOAT, new BoatDispenseItemBehavior(Boat.Type.ACACIA));
        DispenseItemBehavior dispenseitembehavior1 = new DefaultDispenseItemBehavior()
        {
            private final DefaultDispenseItemBehavior defaultDispenseItemBehavior = new DefaultDispenseItemBehavior();
            public ItemStack execute(BlockSource pSource, ItemStack pStack)
            {
                DispensibleContainerItem dispensiblecontaineritem = (DispensibleContainerItem)pStack.getItem();
                BlockPos blockpos = pSource.getPos().relative(pSource.getBlockState().getValue(DispenserBlock.FACING));
                Level level = pSource.getLevel();

                if (dispensiblecontaineritem.emptyContents((Player)null, level, blockpos, (BlockHitResult)null))
                {
                    dispensiblecontaineritem.checkExtraContent((Player)null, level, pStack, blockpos);
                    return new ItemStack(Items.BUCKET);
                }
                else
                {
                    return this.defaultDispenseItemBehavior.dispense(pSource, pStack);
                }
            }
        };
        DispenserBlock.registerBehavior(Items.LAVA_BUCKET, dispenseitembehavior1);
        DispenserBlock.registerBehavior(Items.WATER_BUCKET, dispenseitembehavior1);
        DispenserBlock.registerBehavior(Items.POWDER_SNOW_BUCKET, dispenseitembehavior1);
        DispenserBlock.registerBehavior(Items.SALMON_BUCKET, dispenseitembehavior1);
        DispenserBlock.registerBehavior(Items.COD_BUCKET, dispenseitembehavior1);
        DispenserBlock.registerBehavior(Items.PUFFERFISH_BUCKET, dispenseitembehavior1);
        DispenserBlock.registerBehavior(Items.TROPICAL_FISH_BUCKET, dispenseitembehavior1);
        DispenserBlock.registerBehavior(Items.AXOLOTL_BUCKET, dispenseitembehavior1);
        DispenserBlock.registerBehavior(Items.BUCKET, new DefaultDispenseItemBehavior()
        {
            private final DefaultDispenseItemBehavior defaultDispenseItemBehavior = new DefaultDispenseItemBehavior();
            public ItemStack execute(BlockSource pSource, ItemStack pStack)
            {
                LevelAccessor levelaccessor = pSource.getLevel();
                BlockPos blockpos = pSource.getPos().relative(pSource.getBlockState().getValue(DispenserBlock.FACING));
                BlockState blockstate = levelaccessor.getBlockState(blockpos);
                Block block = blockstate.getBlock();

                if (block instanceof BucketPickup)
                {
                    ItemStack itemstack = ((BucketPickup)block).pickupBlock(levelaccessor, blockpos, blockstate);

                    if (itemstack.isEmpty())
                    {
                        return super.execute(pSource, pStack);
                    }
                    else
                    {
                        levelaccessor.gameEvent((Entity)null, GameEvent.FLUID_PICKUP, blockpos);
                        Item item = itemstack.getItem();
                        pStack.shrink(1);

                        if (pStack.isEmpty())
                        {
                            return new ItemStack(item);
                        }
                        else
                        {
                            if (pSource.<DispenserBlockEntity>getEntity().addItem(new ItemStack(item)) < 0)
                            {
                                this.defaultDispenseItemBehavior.dispense(pSource, new ItemStack(item));
                            }

                            return pStack;
                        }
                    }
                }
                else
                {
                    return super.execute(pSource, pStack);
                }
            }
        });
        DispenserBlock.registerBehavior(Items.FLINT_AND_STEEL, new OptionalDispenseItemBehavior()
        {
            protected ItemStack execute(BlockSource pSource, ItemStack pStack)
            {
                Level level = pSource.getLevel();
                this.setSuccess(true);
                Direction direction = pSource.getBlockState().getValue(DispenserBlock.FACING);
                BlockPos blockpos = pSource.getPos().relative(direction);
                BlockState blockstate = level.getBlockState(blockpos);

                if (BaseFireBlock.canBePlacedAt(level, blockpos, direction))
                {
                    level.setBlockAndUpdate(blockpos, BaseFireBlock.getState(level, blockpos));
                    level.gameEvent((Entity)null, GameEvent.BLOCK_PLACE, blockpos);
                }
                else if (!CampfireBlock.canLight(blockstate) && !CandleBlock.canLight(blockstate) && !CandleCakeBlock.canLight(blockstate))
                {
                    if (blockstate.getBlock() instanceof TntBlock)
                    {
                        TntBlock.explode(level, blockpos);
                        level.removeBlock(blockpos, false);
                    }
                    else
                    {
                        this.setSuccess(false);
                    }
                }
                else
                {
                    level.setBlockAndUpdate(blockpos, blockstate.setValue(BlockStateProperties.LIT, Boolean.valueOf(true)));
                    level.gameEvent((Entity)null, GameEvent.BLOCK_CHANGE, blockpos);
                }

                if (this.isSuccess() && pStack.hurt(1, level.random, (ServerPlayer)null))
                {
                    pStack.setCount(0);
                }

                return pStack;
            }
        });
        DispenserBlock.registerBehavior(Items.BONE_MEAL, new OptionalDispenseItemBehavior()
        {
            protected ItemStack execute(BlockSource pSource, ItemStack pStack)
            {
                this.setSuccess(true);
                Level level = pSource.getLevel();
                BlockPos blockpos = pSource.getPos().relative(pSource.getBlockState().getValue(DispenserBlock.FACING));

                if (!BoneMealItem.growCrop(pStack, level, blockpos) && !BoneMealItem.growWaterPlant(pStack, level, blockpos, (Direction)null))
                {
                    this.setSuccess(false);
                }
                else if (!level.isClientSide)
                {
                    level.levelEvent(1505, blockpos, 0);
                }

                return pStack;
            }
        });
        DispenserBlock.registerBehavior(Blocks.TNT, new DefaultDispenseItemBehavior()
        {
            protected ItemStack execute(BlockSource pSource, ItemStack pStack)
            {
                Level level = pSource.getLevel();
                BlockPos blockpos = pSource.getPos().relative(pSource.getBlockState().getValue(DispenserBlock.FACING));
                PrimedTnt primedtnt = new PrimedTnt(level, (double)blockpos.getX() + 0.5D, (double)blockpos.getY(), (double)blockpos.getZ() + 0.5D, (LivingEntity)null);
                level.addFreshEntity(primedtnt);
                level.playSound((Player)null, primedtnt.getX(), primedtnt.getY(), primedtnt.getZ(), SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.gameEvent((Entity)null, GameEvent.ENTITY_PLACE, blockpos);
                pStack.shrink(1);
                return pStack;
            }
        });
        DispenseItemBehavior dispenseitembehavior = new OptionalDispenseItemBehavior()
        {
            protected ItemStack execute(BlockSource pSource, ItemStack pStack)
            {
                this.setSuccess(ArmorItem.dispenseArmor(pSource, pStack));
                return pStack;
            }
        };
        DispenserBlock.registerBehavior(Items.CREEPER_HEAD, dispenseitembehavior);
        DispenserBlock.registerBehavior(Items.ZOMBIE_HEAD, dispenseitembehavior);
        DispenserBlock.registerBehavior(Items.DRAGON_HEAD, dispenseitembehavior);
        DispenserBlock.registerBehavior(Items.SKELETON_SKULL, dispenseitembehavior);
        DispenserBlock.registerBehavior(Items.PLAYER_HEAD, dispenseitembehavior);
        DispenserBlock.registerBehavior(Items.WITHER_SKELETON_SKULL, new OptionalDispenseItemBehavior()
        {
            protected ItemStack execute(BlockSource pSource, ItemStack pStack)
            {
                Level level = pSource.getLevel();
                Direction direction = pSource.getBlockState().getValue(DispenserBlock.FACING);
                BlockPos blockpos = pSource.getPos().relative(direction);

                if (level.isEmptyBlock(blockpos) && WitherSkullBlock.canSpawnMob(level, blockpos, pStack))
                {
                    level.setBlock(blockpos, Blocks.WITHER_SKELETON_SKULL.defaultBlockState().setValue(SkullBlock.ROTATION, Integer.valueOf(direction.getAxis() == Direction.Axis.Y ? 0 : direction.getOpposite().get2DDataValue() * 4)), 3);
                    level.gameEvent((Entity)null, GameEvent.BLOCK_PLACE, blockpos);
                    BlockEntity blockentity = level.getBlockEntity(blockpos);

                    if (blockentity instanceof SkullBlockEntity)
                    {
                        WitherSkullBlock.checkSpawn(level, blockpos, (SkullBlockEntity)blockentity);
                    }

                    pStack.shrink(1);
                    this.setSuccess(true);
                }
                else
                {
                    this.setSuccess(ArmorItem.dispenseArmor(pSource, pStack));
                }

                return pStack;
            }
        });
        DispenserBlock.registerBehavior(Blocks.CARVED_PUMPKIN, new OptionalDispenseItemBehavior()
        {
            protected ItemStack execute(BlockSource pSource, ItemStack pStack)
            {
                Level level = pSource.getLevel();
                BlockPos blockpos = pSource.getPos().relative(pSource.getBlockState().getValue(DispenserBlock.FACING));
                CarvedPumpkinBlock carvedpumpkinblock = (CarvedPumpkinBlock)Blocks.CARVED_PUMPKIN;

                if (level.isEmptyBlock(blockpos) && carvedpumpkinblock.canSpawnGolem(level, blockpos))
                {
                    if (!level.isClientSide)
                    {
                        level.setBlock(blockpos, carvedpumpkinblock.defaultBlockState(), 3);
                        level.gameEvent((Entity)null, GameEvent.BLOCK_PLACE, blockpos);
                    }

                    pStack.shrink(1);
                    this.setSuccess(true);
                }
                else
                {
                    this.setSuccess(ArmorItem.dispenseArmor(pSource, pStack));
                }

                return pStack;
            }
        });
        DispenserBlock.registerBehavior(Blocks.SHULKER_BOX.asItem(), new ShulkerBoxDispenseBehavior());

        for (DyeColor dyecolor : DyeColor.values())
        {
            DispenserBlock.registerBehavior(ShulkerBoxBlock.getBlockByColor(dyecolor).asItem(), new ShulkerBoxDispenseBehavior());
        }

        DispenserBlock.registerBehavior(Items.GLASS_BOTTLE.asItem(), new OptionalDispenseItemBehavior()
        {
            private final DefaultDispenseItemBehavior defaultDispenseItemBehavior = new DefaultDispenseItemBehavior();
            private ItemStack takeLiquid(BlockSource pSource, ItemStack pEmpty, ItemStack pFilled)
            {
                pEmpty.shrink(1);

                if (pEmpty.isEmpty())
                {
                    pSource.getLevel().gameEvent((Entity)null, GameEvent.FLUID_PICKUP, pSource.getPos());
                    return pFilled.copy();
                }
                else
                {
                    if (pSource.<DispenserBlockEntity>getEntity().addItem(pFilled.copy()) < 0)
                    {
                        this.defaultDispenseItemBehavior.dispense(pSource, pFilled.copy());
                    }

                    return pEmpty;
                }
            }
            public ItemStack execute(BlockSource pSource, ItemStack pStack)
            {
                this.setSuccess(false);
                ServerLevel serverlevel = pSource.getLevel();
                BlockPos blockpos = pSource.getPos().relative(pSource.getBlockState().getValue(DispenserBlock.FACING));
                BlockState blockstate = serverlevel.getBlockState(blockpos);

                if (blockstate.is(BlockTags.BEEHIVES, (p_123442_) ->
            {
                return p_123442_.hasProperty(BeehiveBlock.HONEY_LEVEL);
                }) && blockstate.getValue(BeehiveBlock.HONEY_LEVEL) >= 5)
                {
                    ((BeehiveBlock)blockstate.getBlock()).releaseBeesAndResetHoneyLevel(serverlevel, blockstate, blockpos, (Player)null, BeehiveBlockEntity.BeeReleaseStatus.BEE_RELEASED);
                    this.setSuccess(true);
                    return this.takeLiquid(pSource, pStack, new ItemStack(Items.HONEY_BOTTLE));
                }
                else if (serverlevel.getFluidState(blockpos).is(FluidTags.WATER))
                {
                    this.setSuccess(true);
                    return this.takeLiquid(pSource, pStack, PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER));
                }
                else
                {
                    return super.execute(pSource, pStack);
                }
            }
        });
        DispenserBlock.registerBehavior(Items.GLOWSTONE, new OptionalDispenseItemBehavior()
        {
            public ItemStack execute(BlockSource pSource, ItemStack pStack)
            {
                Direction direction = pSource.getBlockState().getValue(DispenserBlock.FACING);
                BlockPos blockpos = pSource.getPos().relative(direction);
                Level level = pSource.getLevel();
                BlockState blockstate = level.getBlockState(blockpos);
                this.setSuccess(true);

                if (blockstate.is(Blocks.RESPAWN_ANCHOR))
                {
                    if (blockstate.getValue(RespawnAnchorBlock.CHARGE) != 4)
                    {
                        RespawnAnchorBlock.charge(level, blockpos, blockstate);
                        pStack.shrink(1);
                    }
                    else
                    {
                        this.setSuccess(false);
                    }

                    return pStack;
                }
                else
                {
                    return super.execute(pSource, pStack);
                }
            }
        });
        DispenserBlock.registerBehavior(Items.SHEARS.asItem(), new ShearsDispenseItemBehavior());
        DispenserBlock.registerBehavior(Items.HONEYCOMB, new OptionalDispenseItemBehavior()
        {
            public ItemStack execute(BlockSource p_175747_, ItemStack p_175748_)
            {
                BlockPos blockpos = p_175747_.getPos().relative(p_175747_.getBlockState().getValue(DispenserBlock.FACING));
                Level level = p_175747_.getLevel();
                BlockState blockstate = level.getBlockState(blockpos);
                Optional<BlockState> optional = HoneycombItem.getWaxed(blockstate);

                if (optional.isPresent())
                {
                    level.setBlockAndUpdate(blockpos, optional.get());
                    level.levelEvent(3003, blockpos, 0);
                    p_175748_.shrink(1);
                    this.setSuccess(true);
                    return p_175748_;
                }
                else
                {
                    return super.execute(p_175747_, p_175748_);
                }
            }
        });
    }

    static void setEntityPokingOutOfBlock(BlockSource pSource, Entity pEntity, Direction pDirection)
    {
        pEntity.setPos(pSource.x() + (double)pDirection.getStepX() * (0.5000099999997474D - (double)pEntity.getBbWidth() / 2.0D), pSource.y() + (double)pDirection.getStepY() * (0.5000099999997474D - (double)pEntity.getBbHeight() / 2.0D) - (double)pEntity.getBbHeight() / 2.0D, pSource.z() + (double)pDirection.getStepZ() * (0.5000099999997474D - (double)pEntity.getBbWidth() / 2.0D));
    }
}
