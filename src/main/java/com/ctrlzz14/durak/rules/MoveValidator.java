package com.ctrlzz14.durak.rules;

import com.ctrlzz14.durak.model.Card;
import com.ctrlzz14.durak.model.GamePhase;
import com.ctrlzz14.durak.model.GameState;
import com.ctrlzz14.durak.model.Player;
import com.ctrlzz14.durak.model.Rank;
import com.ctrlzz14.durak.model.TrumpMode;
import com.ctrlzz14.durak.move.Move;

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
     * <p>Требования к переводу:</p>
     * <ul>
     *   <li>переводить может только защищающийся, сразу после того как он
     *       отбился картой (в этот момент партия находится в фазе атаки,
     *       но ходить должен защищающийся);</li>
     *   <li>карты перевода должны лежать в руке защищающегося;</li>
     *   <li>достоинство карты перевода должно совпадать с достоинством
     *       отбитой карты; при открытом козыре переводить можно также любой
     *       козырной картой;</li>
     *   <li>число карт перевода не должно превышать число карт следующего
     *       по кругу игрока, который обязан отбить перевод;</li>
     *   <li>перевод возможен только в партии на троих и более игроков: в
     *       партии на двоих следующим по кругу был бы сам защищающийся.</li>
     * </ul>
     *
     * @param defender защищающийся, выполняющий перевод
     * @param cards карты перевода
     * @throws IllegalMoveException если перевод недопустим
     */
    public void validateTransfer(Player defender, List<Card> cards) {
        requirePhase(GamePhase.ATTACK, "перевод атаки");
        requireDefender(defender);
        if (state.players().size() < 3) {
            throw new IllegalMoveException("Перевод возможен только в партии на троих и более игроков");
        }
        if (cards.isEmpty()) {
            throw new IllegalMoveException("Перевод должен содержать хотя бы одну карту");
        }
        if (!state.isTransferAvailable()) {
            throw new IllegalMoveException("Переводить можно только сразу после того, как защищающийся отбился картой");
        }
        for (Card card : cards) {
            if (!defender.hand().contains(card)) {
                throw new IllegalMoveException("У игрока " + defender.name() + " нет карты " + card);
            }
        }
        Card baseCard = state.transferBaseCard()
                .orElseThrow(() -> new IllegalMoveException("Переводить нечего: атака ещё не отбита"));
        for (Card card : cards) {
            if (!isValidTransferCard(baseCard, card)) {
                throw new IllegalMoveException("Карта " + card + " не подходит для перевода карты " + baseCard);
            }
        }
        Player newDefender = state.nextPlayer(defender);
        if (newDefender.cardCount() < cards.size()) {
            throw new IllegalMoveException("У игрока " + newDefender.name() + " всего " + newDefender.cardCount()
                    + " карт, перевести больше нельзя");
        }
    }

    /**
     * Проверяет, подходит ли карта для перевода отбитой карты.
     *
     * @param baseCard отбитая карта
     * @param card карта перевода
     * @return {@code true}, если картой можно перевести
     */
    private boolean isValidTransferCard(Card baseCard, Card card) {
        if (card.rank() == baseCard.rank()) {
            return true;
        }
        return state.rules().trumpMode() == TrumpMode.OPEN && card.isTrump(state.trumpSuit());
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
     * Возвращает карты, которыми защищающийся может перевести атаку прямо
     * сейчас.
     *
     * @param defender защищающийся
     * @return список допустимых карт перевода (может быть пустым)
     */
    public List<Card> legalTransferCards(Player defender) {
        if (state.phase() != GamePhase.ATTACK || state.defender() != defender || !state.isTransferAvailable()) {
            return List.of();
        }
        if (state.players().size() < 3) {
            return List.of();
        }
        Card baseCard = state.transferBaseCard().orElse(null);
        if (baseCard == null || state.nextPlayer(defender).cardCount() == 0) {
            return List.of();
        }
        return defender.hand().cards().stream()
                .filter(card -> isValidTransferCard(baseCard, card))
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
