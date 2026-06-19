package com.dmt.backend.metadata.screen.service;

import com.dmt.backend.metadata.screen.dto.ScreenRequest;
import com.dmt.backend.metadata.screen.dto.ScreenResponse;
import com.dmt.backend.metadata.screen.entity.DmtScreen;
import com.dmt.backend.metadata.screen.repository.DmtScreenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DmtScreenService {

    private final DmtScreenRepository repository;

    public ScreenResponse create(ScreenRequest request) {

        if (repository.existsByScreenCode(request.screenCode())) {
            throw new RuntimeException(
                    "Screen code already exists");
        }

        DmtScreen screen = DmtScreen.builder()
                .screenCode(request.screenCode())
                .screenName(request.screenName())
                .description(request.description())
                .selectQuery(request.selectQuery())
                .defaultPageSize(request.defaultPageSize())
                .defaultSortColumn(request.defaultSortColumn())
                .defaultSortDirection(request.defaultSortDirection())
                .active(request.active())
                .build();

        DmtScreen saved = repository.save(screen);

        return map(saved);
    }

    public List<ScreenResponse> getAll() {

        return repository.findAll()
                .stream()
                .map(this::map)
                .toList();
    }

    public ScreenResponse getById(Long id) {

        DmtScreen screen = repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Screen not found"));

        return map(screen);
    }

    public void delete(Long id) {

        repository.deleteById(id);
    }

    private ScreenResponse map(DmtScreen screen) {

        return new ScreenResponse(
                screen.getId(),
                screen.getScreenCode(),
                screen.getScreenName(),
                screen.getDescription(),
                screen.getSelectQuery(),
                screen.getDefaultPageSize(),
                screen.getDefaultSortColumn(),
                screen.getDefaultSortDirection(),
                screen.getActive()
        );
    }
}