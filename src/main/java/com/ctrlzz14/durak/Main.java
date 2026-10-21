package com.ctrlzz14.durak;

import com.ctrlzz14.durak.bot.SimpleBotStrategy;
import com.ctrlzz14.durak.cli.ConsoleGame;
import com.ctrlzz14.durak.cli.ConsoleIO;
import com.ctrlzz14.durak.cli.GameOptions;
import com.ctrlzz14.durak.engine.GameEngine;
import com.ctrlzz14.durak.model.GameRules;
import com.ctrlzz14.durak.model.Player;
import com.ctrlzz14.durak.strategy.HumanStrategy;
import com.ctrlzz14.durak.strategy.PlayerStrategy;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Точка входа консольного приложения «Переводной дурак».
 *
 * <p>Приложение запускает партию по правилам, заданным параметрами командной
 * строки. Первый игрок играет за клавиатуру, остальные — компьютерные
 * соперники.</p>
 */
public final class Main {

    private Main() {
    }

    /**
     * Запускает партию.
     *
     * @param args аргументы командной строки
     */
    public static void main(String[] args) {
        GameOptions options;
        try {
            options = GameOptions.parse(args);
        } catch (IllegalArgumentException e) {
            System.err.println("Ошибка разбора параметров: " + e.getMessage());
            System.err.println(GameOptions.usage(GameOptions.parse(new String[0])));
            System.exit(1);
            return;
        }
        if (args.length > 0 && (args[0].equals("--help") || args[0].equals("-h"))) {
            System.out.println(GameOptions.usage(options));
            return;
        }

        GameRules rules = GameRules.builder()
                .trumpMode(options.trumpMode())
                .build();
        List<String> names = options.playerNames();
        GameEngine engine = GameEngine.start(rules, names, options.seed());

        ConsoleIO io = new ConsoleIO(
                new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8)),
                new PrintStream(System.out, true, StandardCharsets.UTF_8));

        Map<Player, PlayerStrategy> strategies = new LinkedHashMap<>();
        List<Player> players = engine.state().players();
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            if (i < options.humanCount()) {
                strategies.put(player, new HumanStrategy(io, player.name()));
            } else {
                strategies.put(player, new SimpleBotStrategy(player.name()));
            }
        }

        new ConsoleGame(engine, strategies, io).run();
    }
}
