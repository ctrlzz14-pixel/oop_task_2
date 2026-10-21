package com.ctrlzz14.durak.cli;

import com.ctrlzz14.durak.bot.SimpleBotStrategy;
import com.ctrlzz14.durak.engine.GameEngine;
import com.ctrlzz14.durak.event.AttackEvent;
import com.ctrlzz14.durak.event.EventType;
import com.ctrlzz14.durak.event.GameEndedEvent;
import com.ctrlzz14.durak.event.RoundEndedEvent;
import com.ctrlzz14.durak.event.TrumpChosenEvent;
import com.ctrlzz14.durak.model.Card;
import com.ctrlzz14.durak.model.GamePhase;
import com.ctrlzz14.durak.model.GameRules;
import com.ctrlzz14.durak.model.GameState;
import com.ctrlzz14.durak.model.Player;
import com.ctrlzz14.durak.model.Rank;
import com.ctrlzz14.durak.model.Suit;
import com.ctrlzz14.durak.model.TrumpMode;
import com.ctrlzz14.durak.rules.MoveValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Форматирование событий")
class EventFormatterTest {

    private final EventFormatter formatter = new EventFormatter();

    @Test
    @DisplayName("начало партии содержит число игроков и зерно")
    void gameStartedIsFormatted() {
        GameEngine engine = GameEngine.start(GameRules.standard(), List.of("Первый", "Второй"), 42L);
        String text = formatter.format(engine.journal().eventsOfType(EventType.GAME_STARTED).get(0));

        assertTrue(text.contains("Партия началась"));
        assertTrue(text.contains("2"));
        assertTrue(text.contains("42"));
    }

    @Test
    @DisplayName("козырь выводится вместе с мастью")
    void trumpIsFormatted() {
        GameEngine engine = GameEngine.start(GameRules.standard(), List.of("Первый", "Второй"), 42L);
        TrumpChosenEvent event = engine.journal().eventsOfType(EventType.TRUMP_CHOSEN).stream()
                .map(TrumpChosenEvent.class::cast)
                .findFirst()
                .orElseThrow();

        String text = formatter.format(event);

        assertTrue(text.startsWith("Козырь: "));
        assertTrue(text.contains(event.trumpSuit().russianName()));
    }

    @Test
    @DisplayName("атака выводится вместе с картами")
    void attackIsFormatted() {
        GameEngine engine = GameEngine.start(GameRules.standard(), List.of("Первый", "Второй"), 42L);
        Card card = engine.state().attacker().hand().cards().get(0);
        engine.attack(engine.state().attacker(), card);

        AttackEvent event = engine.journal().eventsOfType(EventType.ATTACK).stream()
                .map(AttackEvent.class::cast)
                .findFirst()
                .orElseThrow();
        String text = formatter.format(event);

        assertTrue(text.contains("атакует"));
        assertTrue(text.contains(engine.state().attacker().name()));
    }

    @Test
    @DisplayName("завершение раунда содержит причину и число карт в отбое")
    void roundEndIsFormatted() {
        GameEngine engine = GameEngine.start(GameRules.standard(), List.of("Первый", "Второй"), 42L);
        Card card = engine.state().attacker().hand().cards().get(0);
        engine.attack(engine.state().attacker(), card);
        engine.take(engine.state().defender());

        RoundEndedEvent event = engine.journal().eventsOfType(EventType.ROUND_ENDED).stream()
                .map(RoundEndedEvent.class::cast)
                .findFirst()
                .orElseThrow();
        String text = formatter.format(event);

        assertTrue(text.contains("Раунд 1 завершён"));
        assertTrue(text.contains("карты взяты"));
    }

    @Test
    @DisplayName("окончание партии содержит победителей")
    void gameEndIsFormatted() {
        GameEngine engine = GameEngine.start(GameRules.standard(), List.of("Первый", "Второй"), 42L);
        SimpleBotStrategy bot = new SimpleBotStrategy("Бот");
        while (!engine.state().isGameOver()) {
            GameState state = engine.state();
            Player current = state.phase() == GamePhase.DEFENSE ? state.defender() : state.attacker();
            engine.apply(bot.chooseMove(state, new MoveValidator(state)));
        }

        GameEndedEvent event = engine.journal().eventsOfType(EventType.GAME_ENDED).stream()
                .map(GameEndedEvent.class::cast)
                .findFirst()
                .orElseThrow();
        String text = formatter.format(event);

        assertTrue(text.contains("Партия окончена"));
        assertTrue(text.contains("Победил"));
    }

    @Test
    @DisplayName("список карт выводится через запятую")
    void cardsAreJoined() {
        String text = formatter.formatCards(List.of(
                new Card(Rank.SIX, Suit.HEARTS),
                new Card(Rank.ACE, Suit.SPADES)));

        assertTrue(text.contains(", "));
        assertEquals(2, text.split(", ").length);
    }

    @Test
    @DisplayName("пустой список карт выводится прочерком")
    void emptyCardsAreDashed() {
        assertEquals("—", formatter.formatCards(List.of()));
    }

    @Test
    @DisplayName("неизвестное событие не приводит к ошибке")
    void unknownEventIsFormatted() {
        GameEngine engine = GameEngine.start(GameRules.builder().trumpMode(TrumpMode.CLOSED).build(),
                List.of("Первый", "Второй"), 42L);

        assertFalse(formatter.format(engine.journal().events().get(0)).isEmpty());
    }
}
