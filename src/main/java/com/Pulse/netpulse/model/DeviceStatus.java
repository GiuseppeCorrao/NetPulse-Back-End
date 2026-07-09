package com.Pulse.netpulse.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Deprecated
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class DeviceStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private long deviceId;
    private String inter_face;
    private String ipAddress;
    private String ok;
    private String method;
    private String status;
    private String protocol;

    public  DeviceStatus(long deviceId, String inter_face, String ipAddress, String ok, String method, String status, String protocol) {
        this.deviceId = deviceId;
        this.inter_face = inter_face;
        this.ipAddress = ipAddress;
        this.ok = ok;
        this.method = method;
        this.status = status;
        this.protocol = protocol;
    }
}

