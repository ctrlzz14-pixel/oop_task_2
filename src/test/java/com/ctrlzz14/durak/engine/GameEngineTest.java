package com.ctrlzz14.durak.engine;

import com.ctrlzz14.durak.event.AttackEvent;
import com.ctrlzz14.durak.event.DefendEvent;
import com.ctrlzz14.durak.event.EventType;
import com.ctrlzz14.durak.event.GameEndedEvent;
import com.ctrlzz14.durak.event.RefillEvent;
import com.ctrlzz14.durak.event.RoundEndReason;
import com.ctrlzz14.durak.event.RoundEndedEvent;
import com.ctrlzz14.durak.event.TakeEvent;
import com.ctrlzz14.durak.model.Card;
import com.ctrlzz14.durak.model.EndReason;
import com.ctrlzz14.durak.model.GamePhase;
import com.ctrlzz14.durak.model.GameResult;
import com.ctrlzz14.durak.model.GameRules;
import com.ctrlzz14.durak.model.GameState;
import com.ctrlzz14.durak.model.Player;
import com.ctrlzz14.durak.model.Rank;
import com.ctrlzz14.durak.model.Suit;
import com.ctrlzz14.durak.model.TrumpMode;
import com.ctrlzz14.durak.rules.IllegalMoveException;
import com.ctrlzz14.durak.support.TestGame;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Движок партии")
class GameEngineTest {

    private static final Card SIX_HEARTS = new Card(Rank.SIX, Suit.HEARTS);
    private static final Card SEVEN_HEARTS = new Card(Rank.SEVEN, Suit.HEARTS);
    private static final Card NINE_SPADES = new Card(Rank.NINE, Suit.SPADES);
    private static final Card TEN_CLUBS = new Card(Rank.TEN, Suit.CLUBS);
    private static final Card NINE_CLUBS = new Card(Rank.NINE, Suit.CLUBS);

    private static GameEngine engine() {
        return TestGame.create()
                .trump(Suit.CLUBS, TrumpMode.OPEN)
                .player("Атакующий", SIX_HEARTS, NINE_SPADES)
                .player("Защищающийся", SEVEN_HEARTS, TEN_CLUBS)
                .player("Третий", NINE_CLUBS)
                .build();
    }

    @Test
    @DisplayName("атака и защита переводят партию в фазу атаки")
    void attackAndDefendReturnToAttackPhase() {
        GameEngine engine = engine();

        engine.attack(engine.state().attacker(), SIX_HEARTS);
        assertEquals(GamePhase.DEFENSE, engine.state().phase());

        engine.defend(engine.state().defender(), SEVEN_HEARTS);
        assertEquals(GamePhase.ATTACK, engine.state().phase());
        assertEquals(1, engine.state().table().attackCards().size());
        assertEquals(1, engine.state().table().defenseCards().size());
    }

    @Test
    @DisplayName("завершённая атака отправляет карты в отбой")
    void finishedAttackGoesToDiscardPile() {
        GameEngine engine = engine();

        engine.attack(engine.state().attacker(), SIX_HEARTS);
        engine.defend(engine.state().defender(), SEVEN_HEARTS);
        engine.endAttack(engine.state().attacker());

        assertEquals(2, engine.state().discardPile().size());
        assertTrue(engine.state().table().isEmpty());
        assertEquals(2, engine.state().roundNumber());
    }

    @Test
    @DisplayName("после раунда ход переходит к следующему игроку")
    void nextPlayerBecomesAttacker() {
        GameEngine engine = engine();

        engine.attack(engine.state().attacker(), SIX_HEARTS);
        engine.defend(engine.state().defender(), SEVEN_HEARTS);
        engine.endAttack(engine.state().attacker());

        assertEquals("Третий", engine.state().attacker().name());
        assertEquals("Атакующий", engine.state().defender().name());
    }

