package com.richardl.wintak.controller;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class AutoSaveCoalescerTest {

    /** Captures scheduled actions for manual invocation; ignores the delay. */
    private static List<Runnable> capturing() { return new ArrayList<>(); }
    private static DelayedRunner capturer(List<Runnable> bucket) {
        return (delay, action) -> bucket.add(action);
    }

    @Test
    void notifyChangedSchedulesSave() {
        AtomicInteger saves = new AtomicInteger();
        List<Runnable> bucket = capturing();
        AutoSaveCoalescer c = new AutoSaveCoalescer(Duration.ofSeconds(2), saves::incrementAndGet, capturer(bucket));

        c.notifyChanged();

        assertEquals(1, bucket.size(), "one delayed action must be scheduled");
        bucket.get(0).run();
        assertEquals(1, saves.get(), "delayed action must invoke the save callback");
    }

    @Test
    void burstCoalescesToOneSave() {
        AtomicInteger saves = new AtomicInteger();
        List<Runnable> bucket = capturing();
        AutoSaveCoalescer c = new AutoSaveCoalescer(Duration.ofSeconds(2), saves::incrementAndGet, capturer(bucket));

        c.notifyChanged();
        c.notifyChanged();
        c.notifyChanged();

        assertEquals(3, bucket.size(), "each notifyChanged schedules a delayed action");
        bucket.forEach(Runnable::run);
        assertEquals(1, saves.get(), "only the last scheduled action must fire (stale tokens skipped)");
    }

    @Test
    void staleTokenDoesNotSave() {
        AtomicInteger saves = new AtomicInteger();
        List<Runnable> bucket = capturing();
        AutoSaveCoalescer c = new AutoSaveCoalescer(Duration.ofSeconds(2), saves::incrementAndGet, capturer(bucket));

        c.notifyChanged();
        c.notifyChanged();
        bucket.get(0).run();  // fire only the first (stale) action

        assertEquals(0, saves.get(), "a stale-token action must not invoke the save callback");
    }

    @Test
    void flushWithPendingRunsSaveAndCancelsPending() {
        AtomicInteger saves = new AtomicInteger();
        List<Runnable> bucket = capturing();
        AutoSaveCoalescer c = new AutoSaveCoalescer(Duration.ofSeconds(2), saves::incrementAndGet, capturer(bucket));

        c.notifyChanged();
        c.flush();

        assertEquals(1, saves.get(), "flush must invoke the save callback immediately");
        bucket.get(0).run();  // the now-cancelled scheduled action fires
        assertEquals(1, saves.get(), "the cancelled action must not fire a second save");
    }

    @Test
    void flushWithNoPendingIsNoOp() {
        AtomicInteger saves = new AtomicInteger();
        AutoSaveCoalescer c = new AutoSaveCoalescer(Duration.ofSeconds(2), saves::incrementAndGet, capturer(capturing()));

        c.flush();

        assertEquals(0, saves.get(), "flush with nothing pending must not invoke the save callback");
    }

    @Test
    void flushAfterSaveIsNoOp() {
        AtomicInteger saves = new AtomicInteger();
        List<Runnable> bucket = capturing();
        AutoSaveCoalescer c = new AutoSaveCoalescer(Duration.ofSeconds(2), saves::incrementAndGet, capturer(bucket));

        c.notifyChanged();
        bucket.get(0).run();  // delayed save fires normally
        c.flush();            // nothing pending anymore

        assertEquals(1, saves.get(), "flush after the debounce already fired must not save again");
    }

    @Test
    void noneCoalescerIsInert() {
        AutoSaveCoalescer.NONE.notifyChanged();
        AutoSaveCoalescer.NONE.flush();
        // no exception, no save
    }
}
