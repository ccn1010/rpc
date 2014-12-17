package com.my.server.internal;

import java.net.Socket;

public interface NewSocketCallback{

    void newSocketAccepted( Socket newSocket );

}
