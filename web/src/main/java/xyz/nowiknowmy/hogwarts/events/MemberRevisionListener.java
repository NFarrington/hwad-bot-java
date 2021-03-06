package xyz.nowiknowmy.hogwarts.events;

import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import xyz.nowiknowmy.hogwarts.domain.Member;
import xyz.nowiknowmy.hogwarts.domain.Revision;
import xyz.nowiknowmy.hogwarts.repositories.RevisionRepository;

@Component
public class MemberRevisionListener implements ApplicationListener<MemberPreSaveEvent> {

    private static final String KEY_USERNAME = "username";
    private static final String KEY_NICKNAME = "nickname";

    private final RevisionRepository revisionRepository;

    public MemberRevisionListener(RevisionRepository revisionRepository) {
        this.revisionRepository = revisionRepository;
    }

    @Override
    public void onApplicationEvent(MemberPreSaveEvent event) {
        Member member = event.getMember();

        if (member.getId() == null) {
            return;
        }

        if (member.getOriginalAttributes().containsKey(KEY_USERNAME)) {
            Revision revision = new Revision("App\\Models\\Member",
                member.getId().longValue(),
                KEY_USERNAME,
                (String) member.getOriginalAttributes().get(KEY_USERNAME),
                member.getUsername());
            revisionRepository.save(revision);
        }

        if (member.getOriginalAttributes().containsKey(KEY_NICKNAME)) {
            Revision revision = new Revision("App\\Models\\Member",
                member.getId().longValue(),
                KEY_NICKNAME,
                (String) member.getOriginalAttributes().get(KEY_NICKNAME),
                member.getNickname());
            revisionRepository.save(revision);
        }
    }

}
