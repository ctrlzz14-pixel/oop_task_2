package com.ctrlzz14.durak.event;

/**
 * Причина завершения раунда.
 */
public enum RoundEndReason {

    /** Атакующий закончил атаку, все карты отбиты и уходят в отбой. */
    ATTACK_FINISHED,

    /** Защищающийся взял карты со стола. */
    CARDS_TAKEN
}
