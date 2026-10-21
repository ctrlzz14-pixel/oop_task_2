package com.ctrlzz14.durak.event;

/**
 * Событие завершения раунда.
 *
 * <p>Событие фиксирует номер раунда, количество карт, ушедших в отбой, и
 * причину завершения раунда.</p>
 */
public final class RoundEndedEvent extends GameEvent {

    private final int roundNumber;
    private final int discardedCount;
    private final RoundEndReason reason;

    /**
     * Создаёт событие завершения раунда.
     *
     * @param roundNumber номер раунда
     * @param discardedCount количество карт, ушедших в отбой
     * @param reason причина завершения раунда
     */
    public RoundEndedEvent(int roundNumber, int discardedCount, RoundEndReason reason) {
        this.roundNumber = roundNumber;
        this.discardedCount = discardedCount;
        this.reason = reason;
    }

    /**
     * Возвращает номер раунда.
     *
     * @return номер раунда
     */
    public int roundNumber() {
        return roundNumber;
    }

    /**
     * Возвращает количество карт, ушедших в отбой.
     *
     * @return число карт в отбое за этот раунд
     */
    public int discardedCount() {
        return discardedCount;
    }

    /**
     * Возвращает причину завершения раунда.
     *
     * @return причина завершения
     */
    public RoundEndReason reason() {
        return reason;
    }

    /**
     * Возвращает тип события.
     *
     * @return {@link EventType#ROUND_ENDED}
     */
    @Override
    public EventType type() {
        return EventType.ROUND_ENDED;
    }
}
