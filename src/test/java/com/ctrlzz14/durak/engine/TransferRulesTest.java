package com.ctrlzz14.durak.engine;

import com.ctrlzz14.durak.event.EventType;
import com.ctrlzz14.durak.event.TransferEvent;
import com.ctrlzz14.durak.model.Card;
import com.ctrlzz14.durak.model.GamePhase;
import com.ctrlzz14.durak.model.GameState;
import com.ctrlzz14.durak.model.Player;
import com.ctrlzz14.durak.model.Rank;
import com.ctrlzz14.durak.model.Suit;
import com.ctrlzz14.durak.model.TrumpMode;
import com.ctrlzz14.durak.rules.IllegalMoveException;
import com.ctrlzz14.durak.rules.MoveValidator;
import com.ctrlzz14.durak.support.TestGame;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Перевод атаки")
class TransferRulesTest {

    private static final Card SIX_SPADES = new Card(Rank.SIX, Suit.SPADES);
    private static final Card SIX_HEARTS = new Card(Rank.SIX, Suit.HEARTS);
    private static final Card SEVEN_SPADES = new Card(Rank.SEVEN, Suit.SPADES);
    private static final Card NINE_CLUBS = new Card(Rank.NINE, Suit.CLUBS);
    private static final Card NINE_HEARTS = new Card(Rank.NINE, Suit.HEARTS);

    private static GameEngine engine(TrumpMode mode, Card... thirdPlayerCards) {
        return TestGame.create()
                .trump(Suit.HEARTS, mode)
                .player("Первый", SIX_SPADES)
                .player("Второй", SEVEN_SPADES, SIX_HEARTS, NINE_CLUBS, NINE_HEARTS)
                .player("Третий", thirdPlayerCards)
                .build();
    }

    @Test
    @DisplayName("перевод меняет роли игроков")
    void transferSwapsRoles() {
        GameEngine engine = engine(TrumpMode.OPEN, NINE_CLUBS);

        engine.attack(engine.state().attacker(), SIX_SPADES);
        engine.defend(engine.state().defender(), SEVEN_SPADES);
        engine.transfer(engine.state().defender(), List.of(SIX_HEARTS));

        assertEquals("Второй", engine.state().attacker().name());
        assertEquals("Третий", engine.state().defender().name());
        assertEquals(GamePhase.DEFENSE, engine.state().phase());
    }

    @Test
    @DisplayName("карты перевода ложатся на стол как атака")
    void transferCardsBecomeAttack() {
        GameEngine engine = engine(TrumpMode.OPEN, NINE_CLUBS);

        engine.attack(engine.state().attacker(), SIX_SPADES);
        engine.defend(engine.state().defender(), SEVEN_SPADES);
        engine.transfer(engine.state().defender(), List.of(SIX_HEARTS));

        assertEquals(2, engine.state().table().attackCards().size());
        assertEquals(1, engine.state().table().defenseCards().size());
        assertTrue(engine.state().table().attackCards().contains(SIX_HEARTS));
    }

    @Test
    @DisplayName("после перевода начинается новый отрезок атаки")
    void transferStartsNewSegment() {
        GameEngine engine = engine(TrumpMode.OPEN, NINE_CLUBS);

        engine.attack(engine.state().attacker(), SIX_SPADES);
        engine.defend(engine.state().defender(), SEVEN_SPADES);
        engine.transfer(engine.state().defender(), List.of(SIX_HEARTS));

        assertEquals(1, engine.state().cardsThrownInSegment());
        assertEquals(1, engine.state().segmentLimit());
    }

    @Test
    @DisplayName("перевод записывается в журнал")
    void transferIsRecordedInJournal() {
        GameEngine engine = engine(TrumpMode.OPEN, NINE_CLUBS);

        engine.attack(engine.state().attacker(), SIX_SPADES);
        engine.defend(engine.state().defender(), SEVEN_SPADES);
        engine.transfer(engine.state().defender(), List.of(SIX_HEARTS));

        List<TransferEvent> transfers = engine.journal().eventsOfType(EventType.TRANSFER).stream()
                .map(TransferEvent.class::cast)
                .toList();
        assertEquals(1, transfers.size());
        assertEquals("Второй", transfers.get(0).defender().name());
        assertEquals("Третий", transfers.get(0).newDefender().name());
        assertEquals(List.of(SIX_HEARTS), transfers.get(0).cards());
    }

    @Test
    @DisplayName("переводить можно картой того же достоинства или козырной")
    void transferRequiresSameRankOrTrump() {
        GameEngine engine = engine(TrumpMode.OPEN, NINE_CLUBS);
        MoveValidator validator = new MoveValidator(engine.state());

        engine.attack(engine.state().attacker(), SIX_SPADES);
        engine.defend(engine.state().defender(), SEVEN_SPADES);

        List<Card> legal = validator.legalTransferCards(engine.state().defender());
        assertEquals(2, legal.size(), "подходят карта того же достоинства и козырная");
        assertTrue(legal.contains(SIX_HEARTS));
        assertTrue(legal.contains(NINE_HEARTS));

        assertThrows(IllegalMoveException.class,
                () -> engine.transfer(engine.state().defender(), List.of(NINE_CLUBS)));
    }

