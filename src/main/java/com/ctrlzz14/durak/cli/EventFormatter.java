package com.ctrlzz14.durak.cli;

import com.ctrlzz14.durak.event.AttackEvent;
import com.ctrlzz14.durak.event.CardsDealtEvent;
import com.ctrlzz14.durak.event.DefendEvent;
import com.ctrlzz14.durak.event.GameEndedEvent;
import com.ctrlzz14.durak.event.GameEvent;
import com.ctrlzz14.durak.event.GameStartedEvent;
import com.ctrlzz14.durak.event.RefillEvent;
import com.ctrlzz14.durak.event.RoundEndedEvent;
import com.ctrlzz14.durak.event.TakeEvent;
import com.ctrlzz14.durak.event.TransferEvent;
import com.ctrlzz14.durak.event.TrumpChosenEvent;
import com.ctrlzz14.durak.model.Card;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Форматирование событий партии в текстовые строки.
 *
 * <p>Форматирование вынесено в отдельный класс, чтобы движок не зависел от
 * представления событий, а консольный вывод можно было менять независимо от
 * механики игры.</p>
 */
public final class EventFormatter {

    /**
     * Создаёт форматтер событий партии.
     */
    public EventFormatter() {
    }

    /**
     * Преобразует событие в строку для вывода в консоль.
     *
     * @param event событие партии
     * @return текстовое представление события
     */
    public String format(GameEvent event) {
        if (event instanceof GameStartedEvent started) {
            return "Партия началась. Игроков: " + started.playerNames().size()
                    + ", зерно: " + started.seed();
        }
        if (event instanceof TrumpChosenEvent trump) {
            return "Козырь: " + trump.trumpSuit().russianName()
                    + (trump.isCardVisible() ? " (" + trump.trumpCard() + ")" : " (нижняя карта скрыта)");
        }
        if (event instanceof CardsDealtEvent dealt) {
            return "Раздача: " + dealt.hands().entrySet().stream()
                    .map(entry -> entry.getKey().name() + " — " + formatCards(entry.getValue()))
                    .collect(Collectors.joining("; "));
        }
        if (event instanceof AttackEvent attack) {
            return attack.attacker().name() + " атакует: " + formatCards(attack.cards());
        }
        if (event instanceof DefendEvent defend) {
            return defend.defender().name() + " отбивается: " + defend.defenseCard()
                    + " (от " + defend.attackedCard() + ")";
        }
        if (event instanceof TransferEvent transfer) {
            return transfer.defender().name() + " переводит атаку на " + transfer.newDefender().name()
                    + ": " + formatCards(transfer.cards());
        }
        if (event instanceof TakeEvent take) {
            return take.defender().name() + " берёт карты: " + formatCards(take.takenCards());
        }
        if (event instanceof RefillEvent refill) {
            return refill.player().name() + " добрал из колоды: " + formatCards(refill.cards());
        }
        if (event instanceof RoundEndedEvent round) {
            String reason = switch (round.reason()) {
                case ATTACK_FINISHED -> "атака отбита";
                case CARDS_TAKEN -> "карты взяты";
            };
            return "Раунд " + round.roundNumber() + " завершён (" + reason
                    + "), в отбой ушло карт: " + round.discardedCount();
        }
        if (event instanceof GameEndedEvent ended) {
            return "Партия окончена. " + describeResult(ended);
        }
        return event.toString();
    }

    /**
     * Преобразует список карт в читаемую строку.
     *
     * @param cards карты
     * @return текстовое представление карт
     */
    public String formatCards(List<Card> cards) {
        if (cards.isEmpty()) {
            return "—";
        }
        return cards.stream().map(Card::toString).collect(Collectors.joining(", "));
    }

    private String describeResult(GameEndedEvent ended) {
        String winners = ended.result().winners().stream()
                .map(player -> player.name())
                .collect(Collectors.joining(", "));
        String reason = switch (ended.result().reason()) {
            case PLAYER_FINISHED -> "игрок вышел из партии";
            case STALEMATE -> "тупик";
        };
        return "Победил(и): " + winners + " (" + reason + ").";
    }
}
