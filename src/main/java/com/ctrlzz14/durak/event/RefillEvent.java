package com.ctrlzz14.durak.event;

import com.ctrlzz14.durak.model.Card;
import com.ctrlzz14.durak.model.Player;

import java.util.List;
import java.util.Objects;

/**
 * Событие добора: игрок взял карты из колоды после раунда.
 */
public final class RefillEvent extends GameEvent {

    private final Player player;
    private final List<Card> cards;

    /**
     * Создаёт событие добора.
     *
     * @param player игрок, добравший карты
     * @param cards добранные карты
     */
    public RefillEvent(Player player, List<Card> cards) {
        this.player = Objects.requireNonNull(player, "Игрок не должен быть null");
        this.cards = List.copyOf(Objects.requireNonNull(cards, "Список карт не должен быть null"));
    }

    /**
     * Возвращает игрока, добравшего карты.
     *
     * @return игрок
     */
    public Player player() {
        return player;
    }

    /**
     * Возвращает добранные карты.
     *
     * @return неизменяемый список карт
     */
    public List<Card> cards() {
        return cards;
    }

    /**
     * Возвращает тип события.
     *
     * @return {@link EventType#REFILL}
     */
    @Override
    public EventType type() {
        return EventType.REFILL;
    }
}
