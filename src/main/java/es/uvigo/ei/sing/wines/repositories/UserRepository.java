package es.uvigo.ei.sing.wines.repositories;

import es.uvigo.ei.sing.wines.entities.UserEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<UserEntity, Integer> {
    Optional<UserEntity> findByUserName(String userName);
}
