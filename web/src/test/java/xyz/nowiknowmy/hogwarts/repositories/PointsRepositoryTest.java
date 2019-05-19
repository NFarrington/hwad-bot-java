package xyz.nowiknowmy.hogwarts.repositories;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import xyz.nowiknowmy.hogwarts.domain.Points;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class PointsRepositoryTest {

    @Autowired
    PointsRepository pointsRepository;

    @Before
    public void setUp() {
        Points points = new Points(99, "s", 987L);
        this.pointsRepository.save(points);
        assertNotNull(points.getId());
    }

    @After
    public void tearDown() {
        pointsRepository.deleteAll();
    }

    @Test
    public void testFindByGuildId() {
        givenTwoHousesWithPoints();

        List<Points> pointsList = pointsRepository.findByGuildId(1);
        assertEquals(2, pointsList.size());
        assertEquals(1, pointsList.stream().filter(points -> "g".equals(points.getHouse())).count());
        assertEquals(2, pointsList.stream().filter(points -> 1 == points.getGuildId()).count());
    }

    @Test
    public void testFindByGuildIdAndHouse() {
        givenTwoHousesWithPoints();

        Points points = pointsRepository.findByGuildIdAndHouse(1, "g");
        assertNotNull(points);
        assertEquals(123, (long) points.getPoints());
    }

    private void givenTwoHousesWithPoints() {
        Points points1 = new Points(1, "g", 123L);
        this.pointsRepository.save(points1);
        assertNotNull(points1.getId());

        Points points2 = new Points(1, "h", 456L);
        this.pointsRepository.save(points2);
        assertNotNull(points2.getId());
    }

}
