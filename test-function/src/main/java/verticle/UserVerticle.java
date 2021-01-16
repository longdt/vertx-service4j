package verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import service.UserService_ProxyHandler;
import service.impl.UserServiceFake;

public class UserVerticle extends AbstractVerticle {
    public static final String ADDRESS = "users";

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        var userService = new UserServiceFake();
        var proxyHandler = new UserService_ProxyHandler(vertx, userService, true, UserService_ProxyHandler.DEFAULT_CONNECTION_TIMEOUT, true);
        vertx.eventBus().consumer(ADDRESS, proxyHandler);
        startPromise.complete();
    }
}
