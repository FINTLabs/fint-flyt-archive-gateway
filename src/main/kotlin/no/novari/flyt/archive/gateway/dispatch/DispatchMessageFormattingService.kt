package no.novari.flyt.archive.gateway.dispatch

import org.springframework.stereotype.Service
import java.util.Optional

@Service
class DispatchMessageFormattingService {
    fun createFunctionalWarningMessage(
        objectDisplayName: String,
        refDisplayName: String,
        objectRefs: List<String>,
    ): Optional<String> =
        Optional.ofNullable(createFunctionalWarningMessageOrNull(objectDisplayName, refDisplayName, objectRefs))

    fun createFunctionalWarningMessageOrNull(
        objectDisplayName: String,
        refDisplayName: String,
        objectRefs: List<String>,
    ): String? {
        if (objectRefs.isEmpty()) {
            return null
        }

        return if (objectRefs.size == 1) {
            "$objectDisplayName with $refDisplayName='${objectRefs.first()}'"
        } else {
            val refs = objectRefs.joinToString("', '", prefix = "['", postfix = "']")
            "${objectDisplayName}s with ${refDisplayName}s=$refs"
        }
    }

    fun formatCaseIdAndJournalpostIds(
        caseId: String,
        journalpostNumbers: List<Long>,
    ): String {
        if (journalpostNumbers.isEmpty()) {
            return caseId
        }

        return caseId + journalpostNumbers.joinToString(",", prefix = "-[", postfix = "]")
    }

    fun combineFunctionalWarningMessages(
        archiveCaseId: String,
        newCase: Boolean,
        functionalWarningMessages: List<String>,
    ): Optional<String> =
        Optional.ofNullable(combineFunctionalWarningMessagesOrNull(archiveCaseId, newCase, functionalWarningMessages))

    fun combineFunctionalWarningMessagesOrNull(
        archiveCaseId: String,
        newCase: Boolean,
        functionalWarningMessages: List<String>,
    ): String? {
        if (!newCase && functionalWarningMessages.isEmpty()) {
            return null
        }

        val messages = mutableListOf<String>()
        if (newCase) {
            messages += "sak with id=$archiveCaseId"
        }
        messages += functionalWarningMessages

        return messages.joinToString(", ", prefix = "(!) Already successfully dispatched ", postfix = " (!)")
    }
}
