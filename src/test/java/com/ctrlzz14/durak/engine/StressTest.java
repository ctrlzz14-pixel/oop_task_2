package com.ctrlzz14.durak.engine;

import com.ctrlzz14.durak.model.Card;
import com.ctrlzz14.durak.model.GamePhase;
import com.ctrlzz14.durak.model.GameRules;
import com.ctrlzz14.durak.model.GameState;
import com.ctrlzz14.durak.rules.MoveValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Нагрузочная проверка: партия должна завершаться при любых зёрнах и любом
 * количестве игроков, а карты не должны теряться или дублироваться.
 */
@DisplayName("Нагрузочная проверка партий")
class StressTest {

    private static void playOne(long seed, int playerCount, boolean attackLowest) {
        GameEngine engine = GameEngine.start(GameRules.standard(),
                List.of("Первый", "Второй", "Третий", "Четвёртый", "Пятый").subList(0, playerCount), seed);
        MoveValidator validator = new MoveValidator(engine.state());
        int guard = 0;
        while (!engine.state().isGameOver() && guard++ < 5000) {
            GameState state = engine.state();
            if (state.phase() == GamePhase.ATTACK) {
                List<Card> legal = validator.legalAttackCards(state.attacker());
                if (legal.isEmpty()) {
                    engine.endAttack(state.attacker());
                } else {
                    Card card = attackLowest
                            ? legal.stream().min(Comparator.comparingInt(c -> c.rank().power())).orElseThrow()
                            : legal.get(legal.size() - 1);
                    engine.attack(state.attacker(), card);
                }
            } else {
                List<Card> legal = validator.legalDefenseCards(state.defender());
                if (legal.isEmpty()) {
                    engine.take(state.defender());
                } else {
                    engine.defend(state.defender(), legal.get(0));
                }
            }
        }
        assertTrue(engine.state().isGameOver(), "seed=" + seed + ": партия должна завершиться");

        List<Card> all = new ArrayList<>();
        engine.state().players().forEach(player -> all.addAll(player.hand().cards()));
        all.addAll(engine.state().deck().cards());
        all.addAll(engine.state().table().allCards());
        all.addAll(engine.state().discardPile().cards());
        assertEquals(36, all.size(), "seed=" + seed + ": все карты должны быть на месте");
        assertEquals(36, all.stream().distinct().count(), "seed=" + seed + ": дубликатов быть не должно");
    }

    @ParameterizedTest(name = "{0} игроков")
    @ValueSource(ints = {2, 3, 4, 5})
    @DisplayName("партии на любых зёрнах завершаются")
    void manySeedsAlwaysFinish(int playerCount) {
        for (long seed = 1; seed <= 100; seed++) {
            playOne(seed, playerCount, false);
            playOne(seed, playerCount, true);
        }
    }
}
