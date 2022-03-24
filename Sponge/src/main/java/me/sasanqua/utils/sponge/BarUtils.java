package me.sasanqua.utils.sponge;

import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import me.sasanqua.utils.common.ImmutableTuple;
import me.sasanqua.utils.common.MutableTuple;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.chat.ChatTypes;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class BarUtils {

	private static final int BARS = 20;
	private static final String GREEN_BAR = "&a|";
	private static final String YELLOW_BAR = "&e|";
	private static final String RED_BAR = "&c|";
	private static final String GRAY_BAR = "&7|";

	private static final Set<UUID> ACTIVE_PLAYERS = Sets.newConcurrentHashSet();

	private static final HashMultimap<UUID, ImmutableTuple<String, Float>> PERCENTAGE_ACTIVES = HashMultimap.create();
	private static final HashMultimap<UUID, ImmutableTuple<String, MutableTuple<Integer, Integer>>> COOLDOWN_ACTIVES = HashMultimap.create();

	private static final ReentrantLock LOCK = new ReentrantLock();

	private static volatile boolean started = false;
	private static volatile int updated = 0;

	public static void startTask(Object pluginInstance) {
		if (!started) {
			started = true;
			Task.builder()
					.async()
					.interval(Sponge.getScheduler().getPreferredTickInterval(), TimeUnit.MILLISECONDS)
					.execute(task -> {

						LOCK.lock();

						try {

							Iterator<UUID> iterator = ACTIVE_PLAYERS.iterator();

							while (iterator.hasNext()) {

								UUID playerId = iterator.next();
								Optional<Player> player = Sponge.getServer().getPlayer(playerId);

								if (!player.isPresent() || !player.get().isOnline()) {
									iterator.remove();
									continue;
								}

								List<ImmutableTuple<String, Float>> percentageList = PERCENTAGE_ACTIVES.get(playerId)
										.stream()
										.sorted(Comparator.comparing(ImmutableTuple::getFirst,
												Comparator.naturalOrder()))
										.collect(Collectors.toList());
								List<ImmutableTuple<String, MutableTuple<Integer, Integer>>> cooldownList = COOLDOWN_ACTIVES.get(
												playerId)
										.stream()
										.sorted(Comparator.comparing(ImmutableTuple::getFirst,
												Comparator.naturalOrder()))
										.collect(Collectors.toList());

								if (percentageList.isEmpty() && cooldownList.isEmpty()) {
									iterator.remove();
									continue;
								}

								List<String> messagesList = Lists.newArrayList();

								percentageList.forEach(tuple -> {

									String id = tuple.getFirst();
									float percent = tuple.getSecond();

									int colorBars = (int) (percent * BARS);
									int grayBars = BARS - colorBars;

									messagesList.add(String.format("&c%s %s%s", id,
											Strings.repeat(getColorBar(percent), colorBars),
											Strings.repeat(GRAY_BAR, grayBars)));

									if (updated % 40 == 0) {
										PERCENTAGE_ACTIVES.remove(playerId, tuple);
									}

								});

								cooldownList.forEach(tuple -> {
									String id = tuple.getFirst();
									tuple.getSecond().get((totalSeconds, elapsedSeconds) -> {

										float elapsedPercent = 1.0f * elapsedSeconds / totalSeconds;

										int colorBars = (int) ((1.0f - elapsedPercent) * BARS);
										int grayBars = BARS - colorBars;

										messagesList.add(String.format("&c%s %s%s &a%ds", id,
												Strings.repeat(getColorBar(elapsedPercent), colorBars),
												Strings.repeat(GRAY_BAR, grayBars), totalSeconds - elapsedSeconds));

										if (elapsedSeconds >= totalSeconds) {
											COOLDOWN_ACTIVES.remove(playerId, tuple);
											return;
										}

										if (updated % 20 == 0) {
											tuple.getSecond().setSecond(elapsedSeconds + 1);
										}
									});

								});

								if (messagesList.size() > 0) {
									Task.builder().execute(() -> {
										player.get()
												.sendMessage(ChatTypes.ACTION_BAR,
														TextUtils.deserialize((String.join(" &7- ", messagesList))));
									}).submit(pluginInstance);
								}

							}

						} finally {
							LOCK.unlock();
						}

						updated++;

					})
					.submit(pluginInstance);
		}
	}

	public static void sendPercentageBar(Player player, String percentageId, float percent) {
		if (LOCK.tryLock()) {
			try {
				PERCENTAGE_ACTIVES.get(player.getUniqueId()).removeIf(tuple -> tuple.getFirst().equals(percentageId));
				PERCENTAGE_ACTIVES.put(player.getUniqueId(), entry(percentageId, percent));
			} finally {
				LOCK.unlock();
			}
		}
		ACTIVE_PLAYERS.add(player.getUniqueId());
	}

	public static void sendCooldownBar(Player player, String cooldownId, int seconds) {
		sendCooldownBar(player, cooldownId, seconds, false);
	}

	public static void sendCooldownBar(Player player, String cooldownId, int seconds, boolean removeIfExists) {
		if (seconds > 0) {
			if (LOCK.tryLock()) {
				try {

					if (removeIfExists) {
						COOLDOWN_ACTIVES.get(player.getUniqueId())
								.removeIf(tuple -> tuple.getFirst().equals(cooldownId));
					}

					if (!COOLDOWN_ACTIVES.get(player.getUniqueId())
							.stream()
							.anyMatch(tuple -> tuple.getFirst().equals(cooldownId))) {
						COOLDOWN_ACTIVES.put(player.getUniqueId(), entry(cooldownId, MutableTuple.of(seconds, 0)));
					}
				} finally {
					LOCK.unlock();
				}
			}
			ACTIVE_PLAYERS.add(player.getUniqueId());
		}
	}

	private static String getColorBar(float percent) {
		if (percent >= 0.8f) {
			return GREEN_BAR;
		} else if (percent >= 0.5f) {
			return YELLOW_BAR;
		} else {
			return RED_BAR;
		}
	}

	private static <T> ImmutableTuple<String, T> entry(String id, T value) {
		return ImmutableTuple.of(id, value, (k, v) -> k.hashCode(),
				(k, v, other) -> ((ImmutableTuple<String, T>) other).getFirst().equals(k));
	}

}