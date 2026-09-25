package com.goo.goo_lib.mixin;

import com.goo.goo_lib.common.registry.GLAttributes;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;

@Mixin(FishingRodItem.class)
public class FishingRodItemMixin {

    @WrapMethod(method = "use")
    private InteractionResultHolder<ItemStack> handleMultipleHooks(
            Level level, Player player, InteractionHand hand, Operation<InteractionResultHolder<ItemStack>> original
    ) {
        ItemStack itemstack = player.getItemInHand(hand);
        List<FishingHook> ownedHooks =  level.getEntitiesOfClass(
                FishingHook.class,
                // all hooks are discarded at >= 32 anyway
                player.getBoundingBox().inflate(33),
                hook -> hook.getPlayerOwner() == player && !hook.isRemoved() && hook.isAlive());

        if (!ownedHooks.isEmpty()) {
            if (!level.isClientSide) {
                int maxDamage = 0;

                for (FishingHook hook : ownedHooks) {
                    int damage = hook.retrieve(itemstack);
                    maxDamage = Math.max(maxDamage, damage);
                }

                ItemStack originalStack = itemstack.copy();
                itemstack.hurtAndBreak(maxDamage, player, LivingEntity.getSlotForHand(hand));
                if (itemstack.isEmpty()) {
                    EventHooks.onPlayerDestroyItem(player, originalStack, hand);
                }
            }

            level.playSound(
                null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.FISHING_BOBBER_RETRIEVE, SoundSource.NEUTRAL,
                1.0F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F)
            );
            player.gameEvent(GameEvent.ITEM_INTERACT_FINISH);

        } else {
            if (level instanceof ServerLevel serverLevel) {
                int timeReduction = (int)(EnchantmentHelper.getFishingTimeReduction(serverLevel, itemstack, player) * 20.0F);
                int luckBonus = EnchantmentHelper.getFishingLuckBonus(serverLevel, itemstack, player);

                int hookCount = (int) player.getAttributeValue(GLAttributes.FISHING_BOBBER_COUNT);

                for (int i = 0; i < hookCount; i++) {
                    FishingHook hook = new FishingHook(player, level, luckBonus, timeReduction);

                    if (i > 0) {
                        Vec3 delta = hook.getDeltaMovement();
                        float angleOffset = (i % 2 == 0 ? 1 : -1) * (0.1F * ((i + 1) / 2.0F));
                        hook.setDeltaMovement(delta.yRot(angleOffset));
                    }

                    level.addFreshEntity(hook);
                }
            }

            level.playSound(
                null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.FISHING_BOBBER_THROW, SoundSource.NEUTRAL,
                0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F)
            );
            player.awardStat(Stats.ITEM_USED.get((FishingRodItem)(Object)this));
            player.gameEvent(GameEvent.ITEM_INTERACT_START);

        }
        return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
    }




}