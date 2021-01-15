package service.impl;

import com.github.longdt.vertxorm.repository.Page;
import com.github.longdt.vertxorm.repository.PageRequest;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import model.entity.User;
import model.request.UserCreateRequest;
import model.request.UserUpdateRequest;
import repository.UserRepository;
import service.UserService;

import java.util.List;
import java.util.Optional;


public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Future<Long> countUsers() {
        return null;
    }

    @Override
    public Future<User> createUser(UserCreateRequest createRequest) {
        var user = new User()
                .setUsername(createRequest.getUsername())
                .setPassword(createRequest.getPassword())
                .setEmail(createRequest.getEmail())
                .setDob(createRequest.getDob());
        return userRepository.insert(user);
    }

    @Override
    public Future<User> updateUser(UserUpdateRequest updateRequest) {
        return userRepository.getPool().withTransaction(conn ->
                userRepository.find(conn, updateRequest.getId())
                        .map(Optional::orElseThrow)
                        .flatMap(user -> {
                            user.setPassword(updateRequest.getPassword())
                                    .setEmail(updateRequest.getEmail())
                                    .setDob(updateRequest.getDob());
                            return userRepository.update(conn, user);
                        })
        );
    }

    @Override
    public Future<User> getUser(long id) {
        return userRepository.find(id).map(Optional::orElseThrow);
    }

    @Override
    public Future<List<User>> getUsers() {
        return userRepository.findAll();
    }

    @Override
    public Future<Page<User>> getUsers(JsonObject filter, PageRequest pageRequest) {
        return userRepository.findAll(pageRequest);
    }
}
