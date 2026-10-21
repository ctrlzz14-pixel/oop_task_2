package com.ctrlzz14.durak.cli;

import com.ctrlzz14.durak.model.TrumpMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Параметры запуска партии, разобранные из аргументов командной строки.
 *
 * <p>Поддерживаются параметры вида {@code --key=value}:</p>
 * <ul>
 *   <li>{@code --players=3} — количество игроков от 2 до 5;</li>
 *   <li>{@code --human=1} — количество игроков-людей (0 или 1);</li>
 *   <li>{@code --trump=open} — режим козыря: {@code open} или {@code closed};</li>
 *   <li>{@code --seed=123} — зерно генератора случайных величин;</li>
 *   <li>{@code --names=Аня,Борис} — имена игроков через запятую.</li>
 * </ul>
 */
public record GameOptions(int playerCount, int humanCount, TrumpMode trumpMode, long seed, List<String> names) {

    /** Значение по умолчанию для количества игроков. */
    public static final int DEFAULT_PLAYER_COUNT = 3;
    /** Максимальное количество игроков. */
    public static final int MAX_PLAYER_COUNT = 5;

    /**
     * Разбирает аргументы командной строки.
     *
     * @param args аргументы командной строки
     * @return параметры партии
     * @throws IllegalArgumentException если параметр указан некорректно
     */
    public static GameOptions parse(String[] args) {
        int playerCount = DEFAULT_PLAYER_COUNT;
        int humanCount = 1;
        TrumpMode trumpMode = TrumpMode.OPEN;
        long seed = System.currentTimeMillis();
        List<String> names = List.of();

        for (String arg : args) {
            if (!arg.startsWith("--")) {
                throw new IllegalArgumentException("Неизвестный аргумент: " + arg);
            }
            String[] parts = arg.substring(2).split("=", 2);
            if (parts.length != 2) {
                throw new IllegalArgumentException("Параметр должен иметь вид --key=value, получено: " + arg);
            }
            String key = parts[0].trim().toLowerCase(Locale.ROOT);
            String value = parts[1].trim();
            switch (key) {
                case "players" -> playerCount = parseInt(value, "players");
                case "human" -> humanCount = parseInt(value, "human");
                case "trump" -> trumpMode = parseTrump(value);
                case "seed" -> seed = parseLong(value, "seed");
                case "names" -> names = parseNames(value);
                default -> throw new IllegalArgumentException("Неизвестный параметр: --" + key);
            }
        }

        if (playerCount < 2 || playerCount > MAX_PLAYER_COUNT) {
            throw new IllegalArgumentException("Количество игроков должно быть от 2 до " + MAX_PLAYER_COUNT);
        }
        if (humanCount < 0 || humanCount > 1) {
            throw new IllegalArgumentException("Количество игроков-людей должно быть 0 или 1");
        }
        if (humanCount > playerCount) {
            throw new IllegalArgumentException("Игроков-людей не может быть больше, чем игроков всего");
        }
        if (names.size() > playerCount) {
            throw new IllegalArgumentException("Имён больше, чем игроков в партии");
        }
        return new GameOptions(playerCount, humanCount, trumpMode, seed, names);
    }

    /**
     * Возвращает имена игроков: заданные пользователем либо сгенерированные.
     *
     * <p>Игрок-человек по умолчанию называется «Вы», компьютерные соперники —
     * «Компьютер N». Пользовательские имена имеют приоритет.</p>
     *
     * @return список имён нужной длины
     */
    public List<String> playerNames() {
        List<String> result = new ArrayList<>(playerCount);
        int botIndex = 1;
        for (int i = 0; i < playerCount; i++) {
            if (i < names.size()) {
                result.add(names.get(i));
            } else if (i < humanCount) {
                result.add("Вы");
            } else {
                result.add("Компьютер " + botIndex++);
            }
        }
        return List.copyOf(result);
    }

    /**
     * Возвращает текст справки по параметрам запуска.
     *
     * @param options текущие параметры
     * @return текст справки
     */
    public static String usage(GameOptions options) {
        return """
                Запуск: java -jar durak.jar [параметры]
                Параметры:
                  --players=N  количество игроков (2..%d, по умолчанию %d)
                  --human=N    количество игроков-людей (0 или 1, по умолчанию 1)
                  --trump=open|closed  режим козыря (по умолчанию open)
                  --seed=N     зерно генератора случайных величин
                  --names=A,B  имена игроков через запятую
                Текущие параметры: игроков %d, людей %d, козырь %s, зерно %d""".formatted(
                MAX_PLAYER_COUNT, DEFAULT_PLAYER_COUNT, options.playerCount(), options.humanCount(),
                options.trumpMode(), options.seed());
    }

    private static int parseInt(String value, String key) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Параметр --" + key + " должен быть целым числом, получено: " + value);
        }
    }

    private static long parseLong(String value, String key) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Параметр --" + key + " должен быть целым числом, получено: " + value);
        }
    }

    private static TrumpMode parseTrump(String value) {
        return switch (value.toLowerCase(Locale.ROOT)) {
            case "open", "открытый" -> TrumpMode.OPEN;
            case "closed", "закрытый" -> TrumpMode.CLOSED;
            default -> throw new IllegalArgumentException("Параметр --trump должен быть open или closed, получено: " + value);
        };
    }

    private static List<String> parseNames(String value) {
        if (value.isEmpty()) {
            return List.of();
        }
        return java.util.Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .toList();
    }
}
