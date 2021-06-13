## Vertx Service4J
A lightweight replacement for vertx-service-proxy. This library highly optimizes for vertx java.
### Features
* Support generic
* Service methods return `Future` to avoid callback hell
* Highly optimizes for java
* More supported data type
## Developers
### Maven dependency
```
<dependency>
    <groupId>com.github.longdt</groupId>
    <artifactId>vertx-service4j</artifactId>
    <version>1.1.0</version>
</dependency>
```
### Example
##### Define Pojo classes:
```
public class UserCreateRequest implements Shareable {
    String username;
    String password;
    String email;
    LocalDate dob;

    UserCreateRequest() {
    }
    
    @Override
    public UserCreateRequest copy() {    //if this is immutable class, no need to override this method
        var copied = new UserCreateRequest();
        ...
        return copied;
    }
    
    ...
    setter/getter methods
    ...
}

public class UserUpdateRequest implements Shareable {
    ...
}

public class User implements Shareable {
    ...
}
```
##### Define Service:
```
@Service
public interface UserService {
    static UserService create(UserRepository userRepository) {
        return new UserServiceImpl(userRepository);
    }
    
    static UserService createProxy(Vertx vertx, String address) {
        return new UserServiceProxy(vertx, address);
    }

    Future<Long> countUsers();

    Future<List<Long>> getUserIds();

    Future<List<Long>> aMethod(List<String> buffer);

    Future<Map<Integer, User>> getUserMapping(List<Integer> ids);

    Future<User> createUser(UserCreateRequest createRequest);

    Future<User> updateUser(UserUpdateRequest updateRequest);

    Future<User> getUser(long id);

    Future<List<User>> getUserList();

    Future<Page<User>> getUsers(JsonObject filter, int page, int size);
}

```
##### Compile project to generate implementation of Repository:
```
mvn clean compile
```
After compilation, `vertx-service4j` creates `UserServiceProxy`, `UserServiceProxyHandler`.
##### Exposing your service:
```
var userService = new UserServiceImpl();
var proxyHandler = new UserServiceProxyHandler(vertx, userService, true);
vertx.eventBus().consumer(ADDRESS, proxyHandler);
```
##### Optimize when run cluster mode:
this library uses `kryo` to serialize objects to `Buffer` when sending over network, so you can register pojo class to improve
de/serialization performance
```
Kryos.initialize(kryo -> {
    kryo.setRegistrationRequired(false);
    kryo.register(Page.class, new FieldSerializer<>(kryo, Page.class) {
        @Override
        protected Object create(Kryo kryo, Input input, Class type) {
            return new Page<>(1, 1, 0, Collections.emptyList());
        }
    });
    kryo.register(UserCreateRequest.class);
    kryo.register(UserUpdateRequest.class);
    kryo.register(User.class);
});
```
