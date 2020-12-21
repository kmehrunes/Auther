package com.authguard.extensions.verification;

import com.authguard.config.ConfigContext;
import com.authguard.dal.AccountTokensRepository;
import com.authguard.dal.model.AccountTokenDO;
import com.authguard.emb.MessageSubscriber;
import com.authguard.emb.annotations.Channel;
import com.authguard.emb.model.EventType;
import com.authguard.emb.model.Message;
import com.authguard.external.email.EmailProvider;
import com.authguard.external.email.ImmutableEmail;
import com.authguard.service.config.ConfigParser;
import com.authguard.service.model.AccountBO;
import com.authguard.service.model.VerificationRequestBO;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;

@Channel("verification")
public class VerificationSubscriber implements MessageSubscriber {
    private final String EMAIL_TEMPLATE = "verify-email";

    private static final Logger LOG = LoggerFactory.getLogger(VerificationSubscriber.class);

    private final EmailProvider emailProvider;
    private final AccountTokensRepository accountTokensRepository;
    private final ImmutableVerificationConfig verificationConfig;

    @Inject
    public VerificationSubscriber(final EmailProvider emailProvider,
                                  final AccountTokensRepository accountTokensRepository,
                                  final @Named("verification") ConfigContext verificationConfig) {
        this.emailProvider = emailProvider;
        this.accountTokensRepository = accountTokensRepository;
        this.verificationConfig = verificationConfig.asConfigBean(ImmutableVerificationConfig.class);
    }

    @Override
    public void onMessage(final Message message) {
        if (message.getEventType() == EventType.EMAIL_VERIFICATION) {
            if (message.getBodyType() != VerificationRequestBO.class) {
                LOG.error("Received an event of type {} but the body type was invalid", message.getEventType());
            } else {
                sendVerificationEmails((VerificationRequestBO) message.getMessageBody());
            }
        } else {
            LOG.warn("An event of type {} was published to the verification channel and cannot be processed", message.getEventType());
        }
    }

    private void sendVerificationEmails(final VerificationRequestBO verificationRequest) {
        Objects.requireNonNull(verificationRequest.getAccount(), "Account cannot be null");
        Objects.requireNonNull(verificationRequest.getEmails(), "Emails cannot be null");

        if (verificationRequest.getEmails().isEmpty()) {
            LOG.warn("A verification request was published with an empty list of emails");
        } else {
            doSendEmails(verificationRequest);
        }
    }

    private void doSendEmails(final VerificationRequestBO verificationRequest) {
        final AccountBO account = verificationRequest.getAccount();

        verificationRequest.getEmails().forEach(email -> {
            if (email.isVerified()) {
                LOG.warn("Email is already verified. Skipping.");
            } else {
                final String token = generateVerificationString();

                final ZonedDateTime expiration = ZonedDateTime.now()
                        .plus(ConfigParser.parseDuration(verificationConfig.getEmailVerificationLife()));

                final AccountTokenDO accountToken = AccountTokenDO.builder()
                        .expiresAt(expiration)
                        .associatedAccountId(account.getId())
                        .token(token)
                        .additionalInformation(Collections.singletonMap("email", email.getEmail()))
                        .build();

                accountTokensRepository.save(accountToken);

                final ImmutableEmail email1 = ImmutableEmail.builder()
                        .template(EMAIL_TEMPLATE)
                        .to(email.getEmail())
                        .putParameters("token", token)
                        .build();

                emailProvider.send(email1);
            }
        });
    }

    private String generateVerificationString() {
        final byte[] verificationCode = UUID.randomUUID().toString().getBytes();
        return Base64.getEncoder().encodeToString(verificationCode);
    }
}
