package xyz.nowiknowmy.hogwarts.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import xyz.nowiknowmy.hogwarts.domain.Member;

@Repository
public interface MemberRepository extends CrudRepository<Member, Integer> {
}
