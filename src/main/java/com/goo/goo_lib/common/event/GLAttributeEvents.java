package com.goo.goo_lib.common.event;

import com.goo.goo_lib.common.registry.GLAttributes;
import com.goo.goo_lib.common.GooLib;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.TridentItem;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.GrindstoneEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;

@EventBusSubscriber(modid = GooLib.MOD_ID)
public class GLAttributeEvents {

    @SubscribeEvent
    public static void addAttributes(EntityAttributeModificationEvent event) {
        for (EntityType<? extends LivingEntity> type : event.getTypes()) {
            if (!event.has(type, GLAttributes.LAVA_MOVEMENT_EFFICIENCY)) {
                event.add(type, GLAttributes.LAVA_MOVEMENT_EFFICIENCY);
            }
            if (!event.has(type, GLAttributes.LIFESTEAL)) {
                event.add(type, GLAttributes.LIFESTEAL);
            }
            if (!event.has(type, GLAttributes.ARROW_GRAVITY)) {
                event.add(type, GLAttributes.ARROW_GRAVITY);
            }
            if (!event.has(type, GLAttributes.ARROW_DAMAGE)) {
                event.add(type, GLAttributes.ARROW_DAMAGE);
            }
            if (!event.has(type, GLAttributes.ARROW_VELOCITY)) {
                event.add(type, GLAttributes.ARROW_VELOCITY);
            }
            if (!event.has(type, GLAttributes.DRAW_SPEED)) {
                event.add(type, GLAttributes.DRAW_SPEED);
            }
            if (!event.has(type, GLAttributes.HEALING_RECEIVED)) {
                event.add(type, GLAttributes.HEALING_RECEIVED);
            }
        }

        if (!event.has(EntityType.PLAYER, GLAttributes.VILLAGER_REPUTATION)) {
            event.add(EntityType.PLAYER, GLAttributes.VILLAGER_REPUTATION);
        }
        if (!event.has(EntityType.PLAYER, GLAttributes.CRITICAL_DAMAGE)) {
            event.add(EntityType.PLAYER, GLAttributes.CRITICAL_DAMAGE);
        }
        if (!event.has(EntityType.PLAYER, GLAttributes.XP_GAIN)) {
            event.add(EntityType.PLAYER, GLAttributes.XP_GAIN);
        }
    }

    @SubscribeEvent
    public static void onLivingHurtPost(LivingDamageEvent.Post event) {
        DamageSource source = event.getSource();

        // Lifesteal
        if (source.is(DamageTypes.PLAYER_ATTACK) || source.is(DamageTypes.MOB_ATTACK) || source.is(DamageTypes.MOB_ATTACK_NO_AGGRO)) {
            if (source.getDirectEntity() == source.getEntity()) {
                if (source.getEntity() instanceof LivingEntity livingEntity)
                    livingEntity.heal((float) (event.getNewDamage() * livingEntity.getAttributeValue(GLAttributes.LIFESTEAL)));
            }
        }
    }

    @SubscribeEvent
    public static void onCrit(CriticalHitEvent event) {
        if (event.isVanillaCritical()) {
            event.setDamageMultiplier((float) (event.getEntity().getAttributeValue(GLAttributes.CRITICAL_DAMAGE)));
        }
    }

    private static boolean canBenefitFromDrawSpeed(ItemStack stack) {
        return stack.getItem() instanceof ProjectileWeaponItem || stack.getItem() instanceof TridentItem;
    }

    /**
     * This event handler is the implementation for {@link GLAttributes#DRAW_SPEED}.<br>
     * Each full point of draw speed provides an extra using tick per game tick.<br>
     * Each partial point of draw speed provides an extra using tick periodically.<br>
     * Adapted from <a href="https://github.com/Shadows-of-Fire/Apothic-Attributes/blob/1.21/src/main/java/dev/shadowsoffire/apothic_attributes/impl/AttributeEvents.java#L74">Apotheosis</a>
     */
    @SubscribeEvent
    public static void drawSpeed(LivingEntityUseItemEvent.Tick e) {
        LivingEntity livingEntity = e.getEntity();
        double drawSpeed = livingEntity.getAttributeValue(GLAttributes.DRAW_SPEED);
        if (drawSpeed == 1.0 || !canBenefitFromDrawSpeed(e.getItem())) return;

        // Speed up logic
        if (drawSpeed > 1.0) {
            double t = drawSpeed - 1.0;
            int extraTicks = (int) t;
            t -= extraTicks;

            if (extraTicks > 0) {
                e.setDuration(e.getDuration() - extraTicks); // Subtract more to speed up countdown
            }
            if (t > 0) {
                int mod = (int) Math.ceil(1.0 / t);
                if (livingEntity.tickCount % mod == 0) {
                    e.setDuration(e.getDuration() - 1);
                }
            }
            return;
        }

        // Slow down logic
        if (drawSpeed < 1.0 && drawSpeed > 0) {
            int mod = (int) Math.round(1.0 / drawSpeed); // 0.1 becomes 10, 0.25 becomes 4

            // Only let the tick pass if the modulo matches.
            // On every other tick, counteract Minecraft's natural subtraction by adding 1.
            if (livingEntity.tickCount % mod != 0) {
                e.setDuration(e.getDuration() + 1);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingHeal(LivingHealEvent event) {
        if (event.getEntity() instanceof Player player) {
            event.setAmount((float) (event.getAmount() * player.getAttributeValue(GLAttributes.HEALING_RECEIVED)));
            if (event.getAmount() <= 0) event.setCanceled(true);
        }
    }

    /**
     * Handles {@link GLAttributes#XP_GAIN}
     */

    @SubscribeEvent
    public static void onMobDropExp(LivingExperienceDropEvent event) {
        if (event.getAttackingPlayer() != null) {
            event.setDroppedExperience((int) (event.getDroppedExperience() * event.getAttackingPlayer().getAttributeValue(GLAttributes.XP_GAIN)));
        }
    }

    @SubscribeEvent
    public static void onBlockDrops(BlockDropsEvent event) {
        if (event.getBreaker() instanceof Player player) {
            event.setDroppedExperience((int) (event.getDroppedExperience() * player.getAttributeValue(GLAttributes.XP_GAIN)));
        }
    }

    @SubscribeEvent
    public static void onGrindstone(GrindstoneEvent.OnTakeItem event) {
        if (event.getPlayer() != null)
            event.setXp((int) (event.getXp() * event.getPlayer().getAttributeValue(GLAttributes.XP_GAIN)));
    }


}
