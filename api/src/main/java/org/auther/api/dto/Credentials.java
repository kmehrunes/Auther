package org.auther.api.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

@Value.Immutable
@DTOStyle
@JsonDeserialize(as = CredentialsDTO.class)
public interface Credentials {
    String getId();
    String getAccountId();
    String getUsername();
    String getPlainPassword();
}
