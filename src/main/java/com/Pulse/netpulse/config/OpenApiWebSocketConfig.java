package com.Pulse.netpulse.config;

import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiWebSocketConfig {

    @Bean
    public OpenApiCustomizer customerLoop() {
        return openApi -> {
            // 1. Define the WebSocket operation
            Operation webSocketOperation = new Operation()
                    .summary("Real-Time WebSocket Connection (SSH Output)")
                    .description("Duplex connection endpoint. Initiates real-time streaming of the Host output buffer encoded in StandardCharsets.UTF_8.")
                    .addTagsItem("Network & Real-Time");

            // 2. Define the schema as a native string (since it is raw text)
            Schema<String> stringSchema = new Schema<>();
            stringSchema.setType("string");
            stringSchema.$comment(" IF Connected: \n \uD83D\uDFE2 CONNECTED TO THE SSH HOST SHELL! \n IF Not Connected: \n Insert the SSH server IP address: \n Default: \n Insert the SSH server IP address:");
            stringSchema.setDescription("Raw text frame (Plain Text) extracted directly from the SSH output stream buffer.");

            // 3. Associate the response using "text/plain" instead of application/json
            ApiResponses responses = new ApiResponses()
                    .addApiResponse("101", new ApiResponse()
                            .description("Switching Protocols. Handshake completed. Text message stream initialized.")
                            .content(new Content().addMediaType("text/plain",
                                    new MediaType().schema(stringSchema))));


            webSocketOperation.setResponses(responses);

            // 4. Add the path to the Swagger paths map (ensure this matches your actual WS endpoint)
            PathItem pathItem = new PathItem().get(webSocketOperation);
            openApi.getPaths().addPathItem("ws://{HOST}:{PORT}/ssh", pathItem);
        };
    }
}