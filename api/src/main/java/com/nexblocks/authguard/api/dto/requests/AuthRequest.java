package com.nexblocks.authguard.api.dto.requests;

import com.nexblocks.authguard.api.dto.entities.TokenRestrictionsDTO;
import com.nexblocks.authguard.api.dto.style.DTOStyle;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@DTOStyle
@JsonSerialize(as = AuthRequestDTO.class)
@JsonDeserialize(as = AuthRequestDTO.class)
public interface AuthRequest {
    String getIdentifier();
    String getPassword();
    String getToken();
    TokenRestrictionsDTO getRestrictions();
}