package com.maxdemarzi.users;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.neo4j.harness.junit.Neo4jRule;
import org.neo4j.test.server.HTTP;

import java.util.HashMap;

import static com.maxdemarzi.Properties.*;

public class CreateUserTest {
    @Rule
    public Neo4jRule neo4j = new Neo4jRule()
            .withExtension("/v1", Users.class);

    @Test
    public void shouldCreateUser() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/v1/users").toString(), input);
        HashMap actual  = response.content();
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void shouldNotCreateUserInvalid() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/v1/users").toString());
        HashMap actual  = response.content();
        Assert.assertEquals(400, response.status());
        Assert.assertEquals("Invalid Input", actual.get("error"));
        Assert.assertFalse(actual.containsKey(USERNAME));
        Assert.assertFalse(actual.containsKey(EMAIL));
        Assert.assertFalse(actual.containsKey(NAME));
        Assert.assertFalse(actual.containsKey(PASSWORD));
    }

    @Test
    public void shouldNotCreateUserMissingUsername() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/v1/users").toString(), missingUsernameInput);
        HashMap actual  = response.content();
        Assert.assertEquals(400, response.status());
        Assert.assertEquals("Missing username Parameter.", actual.get("error"));
        Assert.assertFalse(actual.containsKey(USERNAME));
        Assert.assertFalse(actual.containsKey(EMAIL));
        Assert.assertFalse(actual.containsKey(NAME));
        Assert.assertFalse(actual.containsKey(PASSWORD));
    }

    @Test
    public void shouldNotCreateUserEmptyUsername() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/v1/users").toString(), emptyUsernameInput);
        HashMap actual  = response.content();
        Assert.assertEquals(400, response.status());
        Assert.assertEquals("Empty username Parameter.", actual.get("error"));
        Assert.assertFalse(actual.containsKey(USERNAME));
        Assert.assertFalse(actual.containsKey(EMAIL));
        Assert.assertFalse(actual.containsKey(NAME));
        Assert.assertFalse(actual.containsKey(PASSWORD));
    }

    @Test
    public void shouldNotCreateUserInvalidUsername() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/v1/users").toString(), invalidUsernameInput);
        HashMap actual  = response.content();
        Assert.assertEquals(400, response.status());
        Assert.assertEquals("Invalid username Parameter.", actual.get("error"));
        Assert.assertFalse(actual.containsKey(USERNAME));
        Assert.assertFalse(actual.containsKey(EMAIL));
        Assert.assertFalse(actual.containsKey(NAME));
        Assert.assertFalse(actual.containsKey(PASSWORD));
    }

    @Test
    public void shouldNotCreateUserMissingEmail() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/v1/users").toString(), missingEmailInput);
        HashMap actual  = response.content();
        Assert.assertEquals(400, response.status());
        Assert.assertEquals("Missing email Parameter.", actual.get("error"));
        Assert.assertFalse(actual.containsKey(USERNAME));
        Assert.assertFalse(actual.containsKey(EMAIL));
        Assert.assertFalse(actual.containsKey(NAME));
        Assert.assertFalse(actual.containsKey(PASSWORD));
    }

    @Test
    public void shouldNotCreateUserEmptyEmail() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/v1/users").toString(), emptyEmailInput);
        HashMap actual  = response.content();
        Assert.assertEquals(400, response.status());
        Assert.assertEquals("Empty email Parameter.", actual.get("error"));
        Assert.assertFalse(actual.containsKey(USERNAME));
        Assert.assertFalse(actual.containsKey(EMAIL));
        Assert.assertFalse(actual.containsKey(NAME));
        Assert.assertFalse(actual.containsKey(PASSWORD));
    }

    @Test
    public void shouldNotCreateUserInvalidEmail() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/v1/users").toString(), invalidEmailInput);
        HashMap actual  = response.content();
        Assert.assertEquals(400, response.status());
        Assert.assertEquals("Invalid email Parameter.", actual.get("error"));
        Assert.assertFalse(actual.containsKey(USERNAME));
        Assert.assertFalse(actual.containsKey(EMAIL));
        Assert.assertFalse(actual.containsKey(NAME));
        Assert.assertFalse(actual.containsKey(PASSWORD));
    }

    @Test
    public void shouldNotCreateUserMissingName() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/v1/users").toString(), missingNameInput);
        HashMap actual  = response.content();
        Assert.assertEquals(400, response.status());
        Assert.assertEquals("Missing name Parameter.", actual.get("error"));
        Assert.assertFalse(actual.containsKey(USERNAME));
        Assert.assertFalse(actual.containsKey(EMAIL));
        Assert.assertFalse(actual.containsKey(NAME));
        Assert.assertFalse(actual.containsKey(PASSWORD));
    }

    @Test
    public void shouldNotCreateUserEmptyName() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/v1/users").toString(), emptyNameInput);
        HashMap actual  = response.content();
        Assert.assertEquals(400, response.status());
        Assert.assertEquals("Empty name Parameter.", actual.get("error"));
        Assert.assertFalse(actual.containsKey(USERNAME));
        Assert.assertFalse(actual.containsKey(EMAIL));
        Assert.assertFalse(actual.containsKey(NAME));
        Assert.assertFalse(actual.containsKey(PASSWORD));
    }

    @Test
    public void shouldNotCreateUserMissingPassword() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/v1/users").toString(), missingPasswordInput);
        HashMap actual  = response.content();
        Assert.assertEquals(400, response.status());
        Assert.assertEquals("Missing password Parameter.", actual.get("error"));
        Assert.assertFalse(actual.containsKey(USERNAME));
        Assert.assertFalse(actual.containsKey(EMAIL));
        Assert.assertFalse(actual.containsKey(NAME));
        Assert.assertFalse(actual.containsKey(PASSWORD));
    }

    @Test
    public void shouldNotCreateUserEmptyPassword() {
        HTTP.POST(neo4j.httpURI().resolve("/v1/schema/create").toString());

        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/v1/users").toString(), emptyPasswordInput);
        HashMap actual  = response.content();
        Assert.assertEquals(400, response.status());
        Assert.assertEquals("Empty password Parameter.", actual.get("error"));
        Assert.assertFalse(actual.containsKey(USERNAME));
        Assert.assertFalse(actual.containsKey(EMAIL));
        Assert.assertFalse(actual.containsKey(NAME));
        Assert.assertFalse(actual.containsKey(PASSWORD));
    }

    private static final HashMap input = new HashMap<String, Object>() {{
        put("username", "maxdemarzi");
        put("email", "maxdemarzi@hotmail.com");
        put("name", "Max De Marzi");
        put("password", "swordfish");
    }};

    private static final HashMap missingUsernameInput = new HashMap<String, Object>() {{
        put("not_username", "maxdemarzi");
    }};

    private static final HashMap emptyUsernameInput = new HashMap<String, Object>() {{
        put("username", "");
    }};

    private static final HashMap invalidUsernameInput = new HashMap<String, Object>() {{
        put("username", " has spaces ");
    }};


    private static final HashMap missingEmailInput = new HashMap<String, Object>() {{
        put("username", "maxdemarzi");
        put("not_email", "maxdemarzi@hotmail.com");
    }};

    private static final HashMap emptyEmailInput = new HashMap<String, Object>() {{
        put("username", "maxdemarzi");
        put("email", "");
    }};

    private static final HashMap invalidEmailInput = new HashMap<String, Object>() {{
        put("username", "maxdemarzi");
        put("email", "not an email address");
    }};

    private static final HashMap missingNameInput = new HashMap<String, Object>() {{
        put("username", "maxdemarzi");
        put("email", "maxdemarzi@hotmail.com");
    }};

    private static final HashMap emptyNameInput = new HashMap<String, Object>() {{
        put("username", "maxdemarzi");
        put("email", "maxdemarzi@hotmail.com");
        put("name", "");
    }};

    private static final HashMap missingPasswordInput = new HashMap<String, Object>() {{
        put("username", "maxdemarzi");
        put("email", "maxdemarzi@hotmail.com");
        put("name", "Max De Marzi");
    }};

    private static final HashMap emptyPasswordInput = new HashMap<String, Object>() {{
        put("username", "maxdemarzi");
        put("email", "maxdemarzi@hotmail.com");
        put("name", "Max De Marzi");
        put("password", "");
    }};
    private static final HashMap expected = new HashMap<String, Object>() {{
        put("username", "maxdemarzi");
        put("email", "maxdemarzi@hotmail.com");
        put("name", "Max De Marzi");
        put("password", "swordfish");
        put("hash","58750f2179edbd650b471280aa66fee5");
    }};
}