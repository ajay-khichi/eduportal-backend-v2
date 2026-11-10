package in.ignishers.eduportal.service;

import in.ignishers.eduportal.dto.people.FacultyRequest;
import in.ignishers.eduportal.dto.people.FacultyResponse;
import in.ignishers.eduportal.enums.FacultyRole;
import in.ignishers.eduportal.enums.UserRole;
import in.ignishers.eduportal.exception.custom.EmailAlreadyExistsException;
import in.ignishers.eduportal.exception.custom.ResourceNotFoundException;
import in.ignishers.eduportal.models.auth.User;
import in.ignishers.eduportal.models.institutional.College;
import in.ignishers.eduportal.models.institutional.Department;
import in.ignishers.eduportal.models.people.Faculty;
import in.ignishers.eduportal.repos.auth.UserRepository;
import in.ignishers.eduportal.repos.institutional.CollegeRepository;
import in.ignishers.eduportal.repos.institutional.DepartmentRepository;
import in.ignishers.eduportal.repos.people.FacultyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FacultyService {

    private final FacultyRepository facultyRepo;
    private final UserRepository userRepo;
    private final CollegeRepository collegeRepo;
    private final DepartmentRepository deptRepo;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public FacultyResponse createFaculty(Long collegeId, FacultyRequest request) {
        College college = collegeRepo.findByIdAndDeletedFalse(collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("College not found with id: " + collegeId));

        Department dept = deptRepo.findByIdAndDeletedFalse(request.departmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + request.departmentId()));

        if(!dept.getCollege().getId().equals(collegeId)) {
            throw new IllegalArgumentException("Department id " + request.departmentId()
                    + " does not belong to college id " + collegeId);
        }

        String email = request.email().trim().toLowerCase();
        if(userRepo.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("An account with email '" + email + "' already exists");
        }

        String empNumber = request.employeeNumber().trim();
        if (facultyRepo.existsByCollegeIdAndEmployeeNumberIgnoreCaseAndDeletedFalse(collegeId, empNumber)) {
            throw new IllegalArgumentException("Faculty with employee number '" + empNumber + "' already exists in this college");
        }

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(UserRole.FACULTY);
        user.setActive(true);
        user = userRepo.save(user);

        Faculty faculty = new Faculty();
        faculty.setUser(user);
        faculty.setCollege(college);
        faculty.setDepartment(dept);
        faculty.setEmployeeNumber(empNumber);
        faculty.setFirstName(request.firstName().trim());
        faculty.setLastName(request.lastName() != null ? request.lastName().trim() : null);
        faculty.setPhone(request.phone() != null ? request.phone().trim() : null);
        faculty.setDesignation(request.designation() != null ? request.designation().trim() : null);
        faculty.setRole(request.role() != null ? request.role() : FacultyRole.TEACHER);

        faculty = facultyRepo.save(faculty);
        return toResponse(faculty);
    }

    @Transactional(readOnly = true)
    public List<FacultyResponse> getFacultyByCollege(Long collegeId) {
        if (!collegeRepo.existsById(collegeId)) {
            throw new ResourceNotFoundException("College not found with id: " + collegeId);
        }
        return facultyRepo.findAllByCollegeIdAndDeletedFalse(collegeId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FacultyResponse> getFacultyByDepartment(Long departmentId) {
        if (!deptRepo.existsById(departmentId)) {
            throw new ResourceNotFoundException("Department not found with id: " + departmentId);
        }
        return facultyRepo.findAllByDepartmentIdAndDeletedFalse(departmentId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public FacultyResponse getFacultyById(Long id) {
        Faculty faculty = facultyRepo.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found with id: " + id));
        return toResponse(faculty);
    }

    @Transactional
    public FacultyResponse updateFaculty(Long id, FacultyRequest request) {
        Faculty faculty = facultyRepo.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found with id: " + id));

        Department department = deptRepo.findByIdAndDeletedFalse(request.departmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + request.departmentId()));

        if (!department.getCollege().getId().equals(faculty.getCollege().getId())) {
            throw new IllegalArgumentException("Department does not belong to this college");
        }

        String newEmpNumber = request.employeeNumber().trim();
        if (!faculty.getEmployeeNumber().equalsIgnoreCase(newEmpNumber) &&
                facultyRepo.existsByCollegeIdAndEmployeeNumberIgnoreCaseAndDeletedFalse(faculty.getCollege().getId(), newEmpNumber)) {
            throw new IllegalArgumentException("Faculty with employee number '" + newEmpNumber + "' already exists in this college");
        }

        faculty.setDepartment(department);
        faculty.setEmployeeNumber(newEmpNumber);
        faculty.setFirstName(request.firstName().trim());
        faculty.setLastName(request.lastName() != null ? request.lastName().trim() : null);
        faculty.setPhone(request.phone() != null ? request.phone().trim() : null);
        faculty.setDesignation(request.designation() != null ? request.designation().trim() : null);
        if (request.role() != null) {
            faculty.setRole(request.role());
        }

        return toResponse(faculty);
    }

    @Transactional
    public void deleteFaculty(Long id) {
        Faculty faculty = facultyRepo.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found with id: " + id));

        faculty.setDeleted(true);
        facultyRepo.save(faculty);

        if (faculty.getUser() != null) {
            faculty.getUser().setActive(false);
            userRepo.save(faculty.getUser());
        }
    }

    private FacultyResponse toResponse(Faculty faculty) {
        return new FacultyResponse(
                faculty.getId(),
                faculty.getUser().getId(),
                faculty.getUser().getEmail(),
                faculty.getCollege().getId(),
                faculty.getCollege().getName(),
                faculty.getDepartment().getId(),
                faculty.getDepartment().getName(),
                faculty.getEmployeeNumber(),
                faculty.getFirstName(),
                faculty.getLastName(),
                faculty.getPhone(),
                faculty.getDesignation(),
                faculty.getRole(),
                faculty.getCreatedAt(),
                faculty.getUpdatedAt()
        );
    }
}
