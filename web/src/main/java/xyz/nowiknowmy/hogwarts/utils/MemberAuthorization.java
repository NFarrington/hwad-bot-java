package xyz.nowiknowmy.hogwarts.utils;

import discord4j.core.object.entity.Role;
import discord4j.core.object.util.Permission;

import java.util.Arrays;
import java.util.List;

public class MemberAuthorization {

    private MemberAuthorization() {
        throw new IllegalStateException("Utility class");
    }

    public static boolean canModifyPoints(List<Role> roles) {
        return roles.stream().anyMatch(role ->
            role.getPermissions().contains(Permission.ADMINISTRATOR)
                || Arrays.asList("Professors", "Prefects").contains(role.getName()));
    }

    public static boolean canListInactive(List<Role> roles) {
        return roles.stream().anyMatch(role -> role.getPermissions().contains(Permission.ADMINISTRATOR));
    }

    public static boolean canBumpYears(List<Role> roles) {
        return roles.stream().anyMatch(role -> role.getPermissions().contains(Permission.ADMINISTRATOR));
    }

}
