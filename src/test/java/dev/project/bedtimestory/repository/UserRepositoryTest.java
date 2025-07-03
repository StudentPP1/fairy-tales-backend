package dev.project.bedtimestory.repository;

import dev.project.bedtimestory.dto.StoryDto;
import dev.project.bedtimestory.dto.UserDto;
import dev.project.bedtimestory.entity.Role;
import dev.project.bedtimestory.entity.Story;
import dev.project.bedtimestory.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.liquibase.enabled=true"
})
class UserRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("user")
            .withPassword("password");

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager em; // ! wrapper of entity manager

    @Test
    void shouldReturnUserDtoByIdWithExpectedEmail() {
        User user = new User("John", "john@mail.com", null, "pass", Role.USER,
                false, Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        user = em.persist(user);
        em.flush();

        Optional<UserDto> result = userRepository.getUserDtoById(user.getId());
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("john@mail.com");
    }

    @Test
    void shouldReturnReadStoryByUserIdWithExpectedTitle() {
        Story story = em.persist(new Story("Title", "Desc", "img", "Text", 3));
        User user = new User("Reader", "read@mail.com", null, "pass", Role.USER,
                false, Collections.emptyList(), List.of(story), Collections.emptyList());
        user = em.persist(user);
        em.flush();

        List<StoryDto> stories = userRepository.findReadStoryByUserId(user.getId());
        assertThat(stories).hasSize(1);
        assertThat(stories.get(0).getTitle()).isEqualTo("Title");
    }

    @Test
    void shouldReturnLikedStoryByUserIdWithExpectedLikedCount() {
        Story story = em.persist(new Story("Like", "Desc", "img", "Text", 10));
        User user = new User("Liker", "like@mail.com", null, "pass", Role.USER,
                false, Collections.emptyList(), Collections.emptyList(), List.of(story));
        user = em.persist(user);
        em.flush();

        List<StoryDto> stories = userRepository.findLikedStoryByUserId(user.getId());
        assertThat(stories).hasSize(1);
        assertThat(stories.get(0).getLikedCount()).isEqualTo(10);
    }

    @Test
    void shouldReturnSubscribedEmails() {
        em.persist(new User("Sub", "sub@mail.com", null, "pass", Role.USER,
                true, Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));
        em.persist(new User("Unsub", "unsub@mail.com", null, "pass", Role.USER,
                false, Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));
        em.flush();

        List<String> emails = userRepository.getSubscribedEmails();
        assertThat(emails).containsExactly("sub@mail.com");
    }
}