    @Test
    @DisplayName("защищающийся забирает все карты стола")
    void defenderTakesAllTableCards() {
        GameEngine engine = engine();
        Player defender = engine.state().defender();
        int before = defender.cardCount();

        engine.attack(engine.state().attacker(), SIX_HEARTS);
        engine.take(defender);

        assertEquals(before + 1, defender.cardCount());
        assertTrue(engine.state().table().isEmpty());
        assertEquals(0, engine.state().discardPile().size());
    }

    @Test
    @DisplayName("игроки с маленькой рукой добирают карты из колоды")
    void playersRefillFromDeck() {
        GameEngine engine = TestGame.create()
                .trump(Suit.CLUBS, TrumpMode.OPEN)
                .rules(GameRules.builder().minHandSize(3).build())
                .deck(new Card(Rank.ACE, Suit.HEARTS), new Card(Rank.QUEEN, Suit.HEARTS),
                        new Card(Rank.KING, Suit.HEARTS), new Card(Rank.JACK, Suit.HEARTS),
                        new Card(Rank.TEN, Suit.HEARTS), new Card(Rank.NINE, Suit.DIAMONDS),
                        new Card(Rank.EIGHT, Suit.DIAMONDS))
                .player("Атакующий", SIX_HEARTS, NINE_SPADES)
                .player("Защищающийся", SEVEN_HEARTS, TEN_CLUBS)
                .player("Третий", NINE_CLUBS)
                .build();

        engine.attack(engine.state().attacker(), SIX_HEARTS);
        engine.defend(engine.state().defender(), SEVEN_HEARTS);
        engine.endAttack(engine.state().attacker());

        assertEquals(3, engine.state().playerAt(0).cardCount());
        assertEquals(3, engine.state().playerAt(1).cardCount());
        assertEquals(3, engine.state().playerAt(2).cardCount());
        assertEquals(6, engine.journal().eventsOfType(EventType.REFILL).size());
    }

    @Test
    @DisplayName("добор фиксируется событиями для каждой выданной карты")
    void refillProducesEvents() {
        GameEngine engine = TestGame.create()
                .trump(Suit.CLUBS, TrumpMode.OPEN)
                .rules(GameRules.builder().minHandSize(3).build())
                .deck(new Card(Rank.ACE, Suit.HEARTS), new Card(Rank.QUEEN, Suit.HEARTS),
                        new Card(Rank.KING, Suit.HEARTS), new Card(Rank.JACK, Suit.HEARTS),
                        new Card(Rank.TEN, Suit.HEARTS), new Card(Rank.NINE, Suit.DIAMONDS),
                        new Card(Rank.EIGHT, Suit.DIAMONDS))
                .player("Атакующий", SIX_HEARTS, NINE_SPADES)
                .player("Защищающийся", SEVEN_HEARTS, TEN_CLUBS)
                .player("Третий", NINE_CLUBS)
                .build();

        engine.attack(engine.state().attacker(), SIX_HEARTS);
        engine.defend(engine.state().defender(), SEVEN_HEARTS);
        engine.endAttack(engine.state().attacker());

        List<RefillEvent> refills = engine.journal().eventsOfType(EventType.REFILL).stream()
                .map(RefillEvent.class::cast)
                .toList();
        assertFalse(refills.isEmpty());
        assertEquals(6, refills.size());
        assertTrue(refills.stream().allMatch(event -> event.cards().size() == 1));
    }

    @Test
    @DisplayName("партия заканчивается, когда у игрока не осталось карт")
    void gameEndsWhenPlayerHasNoCards() {
        GameEngine engine = TestGame.create()
                .trump(Suit.CLUBS, TrumpMode.OPEN)
                .rules(GameRules.builder().minHandSize(1).build())
                .player("Атакующий", SIX_HEARTS)
                .player("Защищающийся", SEVEN_HEARTS)
                .player("Третий", TEN_CLUBS)
                .build();

        engine.attack(engine.state().attacker(), SIX_HEARTS);
        engine.defend(engine.state().defender(), SEVEN_HEARTS);
        engine.endAttack(engine.state().attacker());

        assertTrue(engine.state().isGameOver());
        GameResult result = engine.result().orElseThrow();
        assertEquals(EndReason.PLAYER_FINISHED, result.reason());
        assertEquals("Атакующий", result.winners().get(0).name());
        assertEquals(GamePhase.GAME_OVER, engine.state().phase());
    }

