package org.prt.prtvaccinationtracking_fhir.auth.dto;

import java.util.List;

public record SmartConfigurationResponse(
        String issuer,
        String authorization_endpoint,
        String token_endpoint,
        List<String> grant_types_supported,
        List<String> response_types_supported,
        List<String> scopes_supported,
        List<String> capabilities,
        List<String> code_challenge_methods_supported
) {
}
