package com.ctrlzz14.durak.support;

import com.ctrlzz14.durak.engine.GameEngine;
import com.ctrlzz14.durak.model.Card;
import com.ctrlzz14.durak.model.Deck;
import com.ctrlzz14.durak.model.GamePhase;
import com.ctrlzz14.durak.model.GameRules;
import com.ctrlzz14.durak.model.GameState;
import com.ctrlzz14.durak.model.Player;
import com.ctrlzz14.durak.model.Rank;
import com.ctrlzz14.durak.model.Suit;
import com.ctrlzz14.durak.model.TrumpMode;
import com.ctrlzz14.durak.random.GameRandom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Помощник для построения партий с заранее заданными картами.
 *
 * <p>Тесты механики не должны зависеть от случайной раздачи: карты в руках
 * задаются явно, а состояние партии собирается вручную. Так проверки правил
 * получаются однозначными и не зависят от зерна генератора.</p>
 */
public final class TestGame {

    private final List<Player> players = new ArrayList<>();
    private final List<Card> deckCards = new ArrayList<>();
    private TrumpMode trumpMode = TrumpMode.OPEN;
    private Suit trumpSuit = Suit.HEARTS;
    private GameRules rules = GameRules.standard();
    private long seed = 1L;

    private TestGame() {
    }

    /**
     * Создаёт пустой конструктор партии.
     *
     * @return конструктор
     */
    public static TestGame create() {
        return new TestGame();
    }

    /**
     * Добавляет игрока с указанными картами.
     *
     * @param name имя игрока
     * @param cards карты в руке
     * @return тот же конструктор
     */
    public TestGame player(String name, Card... cards) {
        players.add(new Player(players.size(), name, new ArrayList<>(Arrays.asList(cards))));
        return this;
    }

    /**
     * Задаёт козырную масть и режим козыря.
     *
     * @param suit козырная масть
     * @param mode режим козыря
     * @return тот же конструктор
     */
    public TestGame trump(Suit suit, TrumpMode mode) {
        this.trumpSuit = suit;
        this.trumpMode = mode;
        this.rules = GameRules.builder().trumpMode(mode).build();
        return this;
    }

    /**
     * Задаёт правила партии.
     *
     * @param rules правила
     * @return тот же конструктор
     */
    public TestGame rules(GameRules rules) {
        this.rules = rules;
        return this;
    }

    /**
     * Задаёт зерно генератора случайных величин.
     *
     * @param seed зерно
     * @return тот же конструктор
     */
    public TestGame seed(long seed) {
        this.seed = seed;
        return this;
    }

    /**
     * Задаёт карты в колоде.
     *
     * @param cards карты колоды
     * @return тот же конструктор
     */
    public TestGame deck(Card... cards) {
        deckCards.clear();
        deckCards.addAll(Arrays.asList(cards));
        return this;
    }

    /**
     * Собирает состояние партии.
     *
     * @return состояние
     */
    public GameState buildState() {
        Player attacker = players.get(0);
        Player defender = players.get(1);
        Card trumpCard = new Card(Rank.SIX, trumpSuit);
        return GameState.builder()
                .players(List.copyOf(players))
                .deck(new Deck(List.copyOf(deckCards)))
                .rules(rules)
                .seed(seed)
                .trump(trumpSuit, trumpCard)
                .phase(GamePhase.ATTACK)
                .roles(attacker, defender)
                .segment(0, defender.cardCount())
                .roundNumber(1)
                .build();
    }

    /**
     * Собирает движок партии с указанными игроками.
     *
     * @return движок
     */
    public GameEngine build() {
        return new GameEngine(buildState(), new GameRandom(seed));
    }

    /**
     * Возвращает список игроков партии.
     *
     * @return игроки
     */
    public List<Player> players() {
        return List.copyOf(players);
    }
}
