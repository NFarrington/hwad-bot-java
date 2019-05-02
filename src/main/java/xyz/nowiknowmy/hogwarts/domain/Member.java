package xyz.nowiknowmy.hogwarts.domain;

import org.hibernate.envers.Audited;
import org.springframework.beans.factory.annotation.Autowired;
import xyz.nowiknowmy.hogwarts.events.MemberPreSaveEvent;
import xyz.nowiknowmy.hogwarts.events.MemberPreSavePublisher;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "members")
public class Member extends Auditable implements Cloneable {
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
    private List<String> dirty = new ArrayList<>();
    @Transient
    private Member original;

    @PostLoad
    public void setOriginal() throws CloneNotSupportedException {
        clean();
        this.original = (Member) this.clone();
    }

    public Member getOriginal() {
        return this.original;
    }

    @PostPersist
    @PostUpdate
    public void clean() {
        this.dirty.clear();
    }

    public List<String> getDirty() {
        return dirty;
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
            this.dirty.add("username");
        }

        this.username = username;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        if (!Objects.equals(nickname, this.nickname)) {
            this.dirty.add("nickname");
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

    public Object clone() throws CloneNotSupportedException {
        Member member = (Member) super.clone();
        member.id = id;
        member.uid = uid;
        member.guildId = guildId;
        member.bot = bot;
        member.username = username;
        member.nickname = nickname;
        member.lastMessageAt = lastMessageAt;
        member.deletedAt = deletedAt;

        return member;
    }
}
