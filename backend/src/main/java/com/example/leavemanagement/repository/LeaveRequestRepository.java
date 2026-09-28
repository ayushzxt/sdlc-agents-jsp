package com.example.leavemanagement.repository;

import com.example.leavemanagement.entity.LeaveRequest;
import com.example.leavemanagement.entity.LeaveRequestStatus;
import com.example.leavemanagement.entity.User;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Modifying;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, UUID> {
    List<LeaveRequest> findByEmployeeOrderBySubmittedAtDesc(User employee);
    List<LeaveRequest> findByApproverAndStatusInOrderBySubmittedAtDesc(User approver, Collection<LeaveRequestStatus> statuses);
    List<LeaveRequest> findByApproverAndStatusInAndStartDateGreaterThanEqualAndEndDateLessThanEqualOrderBySubmittedAtDesc(User approver, Collection<LeaveRequestStatus> statuses, LocalDate start, LocalDate end);
    @Query("select r from LeaveRequest r where r.approver = :approver and r.status in :statuses and (:startDate is null or r.startDate >= :startDate) and (:endDate is null or r.endDate <= :endDate) order by r.submittedAt desc")
    List<LeaveRequest> findByApproverAndStatusInAndOptionalDateRangeOrderBySubmittedAtDesc(@Param("approver") User approver,
        @Param("statuses") Collection<LeaveRequestStatus> statuses, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    @Modifying
    @Query("update LeaveRequest r set r.status = :status, r.decidedBy = :decidedBy, r.decisionComment = :comment, r.decidedAt = :decidedAt, r.updatedAt = :updatedAt, r.version = r.version + 1 where r.id = :id and r.approver = :approver and r.status = com.example.leavemanagement.entity.LeaveRequestStatus.PENDING")
    int decideIfPending(@Param("id") UUID id, @Param("approver") User approver, @Param("status") LeaveRequestStatus status,
                        @Param("decidedBy") User decidedBy, @Param("comment") String comment, @Param("decidedAt") java.time.Instant decidedAt,
                        @Param("updatedAt") java.time.Instant updatedAt);
    Optional<LeaveRequest> findByIdAndEmployee(UUID id, User employee);
    Optional<LeaveRequest> findByIdAndApprover(UUID id, User approver);
    @Query("select count(r) > 0 from LeaveRequest r where r.employee = :employee and r.status in :statuses and r.startDate <= :endDate and r.endDate >= :startDate and r.id <> :excludedId")
    boolean existsOverlapping(@Param("employee") User employee, @Param("statuses") Collection<LeaveRequestStatus> statuses,
                              @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, @Param("excludedId") UUID excludedId);
}