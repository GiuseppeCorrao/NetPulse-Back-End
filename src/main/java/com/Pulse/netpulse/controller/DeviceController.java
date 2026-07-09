package com.Pulse.netpulse.controller;

import com.Pulse.netpulse.model.Device;
import com.Pulse.netpulse.repository.DeviceRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/devices")
@Tag(name = "Device API")
public class DeviceController {

    @Autowired
    DeviceRepository deviceRepository;

    // 1. GET - Retrieve all devices
    @Operation(summary = "Get all devices", description = "Returns a list of all devices in the database")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of devices"),
    })
    @GetMapping // Example: GET http://localhost:8080/api/devices
    public List<Device> getAllDevices() {
        return deviceRepository.findAll();
    }

    // 2. GET - Returns a single Device as per the provided id
    @Operation(summary = "Get a device by id", description = "Returns a single device as per the provided id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the device"),
            @ApiResponse(responseCode = "404", description = "Not found - The device was not found", content = @Content(schema = @Schema(implementation = String.class)))
    })
    @GetMapping("/{id}") // Example: GET http://localhost:8080/api/devices/1
    public ResponseEntity<Device> getDeviceById(@PathVariable Long id) {
        Optional<Device> device = deviceRepository.findById(id);

        // If the Device exist return 200 OK, Otherwise 404 Not Found
        return device.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 3. POST - Create and save a new Device in the Database
    @Operation(summary = "Create a new device", description = "Creates and saves a new device in the database")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully created the device"),
            @ApiResponse(responseCode = "400", description = "Bad request - Invalid input data", content = @Content(schema = @Schema(implementation = String.class)))
    })
    // Example: POST http://localhost:8080/api/devices with a json in Body
    @PostMapping
    public Device createDevice(@RequestBody Device device) {

        //Pick the JSON sent from app (or Postman) then he save the device on Database
        return deviceRepository.save(device);
    }

    // 4. PUT -update an existing device
    @Operation(summary = "Update a device", description = "Updates an existing device's details based on the provided id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully updated the device"),
            @ApiResponse(responseCode = "404", description = "Not found - The device to update was not found", content = @Content(schema = @Schema(implementation = String.class)))
    })
    // Example: PUT http://localhost:8080/api/devices/1 with a JSON update in Body
    @PutMapping("/{id}")
    public ResponseEntity<Device> updateDevice(@PathVariable Long id, @RequestBody Device deviceDetails) {
        return deviceRepository.findById(id).map(existingDevice -> {
            // Update Device's field with Lombok's getter/setter
            existingDevice.setIp(deviceDetails.getIp());
            existingDevice.setName(deviceDetails.getName());
            existingDevice.setUsername(deviceDetails.getUsername());
            existingDevice.setPassword(deviceDetails.getPassword());

            Device updatedDevice = deviceRepository.save(existingDevice);
            return ResponseEntity.ok(updatedDevice);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 5. DELETE - Delete a device from the database with ID
    @Operation(summary = "Delete a device by id", description = "Deletes a device from the database as per the provided id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully deleted the device"),
            @ApiResponse(responseCode = "404", description = "Not found - The device to delete was not found", content = @Content(schema = @Schema(implementation = String.class)))
    })
    // Example: DELETE http://localhost:8080/api/devices/1
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDevice(@PathVariable Long id) {
        if (deviceRepository.existsById(id)) {
            deviceRepository.deleteById(id);
            return ResponseEntity.ok().build(); // 200 OK se eliminato
        }
        return ResponseEntity.notFound().build(); // 404 se non trovato
    }
}