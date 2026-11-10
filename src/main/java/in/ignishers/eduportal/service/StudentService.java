package in.ignishers.eduportal.service;

import in.ignishers.eduportal.dto.people.StudentRequest;
import in.ignishers.eduportal.dto.people.StudentResponse;
import in.ignishers.eduportal.enums.UserRole;
import in.ignishers.eduportal.exception.custom.EmailAlreadyExistsException;
import in.ignishers.eduportal.exception.custom.ResourceNotFoundException;
import in.ignishers.eduportal.models.auth.User;
import in.ignishers.eduportal.models.institutional.College;
import in.ignishers.eduportal.models.institutional.Department;
import in.ignishers.eduportal.models.people.Student;
import in.ignishers.eduportal.repos.auth.UserRepository;
import in.ignishers.eduportal.repos.institutional.CollegeRepository;
import in.ignishers.eduportal.repos.institutional.DepartmentRepository;
import in.ignishers.eduportal.repos.people.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepo;
    private final UserRepository userRepo;
    private final CollegeRepository collegeRepo;
    private final DepartmentRepository departmentRepo;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public StudentResponse createStudent(Long collegeId, StudentRequest request) {
        College college = collegeRepo.findByIdAndDeletedFalse(collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("College not found with id: " + collegeId));

        Department department = departmentRepo.findByIdAndDeletedFalse(request.departmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + request.departmentId()));

        if (!department.getCollege().getId().equals(collegeId)) {
            throw new IllegalArgumentException("Department id " + request.departmentId() + " does not belong to college id " + collegeId);
        }

        String email = request.email().trim().toLowerCase();
        if (userRepo.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("An account with email '" + email + "' already exists");
        }

        String enrollmentNo = request.enrollmentNumber().trim();
        if (studentRepo.existsByCollegeIdAndEnrollmentNumberIgnoreCaseAndDeletedFalse(collegeId, enrollmentNo)) {
            throw new IllegalArgumentException("Student with enrollment number '" + enrollmentNo + "' already exists in this college");
        }

        // 1. Create User account
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(UserRole.STUDENT);
        user.setActive(true);
        user = userRepo.save(user);

        // 2. Create Student Profile
        Student student = new Student();
        student.setUser(user);
        student.setCollege(college);
        student.setDepartment(department);
        student.setEnrollmentNumber(enrollmentNo);
        student.setFirstName(request.firstName().trim());
        student.setLastName(request.lastName() != null ? request.lastName().trim() : null);
        student.setPhone(request.phone() != null ? request.phone().trim() : null);
        student.setAdmissionYear(request.admissionYear());

        student = studentRepo.save(student);
        return toResponse(student);
    }

    @Transactional(readOnly = true)
    public List<StudentResponse> getStudentsByCollege(Long collegeId) {
        if (!collegeRepo.existsById(collegeId)) {
            throw new ResourceNotFoundException("College not found with id: " + collegeId);
        }
        return studentRepo.findAllByCollegeIdAndDeletedFalse(collegeId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<StudentResponse> getStudentsByDepartment(Long departmentId) {
        if (!departmentRepo.existsById(departmentId)) {
            throw new ResourceNotFoundException("Department not found with id: " + departmentId);
        }
        return studentRepo.findAllByDepartmentIdAndDeletedFalse(departmentId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public StudentResponse getStudentById(Long id) {
        Student student = studentRepo.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));
        return toResponse(student);
    }

    @Transactional
    public StudentResponse updateStudent(Long id, StudentRequest request) {
        Student student = studentRepo.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));

        Department department = departmentRepo.findByIdAndDeletedFalse(request.departmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + request.departmentId()));

        if (!department.getCollege().getId().equals(student.getCollege().getId())) {
            throw new IllegalArgumentException("Department does not belong to this college");
        }

        String newEnrollmentNo = request.enrollmentNumber().trim();
        if (!student.getEnrollmentNumber().equalsIgnoreCase(newEnrollmentNo) &&
                studentRepo.existsByCollegeIdAndEnrollmentNumberIgnoreCaseAndDeletedFalse(student.getCollege().getId(), newEnrollmentNo)) {
            throw new IllegalArgumentException("Student with enrollment number '" + newEnrollmentNo + "' already exists in this college");
        }

        student.setDepartment(department);
        student.setEnrollmentNumber(newEnrollmentNo);
        student.setFirstName(request.firstName().trim());
        student.setLastName(request.lastName() != null ? request.lastName().trim() : null);
        student.setPhone(request.phone() != null ? request.phone().trim() : null);
        student.setAdmissionYear(request.admissionYear());

        return toResponse(student);
    }

    @Transactional
    public void deleteStudent(Long id) {
        Student student = studentRepo.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));

        student.setDeleted(true);
        studentRepo.save(student);

        if (student.getUser() != null) {
            student.getUser().setActive(false);
            userRepo.save(student.getUser());
        }
    }

    private StudentResponse toResponse(Student student) {
        return new StudentResponse(
                student.getId(),
                student.getUser().getId(),
                student.getUser().getEmail(),
                student.getCollege().getId(),
                student.getCollege().getName(),
                student.getDepartment().getId(),
                student.getDepartment().getName(),
                student.getEnrollmentNumber(),
                student.getFirstName(),
                student.getLastName(),
                student.getPhone(),
                student.getAdmissionYear(),
                student.getCreatedAt(),
                student.getUpdatedAt()
        );
    }
}