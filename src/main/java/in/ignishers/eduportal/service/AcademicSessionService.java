package in.ignishers.eduportal.service;

import in.ignishers.eduportal.dto.curriculum.AcademicSessionRequest;
import in.ignishers.eduportal.dto.curriculum.AcademicSessionResponse;
import in.ignishers.eduportal.exception.custom.ResourceNotFoundException;
import in.ignishers.eduportal.models.curriculum.AcademicSession;
import in.ignishers.eduportal.models.institutional.College;
import in.ignishers.eduportal.repos.curriculum.AcademicSessionRepository;
import in.ignishers.eduportal.repos.institutional.CollegeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AcademicSessionService {

    private final AcademicSessionRepository sessionRepo;
    private final CollegeRepository collegeRepo;

    @Transactional
    public AcademicSessionResponse createAcademicSession(Long collegeId, AcademicSessionRequest request) {
        College college = collegeRepo.findByIdAndDeletedFalse(collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("College not found with id: " + collegeId));

        validateDates(request);

        String sessionName = request.name().trim();
        if (sessionRepo.existsByCollegeIdAndNameIgnoreCaseAndDeletedFalse(collegeId, sessionName)) {
            throw new IllegalArgumentException("Academic session '" + sessionName + "' already exists for this college");
        }

        boolean isCurrent = Boolean.TRUE.equals(request.current());
        if (isCurrent) {
            sessionRepo.resetCurrentSessionsForCollege(collegeId);
        }

        AcademicSession session = new AcademicSession();
        session.setCollege(college);
        session.setName(sessionName);
        session.setStartDate(request.startDate());
        session.setEndDate(request.endDate());
        session.setCurrent(isCurrent);

        session = sessionRepo.save(session);
        return toResponse(session);
    }

    @Transactional(readOnly = true)
    public List<AcademicSessionResponse> getAcademicSessionsByCollege(Long collegeId) {
        if (!collegeRepo.existsById(collegeId)) {
            throw new ResourceNotFoundException("College not found with id: " + collegeId);
        }
        return sessionRepo.findAllByCollegeIdAndDeletedFalse(collegeId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AcademicSessionResponse getAcademicSessionById(Long id) {
        AcademicSession session = sessionRepo.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Academic session not found with id: " + id));
        return toResponse(session);
    }

    @Transactional(readOnly = true)
    public AcademicSessionResponse getCurrentAcademicSession(Long collegeId) {
        AcademicSession session = sessionRepo.findByCollegeIdAndCurrentTrueAndDeletedFalse(collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("No active academic session found for college id: " + collegeId));
        return toResponse(session);
    }

    @Transactional
    public AcademicSessionResponse updateAcademicSession(Long id, AcademicSessionRequest request) {
        AcademicSession session = sessionRepo.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Academic session not found with id: " + id));

        validateDates(request);

        String newName = request.name().trim();
        if (!session.getName().equalsIgnoreCase(newName) &&
                sessionRepo.existsByCollegeIdAndNameIgnoreCaseAndDeletedFalse(session.getCollege().getId(), newName)) {
            throw new IllegalArgumentException("Academic session '" + newName + "' already exists for this college");
        }

        boolean markCurrent = Boolean.TRUE.equals(request.current());
        if (markCurrent && !session.isCurrent()) {
            sessionRepo.resetCurrentSessionsForCollege(session.getCollege().getId());
            session.setCurrent(true);
        } else if (request.current() != null && !markCurrent) {
            session.setCurrent(false);
        }

        session.setName(newName);
        session.setStartDate(request.startDate());
        session.setEndDate(request.endDate());

        return toResponse(session);
    }

    @Transactional
    public AcademicSessionResponse setCurrentSession(Long collegeId, Long sessionId) {
        AcademicSession session = sessionRepo.findByIdAndDeletedFalse(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Academic session not found with id: " + sessionId));

        if (!session.getCollege().getId().equals(collegeId)) {
            throw new IllegalArgumentException("Session id " + sessionId + " does not belong to college id " + collegeId);
        }

        sessionRepo.resetCurrentSessionsForCollege(collegeId);
        session.setCurrent(true);

        return toResponse(session);
    }

    @Transactional
    public void deleteAcademicSession(Long id) {
        AcademicSession session = sessionRepo.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Academic session not found with id: " + id));
        session.setDeleted(true);
    }

    private void validateDates(AcademicSessionRequest request) {
        if (request.startDate().isAfter(request.endDate()) || request.startDate().isEqual(request.endDate())) {
            throw new IllegalArgumentException("Start date must be strictly before end date");
        }
    }

    private AcademicSessionResponse toResponse(AcademicSession session) {
        return new AcademicSessionResponse(
                session.getId(),
                session.getCollege().getId(),
                session.getCollege().getName(),
                session.getName(),
                session.getStartDate(),
                session.getEndDate(),
                session.isCurrent(),
                session.getCreatedAt(),
                session.getUpdatedAt()
        );
    }
}