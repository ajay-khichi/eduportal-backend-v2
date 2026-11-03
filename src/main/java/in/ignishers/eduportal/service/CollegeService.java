package in.ignishers.eduportal.service;

import in.ignishers.eduportal.dto.institutional.AssignCollegeAdminRequest;
import in.ignishers.eduportal.dto.institutional.CollegeRequest;
import in.ignishers.eduportal.dto.institutional.CollegeResponse;
import in.ignishers.eduportal.enums.UserRole;
import in.ignishers.eduportal.exception.custom.ResourceNotFoundException;
import in.ignishers.eduportal.models.auth.User;
import in.ignishers.eduportal.models.institutional.College;
import in.ignishers.eduportal.repos.auth.UserRepository;
import in.ignishers.eduportal.repos.institutional.CollegeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CollegeService {

    private final CollegeRepository collegeRepo;
    private final UserRepository userRepo;

    @Transactional
    public CollegeResponse createCollege(CollegeRequest request) {
        String code = request.code().trim().toUpperCase();

        if(collegeRepo.existsByCode(code)) {
            throw new IllegalArgumentException("College with code '" + code + "' already exists");
        }
        User user = null;
        if(request.adminEmail() != null && !request.adminEmail().isBlank()) {
            String email = request.adminEmail().trim().toLowerCase();
             user = userRepo.findByEmail(email)
                    .orElseThrow( () -> new ResourceNotFoundException("User not found with email: " + email));

            user.setRole(UserRole.COLLEGE_ADMIN);
            userRepo.save(user);
        }
        College college = College.builder()
                .address(request.address() != null ? request.address().trim() : null)
                .name(request.name().trim())
                .code(code)
                .admin(user)
                .build();

        college = collegeRepo.save(college);
        return toResponse(college);
    }

    @Transactional(readOnly = true)
    public CollegeResponse getCollegeById(Long id) {
        College college = collegeRepo.findByIdAndDeletedFalse(id)
                .orElseThrow( () -> new ResourceNotFoundException("College not found with id: " + id));
        return toResponse(college);
    }

    @Transactional(readOnly = true)
    public List<CollegeResponse> getAllColleges() {
        return collegeRepo.findAllByDeletedFalse()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public CollegeResponse updateCollege(Long id, CollegeRequest request) {
        College college = collegeRepo.findByIdAndDeletedFalse(id)
                .orElseThrow( () -> new ResourceNotFoundException("College not found with id: " + id));

        String newCode = request.code().trim().toUpperCase();
        if(!college.getCode().equalsIgnoreCase(newCode) && collegeRepo.existsByCode(newCode)){
            throw new IllegalArgumentException("College with code: '" + newCode + "' already exists");
        }

        college.setName(request.name().trim());
        college.setCode(newCode);
        college.setAddress(request.address() != null ? request.address().trim() : null);

        if (request.adminEmail() != null && !request.adminEmail().isBlank()) {
            String email = request.adminEmail().trim().toLowerCase();
            User user = userRepo.findByEmail(email)
                    .orElseThrow( () -> new ResourceNotFoundException("User not found with email: " + email));
            user.setRole(UserRole.COLLEGE_ADMIN);
            userRepo.save(user);
            college.setAdmin(user);
        }

        college = collegeRepo.save(college);
        return toResponse(college);
    }

    @Transactional
    public void deleteCollege(Long id) {
        College college = collegeRepo.findByIdAndDeletedFalse(id)
                .orElseThrow( () -> new ResourceNotFoundException("College not found with id: " + id));

        college.setDeleted(true);
        collegeRepo.save(college);
    }

    @Transactional
    public CollegeResponse assignCollegeAdmin(Long collegeId, AssignCollegeAdminRequest request) {
        College college = collegeRepo.findByIdAndDeletedFalse(collegeId)
                .orElseThrow( () -> new ResourceNotFoundException("College not found with id: " + collegeId));

        String email = request.adminEmail().trim().toLowerCase();
        User user = userRepo.findByEmail(email)
                .orElseThrow( () -> new ResourceNotFoundException("User not found with email: " + email));

        user.setRole(UserRole.COLLEGE_ADMIN);
        userRepo.save(user);

        college.setAdmin(user);
        collegeRepo.save(college);

        return toResponse(college);
    }

    private CollegeResponse toResponse(College college) {
        Long adminId = college.getAdmin() != null ? college.getAdmin().getId() : null;
        String adminEmail = college.getAdmin() != null ? college.getAdmin().getEmail() : null;

        return new CollegeResponse(
                college.getId(),
                college.getName(),
                college.getCode(),
                college.getAddress(),
                adminId,
                adminEmail,
                college.getCreatedAt()
        );
    }
}
