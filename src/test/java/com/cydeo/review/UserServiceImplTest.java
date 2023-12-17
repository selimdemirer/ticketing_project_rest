package com.cydeo.review;

import com.cydeo.dto.ProjectDTO;
import com.cydeo.dto.RoleDTO;
import com.cydeo.dto.TaskDTO;
import com.cydeo.dto.UserDTO;
import com.cydeo.entity.Role;
import com.cydeo.entity.User;
import com.cydeo.exception.TicketingProjectException;
import com.cydeo.mapper.UserMapper;
import com.cydeo.repository.UserRepository;
import com.cydeo.service.KeycloakService;
import com.cydeo.service.ProjectService;
import com.cydeo.service.TaskService;
import com.cydeo.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private ProjectService projectService;

    @Mock
    private TaskService taskService;

    @Mock
    private KeycloakService keycloakService;

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    User user;
    UserDTO userDTO;

    @BeforeEach
    void setUp() {

        user = new User();
        user.setId(1L);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setUserName("user");
        user.setPassWord("Abc1");
        user.setEnabled(true);
        user.setRole(new Role("Manager"));

        userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setFirstName("John");
        userDTO.setLastName("Doe");
        userDTO.setUserName("user");
        userDTO.setPassWord("Abc1");
        userDTO.setEnabled(true);

        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setDescription("Manager");
        userDTO.setRole(roleDTO);

    }

    private List<User> getUsers() {
        User user2 = new User();
        user2.setId(2L);
        user2.setFirstName("Emily");
        return List.of(user, user2);
    }

    private List<UserDTO> getUserDTOs() {
        UserDTO userDTO2 = new UserDTO();
        userDTO2.setId(2L);
        userDTO2.setFirstName("Emily");
        return List.of(userDTO, userDTO2);
    }

    private User getUser(String role) {

        User user3 = new User();

        user3.setUserName("user3");
        user3.setEnabled(true);
        user3.setIsDeleted(false);
        user3.setRole(new Role(role));

        return user3;

    }

    @Test
    void should_list_all_users() {

        //given - Preparation
        //stub
        when(userRepository.findAllByIsDeletedOrderByFirstNameDesc(false)).thenReturn(getUsers());
        when(userMapper.convertToDto(any(User.class))).thenReturn(userDTO, getUserDTOs().get(1));
//        when(userMapper.convertToDto(getUsers().get(0))).thenReturn(userDTO);
//        when(userMapper.convertToDto(getUsers().get(1))).thenReturn(getUserDTOs().get(1));

        //when - Action
        List<UserDTO> expectedList = getUserDTOs();
        List<UserDTO> actualList = userService.listAllUsers();

        //then - Assertion/Verification
        assertEquals(expectedList, actualList);     // ExpectedList Data -> ActualList Data  -  list 1 - list 2
        // User object 1, User object 2 -> User object 3, User object 4
        // userObject1.equals(userObject2)  ->  true

        // AssertJ
//        assertThat(actualList)
//                .usingRecursiveComparison()
//                .ignoringExpectedNullFields()
//                .isEqualTo(expectedList);

        verify(userRepository, times(1)).findAllByIsDeletedOrderByFirstNameDesc(false);
        verify(userMapper, times(2)).convertToDto(any(User.class));

    }

    @Test
    void should_throw_nosuchelement_exception_when_user_not_found() {

        // given - Preparation
//        when(userRepository.findByUserNameAndIsDeleted(anyString(), anyBoolean())).thenReturn(null);
//        lenient().when(userMapper.convertToDto(any(User.class))).thenReturn(userDTO);

        // when + then
//        Throwable actualException = assertThrows(RuntimeException.class
//        , () -> userService.findByUserName("SomeUserName"));   // Returns Throwable
//        Throwable actualException = assertThrowsExactly(NoSuchElementException.class
//                , () -> userService.findByUserName("SomeUserName"));    // Returns Throwable
//
//        assertEquals("User not found.", actualException.getMessage());

        // AssertJ
        Throwable actualException = catchThrowable(() -> userService.findByUserName("SomeUserName"));

        assertInstanceOf(RuntimeException.class, actualException);
        assertEquals(NoSuchElementException.class, actualException.getClass());
        assertEquals("User not found.", actualException.getMessage());

    }

    @Test
    void should_not_throw_nosuchelement_exception_when_user_is_found() {
        when(userRepository.findByUserNameAndIsDeleted(anyString(), anyBoolean())).thenReturn(user);
        when(userMapper.convertToDto(any(User.class))).thenReturn(userDTO);
        assertDoesNotThrow(() -> userService.findByUserName("SomeUserName"));
    }

    // 	User Story - 1: As a user of the application, I want my password to be encoded
    //	so that my account remains secure.
    //
    //	Acceptance Criteria:
    //	1 - When a user creates a new account, their password should be encoded using
    //	a secure algorithm such as bcrypt or PBKDF2.
    //
    //	2 - Passwords should not be stored in plain text in the database or any other storage.
    //
    //	3 - Passwords encoding should be implemented consistently throughout the application,
    //	including any password reset or change functionality.

    @Test
    void should_encode_user_password_on_save_operation() {

        when(userMapper.convertToEntity(any(UserDTO.class))).thenReturn(user);
        when(userRepository.save(any())).thenReturn(user);
        when(userMapper.convertToDto(any(User.class))).thenReturn(userDTO);
        when(passwordEncoder.encode(anyString())).thenReturn("Encoded Password");

        userService.save(userDTO);

        verify(passwordEncoder).encode(anyString());

    }

    @Test
    void should_encode_user_password_on_update_operation() {

        when(userRepository.findByUserNameAndIsDeleted(anyString(), anyBoolean())).thenReturn(user);
        when(userMapper.convertToEntity(any(UserDTO.class))).thenReturn(user);
        when(userRepository.save(any())).thenReturn(user);
        when(userMapper.convertToDto(any(User.class))).thenReturn(userDTO);
        when(passwordEncoder.encode(anyString())).thenReturn("Encoded Password");

        userService.update(userDTO);

        verify(passwordEncoder).encode(anyString());

    }

    // 	User Story 2: As an admin, I shouldn't be able to delete a manager user,
    // 	if that manager has projects linked to them to prevent data loss.
    //
    //	Acceptance Criteria:
    //
    //	1 - The system should prevent a manager user from being deleted
    //	if they have projects linked to them.
    //	2 - An error message should be displayed to the user if they attempt
    //	to delete a manager user with linked projects.
    //
    //	User Story 3: As an admin, I shouldn't be able to delete an employee user,
    //	if that employee has tasks linked to them to prevent data loss.
    //
    //	Acceptance Criteria:
    //
    //	1 - The system should prevent an employee user from being deleted
    //	if they have tasks linked to them.
    //	2 - An error message should be displayed to the user if they attempt
    //	to delete an employee user with linked tasks.

    @Test
    void should_delete_manager() throws TicketingProjectException {

        //Given - Preparation
        User managerUser = getUser("Manager");

        when(userRepository.findByUserNameAndIsDeleted(anyString(), anyBoolean())).thenReturn(managerUser);
        when(userRepository.save(any())).thenReturn(managerUser);
        when(projectService.listAllNonCompletedByAssignedManager(any())).thenReturn(new ArrayList<>());

        //When - Action
        userService.delete(managerUser.getUserName());

        //Then - Assertion/Verification
        assertTrue(managerUser.getIsDeleted());
        assertNotEquals("user3", managerUser.getUserName());

    }

    @Test
    void should_delete_employee() throws TicketingProjectException {

        //Given - Preparation
        User employeeUser = getUser("Employee");

        when(userRepository.findByUserNameAndIsDeleted(anyString(), anyBoolean())).thenReturn(employeeUser);
        when(userRepository.save(any())).thenReturn(employeeUser);
        when(taskService.listAllNonCompletedByAssignedEmployee(any())).thenReturn(new ArrayList<>());

        //When - Action
        userService.delete(employeeUser.getUserName());

        //Then - Assertion/Verification
        assertTrue(employeeUser.getIsDeleted());
        assertNotEquals("user3", employeeUser.getUserName());

    }

    // Not the best usage - Better to separate into 2 tests like above
    @ParameterizedTest
    @ValueSource(strings = {"Manager", "Employee"})
    void should_delete_user(String role) throws TicketingProjectException {

        //Given - Preparation
        User testUser = getUser(role);

        when(userRepository.findByUserNameAndIsDeleted(anyString(), anyBoolean())).thenReturn(testUser);
        when(userRepository.save(any())).thenReturn(testUser);

//        if (testUser.getRole().getDescription().equals("Manager")) {
//            when(projectService.listAllNonCompletedByAssignedManager(any())).thenReturn(new ArrayList<>());
//        } else if (testUser.getRole().getDescription().equals("Employee")) {
//            when(taskService.listAllNonCompletedByAssignedEmployee(any())).thenReturn(new ArrayList<>());
//        }

        lenient().when(projectService.listAllNonCompletedByAssignedManager(any())).thenReturn(new ArrayList<>());
        lenient().when(taskService.listAllNonCompletedByAssignedEmployee(any())).thenReturn(new ArrayList<>());

        //When - Action
        userService.delete(testUser.getUserName());

        //Then - Assertion/Verification
        assertTrue(testUser.getIsDeleted());
        assertNotEquals("user3", testUser.getUserName());

    }

    @Test
    void should_throw_exception_when_deleting_manager_with_projects() {

        User managerUser = getUser("Manager");

        when(userRepository.findByUserNameAndIsDeleted(anyString(), anyBoolean())).thenReturn(managerUser);
        when(projectService.listAllNonCompletedByAssignedManager(any())).thenReturn(List.of(new ProjectDTO(), new ProjectDTO()));

        Throwable actual = catchThrowable(() -> userService.delete(userDTO.getUserName()));

        assertEquals(TicketingProjectException.class, actual.getClass());
        assertEquals("User can not be deleted", actual.getMessage());

    }

    @Test
    void should_throw_exception_when_deleting_employee_with_tasks() {

        User employeeUser = getUser("Employee");

        when(userRepository.findByUserNameAndIsDeleted(anyString(), anyBoolean())).thenReturn(employeeUser);
        when(taskService.listAllNonCompletedByAssignedEmployee(any())).thenReturn(List.of(new TaskDTO(), new TaskDTO()));

        Throwable actual = catchThrowable(() -> userService.delete(userDTO.getUserName()));

        assertEquals(TicketingProjectException.class, actual.getClass());
        assertEquals("User can not be deleted", actual.getMessage());

    }

    //	User Story 4: As an admin, I shouldn't be able to delete an admin user,
    //	if that admin is the last admin in the system.
    //
    //	Acceptance Criteria:
    //
    //	1 - The system should prevent an admin user from being deleted
    //	if it is the last admin.
    //	2 - An error message should be displayed to the user if there is an
    //	attempt to delete the last admin user with.

}