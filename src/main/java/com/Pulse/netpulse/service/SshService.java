package com.Pulse.netpulse.service;

import com.Pulse.netpulse.dto.DeviceCompleteInformationDTO;
import com.Pulse.netpulse.dto.utility.SshSessionWrapper;
import com.Pulse.netpulse.exceptions.DuplicateDeviceException;
import com.Pulse.netpulse.model.Device;
import com.Pulse.netpulse.model.DeviceStatus;
import com.Pulse.netpulse.repository.DeviceRepository;
import com.Pulse.netpulse.repository.DeviceStatusRepository;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.TextMessage;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Service
public class SshService {

    @Autowired
    DeviceStatusRepository deviceStatusRepository;

    @Autowired
    DeviceRepository deviceRepository;

    private Session jschSession;

    @Deprecated
    @Transactional
    public DeviceCompleteInformationDTO CheckActiveDeviceOnRouter(Device dv) {

        //CHECK DUPLICATE: Verify is IP or name exist in database
        if (deviceRepository.existsByIp(dv.getIp()) || deviceRepository.existsByName(dv.getName())) {
            // Launch exception
            throw new DuplicateDeviceException("The device with same ip or same name already exist.");
        }

        // DECLARE: ssh information, ip, username,password, command
        String host = dv.getIp();
        String username = dv.getUsername();
        String password = dv.getPassword();
        String command = "show ip interface brief";

        // DECLARE: Device information object and list
        List<DeviceStatus> listOfDeviceStatus = new ArrayList<>();
        Device newDevice = new Device();
        DeviceCompleteInformationDTO newDeviceResponse = new DeviceCompleteInformationDTO();

        //Start Try catch block
        try {

            // Save in Repository New Device,
            newDevice = deviceRepository.save(dv);

            // Set Object Response and genereted Device Id
            newDeviceResponse.setDevice(newDevice);
            Long generatedDeviceId = newDevice.getId();

            // Initializing Jsch Class
            JSch jSch = new JSch();

            // Open and Configure new Session
            jschSession = jSch.getSession(username, host);
            jschSession.setPassword(password);

            // Disabled fingeprint key
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            jschSession.setConfig(config);

            // Connect with device, timeout of 5 second
            jschSession.connect(5000);

            // Open the channel for execute commands
            ChannelExec channel = (ChannelExec) jschSession.openChannel("exec");
            channel.setCommand(command);

            // Redirect error on java console
            channel.setErrStream(System.err);

            // Read the output of the command row for row
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(channel.getInputStream()))) {

                channel.connect(); //Start remote execution

                // Declare all the setup variables (DeviceStatus Field)
                String inter_face = "";
                String ipAddress = "";
                String ok = "";
                String method = "";
                String status = "";
                String protocol = "";

                // Declare Counter and row variable
                int counter = 0;
                String line;

                //Start While cycle, loop when there are entries
                while ((line = reader.readLine()) != null) {

                    if (counter == 0) {

                        //PRINT: Header variable, (test)
                        //System.out.println(line);
                        counter += 1;
                        //If counter == 0 skip
                        continue;
                    }

                    //Regex separator, take a word each space
                    String[] tokens = line.split("\\s+");

                    //ASSIGNAMENT: assign value to temporary variable
                    if (tokens.length >= 6) {
                        inter_face = tokens[0];
                        ipAddress = tokens[1];
                        ok = tokens[2];
                        method = tokens[3];
                        status = tokens[4];
                        protocol = tokens[5];

                        //PRINT: current field for row
                        System.out.println("Interfaccia: " + inter_face);
                        System.out.println("IP:          " + ipAddress);
                        System.out.println("OK:          " + ok);
                        System.out.println("METHOD:          " + method);
                        System.out.println("Status:      " + status);
                        System.out.println("Protocol:    " + protocol);
                        System.out.println("------------------------------------");

                        //ASSIGNMENT: assign the variable to DeviceStatus, and on the temorary list and save on Database
                        DeviceStatus ds = new DeviceStatus(generatedDeviceId, inter_face, ipAddress, ok, method, status, protocol);
                        listOfDeviceStatus.add(ds);


                    }
                }
            }

            deviceStatusRepository.saveAll(listOfDeviceStatus);
            // Time delay for waiting the channel finish the execution
            while (channel.isConnected()) {
                Thread.sleep(100);
            }

            // 4. Close all
            channel.disconnect();
            jschSession.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("SSH Execution failed: " + e.getMessage(), e);

        }

        //Set Converted List to Array to newDeviceResponse
        newDeviceResponse.setDeviceStatus(listOfDeviceStatus.toArray(new DeviceStatus[0]));

        return newDeviceResponse;

    }

    public SshSessionWrapper openSshSession(String host, String user, String password) throws Exception {
        int port = 22;

        Session session = null;
        OutputStream sshOutput= null;
        InputStream ssInput = null;
        ChannelShell channel = null;

        try {
            JSch jsch = new JSch();


            // Creiamo sessione locale al metodo per thread-safety
            session = jsch.getSession(user, host, port);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect(30000);
             channel = (ChannelShell) session.openChannel("shell");

            // 🟢 AGGIUNGI QUESTE RIGHE PRIMA DI PRENDERE GLI STREAM:
            // Imposta un tipo di terminale standard e configura i canali in modo trasparente
            channel.setPtyType("vanilla"); // Evita che caratteri strani o incompatibilità di terminale chiudano la sessione

            sshOutput = channel.getOutputStream();
            ssInput = channel.getInputStream();

            channel.connect();


        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new Exception(e.getMessage());

        }

        // Ritorniamo tutto l'occorrente impacchettato
        return new SshSessionWrapper(session, channel, ssInput, sshOutput);
    }

}




