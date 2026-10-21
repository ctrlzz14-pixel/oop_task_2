package com.ctrlzz14.durak.engine;

import com.ctrlzz14.durak.event.AttackEvent;
import com.ctrlzz14.durak.event.CardsDealtEvent;
import com.ctrlzz14.durak.event.DefendEvent;
import com.ctrlzz14.durak.event.GameEndedEvent;
import com.ctrlzz14.durak.event.GameEvent;
import com.ctrlzz14.durak.event.GameJournal;
import com.ctrlzz14.durak.event.GameStartedEvent;
import com.ctrlzz14.durak.event.RefillEvent;
import com.ctrlzz14.durak.event.RoundEndReason;
import com.ctrlzz14.durak.event.RoundEndedEvent;
import com.ctrlzz14.durak.event.TakeEvent;
import com.ctrlzz14.durak.event.TransferEvent;
import com.ctrlzz14.durak.event.TrumpChosenEvent;
import com.ctrlzz14.durak.model.Card;
import com.ctrlzz14.durak.model.Deck;
import com.ctrlzz14.durak.model.EndReason;
import com.ctrlzz14.durak.model.GamePhase;
import com.ctrlzz14.durak.model.GameResult;
import com.ctrlzz14.durak.model.GameRules;
import com.ctrlzz14.durak.model.GameState;
import com.ctrlzz14.durak.model.Player;
import com.ctrlzz14.durak.model.Suit;
import com.ctrlzz14.durak.model.TrumpMode;
import com.ctrlzz14.durak.move.Move;
import com.ctrlzz14.durak.random.GameRandom;
import com.ctrlzz14.durak.rules.IllegalMoveException;
import com.ctrlzz14.durak.rules.MoveValidator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Движок партии: применяет правила к состоянию игры.
 *
 * <p>Движок не знает о консоли: он принимает объекты ходов, проверяет их
 * допустимость, изменяет состояние партии и возвращает список событий,
 * произошедших в результате. Всё, что касается вывода, остаётся в
 * консольном интерфейсе.</p>
 *
 * <p>Все случайные операции выполняются единственным генератором
 * {@link GameRandom}, который создаётся при старте партии, поэтому партия
 * полностью воспроизводима по зерну.</p>
 */
public final class GameEngine {

    private final GameState state;
    private final GameRandom random;
    private final MoveValidator validator;
    private final GameJournal journal = new GameJournal();

    /**
     * Минимальное число карт, которое было у игроков в предыдущем раунде.
     *
     * <p>Значение используется для отслеживания продвижения партии: если у
     * кого-то стало меньше карт, игрок приблизился к победе.</p>
     */
    private int previousMinHandCount;

    /**
     * Создаёт движок для подготовленного состояния партии.
     *
     * <p>Используется в тестах, где состояние собирается вручную из
     * фиксированных наборов карт.</p>
     *
     * @param state состояние партии
     * @param random генератор случайных величин партии
     * @throws NullPointerException если аргументы равны {@code null}
     */
    public GameEngine(GameState state, GameRandom random) {
        this.state = Objects.requireNonNull(state, "Состояние партии не должно быть null");
        this.random = Objects.requireNonNull(random, "Генератор случайных величин не должен быть null");
        this.validator = new MoveValidator(state);
        this.previousMinHandCount = minHandCount();
    }

