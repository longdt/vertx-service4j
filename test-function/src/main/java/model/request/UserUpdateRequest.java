package model.request;

import io.vertx.core.shareddata.Shareable;

import java.time.LocalDate;
import java.util.Objects;

public class UserUpdateRequest implements Shareable {
    long id;
    String password;
    String email;
    LocalDate dob;

    UserUpdateRequest() {
    }

    public UserUpdateRequest(long id, String password, String email, LocalDate dob) {
        this.id = id;
        this.password = password;
        this.email = email;
        this.dob = dob;
    }

    public long getId() {
        return id;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public LocalDate getDob() {
        return dob;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserUpdateRequest that = (UserUpdateRequest) o;
        return id == that.id && Objects.equals(password, that.password) && Objects.equals(email, that.email) && Objects.equals(dob, that.dob);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, password, email, dob);
    }
}
