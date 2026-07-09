package com.Pulse.netpulse.dto.utility;

import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.Session;
import lombok.Getter;

import java.io.InputStream;
import java.io.OutputStream;

@Getter
public class SshSessionWrapper {
    private final Session session;
    private final ChannelShell channel;
    private final InputStream inputStream;
    private final OutputStream outputStream;

    public SshSessionWrapper(Session session, ChannelShell channel, InputStream inputStream, OutputStream outputStream) {
        this.session = session;
        this.channel = channel;
        this.inputStream = inputStream;
        this.outputStream = outputStream;
    }

    public void closeAll() {
        try { if (channel != null) channel.disconnect(); } catch (Exception ignored) {}
        try { if (session != null) session.disconnect(); } catch (Exception ignored) {}
    }
}