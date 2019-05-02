package xyz.nowiknowmy.hogwarts.events;

import org.springframework.context.ApplicationEvent;
import xyz.nowiknowmy.hogwarts.domain.Member;

public class MemberPreSaveEvent extends ApplicationEvent {
    private final Member member;

    public MemberPreSaveEvent(Object source, Member member) {
        super(source);
        this.member = member;
    }
    public Member getMember() {
        return member;
    }
}
