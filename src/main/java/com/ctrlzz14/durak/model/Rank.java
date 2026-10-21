package com.ctrlzz14.durak.model;

/**
 * Достоинство карты.
 *
 * <p>Каждому достоинству соответствует числовой вес {@link #power()}, который
 * используется для сравнения карт при отбивании: карта бьёт другую, если её
 * вес больше. Порядок объявления констант совпадает с порядком старшинства
 * (от младшей карты к старшей).</p>
 */
public enum Rank {

    /** Шестёрка — младшая карта в колоде. */
    SIX(6, "6"),
    /** Семёрка. */
    SEVEN(7, "7"),
    /** Восьмёрка. */
    EIGHT(8, "8"),
    /** Девятка. */
    NINE(9, "9"),
    /** Десятка. */
    TEN(10, "10"),
    /** Валет. */
    JACK(11, "В"),
    /** Дама. */
    QUEEN(12, "К"),
    /** Король. */
    KING(13, "Д"),
    /** Туз — старшая карта в колоде. */
    ACE(14, "Т");

    private final int power;
    private final String shortName;

    Rank(int power, String shortName) {
        this.power = power;
        this.shortName = shortName;
    }

    /**
     * Возвращает числовой вес достоинства.
     *
     * @return вес карты: от {@code 6} (шестёрка) до {@code 14} (туз)
     */
    public int power() {
        return power;
    }

    /**
     * Возвращает короткое обозначение достоинства, принятое в карточных играх.
     *
     * @return обозначение достоинства, например {@code "Т"} для туза
     */
    public String shortName() {
        return shortName;
    }

    /**
     * Проверяет, что текущее достоинство старше указанного.
     *
     * @param other достоинство для сравнения
     * @return {@code true}, если текущее достоинство старше
     */
    public boolean isHigherThan(Rank other) {
        return this.power > other.power;
    }

    /**
     * Возвращает короткое обозначение достоинства.
     *
     * @return обозначение достоинства
     */
    @Override
    public String toString() {
        return shortName;
    }
}
