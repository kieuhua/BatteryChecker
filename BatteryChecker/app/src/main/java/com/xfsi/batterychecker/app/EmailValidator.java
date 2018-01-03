package com.xfsi.batterychecker.app;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by local-kieu on 8/19/14.
 */
public class EmailValidator {
    private Pattern pattern;
    private Matcher matcher;

    private static final String EMAIL_PATTERN = "\\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}\\b";

    public EmailValidator() {
        pattern = Pattern.compile(EMAIL_PATTERN, Pattern.CASE_INSENSITIVE);
    }

    public boolean validate(String email) {
        matcher = pattern.matcher(email);
        return matcher.matches();
    }
}
