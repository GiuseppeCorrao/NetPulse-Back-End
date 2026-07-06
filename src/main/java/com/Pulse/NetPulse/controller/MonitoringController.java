package com.Pulse.NetPulse.controller;

import com.Pulse.NetPulse.dto.DeviceCompleteInformationDTO;
import com.Pulse.NetPulse.model.Device;
import com.Pulse.NetPulse.repository.DeviceRepository;
import com.Pulse.NetPulse.service.SshService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/monitoring")
@Tag(name = "Monitoring API")
public class MonitoringController {

    @Autowired
    SshService sshService;

    // 3. POST - Check all active device on all interface (CISCO Router)
    @Operation(summary = "Check all active device", description = "Check all active device on each interface of the router")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully created the device and saved information"),
            @ApiResponse(responseCode = "400", description = "Bad request - Invalid input data")
    })
    // Example: POST http://localhost:8080/api/addrouter with a json in Body
    @PostMapping("/addrouter")
    public ResponseEntity<DeviceCompleteInformationDTO> createDevice(@RequestBody Device device) {

        return ResponseEntity.ok(sshService.CheckActiveDeviceOnRouter(device));
    }

}
