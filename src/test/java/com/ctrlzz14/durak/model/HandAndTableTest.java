package com.ctrlzz14.durak.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Рука и стол")
class HandAndTableTest {

    @Test
    @DisplayName("рука хранит и выдаёт карты")
    void handKeepsAndRemovesCards() {
        Hand hand = new Hand();
        Card six = new Card(Rank.SIX, Suit.HEARTS);

        hand.add(six);

        assertEquals(1, hand.size());
        assertTrue(hand.contains(six));
        assertTrue(hand.remove(six));
        assertFalse(hand.contains(six));
        assertTrue(hand.isEmpty());
    }

    @Test
    @DisplayName("рука сортирует карты для отображения")
    void handSortsCards() {
        Hand hand = new Hand();
        hand.add(new Card(Rank.TEN, Suit.HEARTS));
        hand.add(new Card(Rank.SIX, Suit.SPADES));
        hand.add(new Card(Rank.SIX, Suit.HEARTS));

        List<Card> sorted = hand.sorted();

        assertEquals(3, sorted.size());
        assertEquals(Rank.SIX, sorted.get(0).rank());
        assertEquals(Rank.SIX, sorted.get(1).rank());
        assertEquals(Rank.TEN, sorted.get(2).rank());
    }

    @Test
    @DisplayName("карта находится по достоинству и масти")
    void handFindsCard() {
        Hand hand = new Hand();
        hand.add(new Card(Rank.SIX, Suit.HEARTS));

        assertTrue(hand.find(Rank.SIX, Suit.HEARTS).isPresent());
        assertTrue(hand.find(Rank.SIX, Suit.SPADES).isEmpty());
    }

    @Test
    @DisplayName("стол считает неотбитые карты")
    void tableCountsUncoveredCards() {
        Table table = new Table();
        table.addAttack(new Card(Rank.SIX, Suit.HEARTS));
        table.addAttack(new Card(Rank.SEVEN, Suit.HEARTS));
        table.addDefense(new Card(Rank.SEVEN, Suit.SPADES));

        assertEquals(1, table.uncoveredCount());
        assertEquals(1, table.uncoveredAttackCards().size());
    }

    @Test
    @DisplayName("стол хранит атакующие и отбивающие карты")
    void tableStoresAttackAndDefense() {
        Table table = new Table();
        Card attack = new Card(Rank.SIX, Suit.HEARTS);
        table.addAttack(attack);

        assertEquals(attack, table.currentAttackCard().orElseThrow());

        table.addDefense(new Card(Rank.SEVEN, Suit.SPADES));

        assertEquals(1, table.attackCards().size());
        assertEquals(1, table.defenseCards().size());
        assertEquals(2, table.allCards().size());
        assertTrue(table.currentAttackCard().isEmpty(), "все атакующие карты уже отбиты");
    }

    @Test
    @DisplayName("очистка стола убирает все карты")
    void tableClearRemovesCards() {
        Table table = new Table();
        table.addAttack(new Card(Rank.SIX, Suit.HEARTS));
        table.addDefense(new Card(Rank.SEVEN, Suit.SPADES));

        table.clear();

        assertTrue(table.isEmpty());
        assertEquals(0, table.size());
    }

    @Test
    @DisplayName("отбой накапливает карты")
    void discardPileCollectsCards() {
        DiscardPile pile = new DiscardPile();

        pile.add(new Card(Rank.SIX, Suit.HEARTS));
        pile.addAll(List.of(new Card(Rank.SEVEN, Suit.HEARTS)));

        assertEquals(2, pile.size());
        assertFalse(pile.isEmpty());
    }
}
