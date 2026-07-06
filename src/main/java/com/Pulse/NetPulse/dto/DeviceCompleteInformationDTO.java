package com.Pulse.NetPulse.dto;

import com.Pulse.NetPulse.model.Device;
import com.Pulse.NetPulse.model.DeviceStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceCompleteInformationDTO {

    Device device;
    DeviceStatus[] deviceStatus;
}
