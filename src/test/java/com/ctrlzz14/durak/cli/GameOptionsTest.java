package com.ctrlzz14.durak.cli;

import com.ctrlzz14.durak.model.TrumpMode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Параметры запуска")
class GameOptionsTest {

    @Test
    @DisplayName("без параметров используются значения по умолчанию")
    void defaultsAreUsed() {
        GameOptions options = GameOptions.parse(new String[0]);

        assertEquals(3, options.playerCount());
        assertEquals(1, options.humanCount());
        assertEquals(TrumpMode.OPEN, options.trumpMode());
        assertEquals(3, options.playerNames().size());
    }

    @Test
    @DisplayName("параметры разбираются из командной строки")
    void parametersAreParsed() {
        GameOptions options = GameOptions.parse(new String[]{
                "--players=4", "--human=0", "--trump=closed", "--seed=12345"});

        assertEquals(4, options.playerCount());
        assertEquals(0, options.humanCount());
        assertEquals(TrumpMode.CLOSED, options.trumpMode());
        assertEquals(12345L, options.seed());
    }

    @Test
    @DisplayName("имена игроков берутся из параметра")
    void namesAreParsed() {
        GameOptions options = GameOptions.parse(new String[]{"--names=Аня,Борис", "--players=2"});

        assertEquals(List.of("Аня", "Борис"), options.playerNames());
    }

    @Test
    @DisplayName("недостающие имена дополняются автоматически")
    void missingNamesAreGenerated() {
        GameOptions options = GameOptions.parse(new String[]{"--names=Аня", "--players=3", "--human=1"});

        assertEquals(List.of("Аня", "Компьютер 1", "Компьютер 2"), options.playerNames());
    }

    @Test
    @DisplayName("игрок-человек по умолчанию называется «Вы»")
    void humanGetsDefaultName() {
        GameOptions options = GameOptions.parse(new String[]{"--players=2", "--human=1"});

        assertEquals(List.of("Вы", "Компьютер 1"), options.playerNames());
    }

    @Test
    @DisplayName("слишком мало игроков отвергается")
    void tooFewPlayersRejected() {
        assertThrows(IllegalArgumentException.class, () -> GameOptions.parse(new String[]{"--players=1"}));
    }

    @Test
    @DisplayName("слишком много игроков отвергается")
    void tooManyPlayersRejected() {
        assertThrows(IllegalArgumentException.class, () -> GameOptions.parse(new String[]{"--players=6"}));
    }

    @Test
    @DisplayName("нечисловое значение отвергается")
    void nonNumericValueRejected() {
        assertThrows(IllegalArgumentException.class, () -> GameOptions.parse(new String[]{"--players=abc"}));
    }

    @Test
    @DisplayName("неизвестный параметр отвергается")
    void unknownParameterRejected() {
        assertThrows(IllegalArgumentException.class, () -> GameOptions.parse(new String[]{"--unknown=1"}));
    }

    @Test
    @DisplayName("параметр без значения отвергается")
    void parameterWithoutValueRejected() {
        assertThrows(IllegalArgumentException.class, () -> GameOptions.parse(new String[]{"--players"}));
    }

    @Test
    @DisplayName("некорректный режим козыря отвергается")
    void wrongTrumpModeRejected() {
        assertThrows(IllegalArgumentException.class, () -> GameOptions.parse(new String[]{"--trump=secret"}));
    }

    @Test
    @DisplayName("число игроков-людей не может превышать число игроков")
    void tooManyHumansRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> GameOptions.parse(new String[]{"--players=2", "--human=3"}));
    }

    @Test
    @DisplayName("справка содержит описание параметров")
    void usageDescribesParameters() {
        String usage = GameOptions.usage(GameOptions.parse(new String[0]));

        assertTrue(usage.contains("--players"));
        assertTrue(usage.contains("--seed"));
        assertTrue(usage.contains("--trump"));
    }
}
