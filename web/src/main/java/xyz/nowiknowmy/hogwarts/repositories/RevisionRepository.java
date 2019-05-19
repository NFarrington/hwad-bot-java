package xyz.nowiknowmy.hogwarts.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import xyz.nowiknowmy.hogwarts.domain.Revision;

import java.util.List;

@Repository
public interface RevisionRepository extends CrudRepository<Revision, Integer> {

    @Query("select r from Revision r join Member m on r.modelId = m.id where r.modelType = :modelType " +
        "and r.key in ('username', 'nickname') and m.guildId in :guildIds order by r.createdAt desc")
    List<Revision> findByGuildIdWhereKeyIsUsernameOrNickname(String modelType, List<Integer> guildIds);

}
