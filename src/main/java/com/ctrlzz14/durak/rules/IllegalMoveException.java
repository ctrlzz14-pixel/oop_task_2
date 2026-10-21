package com.ctrlzz14.durak.rules;

/**
 * Исключение недопустимого хода.
 *
 * <p>Механика сообщает о недопустимых действиях этим исключением: состояние
 * партии при его выбрасывании не изменяется. Исключение является
 * непроверяемым, чтобы не загромождать сигнатуры методов движка.</p>
 */
public class IllegalMoveException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Создаёт исключение с сообщением о причине.
     *
     * @param message описание причины, по которой ход недопустим
     */
    public IllegalMoveException(String message) {
        super(message);
    }

    /**
     * Создаёт исключение с сообщением о причине и её причиной.
     *
     * @param message описание причины, по которой ход недопустим
     * @param cause исходная причина
     */
    public IllegalMoveException(String message, Throwable cause) {
        super(message, cause);
    }
}
