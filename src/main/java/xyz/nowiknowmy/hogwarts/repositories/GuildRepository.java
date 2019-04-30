package xyz.nowiknowmy.hogwarts.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import xyz.nowiknowmy.hogwarts.domain.Guild;

@Repository
public interface GuildRepository extends SoftDeleteCrudRepository<Guild, String> {
    @Query("select g from Guild g where g.guildId = :guildId and g.deletedAt is null")
    Guild findByGuildId(String guildId);

    @Query("select g from Guild g where g.guildId = :guildId")
    Guild findByGuildIdWithTrashed(String guildId);
}
