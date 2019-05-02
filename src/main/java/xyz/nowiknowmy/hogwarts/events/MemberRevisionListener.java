package xyz.nowiknowmy.hogwarts.events;

import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import xyz.nowiknowmy.hogwarts.domain.Member;
import xyz.nowiknowmy.hogwarts.domain.Revision;
import xyz.nowiknowmy.hogwarts.repositories.RevisionRepository;

@Component
public class MemberRevisionListener implements ApplicationListener<MemberPreSaveEvent> {

    private final RevisionRepository revisionRepository;

    public MemberRevisionListener(RevisionRepository revisionRepository) {
        this.revisionRepository = revisionRepository;
    }

    @Override
    public void onApplicationEvent(MemberPreSaveEvent event) {
        Member member = event.getMember();

        if (member.getDirty().contains("username")) {
            Revision revision = initialiseRevision(member);
            revision.setKey("username");
            revision.setOldValue(member.getOriginal().getUsername());
            revision.setNewValue(member.getUsername());
            revisionRepository.save(revision);
        }

        if (member.getDirty().contains("nickname")) {
            Revision revision = initialiseRevision(member);
            revision.setKey("nickname");
            revision.setOldValue(member.getOriginal().getNickname());
            revision.setNewValue(member.getNickname());
            revisionRepository.save(revision);
        }
    }

    private Revision initialiseRevision(Member member)
    {
        Revision revision = new Revision();
        revision.setModelType("App\\Models\\Member");
        revision.setModelId(member.getId().longValue());

        return revision;
    }
}
