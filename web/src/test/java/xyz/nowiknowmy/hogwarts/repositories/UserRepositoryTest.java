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

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Before
    public void setUp() {
        User user1 = new User("12345678901234567890", "Alice");
        User user2 = new User("12345678901234567891", "Bob");
        //save user, verify has ID value after save
        assertNull(user1.getId());
        assertNull(user2.getId());//null before save
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
        /*Test data retrieval*/
        User userA = userRepository.findByUsername("Bob");
        assertNotNull(userA);
        assertEquals("Bob", userA.getUsername());
        assertNotNull(userA.getCreatedAt());

        /*Get all products, list should only have two*/
        Iterable<User> users = userRepository.findAll();
        int count = 0;
        for (User p : users) {
            count++;
        }
        assertEquals(2, count);
    }
}
