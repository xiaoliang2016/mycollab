/**
 * This file is part of mycollab-web.
 *
 * mycollab-web is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * mycollab-web is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with mycollab-web.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.mycollab.module.project.view.task;

import com.mycollab.common.i18n.GenericI18Enum;
import com.mycollab.common.i18n.OptionI18nEnum.StatusI18nEnum;
import com.mycollab.configuration.SiteConfiguration;
import com.mycollab.core.arguments.ValuedBean;
import com.mycollab.core.utils.BeanUtility;
import com.mycollab.module.project.CurrentProjectVariables;
import com.mycollab.module.project.ProjectRolePermissionCollections;
import com.mycollab.module.project.ProjectTypeConstants;
import com.mycollab.module.project.domain.SimpleTask;
import com.mycollab.module.project.i18n.ProjectCommonI18nEnum;
import com.mycollab.module.project.i18n.TaskI18nEnum;
import com.mycollab.module.project.service.ProjectTaskService;
import com.mycollab.module.project.ui.ProjectAssetsManager;
import com.mycollab.module.project.ui.components.*;
import com.mycollab.module.project.view.task.components.TaskTimeLogSheet;
import com.mycollab.module.project.view.task.components.ToggleTaskSummaryField;
import com.mycollab.module.project.view.task.components.ToggleTaskSummaryWithChildRelationshipField;
import com.mycollab.spring.AppContextUtil;
import com.mycollab.vaadin.MyCollabUI;
import com.mycollab.vaadin.UserUIContext;
import com.mycollab.vaadin.events.HasPreviewFormHandlers;
import com.mycollab.vaadin.mvp.ViewComponent;
import com.mycollab.vaadin.mvp.ViewManager;
import com.mycollab.vaadin.ui.ELabel;
import com.mycollab.vaadin.ui.VerticalRemoveInlineComponentMarker;
import com.mycollab.vaadin.web.ui.*;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.viritin.button.MButton;
import org.vaadin.viritin.layouts.MHorizontalLayout;
import org.vaadin.viritin.layouts.MVerticalLayout;

/**
 * @author MyCollab Ltd.
 * @since 2.0
 */
@ViewComponent
public class TaskReadViewImpl extends AbstractPreviewItemComp<SimpleTask> implements TaskReadView {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(TaskReadViewImpl.class);

    private ProjectActivityComponent activityComponent;
    private TagViewComponent tagViewComponent;
    private ProjectFollowersComp<SimpleTask> followerSheet;
    private DateInfoComp dateInfoComp;
    private TaskTimeLogSheet timeLogComp;
    private PeopleInfoComp peopleInfoComp;
    private MButton quickActionStatusBtn;

    public TaskReadViewImpl() {
        super(UserUIContext.getMessage(TaskI18nEnum.DETAIL),
                ProjectAssetsManager.getAsset(ProjectTypeConstants.TASK), new TaskPreviewFormLayout());
    }

    @Override
    public SimpleTask getItem() {
        return beanItem;
    }

    @Override
    public HasPreviewFormHandlers<SimpleTask> getPreviewFormHandlers() {
        return previewForm;
    }

    @Override
    protected void initRelatedComponents() {
        activityComponent = new ProjectActivityComponent(ProjectTypeConstants.TASK, CurrentProjectVariables.getProjectId());
        dateInfoComp = new DateInfoComp();
        peopleInfoComp = new PeopleInfoComp();
        followerSheet = new ProjectFollowersComp<>(ProjectTypeConstants.TASK, ProjectRolePermissionCollections.TASKS);

        if (SiteConfiguration.isCommunityEdition()) {
            addToSideBar(dateInfoComp, peopleInfoComp, followerSheet);
        } else {
            timeLogComp = ViewManager.getCacheComponent(TaskTimeLogSheet.class);
            addToSideBar(dateInfoComp, peopleInfoComp, timeLogComp, followerSheet);
        }
    }

    @Override
    protected void onPreviewItem() {
        ((TaskPreviewFormLayout) previewLayout).displayTaskHeader(beanItem);

        if (!beanItem.isCompleted()) {
            quickActionStatusBtn.setCaption(UserUIContext.getMessage(GenericI18Enum.BUTTON_CLOSE));
            quickActionStatusBtn.setIcon(FontAwesome.ARCHIVE);
        } else {
            quickActionStatusBtn.setCaption(UserUIContext.getMessage(GenericI18Enum.BUTTON_REOPEN));
            quickActionStatusBtn.setIcon(FontAwesome.CIRCLE_O_NOTCH);
        }

        if (tagViewComponent != null) {
            tagViewComponent.display(ProjectTypeConstants.TASK, beanItem.getId());
        }

        if (timeLogComp != null) {
            timeLogComp.displayTime(beanItem);
        }

        activityComponent.loadActivities("" + beanItem.getId());
        followerSheet.displayFollowers(beanItem);
        peopleInfoComp.displayEntryPeople(beanItem);
        dateInfoComp.displayEntryDateTime(beanItem);
    }

