package in.ignishers.eduportal.service;

import in.ignishers.eduportal.dto.attendance.FaceProfileRequest;
import in.ignishers.eduportal.dto.attendance.FaceProfileResponse;
import in.ignishers.eduportal.exception.custom.ResourceNotFoundException;
import in.ignishers.eduportal.models.attendance.FaceProfile;
import in.ignishers.eduportal.models.people.Student;
import in.ignishers.eduportal.repos.attendance.FaceProfileRepository;
import in.ignishers.eduportal.repos.people.StudentRepository;
import in.ignishers.eduportal.util.BiometricUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FaceProfileService {

    private final FaceProfileRepository faceProfileRepo;
    private final StudentRepository studentRepo;

    @Transactional
    public FaceProfileResponse enrollOrUpdateFaceProfile(Long studentId, FaceProfileRequest request) {
        Student student = studentRepo.findByIdAndDeletedFalse(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));

        byte[] serializedEmbedding = BiometricUtils.toByteArray(request.embedding());

        FaceProfile profile = faceProfileRepo.findByStudentId(studentId)
                .orElseGet(() -> {
                    FaceProfile created = new FaceProfile();
                    created.setStudent(student);
                    return created;
                });

        profile.setEmbedding(serializedEmbedding);
        profile.setModelVersion(request.modelVersion().trim());
        profile.setActive(true);
        profile.setEnrolledAt(LocalDateTime.now());

        profile = faceProfileRepo.save(profile);
        return toResponse(profile, request.embedding().size());
    }

    @Transactional
    public FaceProfileResponse enrollMyFaceProfile(Long userId, FaceProfileRequest request) {
        Student student = studentRepo.findByUserIdAndDeletedFalse(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for current user"));
        return enrollOrUpdateFaceProfile(student.getId(), request);
    }

    @Transactional(readOnly = true)
    public FaceProfileResponse getFaceProfileByStudentId(Long studentId) {
        FaceProfile profile = faceProfileRepo.findByStudentId(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Face profile not found for student id: " + studentId));
        int dimension = profile.getEmbedding().length / Float.BYTES;
        return toResponse(profile, dimension);
    }

    @Transactional(readOnly = true)
    public FaceProfileResponse getMyFaceProfile(Long userId) {
        Student student = studentRepo.findByUserIdAndDeletedFalse(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for current user"));
        return getFaceProfileByStudentId(student.getId());
    }

    @Transactional
    public void deactivateFaceProfile(Long studentId) {
        FaceProfile profile = faceProfileRepo.findByStudentId(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Face profile not found for student id: " + studentId));
        profile.setActive(false);
        faceProfileRepo.save(profile);
    }

    private FaceProfileResponse toResponse(FaceProfile profile, int dimension) {
        Student s = profile.getStudent();
        return new FaceProfileResponse(
                profile.getId(),
                s.getId(),
                s.getEnrollmentNumber(),
                s.getFirstName() + (s.getLastName() != null ? " " + s.getLastName() : ""),
                profile.getModelVersion(),
                dimension,
                profile.isActive(),
                profile.getEnrolledAt()
        );
    }
}
