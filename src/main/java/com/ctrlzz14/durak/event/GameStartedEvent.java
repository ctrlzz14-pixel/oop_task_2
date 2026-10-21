package com.ctrlzz14.durak.event;

import com.ctrlzz14.durak.model.GameRules;

import java.util.List;
import java.util.Objects;

/**
 * Событие начала партии.
 *
 * <p>Событие фиксирует зерно генератора случайных величин, настройки партии
 * и список игроков. Благодаря зерну партию можно воспроизвести: тот же seed
 * при тех же начальных условиях даёт ту же раздачу и ту же последовательность
 * ходов.</p>
 */
public final class GameStartedEvent extends GameEvent {

    private final long seed;
    private final GameRules rules;
    private final List<String> playerNames;

    /**
     * Создаёт событие начала партии.
     *
     * @param seed зерно генератора случайных величин
     * @param rules настройки партии
     * @param playerNames имена игроков в порядке хода
     */
    public GameStartedEvent(long seed, GameRules rules, List<String> playerNames) {
        this.seed = seed;
        this.rules = Objects.requireNonNull(rules, "Правила не должны быть null");
        this.playerNames = List.copyOf(Objects.requireNonNull(playerNames, "Список игроков не должен быть null"));
    }

    /**
     * Возвращает зерно генератора случайных величин.
     *
     * @return зерно партии
     */
    public long seed() {
        return seed;
    }

    /**
     * Возвращает настройки партии.
     *
     * @return правила партии
     */
    public GameRules rules() {
        return rules;
    }

    /**
     * Возвращает имена игроков в порядке хода.
     *
     * @return неизменяемый список имён
     */
    public List<String> playerNames() {
        return playerNames;
    }

    /**
     * Возвращает тип события.
     *
     * @return {@link EventType#GAME_STARTED}
     */
    @Override
    public EventType type() {
        return EventType.GAME_STARTED;
    }
}
