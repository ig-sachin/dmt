package com.dmt.backend.metadata.dropdownparam.service;

import com.dmt.backend.common.exception.ApiException;
import com.dmt.backend.metadata.dropdown.entity.DmtDropdown;
import com.dmt.backend.metadata.dropdown.repository.DmtDropdownRepository;
import com.dmt.backend.metadata.dropdownparam.dto.DropdownParamRequest;
import com.dmt.backend.metadata.dropdownparam.dto.DropdownParamResponse;
import com.dmt.backend.metadata.dropdownparam.entity.DmtDropdownParam;
import com.dmt.backend.metadata.dropdownparam.repository.DmtDropdownParamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DropdownParamService {

    private final DmtDropdownRepository dropdownRepository;
    private final DmtDropdownParamRepository repository;

    public DropdownParamResponse create(
            DropdownParamRequest request) {

        DmtDropdown dropdown =
                dropdownRepository
                        .findById(request.dropdownId())
                        .orElseThrow(() ->
                                new ApiException(
                                        HttpStatus.NOT_FOUND,
                                        "Dropdown not found"));

        DmtDropdownParam param =
                DmtDropdownParam.builder()
                        .dropdown(dropdown)
                        .parameterName(
                                request.parameterName())
                        .requestField(
                                request.requestField())
                        .required(
                                request.required())
                        .parameterOrder(
                                request.parameterOrder())
                        .build();

        param = repository.save(param);

        log.info(
                "Dropdown parameter created: {}",
                param.getParameterName());

        return map(param);
    }

    @Transactional(readOnly = true)
    public List<DropdownParamResponse>
    getByDropdown(Long dropdownId) {

        return repository
                .findByDropdownIdOrderByParameterOrderAsc(
                        dropdownId)
                .stream()
                .map(this::map)
                .toList();
    }

    public void delete(Long id) {

        repository.deleteById(id);

        log.info(
                "Dropdown parameter deleted: {}",
                id);
    }

    private DropdownParamResponse map(
            DmtDropdownParam param) {

        return new DropdownParamResponse(
                param.getId(),
                param.getDropdown().getId(),
                param.getParameterName(),
                param.getRequestField(),
                param.getRequired(),
                param.getParameterOrder()
        );
    }
}
