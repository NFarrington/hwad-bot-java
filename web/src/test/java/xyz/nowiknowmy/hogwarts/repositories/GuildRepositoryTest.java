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

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class GuildRepositoryTest {

    @Autowired
    private GuildRepository guildRepository;

    @Before
    public void setUp() {
        Guild guild = new Guild("99999999999999999999", "Zeke's Guild");
        this.guildRepository.save(guild);
        assertNotNull(guild.getId());
    }

    @After
    public void tearDown() {
        guildRepository.deleteAll();
    }

    @Test
    public void testFindByGuildId() {
        givenAGuild();

        Guild guild = guildRepository.findByGuildId("12345678901234567890");
        assertNotNull(guild);
        assertEquals("Alice's Guild", guild.getName());
    }

    @Test
    public void testFindByGuildIdDoesntReturnTrashed() {
        givenASoftDeletedGuild();

        Guild guild = guildRepository.findByGuildId("12345678901234567890");
        assertNull(guild);
    }

    @Test
    public void testFindByGuildIdIn() {
        givenAGuild();

        List<Guild> guilds = guildRepository.findByGuildIdIn(Collections.singletonList("12345678901234567890"));
        assertEquals(1, guilds.size());
        assertEquals("Alice's Guild", guilds.get(0).getName());
    }

    @Test
    public void testFindByGuildIdWithTrashed() {
        givenASoftDeletedGuild();

        Guild guild = guildRepository.findByGuildIdWithTrashed("12345678901234567890");
        assertNotNull(guild);
        assertEquals("Alice's Guild", guild.getName());
    }

    private void givenASoftDeletedGuild() {
        Guild guild = new Guild("12345678901234567890", "Alice's Guild");
        this.guildRepository.save(guild);
        assertNotNull(guild.getId());
        guildRepository.softDelete(guild.getId());
    }

    private void givenAGuild() {
        Guild guild = new Guild("12345678901234567890", "Alice's Guild");
        this.guildRepository.save(guild);
        assertNotNull(guild.getId());
    }

}
