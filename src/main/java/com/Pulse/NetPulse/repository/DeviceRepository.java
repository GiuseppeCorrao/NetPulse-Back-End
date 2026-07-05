package com.Pulse.NetPulse.repository;

import com.Pulse.NetPulse.model.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {


    //Get a List of Device saved on DB
    @Query(value = "Select * from Devices")
    List<Device> findAll();

    //Get a List of Device's name saved on DB
    @Query(value = "select d.name from Devices d")
    List<String> getAllNameDevice();

    Device findById(long id);

}
