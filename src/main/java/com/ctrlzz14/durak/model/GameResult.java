package com.ctrlzz14.durak.model;

import java.util.List;
import java.util.Objects;

/**
 * Итоговый результат партии.
 *
 * <p>Результат формируется в момент завершения партии и содержит победителей,
 * проигравших и причину окончания. Победители отсортированы по возрастанию
 * числа карт в руке (чем меньше карт, тем лучше результат), проигравшие — по
 * убыванию, чтобы «главный дурак» стоял первым.</p>
 */
public final class GameResult {

    private final List<Player> winners;
    private final List<Player> losers;
    private final EndReason reason;

    /**
     * Создаёт результат партии.
     *
     * @param winners победители
     * @param losers проигравшие
     * @param reason причина завершения партии
     * @throws NullPointerException если любой из аргументов равен {@code null}
     */
    public GameResult(List<Player> winners, List<Player> losers, EndReason reason) {
        this.winners = List.copyOf(Objects.requireNonNull(winners, "Список победителей не должен быть null"));
        this.losers = List.copyOf(Objects.requireNonNull(losers, "Список проигравших не должен быть null"));
        this.reason = Objects.requireNonNull(reason, "Причина окончания не должна быть null");
    }

    /**
     * Возвращает победителей партии.
     *
     * @return неизменяемый список победителей
     */
    public List<Player> winners() {
        return winners;
    }

    /**
     * Возвращает проигравших партии.
     *
     * @return неизменяемый список проигравших
     */
    public List<Player> losers() {
        return losers;
    }

    /**
     * Возвращает причину завершения партии.
     *
     * @return причина окончания
     */
    public EndReason reason() {
        return reason;
    }

    /**
     * Проверяет, завершилась ли партия ничьей.
     *
     * @return {@code true}, если проигравших нет
     */
    public boolean isDraw() {
        return losers.isEmpty();
    }

    /**
     * Возвращает текстовое описание результата.
     *
     * @return описание результата
     */
    @Override
    public String toString() {
        return "Победители: " + winners + ", проигравшие: " + losers + ", причина: " + reason;
    }
}
