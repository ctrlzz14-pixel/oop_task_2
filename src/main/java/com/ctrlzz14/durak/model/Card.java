package com.ctrlzz14.durak.model;

import java.util.Objects;

/**
 * Игральная карта: сочетание достоинства и масти.
 *
 * <p>Класс неизменяемый (immutable): после создания карту нельзя изменить, что
 * позволяет безопасно хранить её в колоде, руке, на столе и в отбое.
 * Две карты равны тогда и только тогда, когда совпадают их достоинство и масть,
 * поэтому карты можно использовать как элементы множеств и в качестве ключей
 * словарей.</p>
 */
public final class Card {

    private final Rank rank;
    private final Suit suit;

    /**
     * Создаёт карту.
     *
     * @param rank достоинство карты, не {@code null}
     * @param suit масть карты, не {@code null}
     * @throws NullPointerException если достоинство или масть равны {@code null}
     */
    public Card(Rank rank, Suit suit) {
        this.rank = Objects.requireNonNull(rank, "Достоинство карты не должно быть null");
        this.suit = Objects.requireNonNull(suit, "Масть карты не должна быть null");
    }

    /**
     * Создаёт карту из достоинства и масти.
     *
     * @param rank достоинство карты
     * @param suit масть карты
     * @return новая карта
     */
    public static Card of(Rank rank, Suit suit) {
        return new Card(rank, suit);
    }

    /**
     * Возвращает достоинство карты.
     *
     * @return достоинство карты
     */
    public Rank rank() {
        return rank;
    }

    /**
     * Возвращает масть карты.
     *
     * @return масть карты
     */
    public Suit suit() {
        return suit;
    }

    /**
     * Проверяет, является ли карта козырной.
     *
     * @param trumpSuit козырная масть; если {@code null}, карта не считается козырной
     * @return {@code true}, если масть карты совпадает с козырной
     */
    public boolean isTrump(Suit trumpSuit) {
        return trumpSuit != null && trumpSuit == suit;
    }

    /**
     * Сравнивает карту с другой по достоинству.
     *
     * @param other карта для сравнения
     * @return {@code true}, если достоинства совпадают
     */
    public boolean hasSameRank(Card other) {
        return other != null && this.rank == other.rank;
    }

    /**
     * Возвращает текстовое представление карты, например {@code "6♥"}.
     *
     * @return представление карты
     */
    @Override
    public String toString() {
        return rank.shortName() + suit.symbol();
    }

    /**
     * Сравнивает карты по достоинству и масти.
     *
     * @param o объект для сравнения
     * @return {@code true}, если это та же самая карта
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Card other)) {
            return false;
        }
        return rank == other.rank && suit == other.suit;
    }

    /**
     * Возвращает хеш-код карты, согласованный с {@link #equals(Object)}.
     *
     * @return хеш-код карты
     */
    @Override
    public int hashCode() {
        return Objects.hash(rank, suit);
    }
}
