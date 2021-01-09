package stolat.model;

import java.util.List;
import java.util.UUID;

public final class TagValidator {

    private TagValidator() {

    }

    private static IllegalArgumentException getInvalidTagException(String tagName, String tagValue, Exception cause) {
        return new IllegalArgumentException("Invalid value for " + tagName + " (" + tagValue + ")", cause);
    }

    static UUID getUUID(String tagName, String tagValue) {
        try {
            if (tagValue != null && !tagValue.isBlank()) {
                return UUID.fromString(tagValue);
            }
        } catch (IllegalArgumentException ex) {
            throw getInvalidTagException(tagName, tagValue, ex);
        }
        throw getInvalidTagException(tagName, tagValue, null);
    }

    static int getPositiveInteger(String tagName, String tagValue) {
        try {
            final int result = Integer.parseInt(tagValue);
            if (result > 0) {
                return result;
            }
        } catch (NumberFormatException ex) {
            throw getInvalidTagException(tagName, tagValue, ex);
        }
        throw getInvalidTagException(tagName, tagValue, null);
    }

    static String getString(String tagName, String tagValue) {
        if (tagValue != null && !tagValue.isBlank()) {
            return tagValue;
        }
        throw getInvalidTagException(tagName, tagValue, null);
    }

    static <T> void checkListsHaveSameSize(List<T> firstList, List<T> secondList, String listsContentDescription) {
        if (firstList.size() != secondList.size()) {
            throw new IllegalArgumentException("Lists containing " + listsContentDescription + " do not have the same size");
        }
    }
}
