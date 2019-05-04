package xyz.nowiknowmy.hogwarts.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import xyz.nowiknowmy.hogwarts.domain.Member;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MemberRepository extends SoftDeleteCrudRepository<Member, String> {
    @Query("select m from Member m where m.guildId = :guildId and m.deletedAt is null")
    List<Member> findByGuildId(Integer guildId);

    @Query("select m from Member m where m.uid = :uid and m.deletedAt is null")
    List<Member> findByUid(String uid);

    @Query("select m from Member m where m.uid = :uid AND m.guildId = :guildId and m.deletedAt is null")
    Member findByUidAndGuildId(String uid, Integer guildId);

    @Query("select m from Member m where m.uid = :uid AND m.guildId = :guildId")
    Member findByUidAndGuildIdWithTrashed(String uid, Integer guildId);

    @Query("SELECT m FROM Member m WHERE m.guildId = :guildId AND (m.lastMessageAt <= :inactiveSince OR m.lastMessageAt IS NULL) AND m.bot = false ORDER BY m.lastMessageAt DESC")
    List<Member> findInactiveMembers(Integer guildId, LocalDateTime inactiveSince);
}
