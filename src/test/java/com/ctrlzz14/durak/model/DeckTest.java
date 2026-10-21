package com.ctrlzz14.durak.model;

import com.ctrlzz14.durak.random.GameRandom;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Колода")
class DeckTest {

    @Test
    @DisplayName("стандартная колода содержит 36 уникальных карт")
    void standardDeckHas36UniqueCards() {
        Deck deck = Deck.standard();

        assertEquals(36, deck.size());
        assertEquals(36, new HashSet<>(deck.cards()).size());
    }

    @Test
    @DisplayName("в колоде есть карты всех мастей и достоинств")
    void standardDeckContainsAllRanksAndSuits() {
        Set<Rank> ranks = new HashSet<>();
        Set<Suit> suits = new HashSet<>();
        Deck.standard().cards().forEach(card -> {
            ranks.add(card.rank());
            suits.add(card.suit());
        });

        assertEquals(9, ranks.size());
        assertEquals(4, suits.size());
    }

    @Test
    @DisplayName("вытягивание карты уменьшает колоду")
    void drawRemovesCard() {
        Deck deck = Deck.standard();

        Card card = deck.draw();

        assertEquals(35, deck.size());
        assertTrue(deck.cards().contains(card) == false);
    }

    @Test
    @DisplayName("вытягивание нескольких карт возвращает их по порядку")
    void drawSeveralCards() {
        Deck deck = Deck.standard();
        List<Card> before = deck.cards();

        List<Card> drawn = deck.draw(4);

        assertEquals(4, drawn.size());
        assertEquals(32, deck.size());
        assertEquals(before.subList(0, 4), drawn);
        assertEquals(before.get(4), deck.draw());
    }

    @Test
    @DisplayName("нижняя карта доступна до вытягивания")
    void bottomCardIsAvailable() {
        Deck deck = Deck.standard();

        assertTrue(deck.bottomCard().isPresent());
        assertEquals(36, deck.size());
    }

    @Test
    @DisplayName("тасовка с одним зерном воспроизводима")
    void shuffleIsReproducible() {
        Deck first = Deck.standard();
        Deck second = Deck.standard();

        first.shuffle(new GameRandom(42L));
        second.shuffle(new GameRandom(42L));

        assertEquals(first.cards(), second.cards());
    }

    @Test
    @DisplayName("тасовка с разными зёрнами даёт разный порядок")
    void shuffleWithDifferentSeedsDiffers() {
        Deck first = Deck.standard();
        Deck second = Deck.standard();

        first.shuffle(new GameRandom(1L));
        second.shuffle(new GameRandom(2L));

        assertFalse(first.cards().equals(second.cards()));
    }

    @Test
    @DisplayName("из пустой колоды нельзя вытянуть карту")
    void drawFromEmptyDeckFails() {
        Deck deck = new Deck(List.of());

        assertTrue(deck.isEmpty());
        assertThrows(IllegalStateException.class, deck::draw);
    }
}
