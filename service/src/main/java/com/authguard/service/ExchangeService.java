package com.authguard.service;

import com.authguard.service.model.TokensBO;

public interface ExchangeService {
    TokensBO exchange(String token, String fromTokenType, String toTokenType);

    boolean supportsExchange(String fromTokenType, String toTokenType);
}