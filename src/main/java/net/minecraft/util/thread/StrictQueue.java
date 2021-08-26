package net.minecraft.util.thread;

import com.google.common.collect.Queues;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.Nullable;

public interface StrictQueue<T, F>
{
    @Nullable
    F pop();

    boolean push(T pValue);

    boolean isEmpty();

    int size();

    public static final class FixedPriorityQueue implements StrictQueue<StrictQueue.IntRunnable, Runnable>
    {
        private final List<Queue<Runnable>> queueList;

        public FixedPriorityQueue(int p_18773_)
        {
            this.queueList = IntStream.range(0, p_18773_).mapToObj((p_18776_) ->
            {
                return Queues.<Runnable>newConcurrentLinkedQueue();
            }).collect(Collectors.toList());
        }

        @Nullable
        public Runnable pop()
        {
            for (Queue<Runnable> queue : this.queueList)
            {
                Runnable runnable = queue.poll();

                if (runnable != null)
                {
                    return runnable;
                }
            }

            return null;
        }

        public boolean push(StrictQueue.IntRunnable pValue)
        {
            int i = pValue.getPriority();
            this.queueList.get(i).add(pValue);
            return true;
        }

        public boolean isEmpty()
        {
            return this.queueList.stream().allMatch(Collection::isEmpty);
        }

        public int size()
        {
            int i = 0;

            for (Queue<Runnable> queue : this.queueList)
            {
                i += queue.size();
            }

            return i;
        }
    }

    public static final class IntRunnable implements Runnable
    {
        private final int priority;
        private final Runnable task;

        public IntRunnable(int p_18786_, Runnable p_18787_)
        {
            this.priority = p_18786_;
            this.task = p_18787_;
        }

        public void run()
        {
            this.task.run();
        }

        public int getPriority()
        {
            return this.priority;
        }
    }

    public static final class QueueStrictQueue<T> implements StrictQueue<T, T>
    {
        private final Queue<T> queue;

        public QueueStrictQueue(Queue<T> p_18792_)
        {
            this.queue = p_18792_;
        }

        @Nullable
        public T pop()
        {
            return this.queue.poll();
        }

        public boolean push(T pValue)
        {
            return this.queue.add(pValue);
        }

        public boolean isEmpty()
        {
            return this.queue.isEmpty();
        }

        public int size()
        {
            return this.queue.size();
        }
    }
}
