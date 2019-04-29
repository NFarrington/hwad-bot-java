package xyz.nowiknowmy.hogwarts.authorization;

import discord4j.core.object.entity.Member;
import discord4j.core.object.util.Permission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import xyz.nowiknowmy.hogwarts.exceptions.AuthorizationException;
import xyz.nowiknowmy.hogwarts.services.MessageService;

import javax.security.auth.message.AuthException;
import java.util.Arrays;

public class MemberAuthorization {

    private static final Logger logger = LoggerFactory.getLogger(MemberAuthorization.class);

    private Member member;

    public MemberAuthorization(Member member) {
        this.member = member;
    }

    public Mono<Boolean> canModifyPoints() {
        logger.info("Checking if permitted");
        return member.getRoles()
                .filter(role -> role.getPermissions().contains(Permission.ADMINISTRATOR)
                        || Arrays.asList("Professors", "Prefects").contains(role.getName()))
                .count()
                .map(count -> {
                    logger.info(count > 0 ? "Permitted" : "Not permitted");
//                    if (count == 0) {
//                        throw new AuthorizationException("User is not permitted to modify points.");
//                    }
                    return count > 0;
                });
//        return member.flatMap(member1 -> member1.getRoles().collectList())
//                .map(role ->
//                        role.stream().map(role1 -> role1.getPermissions().contains(Permission.ADMINISTRATOR)
//                                || Arrays.asList("Professors", "Prefects").contains(role1.getName()))
//                                .count()
//                )
//                .map(count -> count > 0);
    }
//    public static class AuthorizationBuilder {
//        private Member member;
//
//        public AuthorizationBuilder(Member member) {
//            this.member = member;
//        }
//
//        public MemberAuthorization build() {
//            MemberAuthorization memberAuthorization = new MemberAuthorization();
//            memberAuthorization.member = member;
//
//            return memberAuthorization;
//        }
//    }
//
//    private Member member;
//
//    private MemberAuthorization() {
//    }
}
