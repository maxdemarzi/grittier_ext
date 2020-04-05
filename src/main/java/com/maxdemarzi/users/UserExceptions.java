package com.maxdemarzi.users;

import com.maxdemarzi.Exceptions;

public class UserExceptions extends Exceptions {

    public static final Exceptions missingUsernameParameter() {
        return new Exceptions(400, "Missing username Parameter.");
    }
    public static final Exceptions emptyUsernameParameter() {
        return new Exceptions(400, "Empty username Parameter.");
    }
    public static final Exceptions invalidUsernameParameter() {
        return new Exceptions(400, "Invalid username Parameter.");
    }
    public static final Exceptions existingUsernameParameter() {
        return new Exceptions(400, "Existing username Parameter.");
    }

    public static final Exceptions missingEmailParameter() {
        return new Exceptions(400, "Missing email Parameter.");
    }
    public static final Exceptions emptyEmailParameter() {
        return new Exceptions(400, "Empty email Parameter.");
    }
    public static final Exceptions invalidEmailParameter() {
        return new Exceptions(400, "Invalid email Parameter.");
    }
    public static final Exceptions existingEmailParameter() {
        return new Exceptions(400, "Existing email Parameter.");
    }

    public static final Exceptions missingNameParameter() {
        return new Exceptions(400, "Missing name Parameter.");
    }
    public static final Exceptions emptyNameParameter() {
        return new Exceptions(400, "Empty name Parameter.");
    }

    public static final Exceptions missingPasswordParameter() {
        return new Exceptions(400, "Missing password Parameter.");
    }
    public static final Exceptions emptyPasswordParameter() {
        return new Exceptions(400, "Empty password Parameter.");
    }

    public static final Exceptions userNotFound() {
        return new Exceptions(400, "User not Found.");
    }
    public static final Exceptions userBlocked() {
        return new Exceptions(400, "Cannot follow blocked User.");
    }
    public static final Exceptions userSame() {
        return new Exceptions(400, "Cannot follow self.");
    }

    private UserExceptions(int code, String error) {
        super(code, error);
    }
}