    /**
     * Начинает новую партию: тасует колоду, раздаёт карты и определяет козыря.
     *
     * @param rules настройки партии
     * @param playerNames имена игроков в порядке хода (минимум два)
     * @param seed зерно генератора случайных величин
     * @return движок готовой к игре партии
     * @throws IllegalArgumentException если игроков меньше двух
     */
    public static GameEngine start(GameRules rules, List<String> playerNames, long seed) {
        Objects.requireNonNull(rules, "Правила не должны быть null");
        Objects.requireNonNull(playerNames, "Список игроков не должен быть null");
        if (playerNames.size() < 2) {
            throw new IllegalArgumentException("В партии должно участвовать не менее двух игроков");
        }
        GameRandom random = new GameRandom(seed);
        Deck deck = Deck.standard();
        deck.shuffle(random);
        Card trumpCard = deck.bottomCard().orElseThrow(() -> new IllegalStateException("Колода пуста"));
        Suit trumpSuit = trumpCard.suit();

        List<Player> players = new ArrayList<>();
        for (int i = 0; i < playerNames.size(); i++) {
            players.add(new Player(i, playerNames.get(i)));
        }
        Map<Player, List<Card>> hands = new LinkedHashMap<>();
        for (Player player : players) {
            List<Card> dealt = deck.draw(rules.initialHandSize());
            dealt.forEach(player::addCard);
            hands.put(player, dealt);
        }

        GameState state = GameState.builder()
                .players(players)
                .deck(deck)
                .rules(rules)
                .seed(seed)
                .trump(trumpSuit, trumpCard)
                .phase(GamePhase.ATTACK)
                .roles(players.get(0), players.get(1))
                .segment(0, players.get(1).cardCount())
                .roundNumber(1)
                .build();

        GameEngine engine = new GameEngine(state, random);
        engine.previousMinHandCount = engine.minHandCount();
        engine.journal.record(new GameStartedEvent(seed, rules, playerNames));
        engine.journal.record(new TrumpChosenEvent(trumpSuit, rules.trumpMode(),
                rules.trumpMode() == TrumpMode.OPEN ? trumpCard : null));
        engine.journal.record(new CardsDealtEvent(hands));
        return engine;
    }

    /**
     * Применяет ход к партии.
     *
     * @param move ход
     * @return список событий, произошедших в результате хода
     * @throws IllegalMoveException если ход недопустим; состояние при этом не меняется
     */
    public List<GameEvent> apply(Move move) {
        validator.validate(move);
        return switch (move.type()) {
            case ATTACK -> applyAttack(move.player(), move.cards());
            case DEFEND -> applyDefend(move.player(), move.cards().get(0));
            case TAKE -> applyTake(move.player());
            case END_ATTACK -> applyEndAttack(move.player());
            case TRANSFER -> applyTransfer(move.player(), move.cards());
        };
    }

    /**
     * Выполняет атаку.
     *
     * @param attacker атакующий
     * @param card выкладываемая карта
     * @return список событий
     */
    public List<GameEvent> attack(Player attacker, Card card) {
        return apply(Move.attack(attacker, card));
    }

    /**
     * Выполняет атаку несколькими картами.
     *
     * @param attacker атакующий
     * @param cards выкладываемые карты
     * @return список событий
     */
    public List<GameEvent> attack(Player attacker, List<Card> cards) {
        return apply(Move.attack(attacker, cards));
    }

    /**
     * Выполняет защиту.
     *
     * @param defender защищающийся
     * @param card отбивающая карта
     * @return список событий
     */
    public List<GameEvent> defend(Player defender, Card card) {
        return apply(Move.defend(defender, card));
    }

    /**
     * Выполняет перевод атаки.
     *
     * <p>Защищающийся, только что отбившийся картой, кладёт на стол карты
     * перевода. Они становятся новой атакой, а отбиваться от них обязан
     * следующий игрок по кругу. Роли меняются: переводивший становится
     * атакующим, следующий игрок — защищающимся.</p>
     *
     * @param defender защищающийся, выполняющий перевод
     * @param cards карты перевода
     * @return список событий
     */
    public List<GameEvent> transfer(Player defender, List<Card> cards) {
        return apply(Move.transfer(defender, cards));
    }

    /**
     * Выполняет взятие карт.
     *
     * @param defender защищающийся
     * @return список событий
     */
    public List<GameEvent> take(Player defender) {
        return apply(Move.take(defender));
    }

    /**
     * Завершает атаку и раунд.
     *
     * @param attacker атакующий
     * @return список событий
     */
    public List<GameEvent> endAttack(Player attacker) {
        return apply(Move.endAttack(attacker));
    }

    /**
     * Возвращает состояние партии.
     *
     * @return текущее состояние
     */
    public GameState state() {
        return state;
    }

    /**
     * Возвращает журнал событий партии.
     *
     * @return журнал событий
     */
    public GameJournal journal() {
        return journal;
    }

    /**
     * Возвращает генератор случайных величин партии.
     *
     * @return генератор партии
     */
    public GameRandom random() {
        return random;
    }

