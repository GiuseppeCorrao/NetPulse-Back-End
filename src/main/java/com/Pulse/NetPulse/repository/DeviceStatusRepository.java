package com.Pulse.NetPulse.repository;

import com.Pulse.NetPulse.model.Device;
import com.Pulse.NetPulse.model.DeviceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeviceStatusRepository extends JpaRepository<DeviceStatus, Long> {


    //Get a List of Device saved on DB
    List<DeviceStatus> findAll();

    Device findById(long id);

}
