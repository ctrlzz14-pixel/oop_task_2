package com.ctrlzz14.durak.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Отбой: карты, сыгранные и сброшенные в завершённых раундах.
 *
 * <p>Отбой не участвует в правилах и нужен только для отображения и для
 * проверки сохранности карт: любая карта колоды в любой момент находится либо
 * в колоде, либо в руке, либо на столе, либо в отбое.</p>
 */
public final class DiscardPile {

    private final List<Card> cards = new ArrayList<>();

    /**
     * Создаёт пустой отбой.
     */
    public DiscardPile() {
        // пустой отбой
    }

    /**
     * Добавляет карту в отбой.
     *
     * @param card добавляемая карта
     */
    public void add(Card card) {
        cards.add(card);
    }

    /**
     * Добавляет несколько карт в отбой.
     *
     * @param newCards добавляемые карты
     */
    public void addAll(Collection<Card> newCards) {
        cards.addAll(newCards);
    }

    /**
     * Возвращает карты отбоя в порядке добавления.
     *
     * @return неизменяемый список карт
     */
    public List<Card> cards() {
        return Collections.unmodifiableList(cards);
    }

    /**
     * Возвращает количество карт в отбое.
     *
     * @return число карт
     */
    public int size() {
        return cards.size();
    }

    /**
     * Проверяет, пуст ли отбой.
     *
     * @return {@code true}, если отбой пуст
     */
    public boolean isEmpty() {
        return cards.isEmpty();
    }
}
