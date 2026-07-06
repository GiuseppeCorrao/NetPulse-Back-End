package com.Pulse.NetPulse.service;

import com.Pulse.NetPulse.dto.DeviceCompleteInformationDTO;
import com.Pulse.NetPulse.exceptions.DuplicateDeviceException;
import com.Pulse.NetPulse.model.Device;
import com.Pulse.NetPulse.model.DeviceStatus;
import com.Pulse.NetPulse.repository.DeviceRepository;
import com.Pulse.NetPulse.repository.DeviceStatusRepository;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Service
public class SshService {

    @Autowired
    DeviceStatusRepository deviceStatusRepository;

    @Autowired
    DeviceRepository deviceRepository;

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
            Session session = jSch.getSession(username, host);
            session.setPassword(password);

            // Disabled fingeprint key
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);

            // Connect with device, timeout of 5 second
            session.connect(5000);

            // Open the channel for execute commands
            ChannelExec channel = (ChannelExec) session.openChannel("exec");
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
                        DeviceStatus ds = new DeviceStatus(generatedDeviceId,inter_face,ipAddress,ok,method,status,protocol);
                        listOfDeviceStatus.add(ds);
                        deviceStatusRepository.save(ds);

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

        //Set Converted List to Array to newDeviceResponse
        newDeviceResponse.setDeviceStatus(listOfDeviceStatus.toArray(new DeviceStatus[0]));

        return newDeviceResponse;

    }
}
