package com.example.fit_lifegym.utils;

import android.text.TextUtils;
import android.util.Patterns;
import java.util.Calendar;
import java.util.regex.Pattern;

/**
 * Utility class for input validation across the app
 */
public class ValidationUtils {

    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_PASSWORD_LENGTH = 128;
    private static final int MIN_NAME_LENGTH = 2;
    private static final int MAX_NAME_LENGTH = 50;

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$");
    
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[+]?[0-9]{10,15}$");
    private static final Pattern EXPIRY_PATTERN = Pattern.compile("^(0[1-9]|1[0-2])/[0-9]{2}$");
    private static final Pattern MEDICAL_LICENSE_PATTERN = Pattern.compile("^[A-Z0-9-]{5,20}$");
    private static final Pattern ALPHABETIC_PATTERN = Pattern.compile("^[a-zA-Z\\s.]+$");
    private static final Pattern ACCOUNT_NUMBER_PATTERN = Pattern.compile("^[0-9]{8,18}$");

    /**
     * Validates email format
     */
    public static ValidationResult validateEmail(String email) {
        if (TextUtils.isEmpty(email)) {
            return ValidationResult.error("Email is required");
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            return ValidationResult.error("Invalid email format");
        }
        return ValidationResult.success();
    }

    public static boolean isValidEmail(String email) {
        return validateEmail(email).isValid();
    }

