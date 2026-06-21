package com.dmt.backend.metadata.dropdown.service;

import com.dmt.backend.common.exception.ApiException;
import com.dmt.backend.metadata.dropdown.dto.DropdownRequest;
import com.dmt.backend.metadata.dropdown.dto.DropdownResponse;
import com.dmt.backend.metadata.dropdown.entity.DmtDropdown;
import com.dmt.backend.metadata.dropdown.repository.DmtDropdownRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DropdownService {

    private final DmtDropdownRepository repository;

    public DropdownResponse create(
            DropdownRequest request) {

        log.info(
                "Creating dropdown {}",
                request.dropdownCode());

        if (repository.existsByDropdownCode(
                request.dropdownCode())) {

            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Dropdown already exists");
        }

        DmtDropdown dropdown =
                DmtDropdown.builder()
                        .dropdownCode(
                                request.dropdownCode())
                        .dropdownName(
                                request.dropdownName())
                        .query(
                                request.query())
                        .active(
                                request.active())
                        .build();

        dropdown =
                repository.save(dropdown);

        return map(dropdown);
    }

    public List<DropdownResponse> getAll() {

        return repository.findAll()
                .stream()
                .map(this::map)
                .toList();
    }

    public DropdownResponse getById(
            Long id) {

        return repository.findById(id)
                .map(this::map)
                .orElseThrow(() ->
                        new ApiException(
                                HttpStatus.NOT_FOUND,
                                "Dropdown not found"));
    }

    public void delete(Long id) {

        log.info(
                "Deleting dropdown {}",
                id);

        if (!repository.existsById(id)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Dropdown not found");
        }

        repository.deleteById(id);
    }

    private DropdownResponse map(
            DmtDropdown dropdown) {

        return new DropdownResponse(
                dropdown.getId(),
                dropdown.getDropdownCode(),
                dropdown.getDropdownName(),
                dropdown.getQuery(),
                dropdown.getActive()
        );
    }
}