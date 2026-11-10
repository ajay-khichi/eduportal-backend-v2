package in.ignishers.eduportal.controller;

import in.ignishers.eduportal.dto.people.StudentRequest;
import in.ignishers.eduportal.dto.people.StudentResponse;
import in.ignishers.eduportal.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @PostMapping("/colleges/{collegeId}/students")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'FACULTY')")
    public ResponseEntity<StudentResponse> createStudent(
            @PathVariable Long collegeId,
            @Valid @RequestBody StudentRequest request
    ) {
        StudentResponse response = studentService.createStudent(collegeId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/colleges/{collegeId}/students")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'FACULTY')")
    public ResponseEntity<List<StudentResponse>> getStudentsByCollege(
            @PathVariable Long collegeId
    ) {
        return ResponseEntity.ok(studentService.getStudentsByCollege(collegeId));
    }

    @GetMapping("/departments/{departmentId}/students")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'FACULTY')")
    public ResponseEntity<List<StudentResponse>> getStudentsByDepartment(
            @PathVariable Long departmentId
    ) {
        return ResponseEntity.ok(studentService.getStudentsByDepartment(departmentId));
    }

    @GetMapping("/students/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'FACULTY', 'STUDENT')")
    public ResponseEntity<StudentResponse> getStudentById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(studentService.getStudentById(id));
    }

    @PutMapping("/students/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<StudentResponse> updateStudent(
            @PathVariable Long id,
            @Valid @RequestBody StudentRequest request
    ) {
        return ResponseEntity.ok(studentService.updateStudent(id, request));
    }

    @DeleteMapping("/students/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<Void> deleteStudent(
            @PathVariable Long id
    ) {
        studentService.deleteStudent(id);
        return ResponseEntity.noContent().build();
    }
}