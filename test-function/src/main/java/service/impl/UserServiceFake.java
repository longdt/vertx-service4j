package service.impl;

import com.github.longdt.vertxorm.repository.Page;
import com.github.longdt.vertxorm.repository.PageRequest;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import model.entity.User;
import model.request.UserCreateRequest;
import model.request.UserUpdateRequest;
import service.UserService;

import java.util.List;
import java.util.stream.Collectors;

/***
 * no thread-safe. Need run inside verticle
 */
public class UserServiceFake implements UserService {
    private long counter;

    @Override
    public Future<Long> countUsers() {
        return null;
    }

    @Override
    public Future<List<Long>> getUserIds() {
        return null;
    }

    @Override
    public Future<List<Long>> aMethod(List<String> buffer) {
        var res = buffer.stream().map(s -> (long) s.hashCode()).collect(Collectors.toList());
        return Future.succeededFuture(res);
    }

    @Override
    public Future<User> createUser(UserCreateRequest createRequest) {
        var user = new User()
                .setId(++counter)
                .setUsername(createRequest.getUsername())
                .setPassword(createRequest.getPassword())
                .setEmail(createRequest.getEmail())
                .setDob(createRequest.getDob());
        return Future.succeededFuture(user);
    }

    @Override
    public Future<User> updateUser(UserUpdateRequest updateRequest) {
        var user = new User()
                .setId(updateRequest.getId())
                .setPassword(updateRequest.getPassword())
                .setEmail(updateRequest.getEmail())
                .setDob(updateRequest.getDob());
        return Future.succeededFuture(user);
    }

    @Override
    public Future<User> getUser(long id) {
        var user1 = new User()
                .setId(id)
                .setUsername("username1")
                .setPassword("password1");
        return Future.succeededFuture(user1);
    }

    @Override
    public Future<List<User>> getUserList() {
        var user1 = new User()
                .setId(counter)
                .setUsername("username1")
                .setPassword("password1");
        var user2 = new User()
                .setId(++counter)
                .setUsername("username2")
                .setPassword("password2");
        return Future.succeededFuture(List.of(user1, user2));
    }

    @Override
    public Future<Page<User>> getUsers(JsonObject filter, PageRequest pageRequest) {
        return getUserList().map(users -> new Page<>(pageRequest, users.size(), users));
    }
}
