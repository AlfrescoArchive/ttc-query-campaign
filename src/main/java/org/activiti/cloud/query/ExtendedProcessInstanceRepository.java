package org.activiti.cloud.query;

import org.activiti.cloud.services.query.app.repository.ProcessInstanceRepository;
import org.activiti.cloud.services.query.model.ProcessInstance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExtendedProcessInstanceRepository extends ProcessInstanceRepository {

    @Query("select pi from ProcessInstance pi where pi.status='COMPLETED' and pi.businessKey= :campaign and exists ( " +
                     "select v from Variable v where v.name= 'matched' and v.value='true' and v.processInstance = pi)")
    Page<ProcessInstance> findAllCompletedAndMatched(@Param("campaign") String campaign,
                                                     Pageable pageable);
}
