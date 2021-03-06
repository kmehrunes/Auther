package com.nexblocks.authguard.service.impl;

import com.nexblocks.authguard.dal.model.ApiKeyDO;
import com.nexblocks.authguard.dal.persistence.ApiKeysRepository;
import com.nexblocks.authguard.emb.MessageBus;
import com.nexblocks.authguard.service.ApiKeysService;
import com.nexblocks.authguard.service.ApplicationsService;
import com.nexblocks.authguard.service.config.ApiKeyHashingConfig;
import com.nexblocks.authguard.service.config.ApiKeysConfig;
import com.nexblocks.authguard.service.exceptions.ServiceNotFoundException;
import com.nexblocks.authguard.service.exchange.ApiKeyExchange;
import com.nexblocks.authguard.service.keys.ApiKeyHash;
import com.nexblocks.authguard.service.keys.ApiKeyHashProvider;
import com.nexblocks.authguard.service.mappers.ServiceMapper;
import com.nexblocks.authguard.service.mappers.ServiceMapperImpl;
import com.nexblocks.authguard.service.model.ApiKeyBO;
import com.nexblocks.authguard.service.model.AppBO;
import com.nexblocks.authguard.service.model.AuthResponseBO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ApiKeysServiceImplTest {

    private ApplicationsService applicationsService;
    private ApiKeysRepository apiKeysRepository;
    private ApiKeyExchange apiKeyExchange;
    private ApiKeyHashProvider apiKeyHashProvider;
    private ApiKeyHash apiKeyHash;
    private MessageBus messageBus;
    private ServiceMapper serviceMapper;
    private ApiKeysService apiKeysService;

    @BeforeEach
    void setup() {
        applicationsService = Mockito.mock(ApplicationsService.class);
        apiKeysRepository = Mockito.mock(ApiKeysRepository.class);
        apiKeyExchange = Mockito.mock(ApiKeyExchange.class);
        messageBus = Mockito.mock(MessageBus.class);

        apiKeyHashProvider = new ApiKeyHashProvider(ApiKeysConfig.builder()
                .hash(ApiKeyHashingConfig.builder()
                        .algorithm("blake2b")
                        .digestSize(32)
                        .key("test-key")
                        .build())
                .build());

        apiKeyHash = apiKeyHashProvider.getHash();

        serviceMapper = new ServiceMapperImpl();

        apiKeysService = new ApiKeysServiceImpl(applicationsService, apiKeyExchange, apiKeysRepository,
                apiKeyHashProvider, messageBus, serviceMapper);
    }

    @Test
    void generateApiKey() {
        final String appId = "app";
        final String key = "key";
        final AppBO app = AppBO.builder()
                .id(appId)
                .build();

        Mockito.when(applicationsService.getById(appId))
                .thenReturn(Optional.of(app));

        Mockito.when(apiKeysRepository.save(Mockito.any()))
                .thenAnswer(invocation -> CompletableFuture.completedFuture(invocation.getArgument(0, ApiKeyDO.class)));

        Mockito.when(apiKeyExchange.generateKey(app))
                .thenReturn(AuthResponseBO.builder()
                        .token(key)
                        .build());

        final ApiKeyBO actual = apiKeysService.generateApiKey(appId);

        assertThat(actual.getAppId()).isEqualTo(appId);
        assertThat(actual.getKey()).isEqualTo(key);

        // verify that we persisted the hashed key and not the clear key

        final ArgumentCaptor<ApiKeyDO> argumentCaptor = ArgumentCaptor.forClass(ApiKeyDO.class);

        Mockito.verify(apiKeysRepository).save(argumentCaptor.capture());

        final ApiKeyDO persisted = argumentCaptor.getValue();

        assertThat(persisted.getKey()).isEqualTo(apiKeyHash.hash(key));
    }

    @Test
    void generateApiKeyNonexistingApp() {
        final String appId = "app";

        Mockito.when(applicationsService.getById(appId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> apiKeysService.generateApiKey(appId))
                .isInstanceOf(ServiceNotFoundException.class);
    }

    @Test
    void getByAppId() {
        final String appId = "app";
        final String key = "key";
        final ApiKeyBO apiKeyBO = ApiKeyBO.builder()
                .appId(appId)
                .key(key)
                .build();
        final ApiKeyDO apiKeyDO = serviceMapper.toDO(apiKeyBO);

        Mockito.when(apiKeysRepository.getByAppId(appId))
                .thenReturn(CompletableFuture.completedFuture(Collections.singletonList(apiKeyDO)));

        final List<ApiKeyBO> actual = apiKeysService.getByAppId(appId);

        assertThat(actual).isEqualTo(Collections.singletonList(apiKeyBO));
    }

    @Test
    void validateApiKey() {
        final String appId = "app";
        final String key = "key";
        final AppBO app = AppBO.builder()
                .id(appId)
                .build();

        Mockito.when(apiKeyExchange.verifyAndGetAppId(key))
                .thenReturn(CompletableFuture.completedFuture(Optional.of(appId)));

        Mockito.when(applicationsService.getById(appId))
                .thenReturn(Optional.of(app));

        final Optional<AppBO> actual = apiKeysService.validateApiKey(key);

        assertThat(actual).contains(app);
    }

    @Test
    void getById() {
        final String id = "api_key";
        final ApiKeyBO apiKeyBO = ApiKeyBO.builder()
                .id(id)
                .build();
        final ApiKeyDO apiKeyDO = ApiKeyDO.builder()
                .id(id)
                .build();

        Mockito.when(apiKeysRepository.getById(id))
                .thenReturn(CompletableFuture.completedFuture(Optional.of(apiKeyDO)));

        final Optional<ApiKeyBO> actual = apiKeysService.getById(id);

        assertThat(actual).contains(apiKeyBO);
    }

    @Test
    void delete() {
        final String id = "api_key";
        final ApiKeyBO apiKeyBO = ApiKeyBO.builder()
                .id(id)
                .build();
        final ApiKeyDO apiKeyDO = ApiKeyDO.builder()
                .id(id)
                .build();

        Mockito.when(apiKeysRepository.delete(id))
                .thenReturn(CompletableFuture.completedFuture(Optional.of(apiKeyDO)));

        final Optional<ApiKeyBO> actual = apiKeysService.delete(id);

        assertThat(actual).contains(apiKeyBO);
    }
}