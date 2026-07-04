package com.goo.goo_lib.common.event.custom;


import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.fluids.FluidType;

/**
 * Taken from <a href="https://github.com/florensie/ExpandAbility/blob/master/neoforge/src/main/java/be/florens/expandability/api/forge/PlayerSwimEvent.java">ExpandAbility</a>, thank you!
 * Event that allows enabling/disabling the vanilla swimming behaviour even when not in a fluid.
 * Fired multiple times per tick on the client and server.
 * <p>
 * This event has the following {@link EventResult}:
 * <ul>
 *     <li>{@link EventResult#PASS}: Vanilla swimming behaviour</li>
 *     <li>{@link EventResult#SUCCESS}: Always swim, even when not in a fluid</li>
 *     <li>{@link EventResult#FAIL}: Never swim, even when in a fluid</li>
 * </ul>
 * Only called for Water, other fluids can use {@link net.minecraft.world.entity.LivingEntity#canSwimInFluidType(FluidType)}
 */
public class PlayerSwimEvent extends PlayerEvent {

    private EventResult result = EventResult.PASS;

    public PlayerSwimEvent(Player player) {
        super(player);
    }


    public EventResult getResult() {
        return result;
    }

    public void setResult(EventResult result) {
        this.result = result;
    }

    /**
     * Convenience method for Posting of the events
     */
    public static boolean shouldPlayerSwim(Object entity, boolean original) {
        if (entity instanceof Player player) {
            return processEventResult(postAndGetResult(player), original);
        }
        return original;
    }

    public static <T> T processEventResult(EventResult result, T success, T fail, T original) {
        return switch (result) {
            case SUCCESS -> success;
            case FAIL -> fail;
            case PASS -> original;
        };
    }

    public static boolean processEventResult(EventResult result, boolean defaultValue) {
        return processEventResult(result, true, false, defaultValue);
    }

    /**
     * Convenience method for Posting an event and getting the results, modders should not call this
     * @param player
     * @return
     */
    public static EventResult postAndGetResult(Player player) {
        PlayerSwimEvent event = new PlayerSwimEvent(player);
        NeoForge.EVENT_BUS.post(event);
        return event.getResult();
    }
}