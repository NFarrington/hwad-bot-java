package xyz.nowiknowmy.hogwarts.events;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import xyz.nowiknowmy.hogwarts.domain.Member;

@Component
public class MemberPreSavePublisher {
    private final ApplicationEventPublisher applicationEventPublisher;

    public MemberPreSavePublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void publish(Member member) {
        MemberPreSaveEvent customSpringEvent = new MemberPreSaveEvent(this, member);
        applicationEventPublisher.publishEvent(customSpringEvent);
    }
}
