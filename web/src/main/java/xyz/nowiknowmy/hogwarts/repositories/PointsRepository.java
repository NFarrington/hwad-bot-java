package xyz.nowiknowmy.hogwarts.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;
import xyz.nowiknowmy.hogwarts.domain.Points;

import java.util.List;

@Repository
public interface PointsRepository extends CrudRepository<Points, Integer> {

    List<Points> findByGuildId(Integer guildId);

    @Nullable
    Points findByGuildIdAndHouse(Integer guildId, String house);

}
