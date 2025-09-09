package com.whatsapp.service.util;

/**
 * Utility class for WhatsApp Business API related operations
 */
public class WhatsAppUtils {
    
    // WhatsApp Business API limits
    private static final int MAX_LIST_ROW_TITLE_LENGTH = 24;
    private static final int MAX_LIST_ROW_DESCRIPTION_LENGTH = 72;
    private static final int MAX_BUTTON_TEXT_LENGTH = 20;
    private static final int MAX_HEADER_TEXT_LENGTH = 60;
    
    /**
     * Truncates merchant name to fit WhatsApp list row title limit (24 characters)
     * 
     * @param merchantName The original merchant name
     * @return Truncated merchant name if longer than 24 characters, otherwise original name
     */
    public static String truncateMerchantNameForTitle(String merchantName) {
        if (merchantName == null) {
            return "";
        }
        return merchantName.length() > MAX_LIST_ROW_TITLE_LENGTH 
            ? merchantName.substring(0, MAX_LIST_ROW_TITLE_LENGTH) 
            : merchantName;
    }
    
    /**
     * Truncates description text to fit WhatsApp list row description limit (72 characters)
     * 
     * @param description The original description text
     * @return Truncated description if longer than 72 characters, otherwise original description
     */
    public static String truncateDescriptionForList(String description) {
        if (description == null) {
            return "";
        }
        return description.length() > MAX_LIST_ROW_DESCRIPTION_LENGTH 
            ? description.substring(0, MAX_LIST_ROW_DESCRIPTION_LENGTH) 
            : description;
    }
    
    /**
     * Truncates button text to fit WhatsApp button text limit (20 characters)
     * 
     * @param buttonText The original button text
     * @return Truncated button text if longer than 20 characters, otherwise original text
     */
    public static String truncateButtonText(String buttonText) {
        if (buttonText == null) {
            return "";
        }
        return buttonText.length() > MAX_BUTTON_TEXT_LENGTH 
            ? buttonText.substring(0, MAX_BUTTON_TEXT_LENGTH) 
            : buttonText;
    }
    
    /**
     * Truncates header text to fit WhatsApp header text limit (60 characters)
     * 
     * @param headerText The original header text
     * @return Truncated header text if longer than 60 characters, otherwise original text
     */
    public static String truncateHeaderText(String headerText) {
        if (headerText == null) {
            return "";
        }
        return headerText.length() > MAX_HEADER_TEXT_LENGTH 
            ? headerText.substring(0, MAX_HEADER_TEXT_LENGTH) 
            : headerText;
    }
    
    /**
     * Creates a merchant description with full name and street name, truncated to WhatsApp limits
     * 
     * @param merchantName The merchant name
     * @param streetName The street name (can be null)
     * @return Formatted and truncated description
     */
    public static String createMerchantDescription(String merchantName, String streetName) {
        String description = "Select to pay " + merchantName;
        
        if (streetName != null && !streetName.isEmpty()) {
            description = streetName + " - " + merchantName;
        }
        
        return truncateDescriptionForList(description);
    }
}
