package me.untouchedodin0.privatemines.data.database;

import java.io.File;

public interface Database {
    boolean connect(Credentials credentials);

    boolean connect(File file);

    void disconnect();

    boolean isConnected();
}
