package io.jwixel.esplugins.auth.store;

import java.lang.Exception;

public interface IAuthStore {
    public Boolean authentic(String auth) throws Exception;
}
