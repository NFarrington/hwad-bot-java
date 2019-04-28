package xyz.nowiknowmy.hogwarts.domain;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "members")
public class Member extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String uid;
    private Integer guildId;
    private Boolean bot;
    private String username;
    private String nickname;
    private Date lastMessageAt;


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
        this.username = username;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Date getLastMessageAt() {
        return lastMessageAt;
    }

    public void setLastMessageAt(Date lastMessageAt) {
        this.lastMessageAt = lastMessageAt;
    }
}
