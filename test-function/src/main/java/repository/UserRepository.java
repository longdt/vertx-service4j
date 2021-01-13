package repository;

import com.github.longdt.vertxorm.annotation.Repository;
import com.github.longdt.vertxorm.repository.CrudRepository;
import model.entity.User;

@Repository
public interface UserRepository extends CrudRepository<Long, User> {
}
