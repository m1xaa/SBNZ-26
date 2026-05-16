package com.cr.back.domain.player;

import com.cr.back.domain.card.CardEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "player_cards",
        uniqueConstraints = @UniqueConstraint(columnNames = {"player_id", "card_id"})
)
public class PlayerCardEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id")
    private PlayerEntity player;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "card_id")
    private CardEntity card;

    private boolean unlocked;
    private int level;

    protected PlayerCardEntity() {
    }

    public PlayerCardEntity(PlayerEntity player, CardEntity card, boolean unlocked, int level) {
        this.player = player;
        this.card = card;
        this.unlocked = unlocked;
        this.level = level;
    }

    public CardEntity getCard() {
        return card;
    }

    public boolean isUnlocked() {
        return unlocked;
    }

    public int getLevel() {
        return level;
    }

    public void update(boolean unlocked, int level) {
        this.unlocked = unlocked;
        this.level = level;
    }

    public void updateLevel(int level) {
        this.unlocked = true;
        this.level = level;
    }
}
