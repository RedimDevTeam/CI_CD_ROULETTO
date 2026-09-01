package com.dowinn.rouletto.service;


import com.dowinn.rouletto.model.GameDetail;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class RecordingService {

    @Value("${record.host}")
    private  String host;

    @Value("${record.user}")
    private  String user;

    @Value("${record.password}")
    private  String password;

    public void executeRemoteScript(GameDetail gameDetail, String commands, boolean isJackPot) {
        try {
            log.info("Recording service start");
            JSch jsch = new JSch();
            Session session = jsch.getSession(user, host, 22022);
            session.setPassword(password);

            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            String command = "/opt/recorder/recorder_rto.sh "+commands +" "+gameDetail.getGameId()+" "+(isJackPot?"jackpot":"gameplay");

            ChannelExec channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);

            channel.connect();

            channel.disconnect();
            session.disconnect();

        } catch (Exception e) {
            log.info("exception e {}",e);
        }
    }
}
