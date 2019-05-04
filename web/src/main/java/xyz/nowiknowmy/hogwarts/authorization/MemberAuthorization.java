package xyz.nowiknowmy.hogwarts.authorization;

import discord4j.core.object.entity.Member;
import discord4j.core.object.util.Permission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.Arrays;

public class MemberAuthorization {

    private static final Logger logger = LoggerFactory.getLogger(MemberAuthorization.class);

    private Member member;

    public MemberAuthorization(Member member) {
        this.member = member;
    }

    public Mono<Boolean> canModifyPoints() {
        return member.getRoles()
            .filter(role -> role.getPermissions().contains(Permission.ADMINISTRATOR)
                || Arrays.asList("Professors", "Prefects").contains(role.getName()))
            .count()
            .map(count -> count > 0);
    }

    public Mono<Boolean> canListInactive() {
        return member.getRoles()
            .filter(role -> role.getPermissions().contains(Permission.ADMINISTRATOR))
            .count()
            .map(count -> count > 0);
    }

    public Mono<Boolean> canBumpYears() {
        return member.getRoles()
            .filter(role -> role.getPermissions().contains(Permission.ADMINISTRATOR))
            .count()
            .map(count -> count > 0);
    }
}
