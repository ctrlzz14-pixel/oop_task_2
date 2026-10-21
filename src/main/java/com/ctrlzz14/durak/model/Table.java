package com.ctrlzz14.durak.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Стол: карты, лежащие в текущем раунде.
 *
 * <p>Стол хранит две параллельные последовательности: атакующие карты
 * ({@link #attackCards()}) и отбивающие карты ({@link #defenseCards()}).
 * Каждая отбивающая карта закрывает соответствующую атакующую по порядку,
 * поэтому карта, которую сейчас нужно отбить, — это первая атакующая карта,
 * для которой ещё нет отбивающей.</p>
 */
public final class Table {

    private final List<Card> attackCards = new ArrayList<>();
    private final List<Card> defenseCards = new ArrayList<>();

    /**
     * Создаёт пустой стол.
     */
    public Table() {
        // пустой стол
    }

    /**
     * Кладёт атакующую карту на стол.
     *
     * @param card атакующая карта
     */
    public void addAttack(Card card) {
        attackCards.add(card);
    }

    /**
     * Кладёт отбивающую карту на стол.
     *
     * <p>Метод не проверяет соответствие атакующей карте: корректность хода
     * гарантирует {@code MoveValidator}, а таблица отвечает только за хранение
     * порядка карт.</p>
     *
     * @param card отбивающая карта
     */
    public void addDefense(Card card) {
        defenseCards.add(card);
    }

    /**
     * Возвращает атакующую карту, которую сейчас предстоит отбить.
     *
     * @return текущая атакующая карта или {@link Optional#empty()}, если всё отбито
     */
    public Optional<Card> currentAttackCard() {
        int index = defenseCards.size();
        return index < attackCards.size() ? Optional.of(attackCards.get(index)) : Optional.empty();
    }

    /**
     * Возвращает количество атакующих карт, ещё не отбитых.
     *
     * @return число неотбитых карт
     */
    public int uncoveredCount() {
        return attackCards.size() - defenseCards.size();
    }

    /**
     * Возвращает атакующие карты в порядке выкладывания.
     *
     * @return неизменяемый список атакующих карт
     */
    public List<Card> attackCards() {
        return Collections.unmodifiableList(attackCards);
    }

    /**
     * Возвращает отбивающие карты в порядке выкладывания.
     *
     * @return неизменяемый список отбивающих карт
     */
    public List<Card> defenseCards() {
        return Collections.unmodifiableList(defenseCards);
    }

    /**
     * Возвращает все карты стола: сначала атакующие, затем отбивающие.
     *
     * <p>Именно эти карты уходят в отбой по окончании раунда либо достаются
     * защищающемуся при взятии карт.</p>
     *
     * @return список всех карт стола
     */
    public List<Card> allCards() {
        List<Card> all = new ArrayList<>(attackCards);
        all.addAll(defenseCards);
        return all;
    }

    /**
     * Возвращает неотбитые атакующие карты.
     *
     * @return список неотбитых карт
     */
    public List<Card> uncoveredAttackCards() {
        if (uncoveredCount() == 0) {
            return List.of();
        }
        return List.copyOf(attackCards.subList(defenseCards.size(), attackCards.size()));
    }

    /**
     * Возвращает количество карт на столе.
     *
     * @return число карт
     */
    public int size() {
        return attackCards.size() + defenseCards.size();
    }

    /**
     * Проверяет, пуст ли стол.
     *
     * @return {@code true}, если на столе нет карт
     */
    public boolean isEmpty() {
        return attackCards.isEmpty() && defenseCards.isEmpty();
    }

    /**
     * Убирает все карты со стола.
     */
    public void clear() {
        attackCards.clear();
        defenseCards.clear();
    }
}
