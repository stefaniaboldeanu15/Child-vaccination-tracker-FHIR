package org.prt.prtvaccinationtracking_fhir.auth.service;

import ca.uhn.fhir.rest.gclient.TokenClientParam;
import org.hl7.fhir.r5.model.*;
import org.prt.prtvaccinationtracking_fhir.auth.dto.AuthSessionResponse;
import org.prt.prtvaccinationtracking_fhir.auth.dto.RegisterRelatedPersonRequest;
import org.prt.prtvaccinationtracking_fhir.auth.model.AuthenticatedUser;
import org.prt.prtvaccinationtracking_fhir.auth.model.UserRole;
import org.prt.prtvaccinationtracking_fhir.fhir.FhirGateway;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class FhirAuthService {

    public static final String PASSWORD_EXTENSION_URL = "http://example.org/extensions/password";
    public static final String RELATED_PERSON_USERNAME_SYSTEM = "app:login-username";

    private final FhirGateway fhir;
    private final PasswordService passwordService;

    public FhirAuthService(FhirGateway fhir, PasswordService passwordService) {
        this.fhir = fhir;
        this.passwordService = passwordService;
    }

    public Optional<AuthenticatedUser> authenticatePractitioner(String username, String password) {
        Bundle bundle = fhir.client()
                .search()
                .forResource(Practitioner.class)
                .where(new TokenClientParam("identifier").exactly().code(username))
                .returnBundle(Bundle.class)
                .execute();

        for (Practitioner practitioner : resources(bundle, Practitioner.class)) {
            String storedPassword = extensionString(practitioner, PASSWORD_EXTENSION_URL);
            if (!passwordService.matches(password, storedPassword)) {
                continue;
            }

            String practitionerId = practitioner.getIdElement().getIdPart();
            String displayName = practitionerDisplayName(practitioner);
            return Optional.of(new AuthenticatedUser(
                    "Practitioner/" + practitionerId,
                    username,
                    UserRole.PRACTITIONER,
                    displayName,
                    "Practitioner/" + practitionerId,
                    practitionerId,
                    List.of(),
                    List.of()
            ));
        }

        return Optional.empty();
    }

    public Optional<AuthenticatedUser> authenticateRelatedPerson(String username, String password) {
        Bundle bundle = fhir.client()
                .search()
                .forResource(RelatedPerson.class)
                .where(new TokenClientParam("identifier").exactly().systemAndCode(RELATED_PERSON_USERNAME_SYSTEM, username))
                .returnBundle(Bundle.class)
                .execute();

        List<RelatedPerson> matches = new ArrayList<>();
        for (RelatedPerson relatedPerson : resources(bundle, RelatedPerson.class)) {
            String storedPassword = extensionString(relatedPerson, PASSWORD_EXTENSION_URL);
            if (passwordService.matches(password, storedPassword)) {
                matches.add(relatedPerson);
            }
        }

        if (matches.isEmpty()) {
            return Optional.empty();
        }

        Set<String> patientIds = new LinkedHashSet<>();
        Set<String> relatedPersonIds = new LinkedHashSet<>();
        for (RelatedPerson relatedPerson : matches) {
            relatedPersonIds.add(relatedPerson.getIdElement().getIdPart());
            if (relatedPerson.hasPatient() && relatedPerson.getPatient().hasReference()) {
                String[] parts = relatedPerson.getPatient().getReference().split("/");
                patientIds.add(parts[parts.length - 1]);
            }
        }

        RelatedPerson primary = matches.stream()
                .sorted(Comparator.comparing(r -> r.getIdElement().getIdPart()))
                .findFirst()
                .orElse(matches.get(0));

        String relatedPersonId = primary.getIdElement().getIdPart();
        return Optional.of(new AuthenticatedUser(
                "RelatedPerson/" + relatedPersonId,
                username,
                UserRole.RELATED_PERSON,
                relatedPersonDisplayName(primary),
                "RelatedPerson/" + relatedPersonId,
                null,
                List.copyOf(relatedPersonIds),
                List.copyOf(patientIds)
        ));
    }

    public AuthSessionResponse buildSessionResponse(AuthenticatedUser user) {
        return new AuthSessionResponse(
                user.username(),
                user.role().externalValue(),
                user.displayName(),
                user.fhirUser(),
                user.practitionerId(),
                user.relatedPersonIds(),
                user.patientIds()
        );
    }

    public AuthSessionResponse sessionFromJwt(org.springframework.security.oauth2.jwt.Jwt jwt) {
        return new AuthSessionResponse(
                jwt.getClaimAsString("username"),
                jwt.getClaimAsString("role"),
                jwt.getClaimAsString("display_name"),
                jwt.getClaimAsString("fhirUser"),
                jwt.getClaimAsString("practitioner_id"),
                stringList(jwt, "related_person_ids"),
                stringList(jwt, "patient_ids")
        );
    }

    public RelatedPerson registerRelatedPerson(RegisterRelatedPersonRequest request) {
        RelatedPerson resource = new RelatedPerson();

        if (request.patientId() != null && !request.patientId().isBlank()) {
            resource.setPatient(new Reference("Patient/" + request.patientId()));
        }

        if (request.firstName() != null || request.lastName() != null) {
            HumanName name = new HumanName();
            if (request.firstName() != null && !request.firstName().isBlank()) {
                name.addGiven(request.firstName());
            }
            if (request.lastName() != null && !request.lastName().isBlank()) {
                name.setFamily(request.lastName());
            }
            resource.addName(name);
        }

        if (request.relationship() != null && !request.relationship().isBlank()) {
            resource.addRelationship(new CodeableConcept().setText(request.relationship()));
        }

        if (request.phone() != null && !request.phone().isBlank()) {
            resource.addTelecom(new ContactPoint()
                    .setSystem(ContactPoint.ContactPointSystem.PHONE)
                    .setValue(request.phone()));
        }

        if (request.email() != null && !request.email().isBlank()) {
            resource.addTelecom(new ContactPoint()
                    .setSystem(ContactPoint.ContactPointSystem.EMAIL)
                    .setValue(request.email()));
        }

        if (request.address() != null && !request.address().isBlank()) {
            resource.addAddress(new Address().setText(request.address()));
        }

        resource.addIdentifier(new Identifier()
                .setSystem(RELATED_PERSON_USERNAME_SYSTEM)
                .setValue(request.username()));

        resource.addExtension(new Extension(PASSWORD_EXTENSION_URL, new StringType(passwordService.encode(request.password()))));
        return fhir.create(resource);
    }


    private List<String> stringList(org.springframework.security.oauth2.jwt.Jwt jwt, String claim) {
        List<String> values = jwt.getClaimAsStringList(claim);
        return values == null ? List.of() : values;
    }

    private String practitionerDisplayName(Practitioner practitioner) {
        if (!practitioner.hasName()) {
            return practitioner.getIdElement().getIdPart();
        }

        HumanName name = practitioner.getNameFirstRep();
        String given = name.hasGiven() && !name.getGiven().isEmpty() ? name.getGiven().get(0).getValue() : null;
        String family = name.hasFamily() ? name.getFamily() : null;

        if (given == null && family == null) {
            return practitioner.getIdElement().getIdPart();
        }
        if (given == null) {
            return family;
        }
        if (family == null) {
            return given;
        }
        return given + " " + family;
    }

    private String relatedPersonDisplayName(RelatedPerson relatedPerson) {
        if (!relatedPerson.hasName()) {
            return relatedPerson.getIdElement().getIdPart();
        }

        HumanName name = relatedPerson.getNameFirstRep();
        String given = name.hasGiven() && !name.getGiven().isEmpty() ? name.getGiven().get(0).getValue() : null;
        String family = name.hasFamily() ? name.getFamily() : null;

        if (given == null && family == null) {
            return relatedPerson.getIdElement().getIdPart();
        }
        if (given == null) {
            return family;
        }
        if (family == null) {
            return given;
        }
        return given + " " + family;
    }

    private String extensionString(DomainResource resource, String url) {
        if (resource == null || !resource.hasExtension()) {
            return null;
        }

        for (Extension extension : resource.getExtension()) {
            if (extension != null && url.equals(extension.getUrl()) && extension.hasValue()) {
                return extension.getValue().primitiveValue();
            }
        }

        return null;
    }

    private <T extends Resource> List<T> resources(Bundle bundle, Class<T> type) {
        List<T> results = new ArrayList<>();
        if (bundle == null || !bundle.hasEntry()) {
            return results;
        }

        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            if (entry == null || !entry.hasResource()) {
                continue;
            }

            Resource resource = entry.getResource();
            if (type.isInstance(resource)) {
                results.add(type.cast(resource));
            }
        }

        return results;
    }
}
