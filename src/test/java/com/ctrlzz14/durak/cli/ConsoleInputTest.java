package com.ctrlzz14.durak.cli;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Чтение команд игрока")
class ConsoleInputTest {

    private static BufferedReader reader(byte[] bytes, Charset fallback) {
        return new BufferedReader(new ConsoleInput(new ByteArrayInputStream(bytes), fallback));
    }

    @Test
    @DisplayName("строки читаются по одной")
    void linesAreReadOneByOne() throws IOException {
        BufferedReader reader = reader("1\n2\n3\n".getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);

        assertEquals("1", reader.readLine());
        assertEquals("2", reader.readLine());
        assertEquals("3", reader.readLine());
        assertFalse(reader.ready() && reader.readLine() != null);
    }

    @Test
    @DisplayName("кириллица в UTF-8 читается без искажений")
    void utf8IsDecoded() throws IOException {
        BufferedReader reader = reader("конец\n".getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);

        assertEquals("конец", reader.readLine());
    }

    @Test
    @DisplayName("некорректный UTF-8 читается в кодировке платформы")
    void invalidUtf8FallsBackToPlatformCharset() throws IOException {
        byte[] windows1251 = "конец".getBytes(Charset.forName("windows-1251"));
        BufferedReader reader = reader(windows1251, Charset.forName("windows-1251"));

        assertEquals("конец", reader.readLine());
    }

    @Test
    @DisplayName("метка порядка байтов в начале потока не мешает разбору")
    void byteOrderMarkIsRemoved() throws IOException {
        byte[] bytes = ("\uFEFF1\n").getBytes(StandardCharsets.UTF_8);
        BufferedReader reader = reader(bytes, StandardCharsets.UTF_8);

        assertEquals("1", reader.readLine());
    }

    @Test
    @DisplayName("пустой поток сразу даёт конец ввода")
    void emptyStreamIsClosed() throws IOException {
        BufferedReader reader = reader(new byte[0], StandardCharsets.UTF_8);

        assertTrue(reader.readLine() == null);
    }

    @Test
    @DisplayName("кодировка платформы определяется всегда")
    void platformCharsetIsAvailable() {
        assertTrue(ConsoleInput.platformCharset() != null);
    }
}
