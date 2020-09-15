package com.authguard.service.exceptions.codes;

public enum ErrorCode {
    ACCOUNT_DOES_NOT_EXIST("AC.011"),

    APP_DOES_NOT_EXIST("AP.011"),

    PERMISSION_DOES_NOT_EXIST("PR.011"),
    PERMISSION_ALREADY_EXIST("PR.012"),

    ROLE_DOES_NOT_EXIST("RL.011"),
    ROLE_ALREADY_EXISTS("RL.012"),

    CREDENTIALS_DOES_NOT_EXIST("CD.011"),
    IDENTIFIER_ALREADY_EXISTS("CD.012"),
    IDENTIFIER_DOES_NOT_EXIST("CD.013"),

    EXPIRED_TOKEN("TK.021"),
    INVALID_TOKEN("TK.022"),

    UNKNOWN_EXCHANGE("AT.021"),
    UNSUPPORTED_SCHEME("AT.022"),
    INVALID_AUTHORIZATION_FORMAT("AT.023"),
    INVALID_ADDITIONAL_INFORMATION_TYPE("AT.024"),
    TOKEN_EXPIRED_OR_DOES_NOT_EXIST("AT.025"),
    TOKEN_GENERATION_FAILED("AT.031"),

    UNSUPPORTED_JWT_ALGORITHM("JT.021"),

    PASSWORDS_DO_NOT_MATCH("PW.021"),
    INVALID_PASSWORD("PW.022"),

    LDAP_MULTIPLE_PASSWORD_ENTRIES("LD.021"),
    LDAP_ERROR("LD.031");

    private final String code;

    ErrorCode(final String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}