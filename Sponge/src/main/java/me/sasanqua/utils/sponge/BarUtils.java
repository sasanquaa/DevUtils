package me.sasanqua.utils.sponge;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.api.util.Tuple;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public final class BarUtils {

	private static final int BARS = 20;
	private static final String GREEN_BAR = "&a|";
	private static final String YELLOW_BAR = "&e|";
	private static final String RED_BAR = "&c|";
	private static final String GRAY_BAR = "&7|";

	private static final Set<UUID> ACTIVE_MAP = Sets.newConcurrentHashSet();
	private static final Map<String, Float> PERCENTAGE_MAP = new ConcurrentHashMap<>();
	private static final Map<String, Tuple<Float, Float>> COOLDOWN_MAP = new ConcurrentHashMap<>();

	private static volatile boolean started = false;
	private static volatile int updated = 0;

	public static synchronized void startTask(final Object pluginInstance) {
		if (!started) {
			started = true;
			Task.builder()
					.async()
					.interval(Sponge.getScheduler().getPreferredTickInterval(), TimeUnit.MILLISECONDS)
					.execute(task -> {

						if (!started) {
							task.cancel();
							return;
						}

						final Iterator<UUID> iterator = ACTIVE_MAP.iterator();

						while (iterator.hasNext()) {

							final UUID playerId = iterator.next();
							final Optional<Player> player = Sponge.getServer().getPlayer(playerId);

							if (!player.isPresent() || !player.get().isOnline()) {
								PERCENTAGE_MAP.keySet().removeIf(k -> k.startsWith(playerId.toString()));
								COOLDOWN_MAP.keySet().removeIf(k -> k.startsWith(playerId.toString()));
								iterator.remove();
								continue;
							}

							final List<Tuple<String, Float>> percentageList = PERCENTAGE_MAP.entrySet()
									.stream()
									.filter(entry -> entry.getKey().startsWith(playerId.toString()))
									.map(entry -> Tuple.of(entry.getKey().split(":")[1], entry.getValue()))
									.sorted(Comparator.comparing(Tuple::getFirst, Comparator.naturalOrder()))
									.collect(Collectors.toList());
							final List<Tuple<String, Tuple<Float, Float>>> cooldownList = COOLDOWN_MAP.entrySet()
									.stream()
									.filter(entry -> entry.getKey().startsWith(playerId.toString()))
									.map(entry -> Tuple.of(entry.getKey().split(":")[1], entry.getValue()))
									.sorted(Comparator.comparing(Tuple::getFirst, Comparator.naturalOrder()))
									.collect(Collectors.toList());

							if (percentageList.isEmpty() && cooldownList.isEmpty()) {
								iterator.remove();
								continue;
							}

							final List<String> messagesList = Lists.newArrayList();

							percentageList.forEach(tuple -> {
								final String id = tuple.getFirst();
								final float percent = tuple.getSecond();
								final int colorBars = (int) (percent * BARS);
								final int grayBars = BARS - colorBars;
								messagesList.add(
										String.format("&c%s %s%s", id, Strings.repeat(getColorBar(percent), colorBars),
												Strings.repeat(GRAY_BAR, grayBars)));
								if (updated % 40 == 0) {
									PERCENTAGE_MAP.remove(namespacedKey(player.get(), id));
								}
							});

							cooldownList.forEach(tuple -> {
								final String id = tuple.getFirst();
								final Tuple<Float, Float> timeTuple = tuple.getSecond();
								final float totalSeconds = timeTuple.getFirst();
								final float elapsedSeconds = timeTuple.getSecond();
								final float elapsedPercent = elapsedSeconds / totalSeconds;

								final int colorBars = (int) ((1.0f - elapsedPercent) * BARS);
								final int grayBars = BARS - colorBars;

								messagesList.add(String.format("&c%s %s%s &a%ds", id,
										Strings.repeat(getColorBar(elapsedPercent), colorBars),
										Strings.repeat(GRAY_BAR, grayBars), totalSeconds - elapsedSeconds));

								if (elapsedSeconds >= totalSeconds) {
									COOLDOWN_MAP.remove(namespacedKey(player.get(), id), tuple);
									return;
								}

								if (updated % 2 == 0) {
									COOLDOWN_MAP.put(namespacedKey(player.get(), id), Tuple.of(totalSeconds,
											elapsedSeconds + TimeUnit.MILLISECONDS.toSeconds(
													Sponge.getScheduler().getPreferredTickInterval() * 2L)));
								}

							});

							if (messagesList.size() > 0) {
								Task.builder().execute(() -> {
									player.get()
											.sendMessage(ChatTypes.ACTION_BAR,
													TextUtils.deserialize((String.join(" &7- ", messagesList))));
								}).submit(pluginInstance);
							}

						}

						updated++;
					})
					.submit(pluginInstance);
		}
	}

	public static synchronized void stopTask() {
		started = false;
	}

	public static void sendPercentageBar(final Player player, final String percentageId, final float percent) {
		if (percent >= 0) {
			PERCENTAGE_MAP.put(namespacedKey(player, percentageId), percent);
			ACTIVE_MAP.add(player.getUniqueId());
		}
	}

	public static void sendCooldownBar(final Player player, final String cooldownId, final int seconds) {
		sendCooldownBar(player, cooldownId, seconds, false);
	}

	public static void sendCooldownBar(final Player player, final String cooldownId, final int seconds, final boolean replace) {
		if (seconds > 0) {
			if (replace) {
				COOLDOWN_MAP.put(namespacedKey(player, cooldownId), Tuple.of((float) seconds, 0F));
			} else {
				COOLDOWN_MAP.putIfAbsent(namespacedKey(player, cooldownId), Tuple.of((float) seconds, 0F));
			}
			ACTIVE_MAP.add(player.getUniqueId());
		}
	}

	private static String getColorBar(final float percent) {
		if (percent >= 0.8f) {
			return GREEN_BAR;
		} else if (percent >= 0.5f) {
			return YELLOW_BAR;
		} else {
			return RED_BAR;
		}
	}

	private static String namespacedKey(final Player player, final String value) {
		return player.getUniqueId() + ":" + value;
	}

}
