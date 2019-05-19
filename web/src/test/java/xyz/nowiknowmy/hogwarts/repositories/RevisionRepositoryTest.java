package xyz.nowiknowmy.hogwarts.repositories;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import xyz.nowiknowmy.hogwarts.domain.Guild;
import xyz.nowiknowmy.hogwarts.domain.Member;
import xyz.nowiknowmy.hogwarts.domain.Revision;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class RevisionRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private RevisionRepository revisionRepository;

    @Before
    public void setUp() {
        Member member = new Member("99999999999999999999", 99, "Yasmin", LocalDateTime.now());
        this.memberRepository.save(member);
        assertNotNull(member.getId());
        Revision revision = new Revision("App\\Models\\Member", (long) member.getId(), "username", "Yasmin", "Zeke");
        this.revisionRepository.save(revision);
        assertNotNull(revision.getId());
    }

    @After
    public void tearDown() {
        memberRepository.deleteAll();
        revisionRepository.deleteAll();
    }

    @Test
    public void testFindByGuildIdWhereKeyIsUsernameOrNickname() {
        givenTwoMembersWithRevisions();

        List<Revision> revisions = revisionRepository.findByGuildIdWhereKeyIsUsernameOrNickname("App\\Models\\Member", Collections.singletonList(1));
        assertEquals(2, revisions.size());
        assertEquals(1, revisions.stream().filter(revision -> "username".equals(revision.getKey())).count());
    }

    private void givenTwoMembersWithRevisions() {
        Member member1 = new Member("12345678901234567890", 1, "Alice", LocalDateTime.now());
        this.memberRepository.save(member1);
        assertNotNull(member1.getId());
        Revision revision1 = new Revision("App\\Models\\Member", (long) member1.getId(), "username", "Alice", "Bob");
        this.revisionRepository.save(revision1);
        assertNotNull(revision1.getId());

        Member member2 = new Member("12345678901234567891", 1, "Charlie", LocalDateTime.now());
        this.memberRepository.save(member2);
        assertNotNull(member2.getId());
        Revision revision2 = new Revision("App\\Models\\Member", (long) member2.getId(), "nickname", "Charlie", "David");
        this.revisionRepository.save(revision2);
        assertNotNull(revision2.getId());
    }

}
