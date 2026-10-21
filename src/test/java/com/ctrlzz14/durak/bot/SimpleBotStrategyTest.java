package com.ctrlzz14.durak.bot;

import com.ctrlzz14.durak.engine.GameEngine;
import com.ctrlzz14.durak.model.Card;
import com.ctrlzz14.durak.model.GamePhase;
import com.ctrlzz14.durak.model.GameState;
import com.ctrlzz14.durak.model.Rank;
import com.ctrlzz14.durak.model.Suit;
import com.ctrlzz14.durak.model.TrumpMode;
import com.ctrlzz14.durak.move.Move;
import com.ctrlzz14.durak.move.MoveType;
import com.ctrlzz14.durak.rules.MoveValidator;
import com.ctrlzz14.durak.support.TestGame;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Стратегия компьютерного игрока")
class SimpleBotStrategyTest {

    private static final Card SIX_DIAMONDS = new Card(Rank.SIX, Suit.DIAMONDS);
    private static final Card SIX_HEARTS = new Card(Rank.SIX, Suit.HEARTS);
    private static final Card NINE_DIAMONDS = new Card(Rank.NINE, Suit.DIAMONDS);
    private static final Card TEN_DIAMONDS = new Card(Rank.TEN, Suit.DIAMONDS);
    private static final Card TEN_HEARTS = new Card(Rank.TEN, Suit.HEARTS);
    private static final Card KING_HEARTS = new Card(Rank.KING, Suit.HEARTS);
    private static final Card QUEEN_SPADES = new Card(Rank.QUEEN, Suit.SPADES);
    private static final Card ACE_SPADES = new Card(Rank.ACE, Suit.SPADES);

    @Test
    @DisplayName("бот атакует самой младшей картой")
    void botAttacksWithLowestCard() {
        GameEngine engine = TestGame.create()
                .trump(Suit.SPADES, TrumpMode.OPEN)
                .player("Бот", TEN_DIAMONDS, SIX_HEARTS)
                .player("Соперник", KING_HEARTS, QUEEN_SPADES)
                .build();
        GameState state = engine.state();

        Move move = new SimpleBotStrategy("Бот").chooseMove(state, new MoveValidator(state));

        assertEquals(GamePhase.ATTACK, state.phase());
        assertTrue(move.cards().contains(SIX_HEARTS));
        assertFalse(move.cards().contains(TEN_DIAMONDS));
    }

    @Test
    @DisplayName("бот не тратит козыри, если можно отбиться обычной картой")
    void botDefendsWithCheapestCard() {
        GameEngine engine = TestGame.create()
                .trump(Suit.SPADES, TrumpMode.OPEN)
                .player("Атакующий", SIX_DIAMONDS)
                .player("Бот", QUEEN_SPADES, TEN_DIAMONDS)
                .build();
        engine.attack(engine.state().attacker(), SIX_DIAMONDS);

        GameState state = engine.state();
        Move move = new SimpleBotStrategy("Бот").chooseMove(state, new MoveValidator(state));

        assertEquals(GamePhase.DEFENSE, state.phase());
        assertEquals(MoveType.DEFEND, move.type());
        assertEquals(TEN_DIAMONDS, move.cards().get(0));
    }

    @Test
    @DisplayName("бот забирает карты, когда отбиться нечем")
    void botTakesWhenItCannotDefend() {
        GameEngine engine = TestGame.create()
                .trump(Suit.SPADES, TrumpMode.OPEN)
                .player("Атакующий", ACE_SPADES)
                .player("Бот", SIX_HEARTS)
                .build();
        engine.attack(engine.state().attacker(), ACE_SPADES);

        GameState state = engine.state();
        Move move = new SimpleBotStrategy("Бот").chooseMove(state, new MoveValidator(state));

        assertEquals(MoveType.TAKE, move.type());
    }

    @Test
    @DisplayName("бот не переводит атаку в партии на двоих")
    void botDoesNotTransferInTwoPlayerGame() {
        GameEngine engine = TestGame.create()
                .trump(Suit.HEARTS, TrumpMode.OPEN)
                .player("Атакующий", SIX_HEARTS)
                .player("Бот", KING_HEARTS, TEN_HEARTS)
                .build();
        engine.attack(engine.state().attacker(), SIX_HEARTS);
        engine.defend(engine.state().defender(), KING_HEARTS);

        GameState state = engine.state();
        Optional<Move> transfer = new SimpleBotStrategy("Бот").chooseTransfer(state, new MoveValidator(state));

        assertTrue(transfer.isEmpty());
    }

    @Test
    @DisplayName("бот переводит атаку козырной картой, если отбиться нечему")
    void botTransfersWhenNextPlayerCannotDefend() {
        GameEngine engine = TestGame.create()
                .trump(Suit.HEARTS, TrumpMode.OPEN)
                .player("Первый", SIX_HEARTS)
                .player("Бот", KING_HEARTS, TEN_HEARTS)
                .player("Третий", NINE_DIAMONDS)
                .build();
        engine.attack(engine.state().attacker(), SIX_HEARTS);
        engine.defend(engine.state().defender(), KING_HEARTS);

        GameState state = engine.state();
        assertTrue(state.isTransferAvailable());

        Optional<Move> transfer = new SimpleBotStrategy("Бот").chooseTransfer(state, new MoveValidator(state));

        assertTrue(transfer.isPresent());
        assertEquals(TEN_HEARTS, transfer.get().cards().get(0));
    }

    @Test
    @DisplayName("бот не переводит атаку, если следующий игрок может отбиться")
    void botDoesNotTransferWhenNextPlayerCanDefend() {
        GameEngine engine = TestGame.create()
                .trump(Suit.HEARTS, TrumpMode.OPEN)
                .player("Первый", SIX_HEARTS)
                .player("Бот", KING_HEARTS, TEN_HEARTS)
                .player("Третий", new Card(Rank.ACE, Suit.HEARTS))
                .build();
        engine.attack(engine.state().attacker(), SIX_HEARTS);
        engine.defend(engine.state().defender(), KING_HEARTS);

        GameState state = engine.state();
        Optional<Move> transfer = new SimpleBotStrategy("Бот").chooseTransfer(state, new MoveValidator(state));

        assertTrue(transfer.isEmpty());
    }

    @Test
    @DisplayName("название стратегии возвращается как есть")
    void titleIsReturned() {
        assertEquals("Бот", new SimpleBotStrategy("Бот").title());
    }

    @Test
    @DisplayName("бот всегда предлагает допустимый ход")
    void botAlwaysOffersLegalMove() {
        GameEngine engine = TestGame.create()
                .trump(Suit.SPADES, TrumpMode.OPEN)
                .player("Первый", SIX_HEARTS, TEN_DIAMONDS)
                .player("Второй", KING_HEARTS, QUEEN_SPADES)
                .player("Третий", ACE_SPADES)
                .build();
        GameState state = engine.state();

        Move move = new SimpleBotStrategy("Первый").chooseMove(state, new MoveValidator(state));

        new MoveValidator(state).validate(move);
    }
}
