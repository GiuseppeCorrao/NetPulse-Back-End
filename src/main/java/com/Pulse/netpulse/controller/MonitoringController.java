package com.Pulse.netpulse.controller;

import com.Pulse.netpulse.dto.DeviceCompleteInformationDTO;
import com.Pulse.netpulse.model.Device;
import com.Pulse.netpulse.service.SshService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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

    @Deprecated
    // 3. POST - Check all active device on all interface (CISCO Router)
    @Operation(summary = "Check all active device", description = "Check all active device on each interface of the router")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully created the device and saved information"),
            @ApiResponse(responseCode = "400", description = "Bad request - Invalid input data", content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "422", description = "Unprocessable Entity - Duplicate (Entity Already Exists)", content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = String.class)))
    })
    // Example: POST http://localhost:8080/api/addrouter with a json in Body
    @PostMapping("/addrouter")
    public ResponseEntity<DeviceCompleteInformationDTO> createDevice(@RequestBody Device device) {

        return ResponseEntity.ok(sshService.CheckActiveDeviceOnRouter(device));
    }

}
