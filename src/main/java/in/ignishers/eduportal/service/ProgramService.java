package in.ignishers.eduportal.service;

import in.ignishers.eduportal.dto.curriculum.DepartmentProgramResponse;
import in.ignishers.eduportal.dto.curriculum.ProgramRequest;
import in.ignishers.eduportal.dto.curriculum.ProgramResponse;
import in.ignishers.eduportal.exception.custom.ResourceNotFoundException;
import in.ignishers.eduportal.models.institutional.Department;
import in.ignishers.eduportal.models.institutional.DepartmentProgram;
import in.ignishers.eduportal.models.institutional.Program;
import in.ignishers.eduportal.repos.institutional.DepartmentProgramRepository;
import in.ignishers.eduportal.repos.institutional.DepartmentRepository;
import in.ignishers.eduportal.repos.institutional.ProgramRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProgramService {

    private final ProgramRepository programRepo;
    private final DepartmentRepository departmentRepo;
    private final DepartmentProgramRepository departmentProgramRepo;

    @Transactional
    public ProgramResponse createProgram(ProgramRequest request) {
        String code = request.code().trim().toUpperCase();

        if(programRepo.existsByCode(code)) {
            throw new IllegalArgumentException("Program with Code '" + code + "' already exists");
        }

        Program program = Program.builder()
                .name(request.name().trim())
                .code(code)
                .build();

        program = programRepo.save(program);
        return toProgramResponse(program);
    }

    @Transactional(readOnly = true)
    public ProgramResponse getProgramById(Long id) {
        Program program = programRepo.findByIdAndDeletedFalse(id)
                .orElseThrow( () -> new ResourceNotFoundException("Program not found with id: " + id));

        return toProgramResponse(program);
    }

    @Transactional(readOnly = true)
    public List<ProgramResponse> getAllPrograms() {
        return programRepo.findAllByDeletedFalse()
                .stream()
                .map(this::toProgramResponse)
                .toList();
    }

    @Transactional
    public ProgramResponse updateProgram(Long id, ProgramRequest request) {
        Program program = programRepo.findByIdAndDeletedFalse(id)
                .orElseThrow( () -> new ResourceNotFoundException("Program not found with id: " +id));

        String newCode = request.code().trim().toUpperCase();
        if(!program.getCode().equalsIgnoreCase(newCode) && programRepo.existsByCode(newCode)) {
            throw new IllegalArgumentException("Program with code '" + newCode + "' already exists");
        }

        program.setName(request.name().trim());
        program.setCode(newCode);

        program = programRepo.save(program);

        return toProgramResponse(program);
    }

    @Transactional
    public void deleteProgram(Long id) {
        Program program = programRepo.findByIdAndDeletedFalse(id)
                .orElseThrow( () -> new ResourceNotFoundException("Program not found with id: " +id));

        program.setDeleted(true);
        programRepo.save(program);
    }

    // ==========================================
    // DEPARTMENT - PROGRAM ASSOCIATION
    // ==========================================

    @Transactional
    public DepartmentProgramResponse linkProgramToDepartment(Long departmentId, Long programId) {
        Department department = departmentRepo.findByIdAndDeletedFalse(departmentId)
                .orElseThrow( () -> new ResourceNotFoundException("Department not found with id: " + departmentId)) ;

        Program program = programRepo.findByIdAndDeletedFalse(programId)
                .orElseThrow( () -> new ResourceNotFoundException("Program not found with id: " + programId));

        if(departmentProgramRepo.existsByDepartmentIdAndProgramId(departmentId, programId)) {
            throw new IllegalArgumentException("Program '" + program.getCode()
                    + "' is already offered by department '" + department.getCode() + "'");
        }

        DepartmentProgram dp = new DepartmentProgram();
        dp.setDepartment(department);
        dp.setProgram(program);

        dp = departmentProgramRepo.save(dp);
        return toDeptProgramResponse(dp);

    }

    @Transactional(readOnly = true)
    public List<DepartmentProgramResponse> getProgramsByDepartment(Long deptId) {
        return departmentProgramRepo.findAllByDepartmentIdAndDeletedFalse(deptId)
                .stream()
                .map(this::toDeptProgramResponse)
                .toList();
    }

    @Transactional
    public void unlinkProgramFromDepartment(Long deptProgramId) {
        DepartmentProgram dp = departmentProgramRepo.findByIdAndDeletedFalse(deptProgramId)
                .orElseThrow(() -> new ResourceNotFoundException("DepartmentProgram mapping not found with id: " + deptProgramId));

        dp.setDeleted(true);
        departmentProgramRepo.save(dp);
    }

    private ProgramResponse toProgramResponse(Program program) {
        return new ProgramResponse(
                program.getId(),
                program.getName(),
                program.getCode(),
                program.getCreatedAt()
        );
    }

    private DepartmentProgramResponse toDeptProgramResponse(DepartmentProgram dp) {
        return new DepartmentProgramResponse(
                dp.getId(),
                dp.getDepartment().getId(),
                dp.getDepartment().getName(),
                dp.getDepartment().getCode(),
                dp.getProgram().getId(),
                dp.getProgram().getName(),
                dp.getProgram().getCode(),
                dp.getCreatedAt()
        );
    }

}
