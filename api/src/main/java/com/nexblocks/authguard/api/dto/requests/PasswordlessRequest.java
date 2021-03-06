package com.nexblocks.authguard.api.dto.requests;

import com.nexblocks.authguard.api.dto.style.DTOStyle;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@DTOStyle
@JsonDeserialize(as = PasswordlessRequestDTO.class)
@JsonSerialize(as = PasswordlessRequestDTO.class)
public interface PasswordlessRequest {
    String getToken();
}
