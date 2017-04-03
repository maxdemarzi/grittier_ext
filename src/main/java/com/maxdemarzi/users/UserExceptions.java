package com.maxdemarzi.users;

import com.maxdemarzi.Exceptions;

public class UserExceptions extends Exceptions {

    public static final Exceptions missingUsernameParameter = new Exceptions(400, "Missing username Parameter.");
    public static final Exceptions emptyUsernameParameter = new Exceptions(400, "Empty username Parameter.");
    public static final Exceptions invalidUsernameParameter = new Exceptions(400, "Invalid username Parameter.");
    public static final Exceptions existingUsernameParameter = new Exceptions(400, "Existing username Parameter.");

    public static final Exceptions missingEmailParameter = new Exceptions(400, "Missing email Parameter.");
    public static final Exceptions emptyEmailParameter = new Exceptions(400, "Empty email Parameter.");
    public static final Exceptions invalidEmailParameter = new Exceptions(400, "Invalid email Parameter.");
    public static final Exceptions existingEmailParameter = new Exceptions(400, "Existing email Parameter.");

    public static final Exceptions missingNameParameter = new Exceptions(400, "Missing name Parameter.");
    public static final Exceptions emptyNameParameter = new Exceptions(400, "Empty name Parameter.");

    public static final Exceptions missingPasswordParameter = new Exceptions(400, "Missing password Parameter.");
    public static final Exceptions emptyPasswordParameter = new Exceptions(400, "Empty password Parameter.");

    public static final Exceptions userNotFound = new Exceptions(400, "User not Found.");
    public static final Exceptions userBlocked = new Exceptions(400, "Cannot follow blocked User.");

    private UserExceptions(int code, String error) {
        super(code, error);
    }
}