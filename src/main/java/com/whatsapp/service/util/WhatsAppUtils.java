package com.whatsapp.service.util;

/**
 * Utility class for WhatsApp Business API related operations
 */
public class WhatsAppUtils {

    private static final int MAX_LIST_ROW_TITLE_LENGTH = 24;
    private static final int MAX_LIST_ROW_DESCRIPTION_LENGTH = 72;
    private static final int MAX_BUTTON_TEXT_LENGTH = 20;
    private static final int MAX_HEADER_TEXT_LENGTH = 60;

    public static String truncateMerchantNameForTitle(String merchantName) {
        if (merchantName == null) {
            return "";
        }
        return merchantName.length() > MAX_LIST_ROW_TITLE_LENGTH 
            ? merchantName.substring(0, MAX_LIST_ROW_TITLE_LENGTH) 
            : merchantName;
    }

    public static String truncateDescriptionForList(String description) {
        if (description == null) {
            return "";
        }
        return description.length() > MAX_LIST_ROW_DESCRIPTION_LENGTH 
            ? description.substring(0, MAX_LIST_ROW_DESCRIPTION_LENGTH) 
            : description;
    }
    public static String createMerchantDescription(String merchantName, String streetName) {
        String description = "Select to pay " + merchantName;
        
        if (streetName != null && !streetName.isEmpty()) {
            description = streetName + " - " + merchantName;
        }
        
        return truncateDescriptionForList(description);
    }

    public static String[] parseWhatsAppPhoneNumber(String webhookPhoneNumber) {
        if (webhookPhoneNumber == null || webhookPhoneNumber.isEmpty()) {
            return new String[]{"", ""};
        }
        
        // Remove any existing + prefix if present
        String cleanNumber = webhookPhoneNumber.startsWith("+") ? webhookPhoneNumber.substring(1) : webhookPhoneNumber;
        
        // Extract only digits
        String digitsOnly = cleanNumber.replaceAll("[^0-9]", "");
        
        if (digitsOnly.length() < 7) {
            // Too short to be a valid international number
            return new String[]{"", digitsOnly};
        }
        
        // Try to match known country code patterns (ordered by specificity - longer codes first)
        String countryCode = "";
        String mobileNumber = "";
        
        // 4-digit country codes (rare, but exist)
        if (digitsOnly.startsWith("1684") || digitsOnly.startsWith("1787") || digitsOnly.startsWith("1939")) {
            countryCode = "+" + digitsOnly.substring(0, 4);
            mobileNumber = digitsOnly.substring(4);
        }
        // 3-digit country codes
        else if (digitsOnly.startsWith("971") || // UAE
                 digitsOnly.startsWith("966") || // Saudi Arabia
                 digitsOnly.startsWith("965") || // Kuwait
                 digitsOnly.startsWith("974") || // Qatar
                 digitsOnly.startsWith("973") || // Bahrain
                 digitsOnly.startsWith("968") || // Oman
                 digitsOnly.startsWith("507") || // Panama
                 digitsOnly.startsWith("506") || // Costa Rica
                 digitsOnly.startsWith("504") || // Honduras
                 digitsOnly.startsWith("503") || // El Salvador
                 digitsOnly.startsWith("502") || // Guatemala
                 digitsOnly.startsWith("501")) { // Belize
            countryCode = "+" + digitsOnly.substring(0, 3);
            mobileNumber = digitsOnly.substring(3);
        }
        // 2-digit country codes
        else if (digitsOnly.startsWith("91") || // India
                 digitsOnly.startsWith("92") || // Pakistan
                 digitsOnly.startsWith("93") || // Afghanistan
                 digitsOnly.startsWith("94") || // Sri Lanka
                 digitsOnly.startsWith("95") || // Myanmar
                 digitsOnly.startsWith("98") || // Iran
                 digitsOnly.startsWith("44") || // UK
                 digitsOnly.startsWith("49") || // Germany
                 digitsOnly.startsWith("33") || // France
                 digitsOnly.startsWith("39") || // Italy
                 digitsOnly.startsWith("34") || // Spain
                 digitsOnly.startsWith("31") || // Netherlands
                 digitsOnly.startsWith("32") || // Belgium
                 digitsOnly.startsWith("41") || // Switzerland
                 digitsOnly.startsWith("43") || // Austria
                 digitsOnly.startsWith("45") || // Denmark
                 digitsOnly.startsWith("46") || // Sweden
                 digitsOnly.startsWith("47") || // Norway
                 digitsOnly.startsWith("48") || // Poland
                 digitsOnly.startsWith("60") || // Malaysia
                 digitsOnly.startsWith("62") || // Indonesia
                 digitsOnly.startsWith("63") || // Philippines
                 digitsOnly.startsWith("64") || // New Zealand
                 digitsOnly.startsWith("65") || // Singapore
                 digitsOnly.startsWith("66") || // Thailand
                 digitsOnly.startsWith("81") || // Japan
                 digitsOnly.startsWith("82") || // South Korea
                 digitsOnly.startsWith("84") || // Vietnam
                 digitsOnly.startsWith("86") || // China
                 digitsOnly.startsWith("90") || // Turkey
                 digitsOnly.startsWith("20") || // Egypt
                 digitsOnly.startsWith("27") || // South Africa
                 digitsOnly.startsWith("52") || // Mexico
                 digitsOnly.startsWith("54") || // Argentina
                 digitsOnly.startsWith("55") || // Brazil
                 digitsOnly.startsWith("56") || // Chile
                 digitsOnly.startsWith("57") || // Colombia
                 digitsOnly.startsWith("58") || // Venezuela
                 digitsOnly.startsWith("61")) { // Australia
            countryCode = "+" + digitsOnly.substring(0, 2);
            mobileNumber = digitsOnly.substring(2);
        }
        // 1-digit country codes (North America)
        else if (digitsOnly.startsWith("1") && digitsOnly.length() >= 11) {
            countryCode = "+1";
            mobileNumber = digitsOnly.substring(1);
        }
        // Default fallback - assume last 10 digits are mobile number (for backward compatibility)
        else if (digitsOnly.length() >= 10) {
            mobileNumber = digitsOnly.substring(digitsOnly.length() - 10);
            String remainingDigits = digitsOnly.substring(0, digitsOnly.length() - 10);
            countryCode = remainingDigits.isEmpty() ? "" : "+" + remainingDigits;
        }
        // If less than 10 digits, return empty country code and the number as mobile number
        else {
            countryCode = "";
            mobileNumber = digitsOnly;
        }
        
        return new String[]{countryCode, mobileNumber};
    }

