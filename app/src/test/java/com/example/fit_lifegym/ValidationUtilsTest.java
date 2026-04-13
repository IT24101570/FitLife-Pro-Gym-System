package com.example.fit_lifegym;

import com.example.fit_lifegym.utils.ValidationUtils;
import com.example.fit_lifegym.utils.ValidationUtils.ValidationResult;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for ValidationUtils
 */
public class ValidationUtilsTest {

    @Test
    public void testValidEmail() {
        ValidationResult result = ValidationUtils.validateEmail("test@example.com");
        assertTrue(result.isValid());
    }

    @Test
    public void testInvalidEmail() {
        ValidationResult result = ValidationUtils.validateEmail("invalid-email");
        assertFalse(result.isValid());
        assertNotNull(result.getErrorMessage());
    }

    @Test
    public void testEmptyEmail() {
        ValidationResult result = ValidationUtils.validateEmail("");
        assertFalse(result.isValid());
        assertEquals("Email is required", result.getErrorMessage());
    }

    @Test
    public void testValidPassword() {
        ValidationResult result = ValidationUtils.validatePassword("Password123");
        assertTrue(result.isValid());
    }

    @Test
    public void testShortPassword() {
        ValidationResult result = ValidationUtils.validatePassword("12345");
        assertFalse(result.isValid());
        assertTrue(result.getErrorMessage().contains("at least"));
    }

    @Test
    public void testEmptyPassword() {
        ValidationResult result = ValidationUtils.validatePassword("");
        assertFalse(result.isValid());
        assertEquals("Password is required", result.getErrorMessage());
    }

    @Test
    public void testPasswordMatch() {
        ValidationResult result = ValidationUtils.validatePasswordMatch("password123", "password123");
        assertTrue(result.isValid());
    }

    @Test
    public void testPasswordMismatch() {
        ValidationResult result = ValidationUtils.validatePasswordMatch("password123", "password456");
        assertFalse(result.isValid());
        assertEquals("Passwords do not match", result.getErrorMessage());
    }

    @Test
    public void testValidName() {
        ValidationResult result = ValidationUtils.validateName("John Doe");
        assertTrue(result.isValid());
    }

    @Test
    public void testShortName() {
        ValidationResult result = ValidationUtils.validateName("J");
        assertFalse(result.isValid());
        assertTrue(result.getErrorMessage().contains("at least"));
    }

    @Test
    public void testEmptyName() {
        ValidationResult result = ValidationUtils.validateName("");
        assertFalse(result.isValid());
        assertEquals("Name is required", result.getErrorMessage());
    }

    @Test
    public void testValidPhone() {
        ValidationResult result = ValidationUtils.validatePhone("+1234567890");
        assertTrue(result.isValid());
    }

    @Test
    public void testInvalidPhone() {
        ValidationResult result = ValidationUtils.validatePhone("123");
        assertFalse(result.isValid());
        assertTrue(result.getErrorMessage().contains("Invalid"));
    }

    @Test
    public void testValidNumeric() {
        ValidationResult result = ValidationUtils.validateNumeric("123.45", "Weight");
        assertTrue(result.isValid());
    }

    @Test
    public void testInvalidNumeric() {
        ValidationResult result = ValidationUtils.validateNumeric("abc", "Weight");
        assertFalse(result.isValid());
        assertTrue(result.getErrorMessage().contains("valid number"));
    }

    @Test
    public void testValidPositiveNumber() {
        ValidationResult result = ValidationUtils.validatePositiveNumber("10.5", "Height");
        assertTrue(result.isValid());
    }

    @Test
    public void testNegativeNumber() {
        ValidationResult result = ValidationUtils.validatePositiveNumber("-5", "Height");
        assertFalse(result.isValid());
        assertTrue(result.getErrorMessage().contains("greater than 0"));
    }

    @Test
    public void testZeroNumber() {
        ValidationResult result = ValidationUtils.validatePositiveNumber("0", "Height");
        assertFalse(result.isValid());
        assertTrue(result.getErrorMessage().contains("greater than 0"));
    }

    @Test
    public void testRequiredField() {
        ValidationResult result = ValidationUtils.validateRequired("Some value", "Field");
        assertTrue(result.isValid());
    }

    @Test
    public void testEmptyRequiredField() {
        ValidationResult result = ValidationUtils.validateRequired("", "Field");
        assertFalse(result.isValid());
        assertEquals("Field is required", result.getErrorMessage());
    }
}
