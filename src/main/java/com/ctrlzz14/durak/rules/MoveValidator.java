package com.ctrlzz14.durak.rules;

import com.ctrlzz14.durak.model.Card;
import com.ctrlzz14.durak.model.GamePhase;
import com.ctrlzz14.durak.model.GameState;
import com.ctrlzz14.durak.model.Player;
import com.ctrlzz14.durak.model.Rank;
import com.ctrlzz14.durak.move.Move;
import com.ctrlzz14.durak.move.MoveType;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Проверка допустимости ходов.
 *
 * <p>Валидатор — единственное место, где принимаются решения о том, что
 * считать корректным ходом. Он не меняет состояние партии: либо ход
 * признаётся допустимым, либо выбрасывается {@link IllegalMoveException} с
 * описанием причины. Благодаря этому состояние остаётся неизменным при
 * отклонённом ходе.</p>
 *
 * <p>Валидатор также отвечает на вопросы «какие карты можно сыграть сейчас»:
 * эти запросы используются консольным интерфейсом для подсказок и будущими
 * ботами для выбора хода.</p>
 */
public final class MoveValidator {

    private final GameState state;

    /**
     * Создаёт валидатор для указанного состояния партии.
     *
     * @param state состояние партии
     * @throws NullPointerException если состояние равно {@code null}
     */
    public MoveValidator(GameState state) {
        this.state = Objects.requireNonNull(state, "Состояние партии не должно быть null");
    }

    /**
     * Проверяет ход на соответствие правилам и текущей фазе партии.
     *
     * @param move проверяемый ход
     * @throws IllegalMoveException если ход недопустим
     */
    public void validate(Move move) {
        Objects.requireNonNull(move, "Ход не должен быть null");
        switch (move.type()) {
            case ATTACK -> validateAttack(move.player(), move.cards());
            case DEFEND -> validateDefend(move.player(), move.cards());
            case TAKE -> validateTake(move.player());
            case END_ATTACK -> validateEndAttack(move.player());
            case TRANSFER -> validateTransfer(move.player(), move.cards());
        }
    }

    /**
     * Проверяет допустимость атаки.
     *
     * <p>Требования к атаке:</p>
     * <ul>
     *   <li>ходить должен атакующий, партия должна быть в фазе атаки;</li>
     *   <li>карты должны лежать в руке атакующего;</li>
     *   <li>число карт в атаке не должно превышать число карт защищающегося
     *       на момент начала атаки;</li>
     *   <li>подкидывать можно только карты, достоинство которых уже есть
     *       на столе (первая карта раунда может быть любой).</li>
     * </ul>
     *
     * @param attacker атакующий
     * @param cards выкладываемые карты
     * @throws IllegalMoveException если атака недопустима
     */
    public void validateAttack(Player attacker, List<Card> cards) {
        requirePhase(GamePhase.ATTACK, "атака");
        requireAttacker(attacker);
        if (cards.isEmpty()) {
            throw new IllegalMoveException("Атака должна содержать хотя бы одну карту");
        }
        for (Card card : cards) {
            if (!attacker.hand().contains(card)) {
                throw new IllegalMoveException("У игрока " + attacker.name() + " нет карты " + card);
            }
        }
        if (state.cardsThrownInSegment() + cards.size() > state.segmentLimit()) {
            throw new IllegalMoveException("Нельзя выложить больше карт, чем есть у защищающегося ("
                    + state.segmentLimit() + ")");
        }
        Set<Rank> ranksOnTable = ranksOnTable();
        for (Card card : cards) {
            if (!ranksOnTable.isEmpty() && !ranksOnTable.contains(card.rank())) {
                throw new IllegalMoveException("Нельзя подкинуть карту " + card
                        + ": такого достоинства ещё нет на столе");
            }
            ranksOnTable.add(card.rank());
        }
    }

    /**
     * Проверяет допустимость защиты.
     *
     * @param defender защищающийся
     * @param cards отбивающая карта (ровно одна)
     * @throws IllegalMoveException если защита недопустима
     */
    public void validateDefend(Player defender, List<Card> cards) {
        requirePhase(GamePhase.DEFENSE, "защита");
        requireDefender(defender);
        if (cards.size() != 1) {
            throw new IllegalMoveException("Защита выполняется одной картой");
        }
        Card card = cards.get(0);
        if (!defender.hand().contains(card)) {
            throw new IllegalMoveException("У игрока " + defender.name() + " нет карты " + card);
        }
        Card attacked = state.table().currentAttackCard()
                .orElseThrow(() -> new IllegalMoveException("На столе нет карты, которую нужно отбить"));
        if (!CardRules.beats(attacked, card, state.trumpSuit())) {
            throw new IllegalMoveException("Карта " + card + " не бьёт карту " + attacked);
        }
    }

