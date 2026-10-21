package com.ctrlzz14.durak.cli;

/**
 * Исключение, возникающее, когда ввод пользователя закрыт.
 *
 * <p>Обычно это означает, что пользователь нажал {@code Ctrl+D} или поток
 * ввода завершился. Партия при этом прерывается, а состояние игры остаётся
 * согласованным.</p>
 */
public class ConsoleInputClosedException extends RuntimeException {

    /**
     * Создаёт исключение с пояснением.
     *
     * @param message описание причины
     */
    public ConsoleInputClosedException(String message) {
        super(message);
    }
}
