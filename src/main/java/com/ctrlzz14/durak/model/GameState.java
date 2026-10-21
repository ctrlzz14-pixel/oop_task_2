package com.ctrlzz14.durak.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Состояние партии.
 *
 * <p>Класс объединяет всё, что нужно для описания текущего момента игры:
 * игроков и их руки, колоду, стол, отбой, козыря, текущие роли (кто атакует и
 * кто защищается), фазу и счётчики раунда.</p>
 *
 * <p>Состояние изменяет только {@code GameEngine}: методы-мутаторы публичны,
 * чтобы движок находился в другом пакете, но по смыслу принадлежат движку.
 * Читающие методы возвращают неизменяемые или копии коллекций, поэтому
 * внешний код не может случайно испортить состояние.</p>
 */
public final class GameState {

    private final List<Player> players;
    private final Deck deck;
    private final Table table;
    private final DiscardPile discardPile;
    private final GameRules rules;
    private final long seed;

    private Suit trumpSuit;
    private Card trumpCard;
    private GamePhase phase;
    private Player attacker;
    private Player defender;
    private int cardsThrownInSegment;
    private int segmentLimit;
    private int roundNumber;
    private int roundsWithoutDiscard;
    private int roundsWithoutProgress;
    private GameResult result;
    private Card transferBaseCard;

    private GameState(Builder builder) {
        this.players = List.copyOf(builder.players);
        this.deck = builder.deck;
        this.table = builder.table;
        this.discardPile = builder.discardPile;
        this.rules = builder.rules;
        this.seed = builder.seed;
        this.trumpSuit = builder.trumpSuit;
        this.trumpCard = builder.trumpCard;
        this.phase = builder.phase;
        this.attacker = builder.attacker;
        this.defender = builder.defender;
        this.cardsThrownInSegment = builder.cardsThrownInSegment;
        this.segmentLimit = builder.segmentLimit;
        this.roundNumber = builder.roundNumber;
        this.roundsWithoutDiscard = builder.roundsWithoutDiscard;
        this.roundsWithoutProgress = builder.roundsWithoutProgress;
        this.result = builder.result;
        this.transferBaseCard = builder.transferBaseCard;
    }

    /**
     * Создаёт построитель состояния.
     *
     * @return построитель состояния
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Возвращает список игроков в порядке хода.
     *
     * @return неизменяемый список игроков
     */
    public List<Player> players() {
        return players;
    }

    /**
     * Возвращает игрока по его номеру.
     *
     * @param index номер игрока
     * @return игрок
     * @throws IndexOutOfBoundsException если номера не существует
     */
    public Player playerAt(int index) {
        return players.get(index);
    }

    /**
     * Возвращает игрока по идентификатору.
     *
     * @param id идентификатор игрока
     * @return игрок
     * @throws IllegalArgumentException если игрок с таким идентификатором отсутствует
     */
    public Player playerById(int id) {
        return players.stream()
                .filter(player -> player.id() == id)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Игрок с id=" + id + " не найден"));
    }

    /**
     * Возвращает следующего за указанным игроком в направлении хода.
     *
     * <p>Направление хода задаётся порядком игроков в списке: после последнего
     * игрока ходит первый.</p>
     *
     * @param player игрок, после которого определяется следующий
     * @return следующий игрок по кругу
     * @throws IllegalArgumentException если игрок не участвует в партии
     */
    public Player nextPlayer(Player player) {
        int index = players.indexOf(player);
        if (index < 0) {
            throw new IllegalArgumentException("Игрок не участвует в партии: " + player);
        }
        return players.get((index + 1) % players.size());
    }

    /**
     * Возвращает колоду.
     *
     * @return колода
     */
    public Deck deck() {
        return deck;
    }

    /**
     * Возвращает стол текущего раунда.
     *
     * @return стол
     */
    public Table table() {
        return table;
    }

    /**
     * Возвращает отбой.
     *
     * @return отбой
     */
    public DiscardPile discardPile() {
        return discardPile;
    }

    /**
     * Возвращает настройки партии.
     *
     * @return правила
     */
    public GameRules rules() {
        return rules;
    }

    /**
     * Возвращает зерно генератора случайных величин, с которым начата партия.
     *
     * @return зерно партии
     */
    public long seed() {
        return seed;
    }

    /**
     * Возвращает козырную масть.
     *
     * @return козырная масть или {@code null}, если козырь ещё не определён
     */
    public Suit trumpSuit() {
        return trumpSuit;
    }

    /**
     * Возвращает нижнюю (козырную) карту колоды.
     *
     * @return козырная карта или {@link Optional#empty()}, если колода пуста
     */
    public Optional<Card> trumpCard() {
        return Optional.ofNullable(trumpCard);
    }

    /**
     * Возвращает текущую фазу партии.
     *
     * @return фаза партии
     */
    public GamePhase phase() {
        return phase;
    }

    /**
     * Возвращает атакующего игрока.
     *
     * @return атакующий
     */
    public Player attacker() {
        return attacker;
    }

    /**
     * Возвращает защищающегося игрока.
     *
     * @return защищающийся
     */
    public Player defender() {
        return defender;
    }

