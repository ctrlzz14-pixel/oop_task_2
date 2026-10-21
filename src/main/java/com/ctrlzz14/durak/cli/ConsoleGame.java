package com.ctrlzz14.durak.cli;

import com.ctrlzz14.durak.engine.GameEngine;
import com.ctrlzz14.durak.event.GameEvent;
import com.ctrlzz14.durak.model.GamePhase;
import com.ctrlzz14.durak.model.GameState;
import com.ctrlzz14.durak.model.Player;
import com.ctrlzz14.durak.move.Move;
import com.ctrlzz14.durak.rules.MoveValidator;
import com.ctrlzz14.durak.strategy.PlayerStrategy;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Консольный интерфейс партии.
 *
 * <p>Класс отвечает за порядок показа событий и за диалог с игроком-человеком.
 * Правила игры применяет {@link GameEngine}, а решения принимают
 * {@link PlayerStrategy}: так консольный слой не содержит игровой логики.</p>
 */
public final class ConsoleGame {

    private final GameEngine engine;
    private final Map<Player, PlayerStrategy> strategies;
    private final ConsoleIO io;
    private final EventFormatter formatter = new EventFormatter();
    private int printedEvents;

    /**
     * Создаёт консольную партию.
     *
     * @param engine движок партии
     * @param strategies стратегия каждого игрока
     * @param io консольный ввод и вывод
     * @throws NullPointerException если аргументы равны {@code null}
     */
    public ConsoleGame(GameEngine engine, Map<Player, PlayerStrategy> strategies, ConsoleIO io) {
        this.engine = Objects.requireNonNull(engine, "Движок не должен быть null");
        this.strategies = Objects.requireNonNull(strategies, "Стратегии не должны быть null");
        this.io = Objects.requireNonNull(io, "Консольный ввод и вывод не должны быть null");
    }

    /**
     * Играет партию до её завершения.
     */
    public void run() {
        printEvents();
        io.println("Правила: атакуйте младшей картой, отбивайтесь любой старшей картой,"
                + " берите карты, если отбиться нечем, переводите атаку козырной картой.");
        io.println("Зерно партии: " + engine.state().seed() + " (им можно воспроизвести партию).");
        while (!engine.state().isGameOver()) {
            if (!playTurn()) {
                return;
            }
            printEvents();
        }
        printSummary();
    }

    /**
     * Выполняет один ход партии: при необходимости предлагает перевод,
     * затем просит текущего игрока сделать ход.
     *
     * @return {@code false}, если партию следует прервать
     */
    private boolean playTurn() {
        GameState state = engine.state();
        try {
            if (state.phase() == GamePhase.ATTACK && state.isTransferAvailable()) {
                Player defender = state.defender();
                PlayerStrategy strategy = strategies.get(defender);
                Move transfer = strategy.chooseTransfer(state, new MoveValidator(state)).orElse(null);
                if (transfer != null) {
                    engine.apply(transfer);
                    return true;
                }
            }
            Player current = state.phase() == GamePhase.DEFENSE ? state.defender() : state.attacker();
            PlayerStrategy strategy = strategies.get(current);
            Move move = strategy.chooseMove(state, new MoveValidator(state));
            engine.apply(move);
            return true;
        } catch (ConsoleInputClosedException e) {
            io.println("Ввод закрыт, партия прервана.");
            return false;
        }
    }

    private void printEvents() {
        List<GameEvent> events = engine.journal().events();
        for (int i = printedEvents; i < events.size(); i++) {
            io.println(formatter.format(events.get(i)));
        }
        printedEvents = events.size();
    }

    private void printSummary() {
        io.println("Итоги партии:");
        for (Player player : engine.state().players()) {
            io.println("  " + player.name() + ": карт в руке — " + player.cardCount());
        }
        engine.result().ifPresent(result -> io.println("  Победил(и): "
                + result.winners().stream().map(Player::name).toList()));
        io.println("Событий в журнале: " + engine.journal().size());
    }
}
