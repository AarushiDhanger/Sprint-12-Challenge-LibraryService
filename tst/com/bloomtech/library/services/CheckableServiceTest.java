package com.bloomtech.library.services;

import com.bloomtech.library.exceptions.CheckableNotFoundException;
import com.bloomtech.library.exceptions.ResourceExistsException;
import com.bloomtech.library.models.checkableTypes.*;
import com.bloomtech.library.repositories.CheckableRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
public class CheckableServiceTest {

    //TODO: Inject dependencies and mocks

    @MockBean
    private CheckableRepository checkableRepository;

    @Autowired
    private CheckableService checkableService;

    private List<Checkable> checkables;

    @BeforeEach
    void init() {
        //Initialize test data
        checkables = new ArrayList<>();

        checkables.addAll(
                Arrays.asList(
                        new Media("1-0", "The White Whale", "Melvin H", MediaType.BOOK),
                        new Media("1-1", "The Sorcerer's Quest", "Ana T", MediaType.BOOK),
                        new Media("1-2", "When You're Gone", "Complaining at the Disco", MediaType.MUSIC),
                        new Media("1-3", "Nature Around the World", "DocuSpecialists", MediaType.VIDEO),
                        new ScienceKit("2-0", "Anatomy Model"),
                        new ScienceKit("2-1", "Robotics Kit"),
                        new Ticket("3-0", "Science Museum Tickets"),
                        new Ticket("3-1", "National Park Day Pass")
                )
        );
    }

    //TODO: Write Unit Tests for all CheckableService methods and possible Exceptions
    @Test
    void testGetAll() {
        when(checkableRepository.findAll()).thenReturn(checkables);

        List<Checkable> foundCheckables = checkableService.getAll();

        assertEquals(8, foundCheckables.size());
        verify(checkableRepository, times(1)).findAll();
    }

    @Test
    void testGetByIsbn() {
        when(checkableRepository.findByIsbn("1-0")).thenReturn(Optional.of(checkables.get(0)));

        Checkable found = checkableService.getByIsbn("1-0");

        assertEquals("1-0", found.getIsbn());
        assertEquals("The White Whale", found.getTitle());
        verify(checkableRepository, times(1)).findByIsbn("1-0");
    }

    @Test
    void testGetByIsbn_NotFound() {
        when(checkableRepository.findByIsbn("1-0")).thenReturn(Optional.empty());

        assertThrows(CheckableNotFoundException.class, () -> checkableService.getByIsbn("1-0"));
        verify(checkableRepository, times(1)).findByIsbn("1-0");
    }

    @Test
    void testGetByType() {
        when(checkableRepository.findByType(Media.class)).thenReturn(Optional.of(checkables.get(0)));

        Checkable found = checkableService.getByType(Media.class);

        assertEquals(Media.class, found.getClass());
        verify(checkableRepository, times(1)).findByType(Media.class);
    }

    @Test
    void testGetByType_NotFound() {
        when(checkableRepository.findByType(Media.class)).thenReturn(Optional.empty());

        assertThrows(CheckableNotFoundException.class, () -> checkableService.getByType(Media.class));
        verify(checkableRepository, times(1)).findByType(Media.class);
    }

    @Test
    void testSave_ResourceExists() {
        // Set up the repository to return a list that includes the checkable to be saved
        when(checkableRepository.findAll()).thenReturn(checkables);

        // Use the appropriate checkable to test
        Checkable checkableToSave = checkables.get(0);  // Example checkable

        // Ensure the correct exception is thrown
        assertThrows(ResourceExistsException.class, () -> {
            checkableService.save(checkableToSave);
        });
    }
    @Test
    void testSave() {
        when(checkableRepository.findAll()).thenReturn(new ArrayList<>());
        //when(checkableRepository.save(any(Checkable.class))).thenReturn(checkables.get(0));

        checkableService.save(checkables.get(0));

        verify(checkableRepository, times(1)).save(checkables.get(0));
    }

    @Test
    void testMockBean() throws NoSuchFieldException {
        assertTrue(Arrays.stream(CheckableServiceTest.class.getDeclaredField("checkableRepository").getAnnotations())
                .anyMatch(a -> a.annotationType().equals(MockBean.class)));
    }

}