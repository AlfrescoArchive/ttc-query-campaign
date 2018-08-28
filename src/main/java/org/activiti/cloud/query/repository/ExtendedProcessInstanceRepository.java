package org.activiti.cloud.query.repository;

import java.util.Date;
import java.util.List;

import org.activiti.cloud.services.query.app.repository.ProcessInstanceRepository;
import org.activiti.cloud.services.query.model.ProcessInstanceEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExtendedProcessInstanceRepository extends ProcessInstanceRepository {

    @Query("select pi from ProcessInstance pi where pi.status='COMPLETED' and pi.businessKey= :campaign and not exists ( " +
            "select v from Variable v where v.name= 'matched' and v.processInstance = pi)")
    Page<ProcessInstanceEntity> findAllCompletedAndDiscarded(@Param("campaign") String campaign,
                                                     Pageable pageable);

    @Query("select pi from ProcessInstance pi where pi.status='RUNNING' and pi.businessKey= :campaign and not exists ( " +
            "select v from Variable v where v.name= 'matched' and v.processInstance = pi)")
    Page<ProcessInstanceEntity> findAllInFlight(@Param("campaign") String campaign,
                                                       Pageable pageable);

    @Query("select pi from ProcessInstance pi where pi.status='COMPLETED' and pi.businessKey= :campaign and pi.lastModified > :since and not exists ( " +
            "select v from Variable v where v.name= 'matched' and v.processInstance = pi) order by pi.lastModified desc")
    List<ProcessInstanceEntity> findAllCompletedAndDiscardedSince(@Param("campaign") String campaign, @Param("since") Date since);
}
