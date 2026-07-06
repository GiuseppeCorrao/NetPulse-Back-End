package com.Pulse.NetPulse.model;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class DeviceStatus {

    private long deviceId;
    private String inter_face;
    private String ipAddress;
    private String ok;
    private String method;
    private String status;
    private String protocol;
}
