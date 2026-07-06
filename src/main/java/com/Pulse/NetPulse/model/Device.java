package com.Pulse.NetPulse.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.IdGeneratorType;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity

public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    //host
    private String ip;
    //user
    private String name;
    private String hostname;
    //user
    private String username;
    private String password;

}
