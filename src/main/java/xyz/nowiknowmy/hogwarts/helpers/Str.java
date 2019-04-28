package xyz.nowiknowmy.hogwarts.helpers;

import java.util.regex.Pattern;

public class Str {
    private Pattern pattern;

    private Str(Pattern pattern) {
        this.pattern = pattern;
    }

    public boolean matches(String string) {
        return pattern.matcher(string).find();
    }

    public static Str regex(String regex) {
        return new Str(Pattern.compile(regex));
    }
}