    /**
     * Возвращает результат партии.
     *
     * @return результат или {@link Optional#empty()}, если партия не завершена
     */
    public Optional<GameResult> result() {
        return state.result();
    }

    private List<GameEvent> applyAttack(Player attacker, List<Card> cards) {
        for (Card card : cards) {
            attacker.hand().remove(card);
            state.table().addAttack(card);
        }
        state.addCardsThrown(cards.size());
        state.clearTransferOffer();
        state.setPhase(GamePhase.DEFENSE);
        return List.of(journal.record(new AttackEvent(attacker, cards)));
    }

    private List<GameEvent> applyDefend(Player defender, Card card) {
        Card attacked = state.table().currentAttackCard()
                .orElseThrow(() -> new IllegalStateException("На столе нет карты, которую нужно отбить"));
        defender.hand().remove(card);
        state.table().addDefense(card);
        if (state.table().uncoveredCount() == 0) {
            state.setPhase(GamePhase.ATTACK);
            state.offerTransfer(attacked);
        } else {
            state.setPhase(GamePhase.DEFENSE);
        }
        return List.of(journal.record(new DefendEvent(defender, attacked, card)));
    }

    private List<GameEvent> applyTransfer(Player defender, List<Card> cards) {
        for (Card card : cards) {
            defender.hand().remove(card);
            state.table().addAttack(card);
        }
        Player newDefender = state.nextPlayer(defender);
        state.setRoles(defender, newDefender);
        state.resetSegment(newDefender.cardCount());
        state.addCardsThrown(cards.size());
        state.clearTransferOffer();
        state.setPhase(GamePhase.DEFENSE);
        return List.of(journal.record(new TransferEvent(defender, cards, newDefender)));
    }

    private List<GameEvent> applyTake(Player defender) {        List<Card> taken = state.table().allCards();
        defender.hand().addAll(taken);
        state.table().clear();
        state.clearTransferOffer();
        List<GameEvent> events = new ArrayList<>();
        events.add(journal.record(new TakeEvent(defender, taken)));
        events.addAll(endRound(RoundEndReason.CARDS_TAKEN, 0));
        return events;
    }

    private List<GameEvent> applyEndAttack(Player attacker) {
        List<Card> discarded = state.table().allCards();
        state.discardPile().addAll(discarded);
        state.table().clear();
        state.clearTransferOffer();
        List<GameEvent> events = new ArrayList<>();
        events.addAll(endRound(RoundEndReason.ATTACK_FINISHED, discarded.size()));
        return events;
    }

    /**
     * Завершает раунд: добор карт, проверка условий окончания партии и
     * подготовка следующего раунда.
     *
     * @param reason причина завершения раунда
     * @param discardedCount количество карт, ушедших в отбой за раунд
     * @return список событий
     */
    private List<GameEvent> endRound(RoundEndReason reason, int discardedCount) {
        state.setPhase(GamePhase.ROUND_END);
        List<GameEvent> events = new ArrayList<>();
        events.add(journal.record(new RoundEndedEvent(state.roundNumber(), discardedCount, reason)));

        Optional<GameResult> finished = checkPlayersFinished();
        if (finished.isPresent()) {
            return finishGame(finished.get(), events);
        }

        Player nextAttacker = state.nextPlayer(state.defender());
        events.addAll(refill(nextAttacker));
        updateProgressCounters(discardedCount);

        Optional<GameResult> stalemate = checkStalemate();
        if (stalemate.isPresent()) {
            return finishGame(stalemate.get(), events);
        }

        Player nextDefender = state.nextPlayer(nextAttacker);
        state.setRoles(nextAttacker, nextDefender);
        state.resetSegment(nextDefender.cardCount());
        state.incrementRound();
        state.setPhase(GamePhase.ATTACK);
        return events;
    }

