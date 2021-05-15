package service;

import com.github.longdt.vertxorm.repository.Page;
import com.github.longdt.vertxorm.repository.PageRequest;
import com.github.longdt.vertxservice.annotation.Service;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import model.entity.User;
import model.request.UserCreateRequest;
import model.request.UserUpdateRequest;
import repository.UserRepository;
import service.impl.UserServiceImpl;

import java.util.List;

@Service
public interface UserService {
    static UserService create(UserRepository userRepository) {
        return new UserServiceImpl(userRepository);
    }

    Future<Long> countUsers();

    Future<List<Long>> getUserIds();

    Future<List<Long>> aMethod(List<String> buffer);

    Future<User> createUser(UserCreateRequest createRequest);

    Future<User> updateUser(UserUpdateRequest updateRequest);

    Future<User> getUser(long id);

    Future<List<User>> getUserList();

    Future<Page<User>> getUsers(JsonObject filter, PageRequest pageRequest);
}
