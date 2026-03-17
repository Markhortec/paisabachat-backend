package com.hamza.paisabachat.backend.domain.exception;

public class BusinessException extends BaseException {

    public BusinessException(String message, String errorCode) {
        super(message, errorCode, 400);
    }

    public BusinessException(String message) {
        super(message, "BUSINESS_ERROR", 400);
    }

    // ── Common Business Exceptions ──
    public static BusinessException goalLimitReached() {
        return new BusinessException(
                "Free tier goal limit reached. Upgrade to Premium for unlimited goals.",
                "GOAL_LIMIT_REACHED"
        );
    }

    public static BusinessException goalAlreadyCompleted() {
        return new BusinessException(
                "Goal is already completed.",
                "GOAL_ALREADY_COMPLETED"
        );
    }

    public static BusinessException invalidContributionAmount() {
        return new BusinessException(
                "Contribution amount must be greater than zero.",
                "INVALID_AMOUNT"
        );
    }

    public static BusinessException futureDateNotAllowed() {
        return new BusinessException(
                "Future contribution dates are not allowed.",
                "FUTURE_DATE_NOT_ALLOWED"
        );
    }
}