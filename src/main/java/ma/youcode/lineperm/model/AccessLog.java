package ma.youcode.lineperm.model;

import java.time.LocalDateTime;

public final class AccessLog {
    private final int id;
    private final int userId;
    private final Integer fichierId;
    private final String action;
    private final String result;
    private final LocalDateTime occurredAt;
    private final String details;

    public AccessLog(
            int userId,
            Integer fichierId,
            String action,
            String result,
            LocalDateTime occurredAt,
            String details
    ) {
        this(0, userId, fichierId, action, result, occurredAt, details);
    }

    public AccessLog(
            int id,
            int userId,
            Integer fichierId,
            String action,
            String result,
            LocalDateTime occurredAt,
            String details
    ) {
        this.id = id;
        this.userId = userId;
        this.fichierId = fichierId;
        this.action = action;
        this.result = result;
        this.occurredAt = occurredAt;
        this.details = details;
    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public Integer getFichierId() {
        return fichierId;
    }

    public String getAction() {
        return action;
    }

    public String getResult() {
        return result;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public String getDetails() {
        return details;
    }
}
