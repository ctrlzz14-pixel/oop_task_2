package com.ctrlzz14.durak.event;

import com.ctrlzz14.durak.model.Card;
import com.ctrlzz14.durak.model.Player;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Событие раздачи карт.
 *
 * <p>Событие содержит начальные руки игроков: это делает журнал самодостаточным
 * (по нему можно понять исходную позицию) и пригодным для последующего
 * воспроизведения партии.</p>
 */
public final class CardsDealtEvent extends GameEvent {

    private final Map<Player, List<Card>> hands;

    /**
     * Создаёт событие раздачи.
     *
     * @param hands начальные руки игроков в порядке хода
     */
    public CardsDealtEvent(Map<Player, List<Card>> hands) {
        Map<Player, List<Card>> copy = new LinkedHashMap<>();
        hands.forEach((player, cards) -> copy.put(player, List.copyOf(cards)));
        this.hands = Collections.unmodifiableMap(copy);
    }

    /**
     * Возвращает начальные руки игроков.
     *
     * @return неизменяемая карта «игрок → карты»
     */
    public Map<Player, List<Card>> hands() {
        return hands;
    }

    /**
     * Возвращает тип события.
     *
     * @return {@link EventType#CARDS_DEALT}
     */
    @Override
    public EventType type() {
        return EventType.CARDS_DEALT;
    }
}
