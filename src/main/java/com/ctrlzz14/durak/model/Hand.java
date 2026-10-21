package com.ctrlzz14.durak.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Рука игрока: набор карт, которые в данный момент держит игрок.
 *
 * <p>Рука хранит карты в порядке получения (раздача, взятие, добор). Порядок
 * не влияет на правила, но сохраняется, чтобы интерфейс мог показывать карты
 * в стабильном виде. Для отображения предусмотрен сортированный вид
 * {@link #sorted()}.</p>
 */
public final class Hand {

    /**
     * Компаратор для отображения карт: сначала по достоинству, затем по масти.
     */
    public static final Comparator<Card> DISPLAY_ORDER =
            Comparator.comparingInt((Card card) -> card.rank().power())
                    .thenComparing(card -> card.suit().ordinal());

    private final List<Card> cards = new ArrayList<>();

    /**
     * Создаёт пустую руку.
     */
    public Hand() {
        // пустая рука
    }

    /**
     * Создаёт руку с указанными картами.
     *
     * @param cards исходные карты
     */
    public Hand(List<Card> cards) {
        this.cards.addAll(cards);
    }

    /**
     * Добавляет карту в руку.
     *
     * @param card добавляемая карта
     */
    public void add(Card card) {
        cards.add(Objects.requireNonNull(card, "Карта не должна быть null"));
    }

    /**
     * Добавляет несколько карт в руку.
     *
     * @param newCards добавляемые карты
     */
    public void addAll(List<Card> newCards) {
        newCards.forEach(this::add);
    }

    /**
     * Убирает карту из руки.
     *
     * @param card убираемая карта
     * @return {@code true}, если карта была в руке и удалена
     */
    public boolean remove(Card card) {
        return cards.remove(card);
    }

    /**
     * Проверяет, есть ли карта в руке.
     *
     * @param card проверяемая карта
     * @return {@code true}, если карта есть в руке
     */
    public boolean contains(Card card) {
        return cards.contains(card);
    }

    /**
     * Ищет карту по достоинству и масти.
     *
     * @param rank искомое достоинство
     * @param suit искомая масть
     * @return найденная карта или {@link Optional#empty()}
     */
    public Optional<Card> find(Rank rank, Suit suit) {
        return cards.stream()
                .filter(card -> card.rank() == rank && card.suit() == suit)
                .findFirst();
    }

    /**
     * Возвращает карты руки в порядке получения.
     *
     * @return неизменяемый список карт
     */
    public List<Card> cards() {
        return List.copyOf(cards);
    }

    /**
     * Возвращает карты руки, отсортированные по достоинству и масти.
     *
     * @return отсортированный список карт
     */
    public List<Card> sorted() {
        List<Card> sorted = new ArrayList<>(cards);
        sorted.sort(DISPLAY_ORDER);
        return sorted;
    }

    /**
     * Возвращает количество карт в руке.
     *
     * @return число карт
     */
    public int size() {
        return cards.size();
    }

    /**
     * Проверяет, пуста ли рука.
     *
     * @return {@code true}, если карт нет
     */
    public boolean isEmpty() {
        return cards.isEmpty();
    }
}
