package app;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.serializers.FieldSerializer;
import com.github.longdt.vertxorm.repository.Page;
import com.github.longdt.vertxorm.repository.PageRequest;
import com.github.longdt.vertxorm.util.Futures;
import com.github.longdt.vertxservice.codecs.ArgumentsMessageCodec;
import com.github.longdt.vertxservice.codecs.Kryos;
import com.github.longdt.vertxservice.codecs.ShareableMessageCodec;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import model.request.UserCreateRequest;
import service.UserService;
import service.UserServiceProxy;
import verticle.UserVerticle;

import java.time.LocalDate;
import java.util.Collections;

public class DemoApplication {
    public static void main(String[] args) throws Exception {
        var vertx = Futures.join(Vertx.clusteredVertx(new VertxOptions()));
        registerCodec(vertx);
        var userService = new UserServiceProxy(vertx, UserVerticle.ADDRESS);
        vertx.deployVerticle(UserVerticle.class, new DeploymentOptions(), ar -> {
           if (ar.succeeded()) {
               sendRequest(userService);
           } else {
               ar.cause().printStackTrace();
           }
        });
    }

    private static void registerCodec(Vertx vertx) {
        Kryos.initialize(kryo -> {
            kryo.setRegistrationRequired(false);
            kryo.register(PageRequest.class, new FieldSerializer<>(kryo, PageRequest.class) {
                @Override
                protected Object create(Kryo kryo, Input input, Class type) {
                    return new PageRequest(0, 0);
                }
            });
            kryo.register(Page.class, new FieldSerializer<>(kryo, Page.class) {
                @Override
                protected Object create(Kryo kryo, Input input, Class type) {
                    return new Page<>(1, 1, 0, Collections.emptyList());
                }
            });
        });
        vertx.eventBus().registerCodec(new ArgumentsMessageCodec());
        vertx.eventBus().registerCodec(new ShareableMessageCodec());
    }

    private static void sendRequest(UserService userService) {
        var createReq = new UserCreateRequest("username", "password", "abc@gmail.com", LocalDate.now());
        userService.createUser(createReq)
                .onSuccess(System.out::println)
                .onComplete(ar -> getUsers(userService));
    }

    private static void getUsers(UserService userService) {
        userService.getUsers(new JsonObject(), new PageRequest(1, 2))
                .map(Page::getContent)
                .onSuccess(System.out::println)
                .onFailure(Throwable::printStackTrace);
    }
}