    @Test
    @DisplayName("при закрытом козыре переводить козырной картой нельзя")
    void transferForbiddenInClosedTrumpMode() {
        GameEngine engine = engine(TrumpMode.CLOSED, NINE_CLUBS);
        MoveValidator validator = new MoveValidator(engine.state());

        engine.attack(engine.state().attacker(), SIX_SPADES);
        engine.defend(engine.state().defender(), SEVEN_SPADES);

        List<Card> legal = validator.legalTransferCards(engine.state().defender());
        assertEquals(1, legal.size());
        assertEquals(SIX_HEARTS, legal.get(0));
        assertFalse(legal.contains(NINE_HEARTS));

        assertThrows(IllegalMoveException.class,
                () -> engine.transfer(engine.state().defender(), List.of(NINE_HEARTS)));
    }

    @Test
    @DisplayName("число карт перевода ограничено рукой следующего игрока")
    void transferCountLimitedByNextPlayerHand() {
        GameEngine engine = TestGame.create()
                .trump(Suit.HEARTS, TrumpMode.OPEN)
                .player("Первый", SIX_SPADES)
                .player("Второй", SEVEN_SPADES, SIX_HEARTS, NINE_HEARTS)
                .player("Третий", NINE_CLUBS)
                .build();

        engine.attack(engine.state().attacker(), SIX_SPADES);
        engine.defend(engine.state().defender(), SEVEN_SPADES);

        assertThrows(IllegalMoveException.class,
                () -> engine.transfer(engine.state().defender(), List.of(SIX_HEARTS, NINE_HEARTS)));
    }

    @Test
    @DisplayName("переводить можно только сразу после защиты")
    void transferNotAllowedWithoutDefence() {
        GameEngine engine = engine(TrumpMode.OPEN, NINE_CLUBS);

        assertThrows(IllegalMoveException.class,
                () -> engine.transfer(engine.state().defender(), List.of(SIX_HEARTS)));
    }

    @Test
    @DisplayName("перевод недоступен в фазе защиты")
    void transferNotAllowedDuringDefence() {
        GameEngine engine = engine(TrumpMode.OPEN, NINE_CLUBS);

        engine.attack(engine.state().attacker(), SIX_SPADES);

        assertEquals(GamePhase.DEFENSE, engine.state().phase());
        assertThrows(IllegalMoveException.class,
                () -> engine.transfer(engine.state().defender(), List.of(SIX_HEARTS)));
    }

    @Test
    @DisplayName("после перевода отбиться обязан следующий игрок")
    void nextPlayerMustDefendAfterTransfer() {
        GameEngine engine = engine(TrumpMode.OPEN, NINE_CLUBS);

        engine.attack(engine.state().attacker(), SIX_SPADES);
        engine.defend(engine.state().defender(), SEVEN_SPADES);
        engine.transfer(engine.state().defender(), List.of(SIX_HEARTS));

        GameState state = engine.state();
        assertEquals("Третий", state.defender().name());
        assertThrows(IllegalMoveException.class,
                () -> engine.defend(state.playerAt(1), NINE_CLUBS));
    }

    @Test
    @DisplayName("перевод невозможен, если у следующего игрока нет карт")
    void transferImpossibleWhenNextPlayerHasNoCards() {
        GameEngine engine = TestGame.create()
                .trump(Suit.HEARTS, TrumpMode.OPEN)
                .player("Первый", SIX_SPADES)
                .player("Второй", SEVEN_SPADES, SIX_HEARTS)
                .player("Третий")
                .build();
        MoveValidator validator = new MoveValidator(engine.state());

        engine.attack(engine.state().attacker(), SIX_SPADES);
        engine.defend(engine.state().defender(), SEVEN_SPADES);

        assertTrue(validator.legalTransferCards(engine.state().defender()).isEmpty());
    }

    @Test
    @DisplayName("после перевода карта, от которой защищались, больше не предлагает перевод")
    void transferOfferIsClosedAfterTransfer() {
        GameEngine engine = engine(TrumpMode.OPEN, NINE_CLUBS);

        engine.attack(engine.state().attacker(), SIX_SPADES);
        engine.defend(engine.state().defender(), SEVEN_SPADES);
        assertTrue(engine.state().isTransferAvailable());

        engine.transfer(engine.state().defender(), List.of(SIX_HEARTS));

        assertFalse(engine.state().isTransferAvailable());
    }

    @Test
    @DisplayName("перевод делает переводившего атакующим в журнале событий")
    void transferEventContainsNewDefender() {
        GameEngine engine = engine(TrumpMode.OPEN, NINE_CLUBS);
        Player defender = engine.state().defender();

        engine.attack(engine.state().attacker(), SIX_SPADES);
        engine.defend(defender, SEVEN_SPADES);
        engine.transfer(defender, List.of(SIX_HEARTS));

        TransferEvent event = engine.journal().eventsOfType(EventType.TRANSFER).stream()
                .map(TransferEvent.class::cast)
                .findFirst()
                .orElseThrow();
        assertEquals(defender, event.defender());
        assertEquals(engine.state().attacker(), event.defender());
    }
}
