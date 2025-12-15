package no.novari.flyt.archive.gateway.dispatch;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

import static java.util.stream.Collectors.joining;

@Service
public class DispatchMessageFormattingService {

    public Optional<String> createFunctionalWarningMessage(
            String objectDisplayName,
            String refDisplayName,
            List<String> objectRefs
    ) {
        if (objectRefs.isEmpty()) {
            return Optional.empty();
        }
        if (objectRefs.size() == 1) {
            return Optional.of(
                    objectDisplayName + " with " + refDisplayName + "='" + objectRefs.getFirst() + "'"
            );
        }
        return Optional.of(
                objectDisplayName + "s with " + refDisplayName + "s=" + objectRefs
                        .stream()
                        .collect(joining("', '", "['", "']"))
        );
    }

    public String formatCaseIdAndJournalpostIds(String caseId, List<Long> journalpostNumbers) {
        if (journalpostNumbers.isEmpty()) {
            return caseId;
        }
        return caseId + journalpostNumbers
                .stream()
                .map(Object::toString)
                .collect(joining(",", "-[", "]"));
    }

    public Optional<String> combineFunctionalWarningMessages(
            String archiveCaseId,
            boolean newCase,
            List<String> functionalWarningMessages
    ) {
        if (!newCase && functionalWarningMessages.isEmpty()) {
            return Optional.empty();
        }

        StringJoiner stringJoiner = new StringJoiner(", ", "(!) Already successfully dispatched ", " (!)");
        if (newCase) {
            stringJoiner.add("sak with id=" + archiveCaseId);
        }
        functionalWarningMessages.forEach(stringJoiner::add);

        return Optional.of(stringJoiner.toString());
    }

}
