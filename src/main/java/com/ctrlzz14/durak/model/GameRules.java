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
    private final int stalemateLimit;

    private GameRules(Builder builder) {
        this.trumpMode = builder.trumpMode;
        this.initialHandSize = builder.initialHandSize;
        this.minHandSize = builder.minHandSize;
        this.stalemateLimit = builder.stalemateLimit;
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
     * Возвращает максимальное количество раундов подряд без продвижения,
     * после которого партия признаётся заведённой в тупик.
     *
     * <p>Раунд считается продвижением, если в отбой ушла хотя бы одна карта
     * или у кого-то из игроков стало меньше карт. Если ни одно из условий не
     * выполнено несколько раундов подряд, партия дальше не может
     * продвинуться и завершается ничьей.</p>
     *
     * @return максимальное число раундов без продвижения
     */
    public int stalemateLimit() {
        return stalemateLimit;
    }

    /**
     * Построитель настроек партии.
     */
    public static final class Builder {

        private TrumpMode trumpMode = TrumpMode.OPEN;
        private int initialHandSize = 6;
        private int minHandSize = 6;
        private int stalemateLimit = 3;

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
         * Задаёт максимальное количество раундов подряд без продвижения,
         * после которого партия признаётся заведённой в тупик.
         *
         * @param stalemateLimit количество раундов без продвижения
         * @return этот построитель
         * @throws IllegalArgumentException если значение меньше единицы
         */
        public Builder stalemateLimit(int stalemateLimit) {
            if (stalemateLimit < 1) {
                throw new IllegalArgumentException("Предел раундов без продвижения должен быть не меньше единицы");
            }
            this.stalemateLimit = stalemateLimit;
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
