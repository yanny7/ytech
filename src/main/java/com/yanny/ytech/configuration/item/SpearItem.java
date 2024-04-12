package com.yanny.ytech.configuration.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.yanny.ytech.YTechMod;
import com.yanny.ytech.configuration.MaterialItemType;
import com.yanny.ytech.configuration.MaterialType;
import com.yanny.ytech.configuration.SimpleEntityType;
import com.yanny.ytech.configuration.Utils;
import com.yanny.ytech.configuration.entity.SpearEntity;
import com.yanny.ytech.configuration.renderer.YTechRenderer;
import com.yanny.ytech.registration.Registration;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.Vanishable;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class SpearItem extends Item implements Vanishable {
    public static final ResourceLocation THROWING_PREDICATE = Utils.modLoc("throwing");
    private final SpearType spearType;

    @NotNull private final Multimap<Attribute, AttributeModifier> defaultModifiers;

    public SpearItem(SpearType spearType) {
        super(new Properties().durability(spearType.durability));
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Tool modifier", spearType.baseDamage, AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Tool modifier", spearType.attackSpeed, AttributeModifier.Operation.ADDITION));
        this.defaultModifiers = builder.build();
        this.spearType = spearType;
    }

    @Override
    public boolean canAttackBlock(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player) {
        return !player.isCreative();
    }

    @NotNull
    @Override
    public UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return UseAnim.SPEAR;
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack) {
        return 36000;
    }

    @Override
    public void releaseUsing(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity entity, int timeLeft) {
        if (entity instanceof Player player) {
            int throwTime = this.getUseDuration(stack) - timeLeft;

            if (throwTime >= spearType.throwThreshold) {
                int riptideLevel = EnchantmentHelper.getRiptide(stack);

                if (riptideLevel <= 0 || player.isInWaterOrRain()) {
                    if (!level.isClientSide) {
                        stack.hurtAndBreak(1, player, (playerEntity) -> playerEntity.broadcastBreakEvent(entity.getUsedItemHand()));

                        if (riptideLevel == 0) {
                            SpearEntity spearEntity = new SpearEntity(level, player, stack, spearType);
                            spearEntity.shootFromRotation(
                                    player,
                                    player.getXRot(),
                                    player.getYRot(),
                                    0.0F,
                                    spearType.shootPower + (float)riptideLevel * 0.5F,
                                    spearType.accuracy
                            );

                            if (player.getAbilities().instabuild) {
                                spearEntity.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                            }

                            level.addFreshEntity(spearEntity);
                            level.playSound(null, spearEntity, SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1.0F, 1.0F);

                            if (!player.getAbilities().instabuild) {
                                player.getInventory().removeItem(stack);
                            }
                        }
                    }

                    player.awardStat(Stats.ITEM_USED.get(this));

                    if (riptideLevel > 0) {
                        float f7 = player.getYRot();
                        float f = player.getXRot();
                        float f1 = -Mth.sin(f7 * ((float)Math.PI / 180F)) * Mth.cos(f * ((float)Math.PI / 180F));
                        float f2 = -Mth.sin(f * ((float)Math.PI / 180F));
                        float f3 = Mth.cos(f7 * ((float)Math.PI / 180F)) * Mth.cos(f * ((float)Math.PI / 180F));
                        float f4 = Mth.sqrt(f1 * f1 + f2 * f2 + f3 * f3);
                        float f5 = 3.0F * ((1.0F + (float)riptideLevel) / 4.0F);

                        f1 *= f5 / f4;
                        f2 *= f5 / f4;
                        f3 *= f5 / f4;
                        player.push(f1, f2, f3);
                        player.startAutoSpinAttack(20);

                        if (player.onGround()) {
                            player.move(MoverType.SELF, new Vec3(0.0D, 1.1999999F, 0.0D));
                        }

                        SoundEvent soundevent;

                        if (riptideLevel >= 3) {
                            soundevent = SoundEvents.TRIDENT_RIPTIDE_3;
                        } else if (riptideLevel == 2) {
                            soundevent = SoundEvents.TRIDENT_RIPTIDE_2;
                        } else {
                            soundevent = SoundEvents.TRIDENT_RIPTIDE_1;
                        }

                        level.playSound(null, player, soundevent, SoundSource.PLAYERS, 1.0F, 1.0F);
                    }
                }
            }
        }
    }

    @NotNull
    @Override
    public InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (itemstack.getDamageValue() >= itemstack.getMaxDamage()) {
            return InteractionResultHolder.fail(itemstack);
        } else if (EnchantmentHelper.getRiptide(itemstack) > 0 && !player.isInWaterOrRain()) {
            return InteractionResultHolder.fail(itemstack);
        } else {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(itemstack);
        }
    }

    @Override
    public boolean hurtEnemy(@NotNull ItemStack stack, @NotNull LivingEntity target, @NotNull LivingEntity attacker) {
        stack.hurtAndBreak(1, attacker, (player) -> player.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        return true;
    }

    @Override
    public boolean mineBlock(@NotNull ItemStack stack, @NotNull Level level, @NotNull BlockState state, @NotNull BlockPos pos, @NotNull LivingEntity livingEntity) {
        if ((double)state.getDestroySpeed(level, pos) != 0.0D) {
            stack.hurtAndBreak(2, livingEntity, (player) -> player.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        }

        return true;
    }

    @SuppressWarnings("deprecation")
    @NotNull
    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(@NotNull EquipmentSlot equipmentSlot) {
        return equipmentSlot == EquipmentSlot.MAINHAND ? this.defaultModifiers : super.getDefaultAttributeModifiers(equipmentSlot);
    }

    @Override
    public int getEnchantmentValue(@NotNull ItemStack itemStack) {
        return 1;
    }

    @Override
    public void initializeClient(@NotNull Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return YTechRenderer.INSTANCE;
            }
        });
    }

    public enum SpearType {
        FLINT(MaterialType.FLINT, SimpleEntityType.FLINT_SPEAR, 20, 4.0f, -3.5f, 2.0f, 1.5f, 10),
        COPPER(MaterialType.COPPER, SimpleEntityType.COPPER_SPEAR, 40, 4.5f, -3.4f, 2.1f, 1.3f, 10),
        BRONZE(MaterialType.BRONZE, SimpleEntityType.BRONZE_SPEAR, 80, 5.0f, -3.3F, 2.3f, 1.2f, 10),
        IRON(MaterialType.IRON, SimpleEntityType.IRON_SPEAR, 160, 6.0f, -3.2F, 2.5f, 1.1f, 10),
        ;

        public static final ResourceLocation TEXTURE_LOCATION = Utils.modLoc("textures/entity/spear.png");
        public static final Map<SimpleEntityType, SpearType> BY_ENTITY_TYPE = new HashMap<>();
        public static final Map<MaterialType, SpearType> BY_MATERIAL_TYPE = new HashMap<>();

        static {
            for (SpearType spearType : SpearType.values()) {
                BY_ENTITY_TYPE.put(spearType.entityType, spearType);
                BY_MATERIAL_TYPE.put(spearType.materialType, spearType);
            }
        }

        public final MaterialType materialType;
        public final SimpleEntityType entityType;
        public final int durability;
        public final float baseDamage;
        public final float attackSpeed;
        public final float shootPower;
        public final float accuracy;
        public final int throwThreshold;
        public final ModelLayerLocation layerLocation;
        public final ModelResourceLocation modelLocation;
        public final ModelResourceLocation modelInHandLocation;

        SpearType(MaterialType materialType, SimpleEntityType entityType, int durability, float baseDamage, float attackSpeed, float shootPower, float accuracy, int throwThreshold) {
            String key = Registration.HOLDER.items().get(MaterialItemType.SPEAR).get(materialType).key;
            this.materialType = materialType;
            this.entityType = entityType;
            this.durability = durability;
            this.baseDamage = baseDamage;
            this.attackSpeed = attackSpeed;
            this.shootPower = shootPower;
            this.accuracy = accuracy;
            this.throwThreshold = throwThreshold;
            layerLocation = new ModelLayerLocation(Utils.modLoc(key), "main");
            modelLocation = new ModelResourceLocation(YTechMod.MOD_ID, key, "inventory");
            modelInHandLocation = new ModelResourceLocation(YTechMod.MOD_ID, key + "_in_hand", "inventory");
        }
    }
}
