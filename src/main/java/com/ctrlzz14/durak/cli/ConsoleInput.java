package com.ctrlzz14.durak.cli;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;

/**
 * Источник команд игрока, читающий поток ввода построчно.
 *
 * <p>Кодировка ввода определяется автоматически: сначала строка читается как
 * UTF-8, а если байты не образуют корректный UTF-8-текст, используется
 * кодировка операционной системы. Так приложение одинаково работает и в
 * терминале с UTF-8, и в старой консоли Windows с однобайтовой кодировкой.</p>
 */
public final class ConsoleInput extends Reader {

    private final InputStream stream;
    private final Charset fallbackCharset;
    private final StringBuilder pending = new StringBuilder();

    /**
     * Создаёт источник команд поверх стандартного ввода.
     *
     * @return источник команд
     */
    public static ConsoleInput system() {
        return new ConsoleInput(System.in, platformCharset());
    }

    /**
     * Создаёт источник команд.
     *
     * @param stream поток с командами игрока
     * @param fallbackCharset кодировка, используемая, если байты не являются UTF-8
     */
    public ConsoleInput(InputStream stream, Charset fallbackCharset) {
        this.stream = stream;
        this.fallbackCharset = fallbackCharset;
    }

    /**
     * Возвращает кодировку операционной системы.
     *
     * @return кодировка платформы либо UTF-8, если она неизвестна
     */
    public static Charset platformCharset() {
        String nativeEncoding = System.getProperty("native.encoding");
        if (nativeEncoding == null || nativeEncoding.isBlank()) {
            return StandardCharsets.UTF_8;
        }
        try {
            return Charset.forName(nativeEncoding);
        } catch (RuntimeException e) {
            return StandardCharsets.UTF_8;
        }
    }

    @Override
    public int read(char[] buffer, int offset, int length) throws IOException {
        if (length == 0) {
            return 0;
        }
        if (pending.length() == 0 && !readNextLine()) {
            return -1;
        }
        int count = Math.min(length, pending.length());
        pending.getChars(0, count, buffer, offset);
        pending.delete(0, count);
        return count;
    }

    @Override
    public void close() throws IOException {
        stream.close();
    }

    private boolean readNextLine() throws IOException {
        ByteArrayOutputStream line = new ByteArrayOutputStream();
        int read = stream.read();
        if (read == -1) {
            return false;
        }
        while (read != -1 && read != '\n') {
            if (read != '\r') {
                line.write(read);
            }
            read = stream.read();
        }
        pending.append(decode(line.toByteArray())).append('\n');
        return true;
    }

    private String decode(byte[] bytes) {
        CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT);
        String text;
        try {
            text = decoder.decode(ByteBuffer.wrap(bytes)).toString();
        } catch (CharacterCodingException e) {
            text = new String(bytes, fallbackCharset);
        }
        return stripByteOrderMark(text);
    }

    /**
     * Убирает метку порядка байтов в начале строки.
     *
     * <p>Некоторые оболочки (например, PowerShell при перенаправлении ввода)
     * добавляют в начало потока символ {@code U+FEFF}. Он не является частью
     * команды игрока и мешал бы разбору номера карты.</p>
     *
     * @param text исходный текст строки
     * @return текст без метки порядка байтов
     */
    private String stripByteOrderMark(String text) {
        return text.startsWith("\uFEFF") ? text.substring(1) : text;
    }
}
