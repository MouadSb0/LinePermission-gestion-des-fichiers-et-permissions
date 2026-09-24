package ma.youcode.lineperm.util;

import ma.youcode.lineperm.dao.LogDao;
import ma.youcode.lineperm.model.AccessLog;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

/**
 * Imports legacy audit lines into the database.
 *
 * Expected format:
 * userId|fileId|action|result|occurredAt|details
 *
 * fileId and details may be empty. occurredAt must be an ISO-8601
 * LocalDateTime, for example 2026-09-24T11:30:00.
 */
public final class AccessLogImporter {
    private AccessLogImporter() {
    }

    public static ImportReport importFile(Path source, LogDao logDao)
            throws IOException, SQLException {
        int imported = 0;
        int skipped = 0;
        int lineNumber = 0;

        try (BufferedReader reader = Files.newBufferedReader(
                source, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) {
                    continue;
                }

                try {
                    logDao.save(parse(line));
                    imported++;
                } catch (IllegalArgumentException e) {
                    skipped++;
                    System.err.println("Ligne ignorée (" + lineNumber + ") : "
                            + e.getMessage());
                }
            }
        }

        return new ImportReport(imported, skipped);
    }

    private static AccessLog parse(String line) {
        String[] fields = line.split("\\|", -1);
        if (fields.length < 5 || fields.length > 6) {
            throw new IllegalArgumentException(
                    "format attendu: userId|fileId|action|result|occurredAt|details"
            );
        }

        int userId = parsePositiveInteger(fields[0], "userId");
        Integer fileId = fields[1].trim().isEmpty()
                ? null
                : parsePositiveInteger(fields[1], "fileId");
        String action = required(fields[2], "action");
        String result = required(fields[3], "result").toUpperCase();
        if (!"ACCEPTE".equals(result) && !"REFUSE".equals(result)) {
            throw new IllegalArgumentException("result doit être ACCEPTE ou REFUSE");
        }

        LocalDateTime occurredAt;
        try {
            occurredAt = LocalDateTime.parse(required(fields[4], "occurredAt"));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("occurredAt doit être une date ISO-8601");
        }

        String details = fields.length == 6 && !fields[5].trim().isEmpty()
                ? fields[5].trim()
                : null;
        return new AccessLog(userId, fileId, action, result, occurredAt, details);
    }

    private static int parsePositiveInteger(String value, String fieldName) {
        try {
            int parsed = Integer.parseInt(value.trim());
            if (parsed <= 0) {
                throw new IllegalArgumentException(fieldName + " doit être positif");
            }
            return parsed;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(fieldName + " doit être un entier");
        }
    }

    private static String required(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " ne peut pas être vide");
        }
        return value.trim();
    }

    public static final class ImportReport {
        private final int imported;
        private final int skipped;

        public ImportReport(int imported, int skipped) {
            this.imported = imported;
            this.skipped = skipped;
        }

        public int getImported() {
            return imported;
        }

        public int getSkipped() {
            return skipped;
        }
    }
}
