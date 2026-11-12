package io.github.hyeonmo.managers;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.*;

/** A deterministic monotonic clock and scheduler: tests never sleep. */
final class ManualScheduler extends AbstractExecutorService implements ScheduledExecutorService {
    private final PriorityQueue<Task<?>> tasks = new PriorityQueue<>();
    private long now;
    private long sequence;
    private boolean shutdown;

    long nanoTime() { return now; }
    long millis() { return TimeUnit.NANOSECONDS.toMillis(now); }

    void next() {
        Task<?> task;
        do {
            task = tasks.poll();
            if (task == null) throw new AssertionError("No scheduled task");
        } while (task.isCancelled());
        now = Math.max(now, task.due);
        task.run();
        try {
            task.get();
        } catch (CancellationException stopped) {
            // A terminal failure shuts down the manager from inside the running task.
            if (!shutdown) throw new AssertionError("Task cancelled without shutdown", stopped);
        } catch (Exception failure) {
            throw new AssertionError("Scheduled task failed", failure);
        }
    }

    public ScheduledFuture<?> schedule(Runnable task, long delay, TimeUnit unit) {
        return schedule(Executors.callable(task, null), delay, unit);
    }
    public <V> ScheduledFuture<V> schedule(Callable<V> task, long delay, TimeUnit unit) {
        if (shutdown) throw new RejectedExecutionException();
        Task<V> future = new Task<>(task, now + unit.toNanos(delay), sequence++);
        tasks.add(future);
        return future;
    }
    public void execute(Runnable task) { schedule(task, 0, TimeUnit.NANOSECONDS); }
    public void shutdown() { shutdown = true; }
    public List<Runnable> shutdownNow() {
        shutdown = true;
        List<Runnable> pending = new ArrayList<>();
        for (Task<?> task : tasks) {
            task.cancel(false);
            pending.add(task);
        }
        tasks.clear();
        return pending;
    }
    public boolean isShutdown() { return shutdown; }
    public boolean isTerminated() { return shutdown && tasks.isEmpty(); }
    public boolean awaitTermination(long timeout, TimeUnit unit) { return isTerminated(); }
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable r, long initial, long period, TimeUnit unit) {
        throw new UnsupportedOperationException();
    }
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable r, long initial, long delay, TimeUnit unit) {
        throw new UnsupportedOperationException();
    }

    private final class Task<V> extends FutureTask<V> implements ScheduledFuture<V> {
        final long due;
        final long order;
        Task(Callable<V> task, long due, long order) { super(task); this.due = due; this.order = order; }
        public long getDelay(TimeUnit unit) { return unit.convert(due - now, TimeUnit.NANOSECONDS); }
        public int compareTo(Delayed other) {
            Task<?> task = (Task<?>) other;
            int compared = Long.compare(due, task.due);
            return compared != 0 ? compared : Long.compare(order, task.order);
        }
    }
}