    /**
     * Обновляет счётчики продвижения партии.
     *
     * <p>Продвижением считается раунд, в котором:</p>
     * <ul>
     *     <li>в отбой ушла хотя бы одна карта;</li>
     *     <li>кто-то добрал карты из колоды;</li>
     *     <li>у кого-то стало меньше карт, то есть игрок приблизился к победе.</li>
     * </ul>
     *
     * @param discardedCount количество карт, ушедших в отбой за раунд
     */
    private void updateProgressCounters(int discardedCount) {
        if (discardedCount > 0) {
            state.resetRoundsWithoutDiscard();
        } else {
            state.incrementRoundsWithoutDiscard();
        }
        if (minHandCount() < previousMinHandCount) {
            previousMinHandCount = minHandCount();
            state.resetRoundsWithoutProgress();
        } else {
            state.incrementRoundsWithoutProgress();
        }
    }

    /**
     * Добирает карты из колоды игрокам, у которых рука меньше минимальной.
     *
     * @param startPlayer игрок, с которого начинается добор
     * @return список событий добора
     */
    private List<GameEvent> refill(Player startPlayer) {
        List<GameEvent> events = new ArrayList<>();
        boolean progressed = true;
        while (progressed && !state.deck().isEmpty()) {
            progressed = false;
            for (Player player : playersStartingFrom(startPlayer)) {
                if (state.deck().isEmpty()) {
                    break;
                }
                if (player.cardCount() < state.rules().minHandSize()) {
                    Card card = state.deck().draw();
                    player.addCard(card);
                    events.add(journal.record(new RefillEvent(player, List.of(card))));
                    progressed = true;
                }
            }
        }
        return events;
    }

    /**
     * Проверяет, остались ли у кого-нибудь из игроков карты.
     *
     * @return результат партии, если игрок вышел из партии
     */
    private Optional<GameResult> checkPlayersFinished() {
        List<Player> finished = state.players().stream()
                .filter(player -> !player.hasCards())
                .toList();
        if (finished.isEmpty()) {
            return Optional.empty();
        }
        List<Player> losers = state.players().stream()
                .filter(Player::hasCards)
                .sorted(Comparator.comparingInt(Player::cardCount).reversed())
                .toList();
        return Optional.of(new GameResult(finished, losers, EndReason.PLAYER_FINISHED));
    }

    /**
     * Проверяет, не завела ли партия в тупик.
     *
     * <p>Тупик наступает, когда партия несколько раундов подряд не
     * продвигается: в отбой ничего не уходит и ни у кого не становится меньше
     * карт. В такой ситуации партия дальше не может двигаться, поэтому она
     * завершается ничьей: побеждают игроки с наименьшим числом карт.</p>
     *
     * @return результат партии, если наступил тупик
     */
    private Optional<GameResult> checkStalemate() {
        int limit = state.rules().stalemateLimit();
        if (state.roundsWithoutDiscard() < limit || state.roundsWithoutProgress() < limit) {
            return Optional.empty();
        }
        int minCards = minHandCount();
        List<Player> winners = state.players().stream()
                .filter(player -> player.cardCount() == minCards)
                .toList();
        List<Player> losers = state.players().stream()
                .filter(player -> player.cardCount() != minCards)
                .sorted(Comparator.comparingInt(Player::cardCount).reversed())
                .toList();
        return Optional.of(new GameResult(winners, losers, EndReason.STALEMATE));
    }

    /**
     * Возвращает минимальное количество карт среди игроков.
     *
     * @return наименьшее число карт в руке
     */
    private int minHandCount() {
        return state.players().stream().mapToInt(Player::cardCount).min().orElse(0);
    }

    /**
     * Завершает партию и фиксирует результат.
     *
     * @param result результат партии
     * @param events накопленные события раунда
     * @return список событий
     */
    private List<GameEvent> finishGame(GameResult result, List<GameEvent> events) {
        state.setResult(result);
        state.setPhase(GamePhase.GAME_OVER);
        events.add(journal.record(new GameEndedEvent(result)));
        return events;
    }

    /**
     * Возвращает игроков в порядке хода, начиная с указанного.
     *
     * @param startPlayer игрок, с которого начинается обход
     * @return список игроков
     */
    private List<Player> playersStartingFrom(Player startPlayer) {
        List<Player> players = state.players();
        int start = players.indexOf(startPlayer);
        List<Player> ordered = new ArrayList<>(players.size());
        for (int i = 0; i < players.size(); i++) {
            ordered.add(players.get((start + i) % players.size()));
        }
        return ordered;
    }
}
