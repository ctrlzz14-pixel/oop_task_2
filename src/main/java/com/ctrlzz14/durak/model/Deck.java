package com.ctrlzz14.durak.model;

import com.ctrlzz14.durak.random.GameRandom;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

/**
 * Колода карт.
 *
 * <p>Колода хранит карты в порядке «сверху вниз»: первая карта вытягивается
 * первой, последняя карта является нижней (нижней картой колоды), которая в
 * подкидном дураке определяет козырную масть.</p>
 *
 * <p>Стандартная колода содержит 36 карт: девять достоинств
 * ({@link Rank#SIX}…{@link Rank#ACE}) в четырёх мастях ({@link Suit}).
 * Карты в колоде не могут повторяться.</p>
 */
public final class Deck {

    private final Deque<Card> cards;

    /**
     * Создаёт колоду из переданных карт.
     *
     * @param cards карты в порядке «сверху вниз»; копия списка
     * @throws NullPointerException если в списке есть {@code null}
     */
    public Deck(List<Card> cards) {
        this.cards = new ArrayDeque<>(cards);
    }

    /**
     * Создаёт стандартную колоду из 36 карт без перемешивания.
     *
     * @return новая колода со всеми картами от младшей до старшей в каждой масти
     */
    public static Deck standard() {
        List<Card> all = new ArrayList<>(36);
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                all.add(new Card(rank, suit));
            }
        }
        return new Deck(all);
    }

    /**
     * Перемешивает колоду заданным генератором случайных величин.
     *
     * <p>Метод не создаёт собственного генератора: последовательность
     * перемешивания полностью определяется зерном переданного
     * {@link GameRandom}, поэтому партии с одинаковым зерном идентичны.</p>
     *
     * @param random генератор случайных величин
     */
    public void shuffle(GameRandom random) {
        List<Card> asList = new ArrayList<>(cards);
        random.shuffle(asList);
        cards.clear();
        cards.addAll(asList);
    }

    /**
     * Вытягивает верхнюю карту колоды.
     *
     * @return снятая карта
     * @throws IllegalStateException если колода пуста
     */
    public Card draw() {
        Card card = cards.pollFirst();
        if (card == null) {
            throw new IllegalStateException("Колода пуста");
        }
        return card;
    }

    /**
     * Вытягивает указанное количество карт с верха колоды.
     *
     * @param count количество карт
     * @return список снятых карт в порядке снятия
     * @throws IllegalArgumentException если {@code count < 0}
     * @throws IllegalStateException если в колоде меньше карт, чем запрошено
     */
    public List<Card> draw(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("Количество карт не может быть отрицательным: " + count);
        }
        List<Card> drawn = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            drawn.add(draw());
        }
        return drawn;
    }

    /**
     * Возвращает нижнюю карту колоды, не снимая её.
     *
     * @return нижняя карта или {@link Optional#empty()}, если колода пуста
     */
    public Optional<Card> bottomCard() {
        return Optional.ofNullable(cards.peekLast());
    }

    /**
     * Возвращает оставшиеся карты в порядке «сверху вниз».
     *
     * @return неизменяемый список оставшихся карт
     */
    public List<Card> cards() {
        return Collections.unmodifiableList(new ArrayList<>(cards));
    }

    /**
     * Возвращает количество оставшихся карт.
     *
     * @return число карт в колоде
     */
    public int size() {
        return cards.size();
    }

    /**
     * Проверяет, пуста ли колода.
     *
     * @return {@code true}, если карт не осталось
     */
    public boolean isEmpty() {
        return cards.isEmpty();
    }
}