    /**
     * Возвращает количество карт, выложенных атакующим в текущем отрезке
     * (с момента последней смены ролей).
     *
     * @return число выложенных карт
     */
    public int cardsThrownInSegment() {
        return cardsThrownInSegment;
    }

    /**
     * Возвращает ограничение на число карт в атаке: столько карт было у
     * защищающегося в начале текущего отрезка.
     *
     * @return максимальное число карт в атаке
     */
    public int segmentLimit() {
        return segmentLimit;
    }

    /**
     * Возвращает номер текущего раунда.
     *
     * @return номер раунда, начиная с единицы
     */
    public int roundNumber() {
        return roundNumber;
    }

    /**
     * Возвращает результат партии.
     *
     * @return результат или {@link Optional#empty()}, если партия не завершена
     */
    public Optional<GameResult> result() {
        return Optional.ofNullable(result);
    }

    /**
     * Проверяет, завершена ли партия.
     *
     * @return {@code true}, если партия завершена
     */
    public boolean isGameOver() {
        return phase == GamePhase.GAME_OVER;
    }

    /**
     * Проверяет, доступен ли перевод атаки.
     *
     * <p>Перевод доступен только сразу после того, как защищающийся отбился
     * картой: до этого момента переводить нечего. Как только атакующий
     * выкладывает новую карту, возможность перевода аннулируется.</p>
     *
     * @return {@code true}, если защищающийся может перевести атаку
     */
    public boolean isTransferAvailable() {
        return transferBaseCard != null;
    }

    /**
     * Возвращает карту, от которой защищались: её достоинство обязательно для
     * карты перевода.
     *
     * @return отбитая карта или {@link Optional#empty()}, если перевод недоступен
     */
    public Optional<Card> transferBaseCard() {
        return Optional.ofNullable(transferBaseCard);
    }

    /**
     * Задаёт козыря.
     *
     * @param trumpSuit козырная масть
     * @param trumpCard нижняя карта колоды
     */
    public void setTrump(Suit trumpSuit, Card trumpCard) {
        this.trumpSuit = trumpSuit;
        this.trumpCard = trumpCard;
    }

    /**
     * Задаёт текущую фазу партии.
     *
     * @param phase новая фаза
     */
    public void setPhase(GamePhase phase) {
        this.phase = phase;
    }

    /**
     * Задаёт атакующего и защищающегося.
     *
     * @param attacker атакующий
     * @param defender защищающийся
     */
    public void setRoles(Player attacker, Player defender) {
        this.attacker = attacker;
        this.defender = defender;
    }

    /**
     * Добавляет число выложенных карт в текущем отрезке.
     *
     * @param count количество добавленных карт
     */
    public void addCardsThrown(int count) {
        this.cardsThrownInSegment += count;
    }

    /**
     * Сбрасывает счётчик выложенных карт и задаёт новое ограничение атаки.
     *
     * @param limit новое ограничение на число карт в атаке
     */
    public void resetSegment(int limit) {
        this.cardsThrownInSegment = 0;
        this.segmentLimit = limit;
    }

    /**
     * Увеличивает номер раунда.
     */
    public void incrementRound() {
        this.roundNumber++;
    }

    /**
     * Возвращает количество последних раундов подряд, в которых не было
     * продвижения.
     *
     * @return число раундов без продвижения
     */
    public int roundsWithoutProgress() {
        return roundsWithoutProgress;
    }

    /**
     * Сбрасывает счётчик раундов без продвижения.
     */
    public void resetRoundsWithoutProgress() {
        this.roundsWithoutProgress = 0;
    }

    /**
     * Увеличивает счётчик раундов без продвижения.
     */
    public void incrementRoundsWithoutProgress() {
        this.roundsWithoutProgress++;
    }

    /**
     * Возвращает количество последних раундов подряд, в которых ни одна
     * карта не ушла в отбой.
     *
     * @return число раундов без отбоя
     */
    public int roundsWithoutDiscard() {
        return roundsWithoutDiscard;
    }

    /**
     * Сбрасывает счётчик раундов без отбоя.
     */
    public void resetRoundsWithoutDiscard() {
        this.roundsWithoutDiscard = 0;
    }

    /**
     * Увеличивает счётчик раундов без отбоя.
     */
    public void incrementRoundsWithoutDiscard() {
        this.roundsWithoutDiscard++;
    }

    /**
     * Открывает возможность перевода атаки.
     *
     * @param baseCard карта, от которой защищались
     */
    public void offerTransfer(Card baseCard) {
        this.transferBaseCard = baseCard;
    }

    /**
     * Закрывает возможность перевода атаки.
     */
    public void clearTransferOffer() {
        this.transferBaseCard = null;
    }

    /**
     * Сохраняет результат завершённой партии.
     *
     * @param result результат партии
     */
    public void setResult(GameResult result) {
        this.result = Objects.requireNonNull(result, "Результат не должен быть null");
    }

    /**
     * Построитель состояния партии.
     *
     * <p>Используется как в основном коде, так и в тестах, где состояние
     * собирается вручную из фиксированных наборов карт.</p>
     */
    public static final class Builder {