    /**
     * Проверяет допустимость взятия карт.
     *
     * @param defender защищающийся
     * @throws IllegalMoveException если брать карты недопустимо
     */
    public void validateTake(Player defender) {
        requirePhase(GamePhase.DEFENSE, "взятие карт");
        requireDefender(defender);
    }

    /**
     * Проверяет допустимость завершения атаки.
     *
     * @param attacker атакующий
     * @throws IllegalMoveException если завершить атаку недопустимо
     */
    public void validateEndAttack(Player attacker) {
        requirePhase(GamePhase.ATTACK, "завершение атаки");
        requireAttacker(attacker);
        if (state.table().isEmpty()) {
            throw new IllegalMoveException("Нельзя завершить раунд, в котором не выложено ни одной карты");
        }
    }

    /**
     * Проверяет допустимость перевода атаки.
     *
     * <p>Перевод реализуется в отдельном этапе разработки; до его появления
     * любой ход типа {@link MoveType#TRANSFER} отклоняется.</p>
     *
     * @param defender защищающийся
     * @param cards карты перевода
     * @throws IllegalMoveException всегда — перевод ещё не реализован
     */
    public void validateTransfer(Player defender, List<Card> cards) {
        throw new IllegalMoveException("Перевод атаки в текущей версии не поддерживается");
    }

    /**
     * Возвращает карты, которые атакующий может выложить прямо сейчас.
     *
     * @param attacker атакующий
     * @return список допустимых карт (может быть пустым)
     */
    public List<Card> legalAttackCards(Player attacker) {
        if (state.phase() != GamePhase.ATTACK || state.attacker() != attacker) {
            return List.of();
        }
        if (state.cardsThrownInSegment() >= state.segmentLimit()) {
            return List.of();
        }
        Set<Rank> ranksOnTable = ranksOnTable();
        return attacker.hand().cards().stream()
                .filter(card -> ranksOnTable.isEmpty() || ranksOnTable.contains(card.rank()))
                .toList();
    }

    /**
     * Возвращает карты, которыми защищающийся может отбиться прямо сейчас.
     *
     * @param defender защищающийся
     * @return список допустимых отбивающих карт (может быть пустым)
     */
    public List<Card> legalDefenseCards(Player defender) {
        if (state.phase() != GamePhase.DEFENSE || state.defender() != defender) {
            return List.of();
        }
        Card attacked = state.table().currentAttackCard().orElse(null);
        if (attacked == null) {
            return List.of();
        }
        return defender.hand().cards().stream()
                .filter(card -> CardRules.beats(attacked, card, state.trumpSuit()))
                .toList();
    }

    /**
     * Требует, чтобы партия находилась в указанной фазе.
     *
     * @param expected ожидаемая фаза
     * @param action название действия для сообщения об ошибке
     */
    private void requirePhase(GamePhase expected, String action) {
        if (state.phase() != expected) {
            throw new IllegalMoveException("Сейчас нельзя выполнить «" + action + "»: партия находится в фазе "
                    + state.phase());
        }
    }

    /**
     * Требует, чтобы ход делал атакующий.
     *
     * @param player проверяемый игрок
     */
    private void requireAttacker(Player player) {
        if (state.attacker() != player) {
            throw new IllegalMoveException("Ходить должен атакующий: " + state.attacker().name());
        }
    }

    /**
     * Требует, чтобы ход делал защищающийся.
     *
     * @param player проверяемый игрок
     */
    private void requireDefender(Player player) {
        if (state.defender() != player) {
            throw new IllegalMoveException("Ходить должен защищающийся: " + state.defender().name());
        }
    }

    /**
     * Возвращает достоинства всех карт, лежащих на столе.
     *
     * @return множество достоинств
     */
    private Set<Rank> ranksOnTable() {
        Set<Rank> ranks = new LinkedHashSet<>();
        state.table().allCards().forEach(card -> ranks.add(card.rank()));
        return ranks;
    }
}
