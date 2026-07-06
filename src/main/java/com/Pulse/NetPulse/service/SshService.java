package com.Pulse.NetPulse.service;
import com.Pulse.NetPulse.model.Device;
import com.jcraft.jsch.JSch;
import org.springframework.stereotype.Service;

@Service
public class SshService {

    public void openSshSession (Device dv){

        String ip = dv.getIp();
        String host = dv.getHostname();
        String password = dv.getPassword();

    }
}
