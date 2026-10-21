package com.ctrlzz14.durak.random;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Генератор случайных величин")
class GameRandomTest {

    @Test
    @DisplayName("одинаковые зёрна дают одинаковую последовательность")
    void sameSeedGivesSameSequence() {
        GameRandom first = new GameRandom(123L);
        GameRandom second = new GameRandom(123L);

        for (int i = 0; i < 20; i++) {
            assertEquals(first.nextInt(100), second.nextInt(100));
        }
    }

    @Test
    @DisplayName("разные зёрна дают разные последовательности")
    void differentSeedsGiveDifferentSequence() {
        GameRandom first = new GameRandom(1L);
        GameRandom second = new GameRandom(2L);

        boolean differs = false;
        for (int i = 0; i < 20; i++) {
            if (first.nextInt(1000) != second.nextInt(1000)) {
                differs = true;
                break;
            }
        }

        assertTrue(differs, "последовательности разных зёрен должны различаться");
    }

    @Test
    @DisplayName("числа выдаются в заданных границах")
    void nextIntRespectsBound() {
        GameRandom random = new GameRandom(7L);

        for (int i = 0; i < 100; i++) {
            int value = random.nextInt(6);
            assertTrue(value >= 0 && value < 6, "число вне границ: " + value);
        }
    }

    @Test
    @DisplayName("тасовка списка воспроизводима по зерну")
    void shuffleIsReproducible() {
        List<Integer> first = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6, 7, 8));
        List<Integer> second = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6, 7, 8));

        new GameRandom(99L).shuffle(first);
        new GameRandom(99L).shuffle(second);

        assertEquals(first, second);
    }

    @Test
    @DisplayName("тасовка не теряет и не дублирует элементы")
    void shuffleKeepsAllElements() {
        List<Integer> values = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));

        new GameRandom(5L).shuffle(values);

        assertEquals(10, values.size());
        assertEquals(10, new java.util.HashSet<>(values).size());
        assertFalse(values.isEmpty());
    }

    @Test
    @DisplayName("зерно генератора сохраняется")
    void keepsSeed() {
        assertEquals(55L, new GameRandom(55L).seed());
    }
}
