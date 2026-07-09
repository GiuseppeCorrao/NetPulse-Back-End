package com.Pulse.netpulse.repository;

import com.Pulse.netpulse.model.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {


    //Get a List of Device saved on DB
    List<Device> findAll();

    //Get a List of Device's name saved on DB
    @Query(value = "select d.name from Device d")
    List<String> getAllNameDevice();

    Device findById(long id);

    //RETURN: true if the ip exist on the database
    boolean existsByIp(String ip);

    //RETURN: true if the name exist on the database
    boolean existsByName(String name);

}
