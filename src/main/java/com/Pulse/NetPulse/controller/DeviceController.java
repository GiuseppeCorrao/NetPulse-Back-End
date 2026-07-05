package com.Pulse.NetPulse.controller;

import com.Pulse.NetPulse.model.Device;
import com.Pulse.NetPulse.repository.DeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/devices") // Rotta base per tutte le richieste in questo controller
public class DeviceController {

    @Autowired
    private DeviceRepository deviceRepository;

    // 1. GET - Recupera tutti i dispositivi dal database
    // Esempio: GET http://localhost:8080/api/devices
    @GetMapping
    public List<Device> getAllDevices() {
        return deviceRepository.findAll();
    }

    // 2. GET - Recupera un singolo dispositivo tramite il suo ID
    // Esempio: GET http://localhost:8080/api/devices/1
    @GetMapping("/{id}")
    public ResponseEntity<Device> getDeviceById(@PathVariable Long id) {
        Optional<Device> device = deviceRepository.findById(id);

        // Se il dispositivo esiste restituisce 200 OK, altrimenti 404 Not Found
        return device.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 3. POST - Crea e salva un nuovo dispositivo nel database
    // Esempio: POST http://localhost:8080/api/devices con un JSON nel Body
    @PostMapping
    public Device createDevice(@RequestBody Device device) {
        // Prende il JSON inviato dall'app (o da Postman) e lo salva su Postgres
        return deviceRepository.save(device);
    }

    // 4. PUT - Aggiorna un dispositivo esistente
    // Esempio: PUT http://localhost:8080/api/devices/1 con il JSON aggiornato nel Body
    @PutMapping("/{id}")
    public ResponseEntity<Device> updateDevice(@PathVariable Long id, @RequestBody Device deviceDetails) {
        return deviceRepository.findById(id).map(existingDevice -> {
            // Aggiorna i campi usando i getter/setter di Lombok
            existingDevice.setIp(deviceDetails.getIp());
            existingDevice.setName(deviceDetails.getName());
            existingDevice.setUsername(deviceDetails.getUsername());
            existingDevice.setPassword(deviceDetails.getPassword());

            Device updatedDevice = deviceRepository.save(existingDevice);
            return ResponseEntity.ok(updatedDevice);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 5. DELETE - Cancella un dispositivo dal database tramite ID
    // Esempio: DELETE http://localhost:8080/api/devices/1
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDevice(@PathVariable Long id) {
        if (deviceRepository.existsById(id)) {
            deviceRepository.deleteById(id);
            return ResponseEntity.ok().build(); // 200 OK se eliminato
        }
        return ResponseEntity.notFound().build(); // 404 se non trovato
    }
}