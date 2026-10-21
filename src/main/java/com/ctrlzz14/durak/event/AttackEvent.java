package com.ctrlzz14.durak.event;

import com.ctrlzz14.durak.model.Card;
import com.ctrlzz14.durak.model.Player;

import java.util.List;
import java.util.Objects;

/**
 * Событие атаки: атакующий выложил карты на стол.
 */
public final class AttackEvent extends GameEvent {

    private final Player attacker;
    private final List<Card> cards;

    /**
     * Создаёт событие атаки.
     *
     * @param attacker атакующий игрок
     * @param cards выложенные карты
     */
    public AttackEvent(Player attacker, List<Card> cards) {
        this.attacker = Objects.requireNonNull(attacker, "Атакующий не должен быть null");
        this.cards = List.copyOf(Objects.requireNonNull(cards, "Список карт не должен быть null"));
    }

    /**
     * Возвращает атакующего игрока.
     *
     * @return атакующий
     */
    public Player attacker() {
        return attacker;
    }

    /**
     * Возвращает выложенные карты.
     *
     * @return неизменяемый список карт
     */
    public List<Card> cards() {
        return cards;
    }

    /**
     * Возвращает тип события.
     *
     * @return {@link EventType#ATTACK}
     */
    @Override
    public EventType type() {
        return EventType.ATTACK;
    }
}
