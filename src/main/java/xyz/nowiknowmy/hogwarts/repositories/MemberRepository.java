package xyz.nowiknowmy.hogwarts.repositories;

import org.hibernate.annotations.Parameter;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import xyz.nowiknowmy.hogwarts.domain.Member;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MemberRepository extends CrudRepository<Member, Integer> {
    List<Member> findByIdIn(Integer[] ids);
    List<Member> findByGuildId(Integer guildId);
    List<Member> findByUid(String uid);
    Member findByUidAndGuildId(String uid, Integer guildId);

    @Query("SELECT m FROM Member m WHERE guild_id = :guildId AND (last_message_at <= :inactiveSince OR last_message_at IS NULL) AND bot = false ORDER BY last_message_at DESC")
    List<Member> findInactiveMembers(@Param("guildId") Integer guildId, @Param("inactiveSince") LocalDateTime inactiveSince);
}
