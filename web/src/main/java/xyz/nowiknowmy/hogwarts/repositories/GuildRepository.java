package xyz.nowiknowmy.hogwarts.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import xyz.nowiknowmy.hogwarts.domain.Guild;

import java.util.List;

@Repository
public interface GuildRepository extends SoftDeleteCrudRepository<Guild, String> {
    @Query("select g from Guild g where g.guildId = :guildId and g.deletedAt is null")
    Guild findByGuildId(String guildId);

    @Query("select g from Guild g where g.guildId in :guildIds")
    List<Guild> findByGuildIdIn(List<String> guildIds);

    @Query("select g from Guild g where g.guildId = :guildId")
    Guild findByGuildIdWithTrashed(String guildId);
}
