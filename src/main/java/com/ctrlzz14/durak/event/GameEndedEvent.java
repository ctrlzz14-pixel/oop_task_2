package com.ctrlzz14.durak.event;

import com.ctrlzz14.durak.model.GameResult;

import java.util.Objects;

/**
 * Событие окончания партии.
 *
 * <p>Событие содержит итоговый результат: победителей, проигравших и причину
 * завершения партии.</p>
 */
public final class GameEndedEvent extends GameEvent {

    private final GameResult result;

    /**
     * Создаёт событие окончания партии.
     *
     * @param result итоговый результат партии
     */
    public GameEndedEvent(GameResult result) {
        this.result = Objects.requireNonNull(result, "Результат не должен быть null");
    }

    /**
     * Возвращает итоговый результат партии.
     *
     * @return результат партии
     */
    public GameResult result() {
        return result;
    }

    /**
     * Возвращает тип события.
     *
     * @return {@link EventType#GAME_ENDED}
     */
    @Override
    public EventType type() {
        return EventType.GAME_ENDED;
    }
}
