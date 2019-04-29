package xyz.nowiknowmy.hogwarts.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import xyz.nowiknowmy.hogwarts.domain.Member;

import java.util.List;

@Repository
public interface MemberRepository extends CrudRepository<Member, Integer> {
    List<Member> findByIdIn(Integer[] ids);
    List<Member> findByGuildId(Integer guildId);
    List<Member> findByUid(String uid);
    Member findByUidAndGuildId(String uid, Integer guildId);
}
