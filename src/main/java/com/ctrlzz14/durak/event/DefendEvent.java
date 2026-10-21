package com.ctrlzz14.durak.event;

import com.ctrlzz14.durak.model.Card;
import com.ctrlzz14.durak.model.Player;

import java.util.Objects;

/**
 * Событие защиты: защищающийся отбился картой.
 *
 * <p>Событие содержит обе карты: атакованную и отбивающую. Отбитая карта
 * становится базой для возможного перевода атаки.</p>
 */
public final class DefendEvent extends GameEvent {

    private final Player defender;
    private final Card attackedCard;
    private final Card defenseCard;

    /**
     * Создаёт событие защиты.
     *
     * @param defender защищающийся игрок
     * @param attackedCard атакованная карта
     * @param defenseCard отбивающая карта
     */
    public DefendEvent(Player defender, Card attackedCard, Card defenseCard) {
        this.defender = Objects.requireNonNull(defender, "Защищающийся не должен быть null");
        this.attackedCard = Objects.requireNonNull(attackedCard, "Атакованная карта не должна быть null");
        this.defenseCard = Objects.requireNonNull(defenseCard, "Отбивающая карта не должна быть null");
    }

    /**
     * Возвращает защищающегося игрока.
     *
     * @return защищающийся
     */
    public Player defender() {
        return defender;
    }

    /**
     * Возвращает атакованную карту.
     *
     * @return атакованная карта
     */
    public Card attackedCard() {
        return attackedCard;
    }

    /**
     * Возвращает отбивающую карту.
     *
     * @return отбивающая карта
     */
    public Card defenseCard() {
        return defenseCard;
    }

    /**
     * Возвращает тип события.
     *
     * @return {@link EventType#DEFEND}
     */
    @Override
    public EventType type() {
        return EventType.DEFEND;
    }
}
