package xyz.nowiknowmy.hogwarts.domain;

import org.hibernate.envers.Audited;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Entity
@Table(name = "members")
public class Member extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String uid;
    private Integer guildId;
    private Boolean bot;
    @Audited
    private String username;
    @Audited
    private String nickname;
    private LocalDateTime lastMessageAt;
    private LocalDateTime deletedAt;

    @Transient
    private Map<Object, Object> originalAttributes = new HashMap<>();

    @PostLoad
    @PostPersist
    @PostUpdate
    @PostRemove
    public void clearDirtyAttributes() {
        originalAttributes.clear();
    }

    public Map<Object, Object> getOriginalAttributes() {
        return originalAttributes;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Integer getGuildId() {
        return guildId;
    }

    public void setGuildId(Integer guildId) {
        this.guildId = guildId;
    }

    public Boolean getBot() {
        return bot;
    }

    public void setBot(Boolean bot) {
        this.bot = bot;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        if (!Objects.equals(username, this.username)) {
            originalAttributes.put("username", this.username);
        }

        this.username = username;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        if (!Objects.equals(nickname, this.nickname)) {
            originalAttributes.put("nickname", this.nickname);
        }

        this.nickname = nickname;
    }

    public LocalDateTime getLastMessageAt() {
        return lastMessageAt;
    }

    public void setLastMessageAt(LocalDateTime lastMessageAt) {
        this.lastMessageAt = lastMessageAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }
}
