package com.ctrlzz14.durak.event;

import com.ctrlzz14.durak.model.Card;
import com.ctrlzz14.durak.model.Player;

import java.util.List;
import java.util.Objects;

/**
 * Событие взятия карт.
 *
 * <p>Защищающийся, не сумевший отбить атаку, забирает со стола все карты
 * раунда: и отбитые, и неотбитые. Это классическое правило дурака: оно
 * гарантирует, что партия продвигается, потому что карты уходят к
 * защищающемуся, а колода постепенно истощается.</p>
 */
public final class TakeEvent extends GameEvent {

    private final Player defender;
    private final List<Card> takenCards;

    /**
     * Создаёт событие взятия карт.
     *
     * @param defender защищающийся, взявший карты
     * @param takenCards карты, доставшиеся защищающемуся
     */
    public TakeEvent(Player defender, List<Card> takenCards) {
        this.defender = Objects.requireNonNull(defender, "Защищающийся не должен быть null");
        this.takenCards = List.copyOf(Objects.requireNonNull(takenCards, "Список взятых карт не должен быть null"));
    }

    /**
     * Возвращает защищающегося, взявшего карты.
     *
     * @return защищающийся
     */
    public Player defender() {
        return defender;
    }

    /**
     * Возвращает карты, доставшиеся защищающемуся.
     *
     * @return неизменяемый список карт
     */
    public List<Card> takenCards() {
        return takenCards;
    }

    /**
     * Возвращает тип события.
     *
     * @return {@link EventType#TAKE}
     */
    @Override
    public EventType type() {
        return EventType.TAKE;
    }
}
