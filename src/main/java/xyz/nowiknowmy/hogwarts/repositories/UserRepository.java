package xyz.nowiknowmy.hogwarts.repositories;

import org.springframework.data.repository.CrudRepository;
import xyz.nowiknowmy.hogwarts.domain.User;

public interface UserRepository extends CrudRepository<User, Integer> {
    User findByUsername(String name);
}
