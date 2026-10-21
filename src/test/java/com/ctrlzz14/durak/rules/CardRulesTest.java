package com.ctrlzz14.durak.rules;

import com.ctrlzz14.durak.model.Card;
import com.ctrlzz14.durak.model.Rank;
import com.ctrlzz14.durak.model.Suit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Правила отбивания")
class CardRulesTest {

    @Test
    @DisplayName("козырь бьёт некозырную карту")
    void trumpBeatsPlainCard() {
        Card trump = new Card(Rank.SIX, Suit.CLUBS);
        Card plain = new Card(Rank.KING, Suit.HEARTS);

        assertTrue(CardRules.beats(plain, trump, Suit.CLUBS));
    }

    @Test
    @DisplayName("некозырная карта не бьёт козырь")
    void plainCardDoesNotBeatTrump() {
        Card trump = new Card(Rank.SIX, Suit.CLUBS);
        Card plain = new Card(Rank.ACE, Suit.HEARTS);

        assertFalse(CardRules.beats(trump, plain, Suit.CLUBS));
    }

    @Test
    @DisplayName("козырь бьётся только более старшим козырем")
    void onlyHigherTrumpBeatsTrump() {
        Card trump = new Card(Rank.TEN, Suit.CLUBS);
        Card higher = new Card(Rank.JACK, Suit.CLUBS);
        Card lower = new Card(Rank.NINE, Suit.CLUBS);

        assertTrue(CardRules.beats(trump, higher, Suit.CLUBS));
        assertFalse(CardRules.beats(trump, lower, Suit.CLUBS));
    }

    @Test
    @DisplayName("карта той же масти бьёт более младшую")
    void sameSuitHigherRankBeats() {
        Card attacked = new Card(Rank.SIX, Suit.HEARTS);
        Card defense = new Card(Rank.SEVEN, Suit.HEARTS);

        assertTrue(CardRules.beats(attacked, defense, Suit.CLUBS));
    }

    @Test
    @DisplayName("карта другой масти не бьёт")
    void otherSuitDoesNotBeat() {
        Card attacked = new Card(Rank.SIX, Suit.HEARTS);
        Card defense = new Card(Rank.ACE, Suit.SPADES);

        assertFalse(CardRules.beats(attacked, defense, Suit.CLUBS));
    }

    @Test
    @DisplayName("равная по достоинству карта не бьёт")
    void equalRankDoesNotBeat() {
        Card attacked = new Card(Rank.SIX, Suit.HEARTS);
        Card defense = new Card(Rank.SIX, Suit.HEARTS);

        assertFalse(CardRules.beats(attacked, defense, Suit.CLUBS));
    }

    @Test
    @DisplayName("козырность карты определяется по масти")
    void isTrumpDetectedBySuit() {
        assertTrue(CardRules.isTrump(new Card(Rank.SIX, Suit.HEARTS), Suit.HEARTS));
        assertFalse(CardRules.isTrump(new Card(Rank.SIX, Suit.SPADES), Suit.HEARTS));
    }
}
