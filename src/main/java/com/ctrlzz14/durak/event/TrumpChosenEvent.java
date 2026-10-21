package com.ctrlzz14.durak.event;

import com.ctrlzz14.durak.model.Card;
import com.ctrlzz14.durak.model.Suit;
import com.ctrlzz14.durak.model.TrumpMode;

import java.util.Objects;

/**
 * Событие выбора козыря.
 *
 * <p>Козырная масть определяется нижней картой колоды. При открытом козыре
 * карта видна всем игрокам, при закрытом — скрыта, но масть всё равно
 * известна.</p>
 */
public final class TrumpChosenEvent extends GameEvent {

    private final Suit trumpSuit;
    private final TrumpMode trumpMode;
    private final Card trumpCard;

    /**
     * Создаёт событие выбора козыря.
     *
     * @param trumpSuit козырная масть
     * @param trumpMode режим козыря
     * @param trumpCard нижняя карта колоды
     */
    public TrumpChosenEvent(Suit trumpSuit, TrumpMode trumpMode, Card trumpCard) {
        this.trumpSuit = Objects.requireNonNull(trumpSuit, "Козырная масть не должна быть null");
        this.trumpMode = Objects.requireNonNull(trumpMode, "Режим козыря не должен быть null");
        this.trumpCard = trumpCard;
    }

    /**
     * Возвращает козырную масть.
     *
     * @return козырная масть
     */
    public Suit trumpSuit() {
        return trumpSuit;
    }

    /**
     * Возвращает режим козыря.
     *
     * @return режим козыря
     */
    public TrumpMode trumpMode() {
        return trumpMode;
    }

    /**
     * Возвращает нижнюю карту колоды.
     *
     * @return козырная карта
     */
    public Card trumpCard() {
        return trumpCard;
    }

    /**
     * Проверяет, открыта ли козырная карта.
     *
     * @return {@code true}, если карта открыта игрокам
     */
    public boolean isCardVisible() {
        return trumpMode == TrumpMode.OPEN;
    }

    /**
     * Возвращает тип события.
     *
     * @return {@link EventType#TRUMP_CHOSEN}
     */
    @Override
    public EventType type() {
        return EventType.TRUMP_CHOSEN;
    }
}
