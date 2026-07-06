package com.Pulse.NetPulse.service;

import com.Pulse.NetPulse.model.Device;
import com.Pulse.NetPulse.model.DeviceStatus;
import com.Pulse.NetPulse.repository.DeviceStatusRepository;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Service
public class SshService {

    public void CheckActiveDeviceOnRouter(Device dv) {

        String host = dv.getIp();
        String username = dv.getUsername();
        String password = dv.getPassword();

        String command = "show ip interface brief";

        try {

            //Initializing Jsch Class
            JSch jSch = new JSch();

            // 1. Open and Configure new Session
            Session session = jSch.getSession(username, host);
            session.setPassword(password);

            // Disabled fingeprint key
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);

            //Connect with device, timeout of 5 second
            session.connect(5000);

            // 2. Open the channel for execute commands
            ChannelExec channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);

            // Redirect error on java console
            channel.setErrStream(System.err);

            // 3. Read the output of the command row for row
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(channel.getInputStream()))) {

                channel.connect(); //Start remote execution

                String inter_face = "";
                String ipAddress = "";
                String ok = "";
                String method = "";
                String status = "";
                String protocol = "";

                int counter = 0;
                String line;

                while ((line = reader.readLine()) != null) {

                    if (counter == 0) {
                        System.out.println(line);
                        counter += 1;
                        continue;
                    }
                    String[] tokens = line.split("\\s+");
                    if (tokens.length >= 5) {
                        inter_face = tokens[0];
                        ipAddress = tokens[1];
                        ok = tokens[2];
                        method = tokens[3];
                        status = tokens[4];
                        protocol = tokens[5];

                        // Troviamo STATUS e PROTOCOL cercando le parole chiave nell'array
                        for (int i = 0; i < tokens.length; i++) {
                            if (tokens[i].equalsIgnoreCase("STATUS") && (i + 1) < tokens.length) {
                                status = tokens[i + 1];
                            }
                            if (tokens[i].equalsIgnoreCase("PROTOCOL") && (i + 1) < tokens.length) {
                                protocol = tokens[i + 1];
                            }
                        }

                        System.out.println("Interfaccia: " + inter_face);
                        System.out.println("IP:          " + ipAddress);
                        System.out.println("OK:          " + ok);
                        System.out.println("METHOD:          " + method);
                        System.out.println("Status:      " + status);
                        System.out.println("Protocol:    " + protocol);
                        System.out.println("------------------------------------");

                        DeviceStatus ds = new DeviceStatus();

                    }
                }
            }catch (Exception e) {
                e.printStackTrace();

            }

            // Time delay for waiting the channel finish the execution
            while (channel.isConnected()) {
                Thread.sleep(100);
            }

            // 4. Close all
            channel.disconnect();
            session.disconnect();

        } catch (Exception e) {
            e.printStackTrace();


        }

    }
}
