package com.ctrlzz14.durak.model;

import java.util.List;
import java.util.Objects;

/**
 * Игрок партии.
 *
 * <p>Игрок владеет своей рукой и больше ничем в состоянии партии не
 * распоряжается. Идентификатор игрока ({@link #id()}) уникален в пределах
 * партии и используется для адресации игроков в событиях журнала.</p>
 */
public final class Player {

    private final int id;
    private final String name;
    private final Hand hand = new Hand();

    /**
     * Создаёт игрока с пустой рукой.
     *
     * @param id уникальный номер игрока в партии
     * @param name отображаемое имя игрока
     * @throws NullPointerException если имя равно {@code null}
     */
    public Player(int id, String name) {
        this.id = id;
        this.name = Objects.requireNonNull(name, "Имя игрока не должно быть null");
    }

    /**
     * Создаёт игрока с указанными картами в руке.
     *
     * @param id уникальный номер игрока в партии
     * @param name отображаемое имя игрока
     * @param cards начальные карты игрока
     */
    public Player(int id, String name, List<Card> cards) {
        this(id, name);
        hand.addAll(cards);
    }

    /**
     * Возвращает номер игрока.
     *
     * @return номер игрока
     */
    public int id() {
        return id;
    }

    /**
     * Возвращает имя игрока.
     *
     * @return имя игрока
     */
    public String name() {
        return name;
    }

    /**
     * Возвращает руку игрока.
     *
     * @return рука игрока
     */
    public Hand hand() {
        return hand;
    }

    /**
     * Возвращает количество карт в руке игрока.
     *
     * @return число карт
     */
    public int cardCount() {
        return hand.size();
    }

    /**
     * Добавляет карту в руку игрока.
     *
     * @param card добавляемая карта
     */
    public void addCard(Card card) {
        hand.add(card);
    }

    /**
     * Проверяет, остались ли у игрока карты.
     *
     * @return {@code true}, если рука не пуста
     */
    public boolean hasCards() {
        return !hand.isEmpty();
    }

    /**
     * Возвращает имя игрока.
     *
     * @return имя игрока
     */
    @Override
    public String toString() {
        return name;
    }
}
