package xyz.nowiknowmy.hogwarts.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.Nullable;
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

    @Query("select m from Member m where m.uid = :uid and m.guildId = :guildId and m.deletedAt is null")
    @Nullable
    Member findByUidAndGuildId(String uid, Integer guildId);

    @Query("select m from Member m where m.uid = :uid and m.guildId = :guildId")
    @Nullable
    Member findByUidAndGuildIdWithTrashed(String uid, Integer guildId);

    @Query("select m from Member m where m.guildId = :guildId and (m.lastMessageAt <= :inactiveSince or m.lastMessageAt is null) and m.bot = false and m.deletedAt is null order by m.lastMessageAt desc")
    List<Member> findByGuildIdAndLastMessageBefore(Integer guildId, LocalDateTime inactiveSince);

}
