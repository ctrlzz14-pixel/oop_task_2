package com.ctrlzz14.durak.engine;

import com.ctrlzz14.durak.model.Card;
import com.ctrlzz14.durak.model.EndReason;
import com.ctrlzz14.durak.model.GameResult;
import com.ctrlzz14.durak.model.GameRules;
import com.ctrlzz14.durak.model.GameState;
import com.ctrlzz14.durak.model.Player;
import com.ctrlzz14.durak.model.Rank;
import com.ctrlzz14.durak.model.Suit;
import com.ctrlzz14.durak.model.TrumpMode;
import com.ctrlzz14.durak.support.TestGame;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Определение тупика")
class StalemateTest {

    private static GameEngine engine() {
        return TestGame.create()
                .trump(Suit.SPADES, TrumpMode.OPEN)
                .rules(GameRules.builder().minHandSize(6).build())
                .player("Первый",
                        new Card(Rank.SIX, Suit.HEARTS),
                        new Card(Rank.SEVEN, Suit.DIAMONDS),
                        new Card(Rank.EIGHT, Suit.DIAMONDS),
                        new Card(Rank.SIX, Suit.CLUBS),
                        new Card(Rank.SEVEN, Suit.CLUBS),
                        new Card(Rank.EIGHT, Suit.CLUBS))
                .player("Второй",
                        new Card(Rank.NINE, Suit.DIAMONDS),
                        new Card(Rank.TEN, Suit.DIAMONDS),
                        new Card(Rank.JACK, Suit.DIAMONDS),
                        new Card(Rank.NINE, Suit.CLUBS),
                        new Card(Rank.TEN, Suit.CLUBS),
                        new Card(Rank.JACK, Suit.CLUBS))
                .build();
    }

    private static void playRoundWithoutProgress(GameEngine engine) {
        Player attacker = engine.state().attacker();
        Card card = attacker.hand().cards().get(0);
        engine.attack(attacker, card);
        engine.take(engine.state().defender());
    }

    @Test
    @DisplayName("тупик наступает после трёх раундов без продвижения")
    void stalemateAfterThreeRoundsWithoutProgress() {
        GameEngine engine = engine();

        playRoundWithoutProgress(engine);
        assertFalse(engine.state().isGameOver(), "после первого раунда партия продолжается");
        assertEquals(1, engine.state().roundsWithoutProgress());

        playRoundWithoutProgress(engine);
        assertFalse(engine.state().isGameOver(), "после второго раунда партия продолжается");
        assertEquals(2, engine.state().roundsWithoutProgress());

        playRoundWithoutProgress(engine);

        assertTrue(engine.state().isGameOver());
        assertEquals(EndReason.STALEMATE, engine.result().orElseThrow().reason());
    }

    @Test
    @DisplayName("победителем тупика становится игрок с наименьшим числом карт")
    void stalemateWinnerHasFewestCards() {
        GameEngine engine = engine();

        playRoundWithoutProgress(engine);
        playRoundWithoutProgress(engine);
        playRoundWithoutProgress(engine);

        GameResult result = engine.result().orElseThrow();
        int minCards = engine.state().players().stream()
                .mapToInt(Player::cardCount)
                .min()
                .orElseThrow();
        assertEquals(1, result.winners().size());
        assertEquals(minCards, result.winners().get(0).cardCount());
        assertEquals("Первый", result.winners().get(0).name());
    }

    @Test
    @DisplayName("проигравшие отсортированы по убыванию числа карт")
    void stalemateLosersAreSorted() {
        GameEngine engine = engine();

        playRoundWithoutProgress(engine);
        playRoundWithoutProgress(engine);
        playRoundWithoutProgress(engine);

        GameResult result = engine.result().orElseThrow();
        assertEquals(1, result.losers().size());
        assertTrue(result.losers().get(0).cardCount() > result.winners().get(0).cardCount());
    }

    @Test
    @DisplayName("отбой считается продвижением и откладывает тупик")
    void discardResetsProgressCounter() {
        GameEngine engine = TestGame.create()
                .trump(Suit.SPADES, TrumpMode.OPEN)
                .rules(GameRules.builder().minHandSize(6).build())
                .player("Первый",
                        new Card(Rank.SIX, Suit.HEARTS),
                        new Card(Rank.SEVEN, Suit.HEARTS),
                        new Card(Rank.EIGHT, Suit.DIAMONDS))
                .player("Второй",
                        new Card(Rank.NINE, Suit.HEARTS),
                        new Card(Rank.TEN, Suit.DIAMONDS),
                        new Card(Rank.JACK, Suit.DIAMONDS))
                .build();

        engine.attack(engine.state().attacker(), new Card(Rank.SIX, Suit.HEARTS));
        engine.take(engine.state().defender());
        assertEquals(1, engine.state().roundsWithoutProgress());

        engine.attack(engine.state().attacker(), new Card(Rank.SEVEN, Suit.HEARTS));
        engine.defend(engine.state().defender(), new Card(Rank.NINE, Suit.HEARTS));
        engine.endAttack(engine.state().attacker());

        assertEquals(0, engine.state().roundsWithoutProgress(), "уход карт в отбой сбрасывает счётчик");
        assertFalse(engine.state().isGameOver());
    }

    @Test
    @DisplayName("добор карт из колоды считается продвижением")
    void refillResetsProgressCounter() {
        GameEngine engine = TestGame.create()
                .trump(Suit.SPADES, TrumpMode.OPEN)
                .rules(GameRules.builder().minHandSize(6).build())
                .deck(new Card(Rank.ACE, Suit.HEARTS),
                        new Card(Rank.QUEEN, Suit.HEARTS),
                        new Card(Rank.KING, Suit.HEARTS),
                        new Card(Rank.JACK, Suit.HEARTS),
                        new Card(Rank.TEN, Suit.HEARTS),
                        new Card(Rank.NINE, Suit.HEARTS))
                .player("Первый",
                        new Card(Rank.SIX, Suit.HEARTS),
                        new Card(Rank.SEVEN, Suit.HEARTS),
                        new Card(Rank.EIGHT, Suit.DIAMONDS))
                .player("Второй",
                        new Card(Rank.NINE, Suit.DIAMONDS),
                        new Card(Rank.TEN, Suit.DIAMONDS),
                        new Card(Rank.JACK, Suit.DIAMONDS))
                .build();

        engine.attack(engine.state().attacker(), new Card(Rank.SIX, Suit.HEARTS));
        engine.take(engine.state().defender());

        GameState state = engine.state();
        assertEquals(0, state.roundsWithoutProgress(), "добор карт сбрасывает счётчик");
        assertFalse(state.isGameOver());
        assertTrue(state.players().stream().anyMatch(player -> player.cardCount() >= 6));
    }
}
