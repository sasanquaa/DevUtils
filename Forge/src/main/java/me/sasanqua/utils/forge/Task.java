package me.sasanqua.utils.forge;

import com.google.common.collect.Sets;
import me.sasanqua.utils.common.PreconditionUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Iterator;
import java.util.Set;
import java.util.function.Consumer;

public final class Task {

	private static final Set<Task> TASK_SET = Sets.newConcurrentHashSet();
	private static final TaskListener TASK_LISTENER = new TaskListener();

	private final Consumer<Task> consumer;

	private final long interval;
	private final long iterations;
	private final long timestamp;
	private long currentIteration;
	private long ticksRemaining;

	private boolean expired;

	Task(TaskBuilder builder) {
		this.consumer = PreconditionUtils.checkNotNull(builder.consumer);
		this.interval = builder.interval;
		this.iterations = builder.iterations;
		this.timestamp = System.currentTimeMillis();
		this.ticksRemaining = builder.delay > 0L ? builder.delay : 0L;
		TASK_SET.add(this);
	}

	public static TaskBuilder builder() {
		return new TaskBuilder();
	}

	public long getTimestamp() {
		return timestamp;
	}

	public long getCurrentIteration() {
		return currentIteration;
	}

	public boolean isExpired() {
		return this.expired;
	}

	public void setExpired() {
		this.expired = true;
	}

	void tick() {
		this.ticksRemaining = Math.max(0L, --this.ticksRemaining);
		if (!this.expired && this.ticksRemaining == 0L) {
			this.consumer.accept(this);
			this.currentIteration++;
			if (this.interval > 0L && (this.currentIteration < this.iterations || this.iterations == -1L)) {
				this.ticksRemaining = this.interval;
			} else {
				this.expired = true;
			}
		}
	}

	private static final class TaskListener {

		TaskListener() {
			MinecraftForge.EVENT_BUS.register(this);
		}

		@SubscribeEvent
		public void onServerTick(TickEvent.ServerTickEvent event) {
			if (event.phase == TickEvent.Phase.END) {
				Iterator<Task> iterator = TASK_SET.iterator();
				while (iterator.hasNext()) {
					Task task = iterator.next();
					task.tick();
					if (task.isExpired()) {
						iterator.remove();
					}
				}
			}
		}

	}

	public static final class TaskBuilder {
		private @Nullable Consumer<Task> consumer;

		private long delay = 0L;

		private long interval = 0L;

		private long iterations = 1L;

		TaskBuilder() {
		}

		public TaskBuilder execute(Runnable runnable) {
			this.consumer = (task -> runnable.run());
			return this;
		}

		public TaskBuilder execute(Consumer<Task> consumer) {
			this.consumer = consumer;
			return this;
		}

		public TaskBuilder delay(long delay) {
			this.delay = delay;
			return this;
		}

		public TaskBuilder interval(long interval) {
			this.interval = interval;
			return this;
		}

		public TaskBuilder iterations(long iterations) {
			this.iterations = iterations;
			return this;
		}

		public TaskBuilder infinite() {
			return iterations(-1L);
		}

		public Task build() {
			PreconditionUtils.checkNotNull(consumer, "Consumer must be set");
			PreconditionUtils.checkState(iterations >= -1L, "Iterations must not be below -1");
			PreconditionUtils.checkState(interval >= 0L, "Interval must not be below 0");
			PreconditionUtils.checkState(delay >= 0L, "Delay must not be below 0");
			return new Task(this);
		}
	}
}
