package service;

import com.github.longdt.vertxservice.codecs.ShareableMessageCodec;
import com.github.longdt.vertxservice.util.Arguments;
import com.github.longdt.vertxservice.util.ShareableList;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.serviceproxy.HelperUtils;
import io.vertx.serviceproxy.ServiceException;
import io.vertx.serviceproxy.ServiceExceptionMessageCodec;

public class UserService_ProxyHandler implements Handler<Message<Object>> {

    public static final long DEFAULT_CONNECTION_TIMEOUT = 5 * 60; // 5 minutes
    private final Vertx vertx;
    private final UserService service;
    private final long timerID;
    private long lastAccessed;
    private final long timeoutSeconds;
    private final boolean includeDebugInfo;

    public UserService_ProxyHandler(Vertx vertx, UserService service) {
        this(vertx, service, DEFAULT_CONNECTION_TIMEOUT);
    }

    public UserService_ProxyHandler(Vertx vertx, UserService service, long timeoutInSecond) {
        this(vertx, service, true, timeoutInSecond);
    }

    public UserService_ProxyHandler(Vertx vertx, UserService service, boolean topLevel, long timeoutInSecond) {
        this(vertx, service, true, timeoutInSecond, false);
    }

    public UserService_ProxyHandler(Vertx vertx, UserService service, boolean topLevel, long timeoutSeconds, boolean includeDebugInfo) {
        this.vertx = vertx;
        this.service = service;
        this.includeDebugInfo = includeDebugInfo;
        this.timeoutSeconds = timeoutSeconds;
        try {
            this.vertx.eventBus().registerDefaultCodec(ServiceException.class,
                    new ServiceExceptionMessageCodec());
        } catch (IllegalStateException ex) {
        }
        if (timeoutSeconds != -1 && !topLevel) {
            long period = timeoutSeconds * 1000 / 2;
            if (period > 10000) {
                period = 10000;
            }
            this.timerID = vertx.setPeriodic(period, this::checkTimedOut);
        } else {
            this.timerID = -1;
        }
        accessed();
    }


    private void checkTimedOut(long id) {
        long now = System.nanoTime();
        if (now - lastAccessed > timeoutSeconds * 1000000000) {
            close();
        }
    }

    //    @Override
    public void close() {
        if (timerID != -1) {
            vertx.cancelTimer(timerID);
        }
//        super.close();
    }

    private void accessed() {
        this.lastAccessed = System.nanoTime();
    }

    public void handle(Message<Object> msg) {
        try {
            String action = msg.headers().get("action");
            if (action == null) throw new IllegalStateException("action not specified");
            accessed();
            switch (action) {
                case "createUser": {
                    var arguments = (Arguments) msg.body();
                    service.createUser(arguments.getObject(0))
                            .onComplete(res -> {
                                if (res.failed()) {
                                    HelperUtils.manageFailure(msg, res.cause(), includeDebugInfo);
                                } else {
                                    msg.reply(res.result(), new DeliveryOptions().setCodecName(ShareableMessageCodec.CODEC_NAME));
                                }
                            });
                    break;
                }
                case "updateUser": {
                    var arguments = (Arguments) msg.body();
                    service.updateUser(arguments.getObject(0))
                            .onComplete(res -> {
                                if (res.failed()) {
                                    HelperUtils.manageFailure(msg, res.cause(), includeDebugInfo);
                                } else {
                                    msg.reply(res.result(), new DeliveryOptions().setCodecName(ShareableMessageCodec.CODEC_NAME));
                                }
                            });
                    break;
                }
                case "getUser": {
                    long id = (long) msg.body();
                    service.getUser(id)
                            .onComplete(res -> {
                                if (res.failed()) {
                                    HelperUtils.manageFailure(msg, res.cause(), includeDebugInfo);
                                } else {
                                    msg.reply(res.result(), new DeliveryOptions().setCodecName(ShareableMessageCodec.CODEC_NAME));
                                }
                            });
                    break;
                }
                case "getUsers": {
                    service.getUsers()
                            .onComplete(res -> {
                                if (res.failed()) {
                                    HelperUtils.manageFailure(msg, res.cause(), includeDebugInfo);
                                } else {
                                    msg.reply(ShareableList.of(res.result()), new DeliveryOptions().setCodecName(ShareableMessageCodec.CODEC_NAME));
                                }
                            });
                    break;
                }
                case "getUsers1": {
                    var arguments = (Arguments) msg.body();
                    service.getUsers(arguments.getObject(0), arguments.getObject(1))
                            .onComplete(res -> {
                                if (res.failed()) {
                                    HelperUtils.manageFailure(msg, res.cause(), includeDebugInfo);
                                } else {
                                    msg.reply(res.result(), new DeliveryOptions().setCodecName(ShareableMessageCodec.CODEC_NAME));
                                }
                            });
                    break;
                }
                default:
                    throw new IllegalStateException("Invalid action: " + action);
            }
        } catch (Throwable t) {
            if (includeDebugInfo)
                msg.reply(new ServiceException(500, t.getMessage(), HelperUtils.generateDebugInfo(t)));
            else msg.reply(new ServiceException(500, t.getMessage()));
            throw t;
        }
    }
}