    @Override
    protected String initFormTitle() {
        return beanItem.getTaskname();
    }

    @Override
    protected AdvancedPreviewBeanForm<SimpleTask> initPreviewForm() {
        return new TaskPreviewForm();
    }

    @Override
    protected HorizontalLayout createButtonControls() {
        ProjectPreviewFormControlsGenerator<SimpleTask> taskPreviewForm = new ProjectPreviewFormControlsGenerator<>(previewForm);
        final HorizontalLayout topPanel = taskPreviewForm.createButtonControls(
                ProjectPreviewFormControlsGenerator.ADD_BTN_PRESENTED
                        | ProjectPreviewFormControlsGenerator.ASSIGN_BTN_PRESENTED
                        | ProjectPreviewFormControlsGenerator.CLONE_BTN_PRESENTED
                        | ProjectPreviewFormControlsGenerator.DELETE_BTN_PRESENTED
                        | ProjectPreviewFormControlsGenerator.EDIT_BTN_PRESENTED
                        | ProjectPreviewFormControlsGenerator.PRINT_BTN_PRESENTED
                        | ProjectPreviewFormControlsGenerator.NAVIGATOR_BTN_PRESENTED,
                ProjectRolePermissionCollections.TASKS);

        quickActionStatusBtn = new MButton("", clickEvent -> {
            if (beanItem.isCompleted()) {
                beanItem.setStatus(StatusI18nEnum.Open.name());
                beanItem.setPercentagecomplete(0d);
                removeLayoutStyleName(WebUIConstants.LINK_COMPLETED);
                quickActionStatusBtn.setCaption(UserUIContext.getMessage(GenericI18Enum.BUTTON_CLOSE));
                quickActionStatusBtn.setIcon(FontAwesome.ARCHIVE);
            } else {
                beanItem.setStatus(StatusI18nEnum.Closed.name());
                beanItem.setPercentagecomplete(100d);
                addLayoutStyleName(WebUIConstants.LINK_COMPLETED);
                quickActionStatusBtn.setCaption(UserUIContext.getMessage(GenericI18Enum.BUTTON_REOPEN));
                quickActionStatusBtn.setIcon(FontAwesome.CIRCLE_O_NOTCH);
            }

            ProjectTaskService projectTaskService = AppContextUtil.getSpringBean(ProjectTaskService.class);
            projectTaskService.updateWithSession(beanItem, UserUIContext.getUsername());

            if (StatusI18nEnum.Closed.name().equals(beanItem.getStatus())) {
                Integer countOfOpenSubTasks = projectTaskService.getCountOfOpenSubTasks(beanItem.getId());
                if (countOfOpenSubTasks > 0) {
                    ConfirmDialogExt.show(UI.getCurrent(),
                            UserUIContext.getMessage(GenericI18Enum.OPT_QUESTION, MyCollabUI.getSiteName()),
                            UserUIContext.getMessage(ProjectCommonI18nEnum.OPT_CLOSE_SUB_ASSIGNMENTS),
                            UserUIContext.getMessage(GenericI18Enum.BUTTON_YES),
                            UserUIContext.getMessage(GenericI18Enum.BUTTON_NO),
                            confirmDialog -> {
                                if (confirmDialog.isConfirmed()) {
                                    projectTaskService.massUpdateTaskStatuses(beanItem.getId(), StatusI18nEnum.Closed.name(), MyCollabUI.getAccountId());
                                }
                            });
                }
            }
        }).withStyleName(WebUIConstants.BUTTON_ACTION);

        taskPreviewForm.insertToControlBlock(quickActionStatusBtn);
        quickActionStatusBtn.setVisible(CurrentProjectVariables.canWrite(ProjectRolePermissionCollections.TASKS));

        return topPanel;
    }

    @Override
    protected ComponentContainer createExtraControls() {
        if (SiteConfiguration.isCommunityEdition()) {
            return null;
        } else {
            tagViewComponent = new TagViewComponent(CurrentProjectVariables.canWrite(ProjectRolePermissionCollections.TASKS));
            return tagViewComponent;
        }
    }

    @Override
    protected ComponentContainer createBottomPanel() {
        return activityComponent;
    }

    @Override
    protected String getType() {
        return ProjectTypeConstants.TASK;
    }

