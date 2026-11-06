package in.ignishers.eduportal.service;

import in.ignishers.eduportal.dto.institutional.AssignHodRequest;
import in.ignishers.eduportal.dto.institutional.DepartmentRequest;
import in.ignishers.eduportal.dto.institutional.DepartmentResponse;
import in.ignishers.eduportal.enums.FacultyRole;
import in.ignishers.eduportal.exception.custom.ResourceNotFoundException;
import in.ignishers.eduportal.models.institutional.College;
import in.ignishers.eduportal.models.institutional.Department;
import in.ignishers.eduportal.models.people.Faculty;
import in.ignishers.eduportal.repos.institutional.CollegeRepository;
import in.ignishers.eduportal.repos.institutional.DepartmentRepository;
import in.ignishers.eduportal.repos.people.FacultyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepo;
    private final CollegeRepository collegeRepo;
    private final FacultyRepository facultyRepo;

    @Transactional
    public DepartmentResponse createDepartment(Long collegeId, DepartmentRequest request) {
        College college = collegeRepo.findByIdAndDeletedFalse(collegeId)
                .orElseThrow( () -> new ResourceNotFoundException("College not found with id:  " + collegeId));

        String code = request.code().trim().toUpperCase();

        if(departmentRepo.existsByCollegeIdAndCode(collegeId, code)) {
            throw new IllegalArgumentException("Department with code '" + code + "' already exists in this college");
        }

        Department dept = new Department();
        dept.setName(request.name().trim());
        dept.setCollege(college);
        dept.setCode(code);

        // Optional: if user want to set HOD at time of creation of department
        if (request.hodEmail() != null && !request.hodEmail().isBlank()) {
            Faculty hod = findAndPromoteHod(request.hodEmail().trim().toLowerCase(), collegeId);
            dept.setHeadOfDepartment(hod);
        }

        dept = departmentRepo.save(dept);
        return toResponse(dept);
    }

    @Transactional(readOnly = true)
    public DepartmentResponse getDepartmentById(Long id) {
        Department dept = departmentRepo.findByIdAndDeletedFalse(id)
                .orElseThrow( () -> new ResourceNotFoundException("Department not found with id: "+ id));
        return toResponse(dept);
    }

    @Transactional(readOnly = true)
    public List<DepartmentResponse> getDepartmentsByCollege(Long collegeId) {
        return departmentRepo.findAllByCollegeIdAndDeletedFalse(collegeId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public DepartmentResponse updateDepartment(Long id, DepartmentRequest request) {
            Department dept = departmentRepo.findByIdAndDeletedFalse(id)
                    .orElseThrow( () -> new ResourceNotFoundException("Department not found with id: " + id));
            String newCode = request.code().trim().toUpperCase();
            Long collegeId = dept.getCollege().getId();

            if(!dept.getCode().equalsIgnoreCase(newCode) &&
            departmentRepo.existsByCollegeIdAndCode(collegeId, newCode)) {
                throw new IllegalArgumentException("Department with code '" + newCode + "' already exists in this college");
            }

            dept.setName(request.name().trim());
            dept.setCode(newCode);

            if (request.hodEmail() != null && !request.hodEmail().isBlank()) {
                Faculty hod = findAndPromoteHod(request.hodEmail().trim().toLowerCase(), collegeId);
                dept.setHeadOfDepartment(hod);
            }
            dept = departmentRepo.save(dept);
            return toResponse(dept);
    }

    @Transactional
    public void deleteDepartment(Long id) {
        Department department = departmentRepo.findByIdAndDeletedFalse(id)
                .orElseThrow( () -> new ResourceNotFoundException("Department not found with id: " + id));

        department.setDeleted(true);
        departmentRepo.save(department);
    }

    @Transactional
    public  DepartmentResponse assignHod(Long departmentId, AssignHodRequest request) {
        Department department = departmentRepo.findByIdAndDeletedFalse(departmentId)
                .orElseThrow( () -> new ResourceNotFoundException("Department not found with id: " + departmentId));

        Faculty hod  = findAndPromoteHod(request.hodEmail().trim().toLowerCase(), department.getCollege().getId());
        department.setHeadOfDepartment(hod);

        department = departmentRepo.save(department);
        return toResponse(department);
    }

    private Faculty findAndPromoteHod(String email, Long collegeId) {
        Faculty faculty = facultyRepo.findByUserEmailIgnoreCaseAndDeletedFalse(email)
                .orElseThrow( () -> new ResourceNotFoundException("Faculty not found with email: "+ email));

        if(!faculty.getCollege().getId().equals(collegeId)) {
            throw new IllegalArgumentException("Faculty does not belong to this college");
        }

        faculty.setRole(FacultyRole.HOD);
        return facultyRepo.save(faculty);
    }

    private DepartmentResponse toResponse(Department dept) {
        Long hodId = null;
        String hodName = null;
        String hodEmail = null;
        if(dept.getHeadOfDepartment() != null){
            Faculty hod = dept.getHeadOfDepartment();
            hodId = hod.getId();
            hodEmail = hod.getUser().getEmail();
            hodName = hod.getFirstName() + (hod.getLastName() != null ? " " + hod.getLastName() : "");
        }
        return new DepartmentResponse(
                dept.getId(),
                dept.getCollege().getId(),
                dept.getCollege().getName(),
                dept.getName(),
                dept.getCode(),
                hodId,
                hodName,
                hodEmail,
                dept.getCreatedAt()
        );
    }
}
