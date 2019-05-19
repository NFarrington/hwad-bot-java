package xyz.nowiknowmy.hogwarts.repositories;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import xyz.nowiknowmy.hogwarts.domain.User;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class UserRepositoryTest {

    @Autowired
    UserRepository userRepository;

    @Before
    public void setUp() {
        User user = new User("99999999999999999999", "Zeke");
        this.userRepository.save(user);
        assertNotNull(user.getId());
    }

    @After
    public void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    public void testFindByGuildId() {
        givenAUser();

        User user = userRepository.findByUid("12345678901234567890");
        assertNotNull(user);
        assertEquals("Alice", user.getUsername());
    }

    private void givenAUser() {
        User user = new User("12345678901234567890", "Alice");
        userRepository.save(user);
        assertNotNull(user.getId());
    }

}
