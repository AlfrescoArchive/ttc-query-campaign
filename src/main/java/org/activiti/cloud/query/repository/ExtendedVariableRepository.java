package org.activiti.cloud.query.repository;

import java.util.Date;
import java.util.List;

import org.activiti.cloud.services.query.app.repository.VariableRepository;
import org.activiti.cloud.services.query.model.VariableEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExtendedVariableRepository extends VariableRepository {

    @Query("select v from Variable v where v.name= 'matched' " +
                     "and v.processInstance.status='COMPLETED' "+
                     "and v.processInstance.businessKey= :campaign " +
                     "order by v.processInstance.lastModified desc")
    List<VariableEntity> findAllCompletedAndMatched(@Param("campaign") String campaign);

    @Query("select v from Variable v where v.name= 'matched' " +
            "and v.processInstance.status='COMPLETED' "+
            "and v.processInstance.businessKey= :campaign and v.processInstance.lastModified > :since order by v.processInstance.lastModified desc")
    List<VariableEntity> findAllCompletedAndMatchedSince(@Param("campaign") String campaign, @Param("since") Date since);

}