        private List<Player> players = List.of();
        private Deck deck = Deck.standard();
        private Table table = new Table();
        private DiscardPile discardPile = new DiscardPile();
        private GameRules rules = GameRules.standard();
        private long seed;
        private Suit trumpSuit;
        private Card trumpCard;
        private GamePhase phase = GamePhase.SETUP;
        private Player attacker;
        private Player defender;
        private int cardsThrownInSegment;
        private int segmentLimit;
        private int roundNumber = 1;
        private int roundsWithoutDiscard;
        private int roundsWithoutProgress;
        private GameResult result;
        private Card transferBaseCard;

        private Builder() {
            // построитель создаётся только через GameState.builder()
        }

        /**
         * Задаёт игроков партии (порядок задаёт направление хода).
         *
         * @param players игроки
         * @return этот построитель
         */
        public Builder players(List<Player> players) {
            this.players = new ArrayList<>(players);
            return this;
        }

        /**
         * Задаёт колоду.
         *
         * @param deck колода
         * @return этот построитель
         */
        public Builder deck(Deck deck) {
            this.deck = deck;
            return this;
        }

        /**
         * Задаёт стол.
         *
         * @param table стол
         * @return этот построитель
         */
        public Builder table(Table table) {
            this.table = table;
            return this;
        }

        /**
         * Задаёт отбой.
         *
         * @param discardPile отбой
         * @return этот построитель
         */
        public Builder discardPile(DiscardPile discardPile) {
            this.discardPile = discardPile;
            return this;
        }

        /**
         * Задаёт настройки партии.
         *
         * @param rules правила
         * @return этот построитель
         */
        public Builder rules(GameRules rules) {
            this.rules = rules;
            return this;
        }

        /**
         * Задаёт зерно партии.
         *
         * @param seed зерно
         * @return этот построитель
         */
        public Builder seed(long seed) {
            this.seed = seed;
            return this;
        }

        /**
         * Задаёт козырную масть и козырную карту.
         *
         * @param trumpSuit козырная масть
         * @param trumpCard нижняя карта колоды
         * @return этот построитель
         */
        public Builder trump(Suit trumpSuit, Card trumpCard) {
            this.trumpSuit = trumpSuit;
            this.trumpCard = trumpCard;
            return this;
        }

        /**
         * Задаёт фазу партии.
         *
         * @param phase фаза
         * @return этот построитель
         */
        public Builder phase(GamePhase phase) {
            this.phase = phase;
            return this;
        }

        /**
         * Задаёт атакующего и защищающегося.
         *
         * @param attacker атакующий
         * @param defender защищающийся
         * @return этот построитель
         */
        public Builder roles(Player attacker, Player defender) {
            this.attacker = attacker;
            this.defender = defender;
            return this;
        }

        /**
         * Задаёт счётчик выложенных карт и ограничение атаки.
         *
         * @param cardsThrownInSegment число выложенных карт
         * @param segmentLimit ограничение на число карт в атаке
         * @return этот построитель
         */
        public Builder segment(int cardsThrownInSegment, int segmentLimit) {
            this.cardsThrownInSegment = cardsThrownInSegment;
            this.segmentLimit = segmentLimit;
            return this;
        }

        /**
         * Задаёт номер раунда.
         *
         * @param roundNumber номер раунда
         * @return этот построитель
         */
        public Builder roundNumber(int roundNumber) {
            this.roundNumber = roundNumber;
            return this;
        }

        /**
         * Задаёт счётчик раундов без продвижения.
         *
         * @param roundsWithoutProgress число раундов без продвижения
         * @return этот построитель
         */
        public Builder roundsWithoutProgress(int roundsWithoutProgress) {
            this.roundsWithoutProgress = roundsWithoutProgress;
            return this;
        }

        /**
         * Задаёт счётчик раундов без отбоя.
         *
         * @param roundsWithoutDiscard число раундов без отбоя
         * @return этот построитель
         */
        public Builder roundsWithoutDiscard(int roundsWithoutDiscard) {
            this.roundsWithoutDiscard = roundsWithoutDiscard;
            return this;
        }

        /**
         * Задаёт результат партии.
         *
         * @param result результат
         * @return этот построитель
         */
        public Builder result(GameResult result) {
            this.result = result;
            return this;
        }

        /**
         * Задаёт карту для перевода атаки.
         *
         * @param transferBaseCard отбитая карта
         * @return этот построитель
         */
        public Builder transferBaseCard(Card transferBaseCard) {
            this.transferBaseCard = transferBaseCard;
            return this;
        }

        /**
         * Собирает состояние партии.
         *
         * @return готовое состояние
         * @throws IllegalStateException если не заданы игроки или роли
         */
        public GameState build() {
            if (players.isEmpty()) {
                throw new IllegalStateException("В партии должен быть хотя бы один игрок");
            }
            if (attacker == null || defender == null) {
                throw new IllegalStateException("В партии должны быть определены атакующий и защищающийся");
            }
            return new GameState(this);
        }
    }
}
