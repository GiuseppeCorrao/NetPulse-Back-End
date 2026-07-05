package com.Pulse.NetPulse.model;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Entity
public class Device {

    String ip;
    String name;
    String username;
    String password;

}
