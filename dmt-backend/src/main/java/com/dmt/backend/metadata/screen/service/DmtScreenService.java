package com.dmt.backend.metadata.screen.service;

import com.dmt.backend.metadata.screen.dto.ScreenRequest;
import com.dmt.backend.metadata.screen.dto.ScreenResponse;
import com.dmt.backend.metadata.screen.entity.DmtScreen;
import com.dmt.backend.metadata.screen.repository.DmtScreenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DmtScreenService {

    private final DmtScreenRepository repository;

    public ScreenResponse create(ScreenRequest request) {

        log.info("Create screen requested screenCode={}", request.screenCode());

        if (repository.existsByScreenCode(request.screenCode())) {
            log.warn("Create screen failed screenCode={} reason=screen_code_exists", request.screenCode());
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

        log.info("Screen created id={} screenCode={}", saved.getId(), saved.getScreenCode());

        return map(saved);
    }

    public List<ScreenResponse> getAll() {

        List<ScreenResponse> screens = repository.findAll()
                .stream()
                .map(this::map)
                .toList();

        log.info("Screens fetched count={}", screens.size());

        return screens;
    }

    public ScreenResponse getById(Long id) {

        DmtScreen screen = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Get screen failed id={} reason=screen_not_found", id);
                    return new RuntimeException("Screen not found");
                });

        log.info("Screen fetched id={} screenCode={}", id, screen.getScreenCode());

        return map(screen);
    }

    public void delete(Long id) {

        repository.deleteById(id);

        log.info("Screen deleted id={}", id);
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
