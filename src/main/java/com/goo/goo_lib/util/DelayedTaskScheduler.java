package com.goo.goo_lib.util;

import com.goo.goo_lib.common.GooLib;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@EventBusSubscriber(modid = GooLib.MOD_ID)
public class DelayedTaskScheduler {
    // replace level-specific maps with standard Level key
    private static final Map<Level, ConcurrentLinkedQueue<AbstractMap.SimpleEntry<Runnable, Integer>>> tasksPerLevel = new ConcurrentHashMap<>();

    public static void queueCommonWork(Level level, int tick, Runnable action) {
        tasksPerLevel.computeIfAbsent(level, k -> new ConcurrentLinkedQueue<>()).add(new AbstractMap.SimpleEntry<>(action, tick));
    }

    public static void queueClientWork(Level level, int tick, Runnable action) {
        if (level.isClientSide()) {
            tasksPerLevel.computeIfAbsent(level, k -> new ConcurrentLinkedQueue<>()).add(new AbstractMap.SimpleEntry<>(action, tick));
        }
    }

    public static void queueServerWork(Level level, int tick, Runnable action) {
        if (!level.isClientSide()) {
            tasksPerLevel.computeIfAbsent(level, k -> new ConcurrentLinkedQueue<>()).add(new AbstractMap.SimpleEntry<>(action, tick));
        }
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        ConcurrentLinkedQueue<AbstractMap.SimpleEntry<Runnable, Integer>> workQueue = tasksPerLevel.get(event.getLevel());
        if (workQueue != null) {
            List<AbstractMap.SimpleEntry<Runnable, Integer>> actions = new ArrayList<>();
            workQueue.forEach((work) -> {
                work.setValue(work.getValue() - 1);
                if (work.getValue() <= 0) {
                    actions.add(work);
                }
            });
            actions.forEach((e) -> e.getKey().run());
            workQueue.removeAll(actions);
        }
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof Level level) {
            tasksPerLevel.remove(level);
        }
    }
}