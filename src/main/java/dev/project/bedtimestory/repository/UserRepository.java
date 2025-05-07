package dev.project.bedtimestory.repository;

import dev.project.bedtimestory.dto.StoryDto;
import dev.project.bedtimestory.dto.UserDto;
import dev.project.bedtimestory.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    @Query("""
    select new dev.project.bedtimestory.dto.UserDto(
        u.name,
        u.img,
        u.email,
        u.role
    )
    from User u where u.id = :userId
    """)
    Optional<UserDto> getUserDtoById(@Param("userId") Long userId);

    @Query("""
    select new dev.project.bedtimestory.dto.StoryDto(
        s.id,
        s.title,
        s.description,
        s.imgUrl,
        s.likedCount
    )
    from User u join u.readStories s where u.id = :userId
    """)
    List<StoryDto> findReadStoryByUserId(@Param("userId") Long userId);

    @Query("""
    select new dev.project.bedtimestory.dto.StoryDto(
        s.id,
        s.title,
        s.description,
        s.imgUrl,
        s.likedCount
    )
    from User u join u.likedStories s where u.id = :userId
    """)
    List<StoryDto> findLikedStoryByUserId(@Param("userId") Long userId);

    @Query("select u.email from User u where u.isSubscribed = true")
    List<String> getSubscribedEmails();
}