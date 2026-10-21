package com.ctrlzz14.durak.bot;

import com.ctrlzz14.durak.model.Card;
import com.ctrlzz14.durak.model.GamePhase;
import com.ctrlzz14.durak.model.GameState;
import com.ctrlzz14.durak.model.Player;
import com.ctrlzz14.durak.model.Suit;
import com.ctrlzz14.durak.move.Move;
import com.ctrlzz14.durak.rules.CardRules;
import com.ctrlzz14.durak.rules.MoveValidator;
import com.ctrlzz14.durak.strategy.PlayerStrategy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Простейшая стратегия компьютерного игрока.
 *
 * <p>Бот играет «скучной», но разумной манерой:</p>
 * <ul>
 *   <li>атакует самой младшей картой (её проще всего отбить), а если
 *       достоинство совпадает с достоинством уже выложенной карты, подкидывает
 *       остальные карты того же достоинства;</li>
 *   <li>отбивается самой дешёвой подходящей картой, стараясь не тратить
 *       козыри без необходимости;</li>
 *   <li>когда отбиться нечем, забирает карты;</li>
 *   <li>переводит атаку козырной картой, если в игре не меньше трёх игроков:
 *       в партии на двоих перевод приводит к тому, что отбиваться придётся
 *       самому.</li>
 * </ul>
 *
 * <p>Бот никогда не обращается к случайным числам: его решения полностью
 * определяются текущим состоянием партии.</p>
 */
public final class SimpleBotStrategy implements PlayerStrategy {

    private final String title;

    /**
     * Создаёт стратегию компьютерного игрока.
     *
     * @param title название игрока для вывода в консоль
     */
    public SimpleBotStrategy(String title) {
        this.title = title;
    }

    /**
     * Возвращает название игрока.
     *
     * @return название игрока
     */
    public String title() {
        return title;
    }

    @Override
    public Move chooseMove(GameState state, MoveValidator validator) {
        if (state.phase() == GamePhase.DEFENSE) {
            return chooseDefense(state, validator);
        }
        return chooseAttack(state, validator);
    }

    @Override
    public Optional<Move> chooseTransfer(GameState state, MoveValidator validator) {
        if (state.players().size() < 3) {
            return Optional.empty();
        }
        List<Card> cards = validator.legalTransferCards(state.defender());
        List<Card> trumps = cards.stream()
                .filter(card -> card.isTrump(state.trumpSuit()))
                .sorted(Comparator.comparingInt(card -> card.rank().power()))
                .toList();
        if (trumps.isEmpty()) {
            return Optional.empty();
        }
        Card candidate = trumps.get(0);
        Player next = state.nextPlayer(state.defender());
        boolean nextCanDefend = next.hand().cards().stream()
                .anyMatch(card -> CardRules.beats(candidate, card, state.trumpSuit()));
        if (nextCanDefend) {
            return Optional.empty();
        }
        return Optional.of(Move.transfer(state.defender(), List.of(candidate)));
    }

    private Move chooseAttack(GameState state, MoveValidator validator) {
        List<Card> legal = validator.legalAttackCards(state.attacker());
        if (legal.isEmpty()) {
            return Move.endAttack(state.attacker());
        }
        Card lowest = lowestCard(legal, state.trumpSuit());
        List<Card> attack = new ArrayList<>();
        attack.add(lowest);
        int limit = state.segmentLimit() - state.cardsThrownInSegment();
        for (Card card : legal) {
            if (attack.size() >= limit) {
                break;
            }
            if (card.rank() == lowest.rank() && !attack.contains(card)) {
                attack.add(card);
            }
        }
        return Move.attack(state.attacker(), attack);
    }

    private Move chooseDefense(GameState state, MoveValidator validator) {
        List<Card> legal = validator.legalDefenseCards(state.defender());
        if (legal.isEmpty()) {
            return Move.take(state.defender());
        }
        return Move.defend(state.defender(), cheapestCard(legal, state.trumpSuit()));
    }

    /**
     * Возвращает самую младшую карту, стараясь не выбирать козырную.
     *
     * @param cards карты-кандидаты
     * @param trumpSuit козырная масть
     * @return выбранная карта
     */
    private Card lowestCard(List<Card> cards, Suit trumpSuit) {
        List<Card> plain = cards.stream().filter(card -> !card.isTrump(trumpSuit)).toList();
        List<Card> source = plain.isEmpty() ? cards : plain;
        return source.stream()
                .min(Comparator.comparingInt(card -> card.rank().power()))
                .orElseThrow();
    }

    /**
     * Возвращает самую дешёвую карту, способную отбить атаку.
     *
     * @param cards карты-кандидаты
     * @param trumpSuit козырная масть
     * @return выбранная карта
     */
    private Card cheapestCard(List<Card> cards, Suit trumpSuit) {
        List<Card> plain = cards.stream().filter(card -> !card.isTrump(trumpSuit)).toList();
        List<Card> source = plain.isEmpty() ? cards : plain;
        return source.stream()
                .min(Comparator.comparingInt((Card card) -> card.rank().power())
                        .thenComparingInt(card -> card.suit().ordinal()))
                .orElseThrow();
    }
}
