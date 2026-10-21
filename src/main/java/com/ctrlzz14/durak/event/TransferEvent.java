package com.ctrlzz14.durak.event;

import com.ctrlzz14.durak.model.Card;
import com.ctrlzz14.durak.model.Player;

import java.util.List;
import java.util.Objects;

/**
 * Событие перевода атаки.
 *
 * <p>Защищающийся, отбившийся картой, переводит атаку на следующего игрока по
 * кругу: карты перевода ложатся на стол как новая атака, а отбиваться от них
 * обязан уже следующий игрок.</p>
 */
public final class TransferEvent extends GameEvent {

    private final Player defender;
    private final List<Card> cards;
    private final Player newDefender;

    /**
     * Создаёт событие перевода атаки.
     *
     * @param defender защищающийся, выполнивший перевод
     * @param cards карты перевода
     * @param newDefender игрок, который обязан отбить перевод
     */
    public TransferEvent(Player defender, List<Card> cards, Player newDefender) {
        this.defender = Objects.requireNonNull(defender, "Переводчик не должен быть null");
        this.cards = List.copyOf(Objects.requireNonNull(cards, "Список карт не должен быть null"));
        this.newDefender = Objects.requireNonNull(newDefender, "Новый защищающийся не должен быть null");
    }

    /**
     * Возвращает игрока, выполнившего перевод.
     *
     * @return защищающийся, выполнивший перевод
     */
    public Player defender() {
        return defender;
    }

    /**
     * Возвращает карты перевода.
     *
     * @return неизменяемый список карт
     */
    public List<Card> cards() {
        return cards;
    }

    /**
     * Возвращает игрока, которому переведена атака.
     *
     * @return новый защищающийся
     */
    public Player newDefender() {
        return newDefender;
    }

    /**
     * Возвращает тип события.
     *
     * @return {@link EventType#TRANSFER}
     */
    @Override
    public EventType type() {
        return EventType.TRANSFER;
    }
}
