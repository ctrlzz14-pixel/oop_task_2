package com.ctrlzz14.durak.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Журнал игровых событий.
 *
 * <p>Журнал не зависит от консоли: это обычный набор типизированных событий,
 * который движок наполняет по ходу партии, а интерфейс читает и форматирует.
 * Благодаря этому журнал можно сохранить в файл, воспроизвести по нему партию
 * или проанализировать ход игры (например, в турнире ботов).</p>
 *
 * <p>Журнал не потокобезопасен и рассчитан на один поток исполнения.</p>
 */
public final class GameJournal {

    private final List<GameEvent> events = new ArrayList<>();
    private long sequence;

    /**
     * Создаёт пустой журнал событий.
     */
    public GameJournal() {
    }

    /**
     * Записывает событие в журнал и присваивает ему порядковый номер.
     *
     * @param event событие для записи
     * @return то же самое событие (для удобства вызывающего кода)
     */
    public GameEvent record(GameEvent event) {
        Objects.requireNonNull(event, "Событие не должно быть null");
        event.assignSequence(++sequence);
        events.add(event);
        return event;
    }

    /**
     * Возвращает все записанные события в порядке следования.
     *
     * @return неизменяемый список событий
     */
    public List<GameEvent> events() {
        return Collections.unmodifiableList(events);
    }

    /**
     * Возвращает события выбранного типа.
     *
     * @param type тип события
     * @return список подходящих событий в исходном порядке
     */
    public List<GameEvent> eventsOfType(EventType type) {
        return events.stream()
                .filter(event -> event.type() == type)
                .toList();
    }

    /**
     * Возвращает последнее записанное событие.
     *
     * @return последнее событие или {@link Optional#empty()}, если журнал пуст
     */
    public Optional<GameEvent> last() {
        return events.isEmpty() ? Optional.empty() : Optional.of(events.get(events.size() - 1));
    }

    /**
     * Возвращает количество записанных событий.
     *
     * @return число событий
     */
    public int size() {
        return events.size();
    }

    /**
     * Проверяет, пуст ли журнал.
     *
     * @return {@code true}, если событий нет
     */
    public boolean isEmpty() {
        return events.isEmpty();
    }
}
