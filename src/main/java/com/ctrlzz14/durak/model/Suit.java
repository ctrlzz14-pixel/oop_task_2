package com.ctrlzz14.durak.model;

/**
 * Масть карты.
 *
 * <p>В игре используются четыре масти, как в стандартной колоде из 36 карт.
 * Порядок объявления констант имеет смыслового значения только для отображения
 * карт в интерфейсе; правила сравнения карт оперируют понятием «козырная масть»
 * и явно передают нужную масть параметром, поэтому никакой «старшей» масти
 * в правилах нет.</p>
 */
public enum Suit {

    /** Червовая масть (красная). */
    HEARTS("♥", "Черви"),
    /** Бубновая масть (красная). */
    DIAMONDS("♦", "Бубны"),
    /** Трефовая масть (чёрная). */
    CLUBS("♣", "Трефы"),
    /** Пиковая масть (чёрная). */
    SPADES("♠", "Пики");

    private final String symbol;
    private final String russianName;

    Suit(String symbol, String russianName) {
        this.symbol = symbol;
        this.russianName = russianName;
    }

    /**
     * Возвращает короткий графический символ масти.
     *
     * @return символ масти, например {@code "♥"}
     */
    public String symbol() {
        return symbol;
    }

    /**
     * Возвращает русское название масти.
     *
     * @return название масти, например {@code "Черви"}
     */
    public String russianName() {
        return russianName;
    }

    /**
     * Возвращает символ масти.
     *
     * @return символ масти
     */
    @Override
    public String toString() {
        return symbol;
    }
}
