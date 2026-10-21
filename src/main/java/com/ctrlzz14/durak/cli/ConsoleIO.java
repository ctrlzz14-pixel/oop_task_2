package com.ctrlzz14.durak.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.util.Objects;
import java.util.Optional;

/**
 * Консольный ввод и вывод.
 *
 * <p>Класс отделяет источник и приёмник данных от игровой логики: движок и
 * стратегии ничего не знают о потоках, поэтому консольный интерфейс можно
 * проверять в тестах, подставляя собственный {@code BufferedReader} и
 * {@code PrintStream}.</p>
 */
public final class ConsoleIO {

    private final BufferedReader reader;
    private final PrintStream out;

    /**
     * Создаёт консольный ввод и вывод.
     *
     * @param reader источник команд
     * @param out приёмник вывода
     * @throws NullPointerException если потоки равны {@code null}
     */
    public ConsoleIO(BufferedReader reader, PrintStream out) {
        this.reader = Objects.requireNonNull(reader, "Источник команд не должен быть null");
        this.out = Objects.requireNonNull(out, "Приёмник вывода не должен быть null");
    }

    /**
     * Читает очередную команду пользователя.
     *
     * @return строка команды или пустой результат, если ввод закрыт
     * @throws UncheckedIOException если при чтении произошла ошибка
     */
    public Optional<String> readLine() {
        try {
            String line = reader.readLine();
            return line == null ? Optional.empty() : Optional.of(line);
        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось прочитать команду", e);
        }
    }

    /**
     * Печатает текст без перевода строки.
     *
     * @param text текст
     */
    public void print(String text) {
        out.print(text);
        out.flush();
    }

    /**
     * Печатает текст с переводом строки.
     *
     * @param text текст
     */
    public void println(String text) {
        out.println(text);
        out.flush();
    }

    /**
     * Печатает пустую строку.
     */
    public void println() {
        out.println();
        out.flush();
    }
}
