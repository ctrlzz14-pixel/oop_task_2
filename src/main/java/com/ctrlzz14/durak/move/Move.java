package com.ctrlzz14.durak.move;

import com.ctrlzz14.durak.model.Card;
import com.ctrlzz14.durak.model.Player;

import java.util.List;
import java.util.Objects;

/**
 * Ход: описание действия игрока.
 *
 * <p>Класс неизменяемый: ход описывает намерение, а не результат. Допустимость
 * хода проверяет {@code MoveValidator}, применяет — {@code GameEngine}.</p>
 *
 * <p>Содержимое {@link #cards()} зависит от типа хода:</p>
 * <ul>
 *   <li>{@link MoveType#ATTACK} — выкладываемые карты (одна или несколько);</li>
 *   <li>{@link MoveType#DEFEND} — единственная отбивающая карта;</li>
 *   <li>{@link MoveType#TRANSFER} — карты перевода (одна или несколько);</li>
 *   <li>{@link MoveType#TAKE} и {@link MoveType#END_ATTACK} — карт нет.</li>
 * </ul>
 *
 * @param type тип хода
 * @param player игрок, совершающий ход
 * @param cards карты, участвующие в ходе
 */
public record Move(MoveType type, Player player, List<Card> cards) {

    /**
     * Создаёт ход.
     *
     * @param type тип хода
     * @param player игрок, совершающий ход
     * @param cards карты, участвующие в ходе
     * @throws NullPointerException если тип или игрок равны {@code null}
     */
    public Move {
        Objects.requireNonNull(type, "Тип хода не должен быть null");
        Objects.requireNonNull(player, "Игрок не должен быть null");
        cards = List.copyOf(Objects.requireNonNull(cards, "Список карт не должен быть null"));
    }

    /**
     * Создаёт атаку одной картой.
     *
     * @param player атакующий
     * @param card выкладываемая карта
     * @return ход атаки
     */
    public static Move attack(Player player, Card card) {
        return new Move(MoveType.ATTACK, player, List.of(card));
    }

    /**
     * Создаёт атаку несколькими картами.
     *
     * @param player атакующий
     * @param cards выкладываемые карты
     * @return ход атаки
     */
    public static Move attack(Player player, List<Card> cards) {
        return new Move(MoveType.ATTACK, player, cards);
    }

    /**
     * Создаёт ход защиты.
     *
     * @param player защищающийся
     * @param card отбивающая карта
     * @return ход защиты
     */
    public static Move defend(Player player, Card card) {
        return new Move(MoveType.DEFEND, player, List.of(card));
    }

    /**
     * Создаёт ход перевода атаки.
     *
     * @param player защищающийся, выполняющий перевод
     * @param cards карты перевода
     * @return ход перевода
     */
    public static Move transfer(Player player, List<Card> cards) {
        return new Move(MoveType.TRANSFER, player, cards);
    }

    /**
     * Создаёт ход взятия карт.
     *
     * @param player защищающийся
     * @return ход взятия карт
     */
    public static Move take(Player player) {
        return new Move(MoveType.TAKE, player, List.of());
    }

    /**
     * Создаёт ход завершения атаки.
     *
     * @param player атакующий
     * @return ход завершения атаки
     */
    public static Move endAttack(Player player) {
        return new Move(MoveType.END_ATTACK, player, List.of());
    }
}
