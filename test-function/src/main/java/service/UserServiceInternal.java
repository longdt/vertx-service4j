package service;

import com.github.longdt.vertxservice.annotation.Service;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import model.entity.User;

import java.util.List;
import java.util.Map;

@Service
public interface UserServiceInternal extends UserService {
    Future<Buffer> download(int x, byte a, List<Buffer> buffers, Map<Character, User> c);
}