    public static String formatCardDescription(String productName, String availableBalance) {
        if (productName == null && availableBalance == null) {
            return "Card details";
        }
        
        StringBuilder description = new StringBuilder();
        
        if (productName != null && !productName.isEmpty()) {
            description.append(productName);
        }
        
        if (availableBalance != null && !availableBalance.isEmpty()) {
            try {
                double balance = Double.parseDouble(availableBalance);
                if (description.length() > 0) {
                    description.append(" | ");
                }
                description.append("Balance: $").append(String.format("%.2f", balance));
            } catch (NumberFormatException e) {
                // If balance parsing fails, just append as string
                if (description.length() > 0) {
                    description.append(" | ");
                }
                description.append("Balance: ").append(availableBalance);
            }
        }
        
        return truncateDescriptionForList(description.toString());
    }

    public static String generateUnique6CharString() {
        long timestamp = System.currentTimeMillis();
        String timestampStr = String.valueOf(timestamp);
        
        // Take the last 6 digits to ensure uniqueness and numerical format
        if (timestampStr.length() >= 6) {
            return timestampStr.substring(timestampStr.length() - 6);
        } else {
            // If somehow less than 6 digits, pad with leading zeros
            return String.format("%06d", timestamp);
        }
    }

    public static String getCurrentTimeHHMMSS() {
        java.time.LocalTime now = java.time.LocalTime.now();
        return String.format("%02d%02d%02d", 
            now.getHour(), 
            now.getMinute(), 
            now.getSecond()
        );
    }

    public static String generateUnique12DigitString() {
        long timestamp = System.currentTimeMillis();
        String timestampStr = String.valueOf(timestamp);
        
        // Take the last 12 digits of timestamp
        // Current timestamps are 13 digits, so this gives us a 12-digit unique string
        if (timestampStr.length() >= 12) {
            return timestampStr.substring(timestampStr.length() - 12);
        } else {
            // If somehow less than 12 digits, pad with leading zeros
            return String.format("%012d", timestamp);
        }
    }

    public static String convertToISO8583Amount(String numberString) {
        if (numberString == null || numberString.trim().isEmpty()) {
            return "000000000000";
        }
        
        try {
            // Parse the input as double to handle decimal places
            double amount = Double.parseDouble(numberString.trim());
            
            // Convert to cents (multiply by 100 and round to avoid floating point issues)
            long amountInCents = Math.round(amount * 100);
            
            // Ensure non-negative
            amountInCents = Math.abs(amountInCents);
            
            // Format as 12-digit string with leading zeros
            return String.format("%012d", amountInCents);
            
        } catch (NumberFormatException e) {
            // If parsing fails, return zeros
            return "000000000000";
        }
    }

    /**
     * Gets current date in mmyy format
     * @return Current date as a 4-digit string (e.g., "0925" for September 2025)
     */
    public static String getCurrentDateMMYY() {
        java.time.LocalDate now = java.time.LocalDate.now();
        return String.format("%02d%02d", 
            now.getMonthValue(), 
            now.getYear() % 100
        );
    }

}
