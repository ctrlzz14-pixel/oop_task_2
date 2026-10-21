package com.ctrlzz14.durak.cli;

import com.ctrlzz14.durak.bot.SimpleBotStrategy;
import com.ctrlzz14.durak.engine.GameEngine;
import com.ctrlzz14.durak.model.GameRules;
import com.ctrlzz14.durak.model.Player;
import com.ctrlzz14.durak.strategy.HumanStrategy;
import com.ctrlzz14.durak.strategy.PlayerStrategy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Консольный интерфейс")
class ConsoleGameTest {

    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    private ConsoleIO io(String input) {
        return new ConsoleIO(new BufferedReader(new StringReader(input)),
                new PrintStream(buffer, true, StandardCharsets.UTF_8));
    }

    private String output() {
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("ввод читается построчно")
    void inputIsReadLineByLine() {
        ConsoleIO io = io("первая\nвторая\n");

        assertEquals("первая", io.readLine().orElseThrow());
        assertEquals("вторая", io.readLine().orElseThrow());
        assertTrue(io.readLine().isEmpty(), "закончившийся ввод возвращает пустой результат");
    }

    @Test
    @DisplayName("вывод печатается в поток")
    void outputIsWritten() {
        ConsoleIO io = io("");

        io.println("строка");
        io.print("без перевода строки");

        assertTrue(output().contains("строка"));
        assertTrue(output().contains("без перевода строки"));
    }

    @Test
    @DisplayName("партия доигрывается до конца и печатается в консоль")
    void gameIsPlayedAndPrinted() {
        GameEngine engine = GameEngine.start(GameRules.standard(), List.of("Первый", "Второй"), 42L);
        Map<Player, PlayerStrategy> strategies = new LinkedHashMap<>();
        engine.state().players().forEach(player -> strategies.put(player, new SimpleBotStrategy(player.name())));

        ConsoleGame game = new ConsoleGame(engine, strategies, io(""));
        game.run();

        String text = output();
        assertTrue(text.contains("Партия началась"));
        assertTrue(text.contains("Итоги партии"));
        assertTrue(engine.state().isGameOver());
    }

    @Test
    @DisplayName("закрытый ввод прерывает партию без ошибки")
    void closedInputStopsTheGame() {
        GameEngine engine = GameEngine.start(GameRules.standard(), List.of("Вы", "Второй"), 42L);
        Map<Player, PlayerStrategy> strategies = new LinkedHashMap<>();
        for (Player player : engine.state().players()) {
            strategies.put(player, player.name().equals("Вы")
                    ? new HumanStrategy(io(""), player.name())
                    : new SimpleBotStrategy(player.name()));
        }

        ConsoleGame game = new ConsoleGame(engine, strategies, io(""));
        game.run();

        assertTrue(output().contains("Ввод закрыт, партия прервана."));
        assertFalse(engine.state().isGameOver());
    }

    @Test
    @DisplayName("человек может сыграть ход, введя номера карт")
    void humanCanPlayUsingCardNumbers() {
        GameEngine engine = GameEngine.start(GameRules.standard(), List.of("Вы", "Второй"), 42L);
        Map<Player, PlayerStrategy> strategies = new LinkedHashMap<>();
        for (Player player : engine.state().players()) {
            strategies.put(player, player.name().equals("Вы")
                    ? new HumanStrategy(io("1\n"), player.name())
                    : new SimpleBotStrategy(player.name()));
        }

        ConsoleGame game = new ConsoleGame(engine, strategies, io(""));
        game.run();

        String text = output();
        System.out.println("=== OUTPUT START ===");
        System.out.println(text);
        System.out.println("=== OUTPUT END ===");
        assertTrue(text.contains("Ваши карты"));
        assertTrue(text.contains("атакует"));
    }
}
