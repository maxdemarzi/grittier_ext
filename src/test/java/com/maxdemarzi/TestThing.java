package com.maxdemarzi;

public class TestThing {
    private Exceptions e;

    public TestThing(Exceptions e) {
        this.e = e;
    }

    public void chuck() {
        throw e;
    }

}