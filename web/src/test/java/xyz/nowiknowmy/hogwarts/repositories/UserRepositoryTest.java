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

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Before
    public void setUp() {
        User user1 = new User("12345678901234567890", "Alice");
        User user2 = new User("12345678901234567891", "Bob");

        assertNull(user1.getId());
        assertNull(user2.getId());
        this.userRepository.save(user1);
        this.userRepository.save(user2);
        assertNotNull(user1.getId());
        assertNotNull(user2.getId());
    }

    @After
    public void tearDown() {
        this.userRepository.delete(this.userRepository.findByUsername("Alice"));
        this.userRepository.delete(this.userRepository.findByUsername("Bob"));
    }

    @Test
    public void testFetchData() {
        User userA = userRepository.findByUsername("Bob");
        assertNotNull(userA);
        assertEquals("Bob", userA.getUsername());
        assertNotNull(userA.getCreatedAt());

        Collection<User> users = (Collection<User>) userRepository.findAll();
        assertEquals(2, users.size());
    }

}
