package com.ctrlzz14.durak.strategy;

import com.ctrlzz14.durak.cli.ConsoleIO;
import com.ctrlzz14.durak.cli.ConsoleInputClosedException;
import com.ctrlzz14.durak.model.Card;
import com.ctrlzz14.durak.model.GamePhase;
import com.ctrlzz14.durak.model.GameState;
import com.ctrlzz14.durak.model.Player;
import com.ctrlzz14.durak.move.Move;
import com.ctrlzz14.durak.rules.IllegalMoveException;
import com.ctrlzz14.durak.rules.MoveValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Стратегия игрока-человека: ходы вводятся с клавиатуры.
 *
 * <p>Человек выбирает карты по их номерам в отображаемой руке. Поддерживаются
 * команды {@code "конец"} (завершить атаку), {@code "взять"} (забрать карты
 * вместо защиты) и {@code "перевести"} (передать атаку следующему игроку).</p>
 */
public final class HumanStrategy implements PlayerStrategy {

    private static final String COMMAND_END = "конец";
    private static final String COMMAND_TAKE = "взять";
    private static final String COMMAND_TRANSFER = "перевести";
    private static final String COMMAND_NO = "нет";

    private final ConsoleIO io;
    private final String title;

    /**
     * Создаёт стратегию игрока-человека.
     *
     * @param io консольный ввод и вывод
     * @param title название игрока для вывода в консоль
     */
    public HumanStrategy(ConsoleIO io, String title) {
        this.io = io;
        this.title = title;
    }

    /**
     * Возвращает название игрока.
     *
     * @return название игрока
     */
    public String title() {
        return title;
    }

    @Override
    public Move chooseMove(GameState state, MoveValidator validator) {
        if (state.phase() == GamePhase.DEFENSE) {
            return chooseDefense(state, validator);
        }
        return chooseAttack(state, validator);
    }

    @Override
    public Optional<Move> chooseTransfer(GameState state, MoveValidator validator) {
        List<Card> legal = validator.legalTransferCards(state.defender());
        if (legal.isEmpty()) {
            return Optional.empty();
        }
        io.println("Атаку можно перевести на " + state.nextPlayer(state.defender()).name() + ".");
        showHand(state.defender(), legal);
        while (true) {
            String answer = ask("Перевести атаку? (" + COMMAND_TRANSFER + " / " + COMMAND_NO + "): ");
            if (answer.startsWith(COMMAND_NO)) {
                return Optional.empty();
            }
            if (!answer.startsWith(COMMAND_TRANSFER)) {
                io.println("Некорректная команда: " + answer);
                continue;
            }
            String numbers = ask("Номера карт для перевода: ");
            List<Card> cards = readCards(state.defender(), legal, numbers);
            if (cards.isEmpty()) {
                return Optional.empty();
            }
            Move move = Move.transfer(state.defender(), cards);
            if (validate(move, validator)) {
                return Optional.of(move);
            }
        }
    }

    private Move chooseAttack(GameState state, MoveValidator validator) {
        Player attacker = state.attacker();
        List<Card> legal = validator.legalAttackCards(attacker);
        int limit = state.segmentLimit() - state.cardsThrownInSegment();
        io.println("--- Атакует " + attacker.name() + " ---");
        showHand(attacker, legal);
        if (limit <= 0) {
            io.println("Лимит отрезка исчерпан, можно только завершить атаку.");
        }
        while (true) {
            String answer = ask("Номера карт для атаки (до " + limit + "), "
                    + COMMAND_END + " — закончить: ");
            if (answer.startsWith(COMMAND_END)) {
                return Move.endAttack(attacker);
            }
            List<Card> cards = readCards(attacker, legal, answer);
            if (cards.isEmpty()) {
                continue;
            }
            Move move = Move.attack(attacker, cards);
            if (validate(move, validator)) {
                return move;
            }
        }
    }

    private Move chooseDefense(GameState state, MoveValidator validator) {
        Player defender = state.defender();
        List<Card> legal = validator.legalDefenseCards(defender);
        io.println("--- Защищается " + defender.name() + " ---");
        showHand(defender, legal);
        while (true) {
            String answer = ask("Номер отбивающей карты, " + COMMAND_TAKE + " — взять карты: ");
            if (answer.startsWith(COMMAND_TAKE)) {
                return Move.take(defender);
            }
            List<Card> cards = readCards(defender, legal, answer);
            if (cards.isEmpty()) {
                continue;
            }
            Move move = Move.defend(defender, cards.get(0));
            if (validate(move, validator)) {
                return move;
            }
        }
    }

    /**
     * Разбирает ответ игрока: номера карт в его руке.
     *
     * @param player игрок, чьи карты выбираются
     * @param legal карты, которые разрешено сыграть сейчас
     * @param answer строка ответа игрока
     * @return выбранные карты; пустой список, если ответ не разобран
     */
    private List<Card> readCards(Player player, List<Card> legal, String answer) {
        List<Card> hand = player.hand().sorted();
        List<Card> chosen = new ArrayList<>();
        for (String token : answer.split("\\s+")) {
            int index;
            try {
                index = Integer.parseInt(token);
            } catch (NumberFormatException e) {
                io.println("Номер карты должен быть числом: " + token);
                return List.of();
            }
            if (index < 1 || index > hand.size()) {
                io.println("Нет карты с номером " + index + " (в руке " + hand.size() + " карт).");
                return List.of();
            }
            Card card = hand.get(index - 1);
            if (!legal.contains(card)) {
                io.println("Карту " + card + " сейчас сыграть нельзя.");
                return List.of();
            }
            if (!chosen.contains(card)) {
                chosen.add(card);
            }
        }
        return chosen;
    }

    private boolean validate(Move move, MoveValidator validator) {
        try {
            validator.validate(move);
            return true;
        } catch (IllegalMoveException e) {
            io.println("Ход невозможен: " + e.getMessage());
            return false;
        }
    }

    private void showHand(Player player, List<Card> legal) {
        List<Card> hand = player.hand().sorted();
        io.println("Ваши карты (" + title + "):");
        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            String mark = legal.contains(card) ? " " : "*";
            io.println("  " + (i + 1) + ") " + card + mark);
        }
        io.println("  (карты со знаком * сейчас сыграть нельзя)");
    }

    private String readLine() {
        Optional<String> line = io.readLine();
        if (line.isEmpty()) {
            throw new ConsoleInputClosedException("Ввод закрыт: невозможно получить ход игрока.");
        }
        return line.get().trim().toLowerCase();
    }

    private String ask(String prompt) {
        io.print(prompt);
        return readLine();
    }
}
