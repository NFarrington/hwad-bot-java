package xyz.nowiknowmy.hogwarts.repositories;

import org.springframework.data.repository.CrudRepository;
import xyz.nowiknowmy.hogwarts.domain.Guild;

public interface GuildRepository extends CrudRepository<Guild, Integer> {
    Guild findByGuildId(String guildId);
}
