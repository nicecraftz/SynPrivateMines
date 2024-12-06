package me.untouchedodin0.privatemines.data.database;

import java.io.File;

public class MySQL implements Database {
    @Override
    public boolean connect(Credentials credentials) {
        return false;
    }

    @Override
    public boolean connect(File file) {
        return false;
    }

    @Override
    public void disconnect() {

    }

    @Override
    public boolean isConnected() {
        return false;
    }
}
