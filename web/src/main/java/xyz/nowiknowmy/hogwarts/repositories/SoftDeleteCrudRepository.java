package xyz.nowiknowmy.hogwarts.repositories;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@NoRepositoryBean
public interface SoftDeleteCrudRepository<T, ID> extends CrudRepository<T, ID> {

    @Override
    @Query("select e from #{#entityName} e where e.deletedAt is null")
    List<T> findAll();

    @Query("update #{#entityName} e set e.deletedAt = current_timestamp where e.id = :id")
    @Modifying
    @Transactional
    void softDelete(Integer id);

}