    private static class TaskPreviewFormLayout extends ReadViewLayout {
        private ToggleTaskSummaryField toggleTaskSummaryField;

        void displayTaskHeader(SimpleTask task) {
            toggleTaskSummaryField = new ToggleTaskSummaryField(task);
            toggleTaskSummaryField.addLabelStyleName(ValoTheme.LABEL_H3);
            toggleTaskSummaryField.addLabelStyleName(ValoTheme.LABEL_NO_MARGIN);
            if (task.getParenttaskid() == null) {
                MHorizontalLayout header = new MHorizontalLayout().withFullWidth();
                header.with(toggleTaskSummaryField);
                this.addHeader(header);
            } else {
                VerticalRemoveInlineComponentMarker header = new VerticalRemoveInlineComponentMarker();
                header.setMargin(false);
                ParentTaskComp parentTaskComp = new ParentTaskComp(task.getParenttaskid(), task);
                header.with(parentTaskComp, toggleTaskSummaryField);
                this.addHeader(header);
            }

            if (task.isCompleted()) {
                toggleTaskSummaryField.closeTask();
            } else if (task.isOverdue()) {
                toggleTaskSummaryField.overdueTask();
            }
        }

        @Override
        public void removeTitleStyleName(String styleName) {
            toggleTaskSummaryField.removeLabelStyleName(styleName);
        }

        @Override
        public void addTitleStyleName(String styleName) {
            toggleTaskSummaryField.addLabelStyleName(styleName);
        }
    }

    private static class ParentTaskComp extends MHorizontalLayout {
        ParentTaskComp(Integer parentTaskId, SimpleTask childTask) {
            ELabel titleLbl = new ELabel(UserUIContext.getMessage(TaskI18nEnum.FORM_PARENT_TASK)).withStyleName(WebUIConstants.ARROW_BTN)
                    .withWidthUndefined();
            with(titleLbl);
            ProjectTaskService taskService = AppContextUtil.getSpringBean(ProjectTaskService.class);
            SimpleTask parentTask = taskService.findById(parentTaskId, MyCollabUI.getAccountId());
            if (parentTask != null) {
                with(new ToggleTaskSummaryWithChildRelationshipField(parentTask, childTask));
            }
        }
    }

    private static class PeopleInfoComp extends MVerticalLayout {
        private static final long serialVersionUID = 1L;

        void displayEntryPeople(ValuedBean bean) {
            this.removeAllComponents();
            this.withMargin(false);

            Label peopleInfoHeader = new Label(FontAwesome.USER.getHtml() + " " +
                    UserUIContext.getMessage(ProjectCommonI18nEnum.SUB_INFO_PEOPLE), ContentMode.HTML);
            peopleInfoHeader.setStyleName("info-hdr");
            this.addComponent(peopleInfoHeader);

            GridLayout layout = new GridLayout(2, 2);
            layout.setWidth("100%");
            layout.setMargin(new MarginInfo(false, false, false, true));
            try {
                Label createdLbl = new Label(UserUIContext.getMessage(ProjectCommonI18nEnum.ITEM_CREATED_PEOPLE));
                createdLbl.setSizeUndefined();
                layout.addComponent(createdLbl, 0, 0);

                String createdUserName = (String) PropertyUtils.getProperty(bean, "logby");
                String createdUserAvatarId = (String) PropertyUtils.getProperty(bean, "logByAvatarId");
                String createdUserDisplayName = (String) PropertyUtils.getProperty(bean, "logByFullName");

                ProjectMemberLink createdUserLink = new ProjectMemberLink(createdUserName,
                        createdUserAvatarId, createdUserDisplayName);
                layout.addComponent(createdUserLink, 1, 0);
                layout.setColumnExpandRatio(1, 1.0f);

                Label assigneeLbl = new Label(UserUIContext.getMessage(ProjectCommonI18nEnum.ITEM_ASSIGN_PEOPLE));
                assigneeLbl.setSizeUndefined();
                layout.addComponent(assigneeLbl, 0, 1);
                String assignUserName = (String) PropertyUtils.getProperty(bean, "assignuser");
                String assignUserAvatarId = (String) PropertyUtils.getProperty(bean, "assignUserAvatarId");
                String assignUserDisplayName = (String) PropertyUtils.getProperty(bean, "assignUserFullName");

                ProjectMemberLink assignUserLink = new ProjectMemberLink(assignUserName, assignUserAvatarId, assignUserDisplayName);
                layout.addComponent(assignUserLink, 1, 1);
            } catch (Exception e) {
                LOG.error("Can not build user link {} ", BeanUtility.printBeanObj(bean));
            }

            this.addComponent(layout);
        }
    }
}