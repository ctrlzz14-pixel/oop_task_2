package com.ctrlzz14.durak.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Карта")
class CardTest {

    @Test
    @DisplayName("хранит достоинство и масть")
    void keepsRankAndSuit() {
        Card card = new Card(Rank.QUEEN, Suit.DIAMONDS);

        assertEquals(Rank.QUEEN, card.rank());
        assertEquals(Suit.DIAMONDS, card.suit());
    }

    @Test
    @DisplayName("равна другой карте того же достоинства и масти")
    void equalsSameCard() {
        Card first = Card.of(Rank.TEN, Suit.HEARTS);
        Card second = Card.of(Rank.TEN, Suit.HEARTS);

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    @DisplayName("не равна карте другого достоинства или масти")
    void differsFromOtherCards() {
        Card card = new Card(Rank.TEN, Suit.HEARTS);

        assertNotEquals(card, new Card(Rank.TEN, Suit.SPADES));
        assertNotEquals(card, new Card(Rank.NINE, Suit.HEARTS));
    }

    @Test
    @DisplayName("определяет козырность по масти")
    void detectsTrump() {
        Card trump = new Card(Rank.SIX, Suit.CLUBS);
        Card plain = new Card(Rank.SIX, Suit.SPADES);

        assertTrue(trump.isTrump(Suit.CLUBS));
        assertFalse(plain.isTrump(Suit.CLUBS));
    }

    @Test
    @DisplayName("сравнивает достоинства")
    void comparesRanks() {
        assertTrue(Rank.ACE.isHigherThan(Rank.KING));
        assertFalse(Rank.SIX.isHigherThan(Rank.SIX));
        assertEquals(14, Rank.ACE.power());
    }

    @Test
    @DisplayName("печатается коротким обозначением")
    void printsShortNotation() {
        assertEquals("6♥", new Card(Rank.SIX, Suit.HEARTS).toString());
        assertEquals("10♠", new Card(Rank.TEN, Suit.SPADES).toString());
    }

    @Test
    @DisplayName("одинаковые достоинства распознаются независимо от масти")
    void detectsSameRank() {
        assertTrue(new Card(Rank.SEVEN, Suit.HEARTS).hasSameRank(new Card(Rank.SEVEN, Suit.CLUBS)));
        assertFalse(new Card(Rank.SEVEN, Suit.HEARTS).hasSameRank(new Card(Rank.EIGHT, Suit.HEARTS)));
    }
}
