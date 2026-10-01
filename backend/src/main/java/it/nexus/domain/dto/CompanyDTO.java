package it.nexus.domain.dto;

import java.time.Instant;

public record CompanyDTO(
        String id,
        String name,
        String vatCode,
        String legalAddress,
        String contactPerson,
        String phone,
        String email,
        boolean active,
        long version,
        Instant updatedAt) {
}
