package com.ctrlzz14.durak.rules;

import com.ctrlzz14.durak.model.Card;
import com.ctrlzz14.durak.model.Suit;

/**
 * Правила сравнения карт.
 *
 * <p>Собраны в одном месте, чтобы правило отбивания одинаково использовалось
 * валидатором ходов, консольным интерфейсом (для подсказок) и будущими
 * ботами.</p>
 */
public final class CardRules {

    private CardRules() {
        // утилитный класс
    }

    /**
     * Определяет, бьёт ли карта {@code defense} карту {@code attacked}.
     *
     * <p>Правила отбивания:</p>
     * <ul>
     *   <li>козырь бьёт любую некозырную карту;</li>
     *   <li>козырь бьётся только более старшим козырем;</li>
     *   <li>некозырная карта бьёт некозырную карту той же масти, если она старше.</li>
     * </ul>
     *
     * @param attacked атакуемая карта
     * @param defense отбивающая карта
     * @param trumpSuit козырная масть
     * @return {@code true}, если карта {@code defense} бьёт карту {@code attacked}
     */
    public static boolean beats(Card attacked, Card defense, Suit trumpSuit) {
        boolean attackedIsTrump = attacked.isTrump(trumpSuit);
        boolean defenseIsTrump = defense.isTrump(trumpSuit);
        if (attackedIsTrump) {
            return defenseIsTrump && defense.rank().isHigherThan(attacked.rank());
        }
        if (defenseIsTrump) {
            return true;
        }
        return defense.suit() == attacked.suit() && defense.rank().isHigherThan(attacked.rank());
    }

    /**
     * Проверяет, является ли карта козырной.
     *
     * @param card проверяемая карта
     * @param trumpSuit козырная масть
     * @return {@code true}, если карта козырная
     */
    public static boolean isTrump(Card card, Suit trumpSuit) {
        return card.isTrump(trumpSuit);
    }
}
