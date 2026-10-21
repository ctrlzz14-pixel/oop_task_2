package com.ctrlzz14.durak.rules;

import com.ctrlzz14.durak.engine.GameEngine;
import com.ctrlzz14.durak.model.Card;
import com.ctrlzz14.durak.model.GameState;
import com.ctrlzz14.durak.model.Player;
import com.ctrlzz14.durak.model.Rank;
import com.ctrlzz14.durak.model.Suit;
import com.ctrlzz14.durak.model.TrumpMode;
import com.ctrlzz14.durak.move.Move;
import com.ctrlzz14.durak.support.TestGame;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Проверка допустимости ходов")
class MoveValidatorTest {

    private static final Card SIX_HEARTS = new Card(Rank.SIX, Suit.HEARTS);
    private static final Card SIX_SPADES = new Card(Rank.SIX, Suit.SPADES);
    private static final Card NINE_HEARTS = new Card(Rank.NINE, Suit.HEARTS);
    private static final Card SEVEN_HEARTS = new Card(Rank.SEVEN, Suit.HEARTS);
    private static final Card SEVEN_SPADES = new Card(Rank.SEVEN, Suit.SPADES);

    private static GameEngine engine() {
        return TestGame.create()
                .trump(Suit.CLUBS, TrumpMode.OPEN)
                .player("Атакующий", SIX_HEARTS, NINE_HEARTS, SIX_SPADES)
                .player("Защищающийся", SEVEN_HEARTS, SEVEN_SPADES)
                .player("Третий", new Card(Rank.ACE, Suit.SPADES))
                .build();
    }

    private static GameState state() {
        return engine().state();
    }

    @Test
    @DisplayName("нельзя атаковать картой, которой нет в руке")
    void attackWithForeignCardIsRejected() {
        GameState state = state();
        MoveValidator validator = new MoveValidator(state);
        Player attacker = state.attacker();

        IllegalMoveException exception = assertThrows(IllegalMoveException.class,
                () -> validator.validateAttack(attacker, List.of(new Card(Rank.KING, Suit.HEARTS))));

        assertTrue(exception.getMessage().contains("нет карты"));
    }

    @Test
    @DisplayName("атака должна содержать хотя бы одну карту")
    void emptyAttackIsRejected() {
        GameState state = state();
        MoveValidator validator = new MoveValidator(state);

        assertThrows(IllegalMoveException.class, () -> validator.validateAttack(state.attacker(), List.of()));
    }

    @Test
    @DisplayName("нельзя подкинуть карту нового достоинства")
    void cannotAddCardWithNewRank() {
        GameEngine engine = engine();
        engine.attack(engine.state().attacker(), SIX_HEARTS);
        engine.defend(engine.state().defender(), SEVEN_HEARTS);
        MoveValidator validator = new MoveValidator(engine.state());

        IllegalMoveException exception = assertThrows(IllegalMoveException.class,
                () -> validator.validateAttack(engine.state().attacker(), List.of(NINE_HEARTS)));

        assertTrue(exception.getMessage().contains("достоинства"));
    }

    @Test
    @DisplayName("нельзя выложить больше карт, чем есть у защищающегося")
    void cannotAttackMoreThanDefenderHas() {
        GameState state = TestGame.create()
                .trump(Suit.CLUBS, TrumpMode.OPEN)
                .player("Атакующий", SIX_HEARTS, SEVEN_HEARTS)
                .player("Защищающийся", new Card(Rank.ACE, Suit.SPADES))
                .buildState();
        MoveValidator validator = new MoveValidator(state);

        assertThrows(IllegalMoveException.class,
                () -> validator.validateAttack(state.attacker(), List.of(SIX_HEARTS, SEVEN_HEARTS)));
    }

    @Test
    @DisplayName("нельзя отбиться более слабой картой")
    void defendWithWeakerCardIsRejected() {
        GameEngine engine = engine();
        engine.attack(engine.state().attacker(), SIX_HEARTS);
        MoveValidator validator = new MoveValidator(engine.state());

        IllegalMoveException exception = assertThrows(IllegalMoveException.class,
                () -> validator.validateDefend(engine.state().defender(), List.of(SEVEN_SPADES)));

        assertTrue(exception.getMessage().contains("бьёт"));
    }

    @Test
    @DisplayName("защищаться должен только защищающийся")
    void onlyDefenderCanDefend() {
        GameEngine engine = engine();
        engine.attack(engine.state().attacker(), SIX_HEARTS);
        MoveValidator validator = new MoveValidator(engine.state());

        assertThrows(IllegalMoveException.class,
                () -> validator.validateDefend(engine.state().playerAt(2), List.of(SEVEN_HEARTS)));
    }

    @Test
    @DisplayName("в фазе атаки нельзя взять карты")
    void cannotTakeDuringAttack() {
        GameState state = state();
        MoveValidator validator = new MoveValidator(state);

        assertThrows(IllegalMoveException.class, () -> validator.validateTake(state.defender()));
    }

    @Test
    @DisplayName("нельзя завершить раунд, в котором никто ничего не сыграл")
    void cannotEndEmptyAttack() {
        GameState state = state();
        MoveValidator validator = new MoveValidator(state);

        assertThrows(IllegalMoveException.class, () -> validator.validateEndAttack(state.attacker()));
    }

    @Test
    @DisplayName("завершить атаку может только атакующий")
    void onlyAttackerCanEndAttack() {
        GameEngine engine = engine();
        engine.attack(engine.state().attacker(), SIX_HEARTS);
        MoveValidator validator = new MoveValidator(engine.state());

        assertThrows(IllegalMoveException.class,
                () -> validator.validateEndAttack(engine.state().defender()));
    }

    @Test
    @DisplayName("доступные карты для атаки ограничены достоинствами на столе")
    void legalAttackCardsRespectTableRanks() {
        GameEngine engine = engine();
        MoveValidator validator = new MoveValidator(engine.state());
        Player attacker = engine.state().attacker();

        assertEquals(3, validator.legalAttackCards(attacker).size());

        engine.attack(attacker, SIX_HEARTS);
        engine.defend(engine.state().defender(), SEVEN_HEARTS);

        List<Card> legal = new MoveValidator(engine.state()).legalAttackCards(attacker);
        assertEquals(1, legal.size());
        assertEquals(SIX_SPADES, legal.get(0));
    }

    @Test
    @DisplayName("доступные карты для защиты бьют атакованную карту")
    void legalDefenseCardsOnlyBeat() {
        GameEngine engine = engine();
        engine.attack(engine.state().attacker(), SIX_HEARTS);

        List<Card> legal = new MoveValidator(engine.state()).legalDefenseCards(engine.state().defender());

        assertEquals(1, legal.size());
        assertEquals(SEVEN_HEARTS, legal.get(0));
    }

    @Test
    @DisplayName("валидатор проверяет ход по его типу")
    void validateDispatchesByMoveType() {
        GameState state = state();
        MoveValidator validator = new MoveValidator(state);

        validator.validate(Move.attack(state.attacker(), SIX_HEARTS));

        assertThrows(IllegalMoveException.class, () -> validator.validate(Move.take(state.defender())));
    }
}
