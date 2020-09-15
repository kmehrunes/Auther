package com.authguard.api.routes;

import com.authguard.api.access.ActorRoles;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.Context;

import static io.javalin.apibuilder.ApiBuilder.post;

public abstract class VerificationApi implements EndpointGroup {

    @Override
    public void addEndpoints() {
        post("/email", this::verifyEmail, ActorRoles.adminClient());
    }

    public abstract void verifyEmail(final Context context);
}