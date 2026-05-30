package com.goo.goo_lib.common.event;

import com.goo.goo_lib.common.Attributes;
import com.goo.goo_lib.common.GooLib;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
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
public class AttributeEvents {

    @SubscribeEvent
    public static void addAttributes(EntityAttributeModificationEvent event) {
        Attributes.ATTRIBUTES.getEntries().forEach(attributeDeferredHolder -> {
            if (!event.has(EntityType.PLAYER, attributeDeferredHolder))
                event.add(EntityType.PLAYER, attributeDeferredHolder);
        });
    }

    @SubscribeEvent
    public static void onLivingHurtPost(LivingDamageEvent.Post event) {
        DamageSource source = event.getSource();

        // Lifesteal
        if (source.is(DamageTypes.PLAYER_ATTACK)) {
            if (source.getDirectEntity() == source.getEntity()) {
                if (source.getEntity() instanceof Player player) {
                    player.heal((float) (event.getNewDamage() * player.getAttributeValue(Attributes.LIFESTEAL)));
                }
            }
        }
    }

    @SubscribeEvent
    public static void onCrit(CriticalHitEvent event) {
        if (event.isVanillaCritical()) {
            event.setDamageMultiplier((float) (event.getEntity().getAttributeValue(Attributes.CRITICAL_DAMAGE)));
        }
    }

    private static boolean canBenefitFromDrawSpeed(ItemStack stack) {
        return stack.getItem() instanceof ProjectileWeaponItem || stack.getItem() instanceof TridentItem;
    }

    /**
     * This event handler is the implementation for {@link Attributes#DRAW_SPEED}.<br>
     * Each full point of draw speed provides an extra using tick per game tick.<br>
     * Each partial point of draw speed provides an extra using tick periodically.<br>
     * Adapted from <a href="https://github.com/Shadows-of-Fire/Apothic-Attributes/blob/1.21/src/main/java/dev/shadowsoffire/apothic_attributes/impl/AttributeEvents.java#L74">Apotheosis</a>
     */
    @SubscribeEvent
    public static void drawSpeed(LivingEntityUseItemEvent.Tick e) {
        if (e.getEntity() instanceof Player player) {
            double drawSpeed = player.getAttributeValue(Attributes.DRAW_SPEED);
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
                    if (player.tickCount % mod == 0) {
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
                if (player.tickCount % mod != 0) {
                    e.setDuration(e.getDuration() + 1);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLivingHeal(LivingHealEvent event) {
        if (event.getEntity() instanceof Player player) {
            event.setAmount((float) (event.getAmount() * player.getAttributeValue(Attributes.HEALING_RECEIVED)));
            if (event.getAmount() <= 0) event.setCanceled(true);
        }
    }

    /**
     * Handles {@link Attributes#XP_GAIN}
     */

    @SubscribeEvent
    public static void onMobDropExp(LivingExperienceDropEvent event) {
        if (event.getAttackingPlayer() != null) {
            event.setDroppedExperience((int) (event.getDroppedExperience() * event.getAttackingPlayer().getAttributeValue(Attributes.XP_GAIN)));
        }
    }

    @SubscribeEvent
    public static void onBlockDrops(BlockDropsEvent event) {
        if (event.getBreaker() instanceof Player player) {
            event.setDroppedExperience((int) (event.getDroppedExperience() * player.getAttributeValue(Attributes.XP_GAIN)));
        }
    }

    @SubscribeEvent
    public static void onGrindstone(GrindstoneEvent.OnTakeItem event) {
        if (event.getPlayer() != null)
            event.setXp((int) (event.getXp() * event.getPlayer().getAttributeValue(Attributes.XP_GAIN)));
    }


}
