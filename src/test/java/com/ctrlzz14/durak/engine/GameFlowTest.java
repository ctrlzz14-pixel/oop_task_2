package com.ctrlzz14.durak.engine;

import com.ctrlzz14.durak.bot.SimpleBotStrategy;
import com.ctrlzz14.durak.model.Card;
import com.ctrlzz14.durak.model.GamePhase;
import com.ctrlzz14.durak.model.GameRules;
import com.ctrlzz14.durak.model.GameState;
import com.ctrlzz14.durak.model.Player;
import com.ctrlzz14.durak.move.Move;
import com.ctrlzz14.durak.rules.MoveValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Партия целиком")
class GameFlowTest {

    private static GameEngine playWithBots(int playerCount, long seed) {
        GameEngine engine = GameEngine.start(GameRules.standard(),
                List.of("Первый", "Второй", "Третий", "Четвёртый", "Пятый").subList(0, playerCount), seed);
        MoveValidator validator = new MoveValidator(engine.state());
        int guard = 0;
        while (!engine.state().isGameOver() && guard++ < 1000) {
            GameState state = engine.state();
            if (state.phase() == GamePhase.ATTACK && state.isTransferAvailable()) {
                SimpleBotStrategy translator = new SimpleBotStrategy(state.defender().name());
                Move transfer = translator.chooseTransfer(state, validator).orElse(null);
                if (transfer != null) {
                    engine.apply(transfer);
                    continue;
                }
            }
            Player current = state.phase() == GamePhase.DEFENSE ? state.defender() : state.attacker();
            SimpleBotStrategy bot = new SimpleBotStrategy(current.name());
            engine.apply(bot.chooseMove(state, validator));
        }
        assertTrue(engine.state().isGameOver(), "seed=" + seed + ": партия должна завершиться");
        return engine;
    }

    @ParameterizedTest(name = "{0} игроков")
    @ValueSource(ints = {2, 3, 4, 5})
    @DisplayName("партия всегда заканчивается")
    void everyGameFinishes(int playerCount) {
        for (long seed = 1; seed <= 40; seed++) {
            GameEngine engine = playWithBots(playerCount, seed);
            assertNotNull(engine.result().orElse(null), "seed=" + seed + ": должен быть результат");
        }
    }

    @ParameterizedTest(name = "{0} игроков")
    @ValueSource(ints = {2, 3, 4, 5})
    @DisplayName("все карты остаются в игре")
    void allCardsAreConserved(int playerCount) {
        for (long seed = 1; seed <= 40; seed++) {
            GameEngine engine = playWithBots(playerCount, seed);
            GameState state = engine.state();

            List<Card> all = new ArrayList<>();
            state.players().forEach(player -> all.addAll(player.hand().cards()));
            all.addAll(state.deck().cards());
            all.addAll(state.table().allCards());
            all.addAll(state.discardPile().cards());

            assertEquals(36, all.size(), "seed=" + seed + ": все 36 карт должны быть на месте");
            assertEquals(36, all.stream().distinct().count(), "seed=" + seed + ": дубликатов быть не должно");
        }
    }

    @Test
    @DisplayName("партия с одним зерном воспроизводится")
    void sameSeedGivesSameGame() {
        GameEngine first = playWithBots(3, 777L);
        GameEngine second = playWithBots(3, 777L);

        assertEquals(first.journal().events().size(), second.journal().events().size());
        assertEquals(first.result().orElseThrow().reason(), second.result().orElseThrow().reason());
        assertEquals(
                first.state().players().stream().map(Player::cardCount).toList(),
                second.state().players().stream().map(Player::cardCount).toList());
    }

    @Test
    @DisplayName("разные зёрна дают разные партии")
    void differentSeedsGiveDifferentGames() {
        GameEngine first = playWithBots(3, 1L);
        GameEngine second = playWithBots(3, 2L);

        assertTrue(first.journal().events().size() != second.journal().events().size()
                || !first.result().orElseThrow().winners().equals(second.result().orElseThrow().winners()));
    }

    @Test
    @DisplayName("журнал партии содержит события всех типов")
    void journalContainsMainEvents() {
        GameEngine engine = playWithBots(3, 5L);

        assertTrue(engine.journal().eventsOfType(com.ctrlzz14.durak.event.EventType.GAME_STARTED).size() == 1);
        assertTrue(engine.journal().eventsOfType(com.ctrlzz14.durak.event.EventType.TRUMP_CHOSEN).size() == 1);
        assertTrue(engine.journal().eventsOfType(com.ctrlzz14.durak.event.EventType.CARDS_DEALT).size() == 1);
        assertTrue(engine.journal().eventsOfType(com.ctrlzz14.durak.event.EventType.ATTACK).size() > 0);
        assertTrue(engine.journal().eventsOfType(com.ctrlzz14.durak.event.EventType.ROUND_ENDED).size() > 0);
        assertTrue(engine.journal().eventsOfType(com.ctrlzz14.durak.event.EventType.GAME_ENDED).size() == 1);
    }
}