    /**
     * Validates password strength
     */
    public static ValidationResult validatePassword(String password) {
        if (TextUtils.isEmpty(password)) {
            return ValidationResult.error("Password is required");
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            return ValidationResult.error("Password must be at least " + MIN_PASSWORD_LENGTH + " characters");
        }
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            return ValidationResult.error("Password must contain at least one digit, one lowercase, and one uppercase letter");
        }
        return ValidationResult.success();
    }

    public static boolean isStrongPassword(String password) {
        return validatePassword(password).isValid();
    }

    /**
     * Validates password confirmation
     */
    public static ValidationResult validatePasswordMatch(String password, String confirmPassword) {
        if (TextUtils.isEmpty(confirmPassword)) {
            return ValidationResult.error("Please confirm your password");
        }
        if (!password.equals(confirmPassword)) {
            return ValidationResult.error("Passwords do not match");
        }
        return ValidationResult.success();
    }

    /**
     * Validates name
     */
    public static ValidationResult validateName(String name) {
        if (TextUtils.isEmpty(name)) {
            return ValidationResult.error("Name is required");
        }
        String trimmedName = name.trim();
        if (trimmedName.length() < MIN_NAME_LENGTH) {
            return ValidationResult.error("Name must be at least " + MIN_NAME_LENGTH + " characters");
        }
        if (trimmedName.length() > MAX_NAME_LENGTH) {
            return ValidationResult.error("Name is too long");
        }
        if (!ALPHABETIC_PATTERN.matcher(trimmedName).matches()) {
            return ValidationResult.error("Name should only contain letters");
        }
        return ValidationResult.success();
    }

    public static boolean isValidName(String name) {
        return validateName(name).isValid();
    }

    /**
     * Validates phone number
     */
    public static ValidationResult validatePhone(String phone) {
        if (TextUtils.isEmpty(phone)) {
            return ValidationResult.error("Phone number is required");
        }
        if (!PHONE_PATTERN.matcher(phone.trim()).matches()) {
            return ValidationResult.error("Invalid phone number format");
        }
        return ValidationResult.success();
    }

    /**
     * Validates medical license number
     */
    public static ValidationResult validateMedicalLicense(String license) {
        if (TextUtils.isEmpty(license)) {
            return ValidationResult.error("Medical license is required for doctors");
        }
        String trimmed = license.trim();
        if (!MEDICAL_LICENSE_PATTERN.matcher(trimmed).matches()) {
            return ValidationResult.error("Invalid medical license format (use 5-20 uppercase letters, numbers, or dashes)");
        }
        return ValidationResult.success();
    }

    /**
     * Validates medical degree
     */
    public static ValidationResult validateMedicalDegree(String degree) {
        if (TextUtils.isEmpty(degree)) {
            return ValidationResult.error("Medical degree is required (e.g., MBBS, MD)");
        }
        String trimmed = degree.trim();
        if (trimmed.length() < 2) {
            return ValidationResult.error("Medical degree is too short");
        }
        if (!ALPHABETIC_PATTERN.matcher(trimmed).matches()) {
            return ValidationResult.error("Degree should only contain letters and dots");
        }
        return ValidationResult.success();
    }

    /**
     * Validates specialization
     */
    public static ValidationResult validateSpecialization(String specialization) {
        if (TextUtils.isEmpty(specialization)) {
            return ValidationResult.error("Specialization is required");
        }
        String trimmed = specialization.trim();
        if (trimmed.length() < 3) {
            return ValidationResult.error("Specialization name is too short");
        }
        if (!ALPHABETIC_PATTERN.matcher(trimmed).matches()) {
            return ValidationResult.error("Specialization should only contain letters");
        }
        return ValidationResult.success();
    }

    /**
     * Validates clinic/hospital name
     */
    public static ValidationResult validateClinicName(String clinic) {
        if (TextUtils.isEmpty(clinic)) {
            return ValidationResult.error("Clinic or Hospital name is required");
        }
        if (clinic.trim().length() < 3) {
            return ValidationResult.error("Clinic name is too short");
        }
        return ValidationResult.success();
    }

    /**
     * Validates bank account number
     */
    public static ValidationResult validateAccountNumber(String accountNumber) {
        if (TextUtils.isEmpty(accountNumber)) {
            return ValidationResult.error("Account number is required");
        }
        if (!ACCOUNT_NUMBER_PATTERN.matcher(accountNumber.trim()).matches()) {
            return ValidationResult.error("Invalid account number (8-18 digits)");
        }
        return ValidationResult.success();
    }

    /**
     * Validates bank account name
     */
    public static ValidationResult validateAccountName(String accountName) {
        if (TextUtils.isEmpty(accountName)) {
            return ValidationResult.error("Account holder name is required");
        }
        if (accountName.trim().length() < 3) {
            return ValidationResult.error("Account name is too short");
        }
        if (!ALPHABETIC_PATTERN.matcher(accountName.trim()).matches()) {
            return ValidationResult.error("Account name should only contain letters");
        }
        return ValidationResult.success();
    }

    /**
     * Validates bank name
     */
    public static ValidationResult validateBankName(String bankName) {
        if (TextUtils.isEmpty(bankName)) {
            return ValidationResult.error("Bank name is required");
        }
        if (bankName.trim().length() < 2) {
            return ValidationResult.error("Bank name is too short");
        }
        return ValidationResult.success();
    }

    /**
     * Validates credit card number (Luhn algorithm)
     */
    public static ValidationResult validateCardNumber(String cardNumber) {
        if (TextUtils.isEmpty(cardNumber)) {
            return ValidationResult.error("Card number is required");
        }
        String cleaned = cardNumber.replaceAll("\\s", "");
        if (cleaned.length() < 13 || cleaned.length() > 19) {
            return ValidationResult.error("Invalid card number length");
        }

        try {
            int sum = 0;
            boolean alternate = false;
            for (int i = cleaned.length() - 1; i >= 0; i--) {
                int n = Integer.parseInt(cleaned.substring(i, i + 1));
                if (alternate) {
                    n *= 2;
                    if (n > 9) {
                        n = (n % 10) + 1;
                    }
                }
                sum += n;
                alternate = !alternate;
            }
            if (sum % 10 != 0) {
                return ValidationResult.error("Invalid card number");
            }
        } catch (NumberFormatException e) {
            return ValidationResult.error("Card number must contain only digits");
        }
        return ValidationResult.success();
    }

    /**
     * Validates card expiry date (MM/YY)
     */
    public static ValidationResult validateExpiryDate(String expiry) {
        if (TextUtils.isEmpty(expiry)) {
            return ValidationResult.error("Expiry date is required");
        }
        if (!EXPIRY_PATTERN.matcher(expiry.trim()).matches()) {
            return ValidationResult.error("Use MM/YY format");
        }

        try {
            String[] parts = expiry.split("/");
            int month = Integer.parseInt(parts[0]);
            int year = Integer.parseInt("20" + parts[1]);

            Calendar calendar = Calendar.getInstance();
            int currentMonth = calendar.get(Calendar.MONTH) + 1;
            int currentYear = calendar.get(Calendar.YEAR);

            if (year < currentYear || (year == currentYear && month < currentMonth)) {
                return ValidationResult.error("Card has expired");
            }
        } catch (NumberFormatException e) {
            return ValidationResult.error("Invalid date format");
        }
        return ValidationResult.success();
    }

    /**
     * Validates CVV
     */
    public static ValidationResult validateCVV(String cvv) {
        if (TextUtils.isEmpty(cvv)) {
            return ValidationResult.error("CVV is required");
        }
        if (cvv.length() < 3 || cvv.length() > 4) {
            return ValidationResult.error("Invalid CVV");
        }
        return ValidationResult.success();
    }

    // --- FITNESS VALIDATIONS ---

    public static boolean isValidCalorie(String calorieStr) {
        try {
            int cal = Integer.parseInt(calorieStr);
            return cal >= 0 && cal <= 5000;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidMacro(String macroStr) {
        try {
            double macro = Double.parseDouble(macroStr);
            return macro >= 0 && macro <= 500;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidWeight(String weightStr) {
        try {
            double weight = Double.parseDouble(weightStr);
            return weight >= 30 && weight <= 300;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidHeight(String heightStr) {
        try {
            double height = Double.parseDouble(heightStr);
            return height >= 100 && height <= 250;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidAge(String ageStr) {
        try {
            int age = Integer.parseInt(ageStr);
            return age >= 12 && age <= 100;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidPostContent(String content) {
        return !TextUtils.isEmpty(content) && content.trim().length() <= 500;
    }

    public static boolean isValidReps(String repsStr) {
        try {
            int reps = Integer.parseInt(repsStr);
            return reps > 0 && reps <= 100;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidSets(String setsStr) {
        try {
            int sets = Integer.parseInt(setsStr);
            return sets > 0 && sets <= 20;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidExerciseName(String name) {
        return !TextUtils.isEmpty(name) && name.trim().length() >= 3;
    }

    // --- ADDITIONAL VALIDATION METHODS FOR TESTS ---

    /**
     * Validates numeric input (decimal numbers allowed)
     */
    public static ValidationResult validateNumeric(String value, String fieldName) {
        if (TextUtils.isEmpty(value)) {
            return ValidationResult.error(fieldName + " is required");
        }
        try {
            Double.parseDouble(value.trim());
            return ValidationResult.success();
        } catch (NumberFormatException e) {
            return ValidationResult.error(fieldName + " must be a valid number");
        }
    }

    /**
     * Validates positive numbers (greater than 0)
     */
    public static ValidationResult validatePositiveNumber(String value, String fieldName) {
        ValidationResult numericResult = validateNumeric(value, fieldName);
        if (!numericResult.isValid()) {
            return numericResult;
        }
        
        try {
            double number = Double.parseDouble(value.trim());
            if (number <= 0) {
                return ValidationResult.error(fieldName + " must be greater than 0");
            }
            return ValidationResult.success();
        } catch (NumberFormatException e) {
            return ValidationResult.error(fieldName + " must be a valid positive number");
        }
    }

    /**
     * Validates required field (non-empty)
     */
    public static ValidationResult validateRequired(String value, String fieldName) {
        if (TextUtils.isEmpty(value) || value.trim().isEmpty()) {
            return ValidationResult.error(fieldName + " is required");
        }
        return ValidationResult.success();
    }

    /**
     * Result class for validation
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;

        private ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }

        public static ValidationResult success() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult error(String message) {
            return new ValidationResult(false, message);
        }

        public boolean isValid() {
            return valid;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
}
