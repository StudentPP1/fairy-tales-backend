package dev.project.bedtimestory.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Table(name = "app_user")
public class User extends BaseEntity {
    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    private String img = null;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;

    private boolean isSubscribed = false;

    @OneToMany(mappedBy = "user")
    private List<UserConnectedAccount> connectedAccounts = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "user_read_stories",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "story_id")
    )
    private List<Story> readStories = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "user_liked_stories",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "story_id")
    )
    private List<Story> likedStories = new ArrayList<>();

    public User(OAuth2User oAuth2User) {
        this.email = oAuth2User.getAttribute("email");
        this.name = oAuth2User.getAttribute("name");
        this.role = Role.USER;
    }
    public void addLikedStory(Story story) {
        likedStories.add(story);
    }
    public void addReadStory(Story story) {
        readStories.add(story);
    }
    public void removeLikedStory(Story story) {
        likedStories.remove(story);
    }
    public void removeReadStory(Story story) {
        readStories.remove(story);
    }
    public void addConnectedAccount(UserConnectedAccount connectedAccount) {
        connectedAccounts.add(connectedAccount);
        connectedAccount.setUser(this);
    }
}