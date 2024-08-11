package com.bloomtech.library.services;

import com.bloomtech.library.exceptions.LibraryNotFoundException;
import com.bloomtech.library.exceptions.ResourceExistsException;
import com.bloomtech.library.models.*;
import com.bloomtech.library.models.checkableTypes.Checkable;
import com.bloomtech.library.models.checkableTypes.Media;
import com.bloomtech.library.repositories.LibraryRepository;
import com.bloomtech.library.models.CheckableAmount;
import com.bloomtech.library.views.LibraryAvailableCheckouts;
import com.bloomtech.library.views.OverdueCheckout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LibraryService {

    //TODO: Implement behavior described by the unit tests in tst.com.bloomtech.library.services.LibraryService

    @Autowired
    private LibraryRepository libraryRepository;
    @Autowired
    private CheckableService checkableService;

    public List<Library> getLibraries() {
        return libraryRepository.findAll();
    }

    public Library getLibraryByName(String name) {
        Optional<Library> library = libraryRepository.findByName(name);
        if (library.isPresent()) {
            return library.get();
        } else {
            throw new LibraryNotFoundException("Library with name: " + name + " not found!");
        }
    }

    public void save(Library library) {
        List<Library> libraries = libraryRepository.findAll();
        if (libraries.stream().filter(p->p.getName().equals(library.getName())).findFirst().isPresent()) {
            throw new ResourceExistsException("Library with name: " + library.getName() + " already exists!");
        }
        libraryRepository.save(library);
    }

    public CheckableAmount getCheckableAmount(String libraryName, String checkableIsbn) {
        //return new CheckableAmount(null, 0);
        Optional<Library> libraryOptional = libraryRepository.findByName(libraryName);
        if (!libraryOptional.isPresent()) {
            throw new LibraryNotFoundException("Library with name: " + libraryName + " not found!");
        }

        Library library = libraryOptional.get();
        Checkable checkable = checkableService.getByIsbn(checkableIsbn);

        return library.getCheckables().stream()
                .filter(ca -> ca.getCheckable().getIsbn().equals(checkableIsbn))
                .findFirst()
                .orElse(new CheckableAmount(checkable, 0));
    }

    public List<LibraryAvailableCheckouts> getLibrariesWithAvailableCheckout(String isbn) {
        Checkable checkable = checkableService.getByIsbn(isbn);
        List<Library> allLibraries = libraryRepository.findAll();

        return allLibraries.stream()
                .map(library -> {
                    int available = library.getCheckables().stream()
                            .filter(ca -> ca.getCheckable().getIsbn().equals(isbn))
                            .mapToInt(CheckableAmount::getAmount)
                            .sum();
                    return new LibraryAvailableCheckouts(available, library.getName());
                })
                .collect(Collectors.toList());
    }

    public List<OverdueCheckout> getOverdueCheckouts(String libraryName) {
        Optional<Library> libraryOptional = libraryRepository.findByName(libraryName);
        if (!libraryOptional.isPresent()) {
            throw new LibraryNotFoundException("Library with name: " + libraryName + " not found!");
        }

        Library library = libraryOptional.get();
        LocalDateTime now = LocalDateTime.now();

        return library.getLibraryCards().stream()
                .flatMap(card -> {
                    Patron patron = card.getPatron();
                    return card.getCheckouts().stream()
                            .filter(checkout -> checkout.getDueDate().isBefore(now))
                            .map(checkout -> new OverdueCheckout(patron, checkout));
                })
                .collect(Collectors.toList());
    }
}
