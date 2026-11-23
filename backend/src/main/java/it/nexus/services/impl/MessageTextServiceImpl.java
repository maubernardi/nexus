package it.nexus.services.impl;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.nexus.domain.MessageText;
import it.nexus.domain.audit.AuditChanges;
import it.nexus.domain.dto.MessageTextDTO;
import it.nexus.domain.dto.MessageTextPartDTO;
import it.nexus.domain.enumeration.AuditEntityType;
import it.nexus.domain.enumeration.MessageKey;
import it.nexus.domain.enumeration.MessagePart;
import it.nexus.repository.MessageTextRepository;
import it.nexus.services.AuditService;
import it.nexus.services.MessageTextService;
import it.nexus.services.messages.MessageTexts;
import it.nexus.web.errors.FieldValidationException;
import it.nexus.web.errors.NotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class MessageTextServiceImpl implements MessageTextService {

    private static final String FIELD = "text";

    private final MessageTextRepository repository;
    private final AuditService auditService;

    @Override
    @Transactional(readOnly = true)
    public MessageTexts current() {
        Map<MessageKey, Map<MessagePart, String>> custom = new EnumMap<>(MessageKey.class);
        for (MessageText t : repository.findAll()) {
            custom.computeIfAbsent(t.getMessage(), k -> new EnumMap<>(MessagePart.class)).put(t.getPart(), t.getText());
        }
        return new MessageTexts(custom);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MessageTextDTO> list() {
        MessageTexts texts = current();
        return Arrays.stream(MessageKey.values()).map(key -> toDto(key, texts)).toList();
    }

    @Override
    public MessageTextDTO update(String keyName, String partName, String text) {
        MessageKey key = key(keyName);
        MessagePart part = part(key, partName);
        String value = normalize(part, text);
        validate(key, part, value);
        if (value.equals(MessageTexts.defaultText(key, part))) {
            return reset(key, part);
        }
        MessageText row = repository.findByMessageAndPart(key, part).orElse(null);
        String before = row != null ? row.getText() : MessageTexts.defaultText(key, part);
        if (row == null) {
            row = repository.saveAndFlush(new MessageText(key, part, value));
        } else if (!value.equals(row.getText())) {
            row.setText(value);
            repository.saveAndFlush(row);
        } else {
            return toDto(key, current());
        }
        auditService.record(AuditEntityType.MESSAGE_TEXT, row.getId(), "TEXT_CHANGED", AuditChanges.of()
                .value("message", null, key.name())
                .value("part", null, part.name())
                .value("text", before, value), null);
        return toDto(key, current());
    }

    @Override
    public MessageTextDTO reset(String keyName, String partName) {
        MessageKey key = key(keyName);
        return reset(key, part(key, partName));
    }

    private MessageTextDTO reset(MessageKey key, MessagePart part) {
        repository.findByMessageAndPart(key, part).ifPresent(row -> {
            repository.delete(row);
            repository.flush();
            auditService.record(AuditEntityType.MESSAGE_TEXT, row.getId(), "TEXT_RESET", AuditChanges.of()
                    .value("message", null, key.name())
                    .value("part", null, part.name())
                    .value("text", row.getText(), MessageTexts.defaultText(key, part)), null);
        });
        return toDto(key, current());
    }

    private static MessageTextDTO toDto(MessageKey key, MessageTexts texts) {
        List<MessageTextPartDTO> parts = key.parts().stream().map(part -> {
            String defaultText = MessageTexts.defaultText(key, part);
            String text = texts.template(key, part);
            return new MessageTextPartDTO(part, text, defaultText, !text.equals(defaultText));
        }).toList();
        return new MessageTextDTO(key, key.isNotification(), key.placeholders(), parts);
    }

    private static MessageKey key(String name) {
        try {
            return MessageKey.valueOf(name);
        } catch (IllegalArgumentException e) {
            throw new NotFoundException("Messaggio non trovato");
        }
    }

    private static MessagePart part(MessageKey key, String name) {
        try {
            MessagePart part = MessagePart.valueOf(name);
            if (key.parts().contains(part)) {
                return part;
            }
        } catch (IllegalArgumentException e) {
            // come una parte che il messaggio non ha
        }
        throw new NotFoundException("Il messaggio non ha questa parte");
    }

    /** Spazi tolti ai bordi; nell'oggetto e nel testo dell'app anche gli a capo diventano spazi. */
    private static String normalize(MessagePart part, String text) {
        String value = text == null ? "" : text.strip();
        return part == MessagePart.EMAIL_BODY ? value.replace("\r\n", "\n") : value.replaceAll("\\s+", " ");
    }

    private static void validate(MessageKey key, MessagePart part, String value) {
        if (value.isEmpty()) {
            throw new FieldValidationException(FIELD, "Scrivi il testo");
        }
        int max = switch (part) {
            case APP -> 500;
            case EMAIL_SUBJECT -> 100;
            case EMAIL_BODY -> 4000;
        };
        if (value.length() > max) {
            throw new FieldValidationException(FIELD, "Massimo %d caratteri".formatted(max));
        }
        Set<String> allowed = new HashSet<>(key.placeholders());
        Matcher m = MessageTexts.PLACEHOLDER.matcher(value);
        while (m.find()) {
            if (!allowed.contains(m.group(1))) {
                throw new FieldValidationException(FIELD, allowed.isEmpty()
                        ? "Questo testo non ammette segnaposto: togli %s".formatted(m.group())
                        : "Segnaposto sconosciuto %s: si possono usare %s".formatted(m.group(), key.placeholders()
                                .stream().map(p -> "{" + p + "}").reduce((a, b) -> a + ", " + b).orElse("")));
            }
        }
        String rest = MessageTexts.PLACEHOLDER.matcher(value).replaceAll("");
        if (rest.indexOf('{') >= 0 || rest.indexOf('}') >= 0) {
            throw new FieldValidationException(FIELD, "Le parentesi graffe servono solo per i segnaposto");
        }
    }
}
