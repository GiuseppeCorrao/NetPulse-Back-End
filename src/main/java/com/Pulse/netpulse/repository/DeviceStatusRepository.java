package com.Pulse.netpulse.repository;

import com.Pulse.netpulse.model.Device;
import com.Pulse.netpulse.model.DeviceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Deprecated
@Repository
public interface DeviceStatusRepository extends JpaRepository<DeviceStatus, Long> {


    //Get a List of Device saved on DB
    List<DeviceStatus> findAll();

    Device findById(long id);

}
