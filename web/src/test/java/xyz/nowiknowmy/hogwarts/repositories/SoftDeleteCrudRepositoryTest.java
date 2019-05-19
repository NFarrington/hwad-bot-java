package xyz.nowiknowmy.hogwarts.repositories;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import xyz.nowiknowmy.hogwarts.domain.Guild;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class SoftDeleteCrudRepositoryTest {

    @Autowired
    EntityManager entityManager;

    @Autowired
    private GuildRepository guildRepository;

    @Before
    public void setUp() {
        //
    }

    @After
    public void tearDown() {
        guildRepository.deleteAll();
    }

    @Test
    public void testFindAll() {
        givenAGuild();

        List<Guild> guilds = guildRepository.findAll();
        assertEquals(1, guilds.size());
    }

    @Test
    public void testFindAllDoesntReturnTrashed() {
        givenASoftDeletedGuild();

        List<Guild> guilds = guildRepository.findAll();
        assertEquals(0, guilds.size());
    }

    @Test
    @Transactional
    public void testSoftDelete() {
        Guild guild = givenAGuild();

        guildRepository.softDelete(guild.getId());
        entityManager.refresh(guild);
        assertNotNull(guild.getDeletedAt());
        assertThat(guild.getDeletedAt(), is(lessThan(LocalDateTime.now())));
    }

    private void givenASoftDeletedGuild() {
        Guild guild = new Guild("12345678901234567890", "Alice's Guild");
        this.guildRepository.save(guild);
        assertNotNull(guild.getId());
        guildRepository.softDelete(guild.getId());
    }

    private Guild givenAGuild() {
        Guild guild = new Guild("12345678901234567890", "Alice's Guild");
        this.guildRepository.save(guild);
        assertNotNull(guild.getId());

        return guild;
    }

}
