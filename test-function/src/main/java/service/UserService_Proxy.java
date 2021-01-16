package service;

import com.github.longdt.vertxorm.repository.Page;
import com.github.longdt.vertxorm.repository.PageRequest;
import com.github.longdt.vertxservice.codecs.ArgumentsMessageCodec;
import com.github.longdt.vertxservice.util.Arguments;
import com.github.longdt.vertxservice.util.ShareableList;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceException;
import io.vertx.serviceproxy.ServiceExceptionMessageCodec;
import model.entity.User;
import model.request.UserCreateRequest;
import model.request.UserUpdateRequest;

import java.util.List;

public class UserService_Proxy implements UserService {
    private Vertx vertx;
    private String address;

    public UserService_Proxy(Vertx vertx, String address) {
        this.vertx = vertx;
        this.address = address;
        try {
            this.vertx.eventBus().registerDefaultCodec(ServiceException.class,
                    new ServiceExceptionMessageCodec());
        } catch (IllegalStateException ex) {
        }
    }

    @Override
    public Future<Long> countUsers() {
        return null;
    }

    @Override
    public Future<User> createUser(UserCreateRequest createRequest) {
        DeliveryOptions _deliveryOptions = new DeliveryOptions();
        _deliveryOptions.addHeader("action", "createUser");
        _deliveryOptions.setCodecName(ArgumentsMessageCodec.CODEC_NAME);

        return vertx.eventBus().<User>request(address, Arguments.of(createRequest), _deliveryOptions)
                .map(Message::body);
    }

    @Override
    public Future<User> updateUser(UserUpdateRequest updateRequest) {
        DeliveryOptions _deliveryOptions = new DeliveryOptions();
        _deliveryOptions.addHeader("action", "updateUser");
        _deliveryOptions.setCodecName(ArgumentsMessageCodec.CODEC_NAME);

        return vertx.eventBus().<User>request(address, Arguments.of(updateRequest), _deliveryOptions)
                .map(Message::body);
    }

    @Override
    public Future<User> getUser(long id) {
        DeliveryOptions _deliveryOptions = new DeliveryOptions();
        _deliveryOptions.addHeader("action", "getUser");

        return vertx.eventBus().<User>request(address, id, _deliveryOptions)
                .map(Message::body);
    }

    @Override
    public Future<List<User>> getUsers() {
        DeliveryOptions _deliveryOptions = new DeliveryOptions();
        _deliveryOptions.addHeader("action", "getUsers");

        return vertx.eventBus().<ShareableList>request(address, null, _deliveryOptions)
                .map(msg -> {
                    var body = msg.body();
                    if (body == null) {
                        return null;
                    }
                    return body.getObject();
                });
    }

    @Override
    public Future<Page<User>> getUsers(JsonObject filter, PageRequest pageRequest) {
        DeliveryOptions _deliveryOptions = new DeliveryOptions();
        _deliveryOptions.addHeader("action", "getUsers1");
        _deliveryOptions.setCodecName(ArgumentsMessageCodec.CODEC_NAME);

        return vertx.eventBus().<Page<User>>request(address, Arguments.of(filter, pageRequest), _deliveryOptions)
                .map(Message::body);
    }
}
