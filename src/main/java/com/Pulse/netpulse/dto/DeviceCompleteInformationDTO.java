package com.Pulse.netpulse.dto;

import com.Pulse.netpulse.model.Device;
import com.Pulse.netpulse.model.DeviceStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Deprecated
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceCompleteInformationDTO {

    Device device;
    DeviceStatus[] deviceStatus;
}