    @Test
    @DisplayName("события партии записываются в журнал")
    void eventsAreRecordedInJournal() {
        GameEngine engine = engine();

        engine.attack(engine.state().attacker(), SIX_HEARTS);
        engine.defend(engine.state().defender(), SEVEN_HEARTS);
        engine.endAttack(engine.state().attacker());

        List<AttackEvent> attacks = engine.journal().eventsOfType(EventType.ATTACK).stream()
                .map(AttackEvent.class::cast)
                .toList();
        List<DefendEvent> defends = engine.journal().eventsOfType(EventType.DEFEND).stream()
                .map(DefendEvent.class::cast)
                .toList();
        List<RoundEndedEvent> rounds = engine.journal().eventsOfType(EventType.ROUND_ENDED).stream()
                .map(RoundEndedEvent.class::cast)
                .toList();

        assertEquals(1, attacks.size());
        assertEquals(1, defends.size());
        assertEquals(1, rounds.size());
        assertEquals(RoundEndReason.ATTACK_FINISHED, rounds.get(0).reason());
        assertEquals(2, rounds.get(0).discardedCount());
    }

    @Test
    @DisplayName("взятие карт завершает раунд и записывается в журнал")
    void takeIsRecordedInJournal() {
        GameEngine engine = engine();

        engine.attack(engine.state().attacker(), SIX_HEARTS);
        engine.take(engine.state().defender());

        List<TakeEvent> takes = engine.journal().eventsOfType(EventType.TAKE).stream()
                .map(TakeEvent.class::cast)
                .toList();
        assertEquals(1, takes.size());
        assertEquals(1, takes.get(0).takenCards().size());
        assertEquals(RoundEndReason.CARDS_TAKEN,
                engine.journal().eventsOfType(EventType.ROUND_ENDED).stream()
                        .map(RoundEndedEvent.class::cast)
                        .findFirst()
                        .orElseThrow()
                        .reason());
    }

    @Test
    @DisplayName("недопустимый ход не меняет состояние партии")
    void illegalMoveKeepsStateUnchanged() {
        GameEngine engine = engine();
        int cardsBefore = engine.state().attacker().cardCount();

        assertThrows(IllegalMoveException.class,
                () -> engine.attack(engine.state().attacker(), TEN_CLUBS));

        assertEquals(cardsBefore, engine.state().attacker().cardCount());
        assertTrue(engine.state().table().isEmpty());
    }

    @Test
    @DisplayName("партия завершается событием окончания")
    void gameEndedEventIsRecorded() {
        GameEngine engine = TestGame.create()
                .trump(Suit.CLUBS, TrumpMode.OPEN)
                .rules(GameRules.builder().minHandSize(1).build())
                .player("Атакующий", SIX_HEARTS)
                .player("Защищающийся", SEVEN_HEARTS)
                .player("Третий", TEN_CLUBS)
                .build();

        engine.attack(engine.state().attacker(), SIX_HEARTS);
        engine.defend(engine.state().defender(), SEVEN_HEARTS);
        engine.endAttack(engine.state().attacker());

        List<GameEndedEvent> ended = engine.journal().eventsOfType(EventType.GAME_ENDED).stream()
                .map(GameEndedEvent.class::cast)
                .toList();
        assertEquals(1, ended.size());
        assertFalse(ended.get(0).result().winners().isEmpty());
    }

    @Test
    @DisplayName("движок и вызывающий код видят одно и то же состояние")
    void stateIsSharedWithCaller() {
        GameEngine engine = engine();
        GameState state = engine.state();

        engine.attack(state.attacker(), SIX_HEARTS);

        assertEquals(1, state.table().attackCards().size());
        assertEquals("Атакующий", state.attacker().name());
        assertEquals(2, state.attacker().cardCount() + state.table().attackCards().size());
    }
}
