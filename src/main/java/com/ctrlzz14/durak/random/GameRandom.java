package com.ctrlzz14.durak.random;

import java.util.List;
import java.util.Random;

/**
 * Управляемый генератор случайных величин.
 *
 * <p>Класс является единственным источником случайности во всей игре: колода
 * перемешивается только этим генератором, поэтому одинаковый {@code seed}
 * всегда даёт одинаковую последовательность раздачи. Собственный
 * {@link Random} внутри других классов не создаётся, а {@code Math.random()}
 * в проекте не используется.</p>
 *
 * <p>Экземпляр неизменяемый: после создания последовательность чисел
 * воспроизводима и не зависит от внешнего состояния.</p>
 */
public final class GameRandom {

    private final long seed;
    private final Random random;

    /**
     * Создаёт генератор с заданным зерном.
     *
     * @param seed зерно генератора; одинаковые зёрна дают одинаковые последовательности
     */
    public GameRandom(long seed) {
        this.seed = seed;
        this.random = new Random(seed);
    }

    /**
     * Возвращает зерно генератора.
     *
     * @return зерно, с которым был создан генератор
     */
    public long seed() {
        return seed;
    }

    /**
     * Возвращает псевдослучайное число в диапазоне {@code [0, bound)}.
     *
     * @param bound верхняя граница (exclusive), должна быть положительной
     * @return псевдослучайное число
     * @throws IllegalArgumentException если {@code bound <= 0}
     */
    public int nextInt(int bound) {
        if (bound <= 0) {
            throw new IllegalArgumentException("Граница должна быть положительной, но равна " + bound);
        }
        return random.nextInt(bound);
    }

    /**
     * Перемешивает список в определённом алгоритмом порядке (алгоритм Фишера — Йетса).
     *
     * @param list список для перемешивания, изменяется на месте
     * @param <T> тип элементов списка
     */
    public <T> void shuffle(List<T> list) {
        for (int i = list.size() - 1; i > 0; i--) {
            int j = nextInt(i + 1);
            T tmp = list.get(i);
            list.set(i, list.get(j));
            list.set(j, tmp);
        }
    }
}
