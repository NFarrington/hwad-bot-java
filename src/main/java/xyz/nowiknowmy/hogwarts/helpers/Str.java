package xyz.nowiknowmy.hogwarts.helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Str {
    private Pattern pattern;

    private Str(Pattern pattern) {
        this.pattern = pattern;
    }

    public boolean matches(String string) {
        return pattern.matcher(string).find();
    }

    public List<String> groups(String string) {
        Matcher m = pattern.matcher(string);
        List<String> groups = new ArrayList<>();
        if (m.find()) {
            for (int i = 0; i <= m.groupCount(); i++) {
                groups.add(m.group(i));
            }
        }

        return groups;
    }

    public static Str regex(String regex) {
        return new Str(Pattern.compile(regex));
    }

    public static Str regex(String regex, int flags) {
        return new Str(Pattern.compile(regex, flags));
    }
}
