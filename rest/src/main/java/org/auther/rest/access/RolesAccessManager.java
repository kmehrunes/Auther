package org.auther.rest.access;

import io.javalin.core.security.AccessManager;
import io.javalin.core.security.Role;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.auther.service.model.AccountBO;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Set;

public class RolesAccessManager implements AccessManager {
    @Override
    public void manage(@NotNull final Handler handler, @NotNull final Context context,
                       @NotNull final Set<Role> set) throws Exception {
        if (set.isEmpty()) {
            handler.handle(context);
        } else {
            final AccountBO actor = context.attribute("actor");

            if (isPermitted(actor, set)) {
                handler.handle(context);
            } else {
                context.status(401).result("");
            }
        }
    }

    private boolean isPermitted(final AccountBO actor, final Set<Role> permittedRoles) {
        if (actor == null) {
            return false;
        }

        final Optional<ActorRole> matchedRole = actor.getRoles().stream()
                .map(ActorRole::of)
                .filter(permittedRoles::contains)
                .findFirst(); // we only need to have one valid role to access the endpoint

        return matchedRole.isPresent();
    }
}
