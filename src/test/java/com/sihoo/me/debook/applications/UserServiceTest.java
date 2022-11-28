package com.sihoo.me.debook.applications;

import com.sihoo.me.debook.domains.User;
import com.sihoo.me.debook.dto.UserRequestData;
import com.sihoo.me.debook.errors.CustomException;
import com.sihoo.me.debook.infra.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class UserServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private UserService userService;

    private static final Long NEW_ID = 5L;
    private static final Long EXISTS_ID = 2L;
    private static final Long NOT_EXISTS_ID = 200L;
    private static final String EXISTS_NICKNAME = "exists";
    private static final String NOT_EXISTS_NICKNAME = "not_exists";

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository);
    }

    @Nested
    @DisplayName("createUser 메서드는")
    class Describe_createUser {
        @Nested
        @DisplayName("올바른 요청이 들어왔을 때")
        class Context_when_valid_requests {
            @BeforeEach
            void setUp() {
                given(userRepository.save(any(User.class))).will(invocation -> {
                    User source = invocation.getArgument(0);
                    return User.builder()
                            .id(NEW_ID)
                            .email(source.getEmail())
                            .password(source.getPassword())
                            .githubId(source.getGithubId())
                            .nickName(source.getNickName())
                            .build();
                });
            }

            @Test
            @DisplayName("유저를 생성한다.")
            void It_creates_user_and_returns_user() {
                UserRequestData userRequest = UserRequestData.builder()
                        .nickName("test")
                        .email("test@email.com")
                        .password("password123")
                        .githubId("mikekang47")
                        .build();

                User user = userService.createUser(userRequest);

                assertThat(user.getEmail()).isEqualTo("test@email.com");
                assertThat(user.getNickName()).isEqualTo("test");
                assertThat(user.getGithubId()).isEqualTo("mikekang47");
            }
        }
    }

    @Nested
    @DisplayName("getUserById 메서드는")
    class Describe_getUserById {
        @Nested
        @DisplayName("userId가 존재할 때")
        class Context_when_exists_user {
            @BeforeEach
            void setUp() {
                User user = User.builder()
                        .id(EXISTS_ID)
                        .email("test@email.com")
                        .build();

                given(userRepository.findUserById(EXISTS_ID)).willReturn(Optional.of(user));
            }

            @Test
            @DisplayName("user를 반환한다.")
            void It_returns_user() {
                User user = userService.getUserById(EXISTS_ID);

                assertThat(user.getId()).isEqualTo(2L);
                assertThat(user.getEmail()).isEqualTo("test@email.com");
            }

        }

        @Nested
        @DisplayName("user가 존재하지 않을 때")
        class Context_when_not_exists_user {
            @Test
            @DisplayName("에러를 발생한다.")
            void It_throws_not_found_exception() {
                assertThatThrownBy(() -> userService.getUserById(NOT_EXISTS_ID))
                        .hasMessageContaining("[ERROR] User not found")
                        .isInstanceOf(CustomException.class);
            }
        }
    }

    @Nested
    @DisplayName("getUserByNickName 메서드는")
    class Describe_getUserBtyNickname {
        @Nested
        @DisplayName("주어진 닉네임을 포함한 유저가 존재하면")
        class Describe_exists_contains_nickname_user {
            @BeforeEach
            void setUp() {
                User user1 = User.builder()
                        .nickName("exists_1")
                        .build();

                User user2 = User.builder()
                        .nickName("exists_2")
                        .build();

                List<User> users = List.of(user1, user2);

                given(userRepository.findUserByNickName(EXISTS_NICKNAME))
                        .willReturn(users);
            }

            @Test
            @DisplayName("포함한 유저를 모두 반환한다.")
            void It_returns_list_of_users() {
                List<User> users = userService.getUserByNickName(EXISTS_NICKNAME);

                assertThat(users).isNotEmpty();
            }
        }

        @Nested
        @DisplayName("주어진 닉네임을 포함한 유저가 존재하지 않는다면")
        class Describe_not_exists_contains_nickname_user {
            @BeforeEach
            void setUp() {
                given(userRepository.findUserByNickName(NOT_EXISTS_NICKNAME)).willReturn(List.of());
            }

            @Test
            @DisplayName("빈 리스트를 반환한다.")
            void It_returns_empty_list() {
                List<User> users = userService.getUserByNickName(NOT_EXISTS_NICKNAME);

                assertThat(users).isEmpty();
            }
        }
    }
}