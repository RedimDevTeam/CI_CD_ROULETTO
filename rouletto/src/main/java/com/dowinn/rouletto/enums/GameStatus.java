package com.dowinn.rouletto.enums;

import com.dowinn.rouletto.entity.Game;

public enum GameStatus {

    INPROGRESS(1),
    COMPLETED(2),
    CANCEL(3);

    GameStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    int status;

    public static GameStatus getValue(Integer value){
        for(GameStatus status:GameStatus.values()){
            if(status.getStatus()==value)return status;
        }
        return null;
    }

}
