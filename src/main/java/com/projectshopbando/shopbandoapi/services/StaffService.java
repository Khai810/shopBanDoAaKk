package com.projectshopbando.shopbandoapi.services;

import com.projectshopbando.shopbandoapi.dtos.request.CreateAccountReq;
import com.projectshopbando.shopbandoapi.dtos.request.CreateStaffReq;
import com.projectshopbando.shopbandoapi.dtos.request.UpdateAccountReq;
import com.projectshopbando.shopbandoapi.dtos.request.UpdateStaffReq;
import com.projectshopbando.shopbandoapi.entities.Staff;
import com.projectshopbando.shopbandoapi.mappers.StaffMapper;
import com.projectshopbando.shopbandoapi.repositories.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Validated
@Service
@RequiredArgsConstructor
public class StaffService {
    private final StaffRepository staffRepository;
    private final StaffMapper staffMapper;
    private final AccountService accountService;

    // Get staff by id
    public Staff getStaffById(String id){
        return staffRepository.findById(id).orElseThrow(() -> new RuntimeException("Staff not found with id: " + id));
    }

    // Create new staff along with an account
    public Staff createStaff(CreateStaffReq req) throws BadRequestException {
        if(staffRepository.existsByAccount_Email(req.getEmail())){
            throw new BadRequestException("Staff already exists with email: " + req.getEmail());
        }
        Staff staff = staffMapper.toStaff(req);
        CreateAccountReq createAccountReq = new CreateAccountReq(req.getEmail(), req.getDob(), req.getPassword());
        staff.setAccount(accountService.createAccount(createAccountReq, null, staff));
        return staffRepository.save(staff);
    }

    public Page<Staff> getAllStaffs(int page, int size, String search){
        Pageable pageable = PageRequest.of(page, size);
        return staffRepository.findAllStaffWithSearch(search, pageable);
    }

    public Staff updateStaff(String id, UpdateStaffReq req) {
        Staff staff = getStaffById(id);
        staffMapper.updateStaff(staff, req);
        UpdateAccountReq updateAccountReq = new UpdateAccountReq(req.getEmail(), req.getRole(), req.getDob());
        accountService.updateAccount(updateAccountReq, staff.getAccount().getId());
        return staffRepository.save(staff);
    }
}
