package com.ctrlzz14.durak.strategy;

import com.ctrlzz14.durak.model.GameState;
import com.ctrlzz14.durak.move.Move;
import com.ctrlzz14.durak.rules.MoveValidator;

import java.util.Optional;

/**
 * Стратегия игрока: решает, какой ход сделать.
 *
 * <p>Интерфейс отделяет правила игры от способа игры. Человек реализует
 * стратегию через консольный ввод, компьютер — через собственную логику, но
 * движок работает с обоими одинаково: стратегия возвращает ход, который
 * движок проверяет и применяет.</p>
 */
public interface PlayerStrategy {

    /**
     * Выбирает ход для текущей ситуации.
     *
     * <p>В фазе атаки выбирается атака или завершение атаки, в фазе защиты —
     * отбивающая карта или взятие карт.</p>
     *
     * @param state состояние партии
     * @param validator валидатор ходов текущей партии
     * @return выбранный ход
     */
    Move chooseMove(GameState state, MoveValidator validator);

    /**
     * Решает, переводить ли атаку следующему игроку.
     *
     * <p>Метод вызывается только в ситуации, когда защищающийся только что
     * отбился картой и может перевести атаку. Отказ от перевода означает, что
     * атакующий продолжает атаку.</p>
     *
     * @param state состояние партии
     * @param validator валидатор ходов текущей партии
     * @return ход перевода или пустой результат, если перевод не выполняется
     */
    default Optional<Move> chooseTransfer(GameState state, MoveValidator validator) {
        return Optional.empty();
    }
}
