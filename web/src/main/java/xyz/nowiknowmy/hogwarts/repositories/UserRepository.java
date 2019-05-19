package xyz.nowiknowmy.hogwarts.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;
import xyz.nowiknowmy.hogwarts.domain.User;

@Repository
public interface UserRepository extends CrudRepository<User, Integer> {

    @Nullable
    User findByUid(String uid);

}
