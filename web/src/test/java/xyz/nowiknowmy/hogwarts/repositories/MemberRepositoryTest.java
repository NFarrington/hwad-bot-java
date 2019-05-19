package xyz.nowiknowmy.hogwarts.repositories;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import xyz.nowiknowmy.hogwarts.domain.Member;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Before
    public void setUp() {
        Member member = new Member("99999999999999999999", 99, "Zeke", LocalDateTime.now());
        this.memberRepository.save(member);
        assertNotNull(member.getId());
    }

    @After
    public void tearDown() {
        memberRepository.deleteAll();
    }

    @Test
    public void testFindByUid() {
        givenAUserInTwoGuilds();

        List<Member> members = memberRepository.findByUid("12345678901234567890");
        assertEquals(2, members.size());
        assertEquals(2, members.stream().filter(member -> "Alice".equals(member.getUsername())).count());
        assertEquals(1, members.stream().filter(member -> 1 == member.getGuildId()).count());
    }

    @Test
    public void testFindByGuildId() {
        givenTwoMembersInAGuild();

        List<Member> members = memberRepository.findByGuildId(1);
        assertEquals(2, members.size());
        assertEquals(1, members.stream().filter(member -> "Alice".equals(member.getUsername())).count());
        assertEquals(2, members.stream().filter(member -> 1 == member.getGuildId()).count());
    }

    @Test
    public void testFindByUidAndGuildId() {
        givenAUserInTwoGuilds();

        Member member = memberRepository.findByUidAndGuildId("12345678901234567890", 1);
        assertNotNull(member);
        assertEquals("Alice", member.getUsername());
        assertEquals(1, (int) member.getGuildId());
    }

    @Test
    public void testFindByUidAndGuildIdDoesntReturnTrashed() {
        givenASoftDeletedMember();

        Member member = memberRepository.findByUidAndGuildId("12345678901234567892", 1);
        assertNull(member);
    }

    @Test
    public void testFindByUidAndGuildIdWithTrashed() {
        givenASoftDeletedMember();

        Member member = memberRepository.findByUidAndGuildIdWithTrashed("12345678901234567892", 1);
        assertNotNull(member);
        assertEquals("Charlie", member.getUsername());
        assertEquals(1, (int) member.getGuildId());
    }

    @Test
    public void testFindByGuildIdAndLastMessageBefore() {
        givenAnActiveMember();
        givenAnInactiveMember();

        List<Member> members = memberRepository.findByGuildIdAndLastMessageBefore(1, LocalDateTime.now().minus(1, ChronoUnit.DAYS));
        assertEquals(1, members.size());
        assertEquals("Bob", members.get(0).getUsername());
    }

    private void givenAnActiveMember() {
        Member member = new Member("12345678901234567890", 1, "Alice", LocalDateTime.now());
        this.memberRepository.save(member);
        assertNotNull(member.getId());
    }

    private void givenAnInactiveMember() {
        Member member = new Member("12345678901234567891", 1, "Bob", LocalDateTime.now().minus(1, ChronoUnit.WEEKS));
        this.memberRepository.save(member);
        assertNotNull(member.getId());
    }

    private void givenASoftDeletedMember() {
        Member member = new Member("12345678901234567892", 1, "Charlie", LocalDateTime.now());
        this.memberRepository.save(member);
        assertNotNull(member.getId());
        this.memberRepository.softDelete(member.getId());
    }

    private void givenAUserInTwoGuilds() {
        Member member1 = new Member("12345678901234567890", 1, "Alice", LocalDateTime.now());
        Member member2 = new Member("12345678901234567890", 2, "Alice", LocalDateTime.now());
        this.memberRepository.save(member1);
        this.memberRepository.save(member2);
        assertNotNull(member1.getId());
        assertNotNull(member2.getId());
    }

    private void givenTwoMembersInAGuild() {
        Member member1 = new Member("12345678901234567890", 1, "Alice", LocalDateTime.now());
        Member member2 = new Member("12345678901234567891", 1, "Bob", LocalDateTime.now());
        this.memberRepository.save(member1);
        this.memberRepository.save(member2);
        assertNotNull(member1.getId());
        assertNotNull(member2.getId());
    }

}
