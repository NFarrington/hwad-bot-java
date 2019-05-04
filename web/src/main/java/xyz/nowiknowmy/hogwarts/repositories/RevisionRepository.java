package xyz.nowiknowmy.hogwarts.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import xyz.nowiknowmy.hogwarts.domain.Revision;

@Repository
public interface RevisionRepository extends CrudRepository<Revision, Integer> {
}
