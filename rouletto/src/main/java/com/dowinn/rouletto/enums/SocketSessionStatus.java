package com.dowinn.rouletto.enums;

public enum SocketSessionStatus {
    EASTABLISH(1),
    MESSAGES(2),
    CLOSE(3);

    int status;

    SocketSessionStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
