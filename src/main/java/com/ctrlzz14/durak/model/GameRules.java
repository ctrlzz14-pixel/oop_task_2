package com.ctrlzz14.durak.model;

/**
 * Настройки партии.
 *
 * <p>Класс неизменяемый и собирается через {@link Builder}. Значения по
 * умолчанию соответствуют классическому подкидному дураку: открытый козырь,
 * по шесть карт на руки и добор до шести карт после каждого раунда.</p>
 */
public final class GameRules {

    private final TrumpMode trumpMode;
    private final int initialHandSize;
    private final int minHandSize;

    private GameRules(Builder builder) {
        this.trumpMode = builder.trumpMode;
        this.initialHandSize = builder.initialHandSize;
        this.minHandSize = builder.minHandSize;
    }

    /**
     * Возвращает стандартные настройки партии.
     *
     * @return правила по умолчанию
     */
    public static GameRules standard() {
        return builder().build();
    }

    /**
     * Создаёт новый построитель настроек.
     *
     * @return построитель правил
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Возвращает режим козыря.
     *
     * @return режим козыря
     */
    public TrumpMode trumpMode() {
        return trumpMode;
    }

    /**
     * Возвращает количество карт, выдаваемых каждому игроку в начале партии.
     *
     * @return начальный размер руки
     */
    public int initialHandSize() {
        return initialHandSize;
    }

    /**
     * Возвращает минимальный размер руки, до которого игроки добирают карты
     * из колоды после раунда.
     *
     * @return минимальный размер руки
     */
    public int minHandSize() {
        return minHandSize;
    }

    /**
     * Построитель настроек партии.
     */
    public static final class Builder {

        private TrumpMode trumpMode = TrumpMode.OPEN;
        private int initialHandSize = 6;
        private int minHandSize = 6;

        private Builder() {
            // построитель создаётся только через GameRules.builder()
        }

        /**
         * Задаёт режим козыря.
         *
         * @param trumpMode режим козыря, не {@code null}
         * @return этот построитель
         */
        public Builder trumpMode(TrumpMode trumpMode) {
            this.trumpMode = trumpMode;
            return this;
        }

        /**
         * Задаёт начальный размер руки.
         *
         * @param initialHandSize количество карт на руки в начале партии
         * @return этот построитель
         * @throws IllegalArgumentException если значение меньше нуля
         */
        public Builder initialHandSize(int initialHandSize) {
            if (initialHandSize < 0) {
                throw new IllegalArgumentException("Начальный размер руки не может быть отрицательным");
            }
            this.initialHandSize = initialHandSize;
            return this;
        }

        /**
         * Задаёт минимальный размер руки для добора.
         *
         * @param minHandSize минимальное число карт после добора
         * @return этот построитель
         * @throws IllegalArgumentException если значение меньше нуля
         */
        public Builder minHandSize(int minHandSize) {
            if (minHandSize < 0) {
                throw new IllegalArgumentException("Минимальный размер руки не может быть отрицательным");
            }
            this.minHandSize = minHandSize;
            return this;
        }

        /**
         * Собирает настройки партии.
         *
         * @return готовые правила
         */
        public GameRules build() {
            return new GameRules(this);
        }
    }
}
