package xyz.nowiknowmy.hogwarts.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import xyz.nowiknowmy.hogwarts.domain.Guild;

@Repository
public interface GuildRepository extends CrudRepository<Guild, Integer> {
    Guild findByGuildId(String guildId);
}
