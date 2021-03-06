/**
 * This file is part of mycollab-services.
 *
 * mycollab-services is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * mycollab-services is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with mycollab-services.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.mycollab.module.project.service.impl;

import com.mycollab.db.arguments.SetSearchField;
import com.mycollab.db.persistence.ISearchableDAO;
import com.mycollab.db.persistence.service.DefaultSearchService;
import com.mycollab.module.project.dao.ProjectGenericTaskMapper;
import com.mycollab.module.project.domain.ProjectGenericTask;
import com.mycollab.module.project.domain.criteria.ProjectGenericTaskSearchCriteria;
import com.mycollab.module.project.service.ProjectGenericTaskService;
import com.mycollab.module.user.domain.BillingAccount;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author MyCollab Ltd.
 * @since 1.0
 */
public abstract class AbstractProjectGenericTaskServiceImpl extends DefaultSearchService<ProjectGenericTaskSearchCriteria>
        implements ProjectGenericTaskService {

    @Autowired
    private ProjectGenericTaskMapper projectGenericTaskMapper;

    @Override
    public ISearchableDAO<ProjectGenericTaskSearchCriteria> getSearchMapper() {
        return projectGenericTaskMapper;
    }

    @Override
    public Integer getTotalCount(ProjectGenericTaskSearchCriteria criteria) {
        return projectGenericTaskMapper.getTotalCountFromRisk(criteria)
                + projectGenericTaskMapper.getTotalCountFromBug(criteria)
                + projectGenericTaskMapper.getTotalCountFromTask(criteria)
                + projectGenericTaskMapper.getTotalCountFromMilestone(criteria);
    }

    @Override
    public List<BillingAccount> getAccountsHasOverdueAssignments(ProjectGenericTaskSearchCriteria searchCriteria) {
        return projectGenericTaskMapper.getAccountsHasOverdueAssignments(searchCriteria);
    }

    @Override
    public List<Integer> getProjectsHasOverdueAssignments(ProjectGenericTaskSearchCriteria searchCriteria) {
        return projectGenericTaskMapper.getProjectsHasOverdueAssignments(searchCriteria);
    }

    @Override
    public ProjectGenericTask findAssignment(String type, Integer typeId) {
        ProjectGenericTaskSearchCriteria searchCriteria = new ProjectGenericTaskSearchCriteria();
        searchCriteria.setTypes(new SetSearchField<>(type));
        searchCriteria.setTypeIds(new SetSearchField<>(typeId));
        List<ProjectGenericTask> assignments = findAbsoluteListByCriteria(searchCriteria, 0, 1);
        return (assignments.size() > 0) ? assignments.get(0) : null;
    }
}
