package org.auther.service.impl;

import org.auther.service.JTIProvider;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;

public class BasicJTIProvider implements JTIProvider {
    private final Set<String> generatedIds;
    private final Set<String> usedIds;

    public BasicJTIProvider() {
        usedIds = new ConcurrentSkipListSet<>();
        generatedIds = new ConcurrentSkipListSet<>();
    }

    @Override
    public String next() {
        final String id = UUID.randomUUID().toString();
        generatedIds.add(id);
        return id;
    }

    @Override
    public boolean validate(final String jti) {
        if (usedIds.contains(jti) || !generatedIds.contains(jti)) {
            return false;
        }

        usedIds.add(jti);
        return true;
    }
}