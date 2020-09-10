package com.bulletjournal.templates.controller;

import com.bulletjournal.clients.UserClient;
import com.bulletjournal.exceptions.UnAuthorizedException;
import com.bulletjournal.repository.UserDaoJpa;
import com.bulletjournal.templates.controller.model.CreateSampleTaskParams;
import com.bulletjournal.templates.controller.model.NextStep;
import com.bulletjournal.templates.controller.model.SampleTask;
import com.bulletjournal.templates.controller.model.UpdateSampleTaskParams;
import com.bulletjournal.templates.repository.RuleDaoJpa;
import com.bulletjournal.templates.repository.SampleTaskDaoJpa;
import com.bulletjournal.templates.repository.model.CategoryRule;
import com.bulletjournal.templates.repository.model.StepRule;
import com.bulletjournal.templates.workflow.models.RuleExpression;
import com.google.gson.Gson;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
public class WorkflowController {

    public static final String NEXT_STEP_ROUTE = "/api/public/steps/{stepId}/next";
    public static final String SAMPLE_TASKS_ROUTE = "/api/sampleTasks";
    public static final String SAMPLE_TASK_ROUTE = "/api/sampleTasks/{sampleTaskId}";

    @Autowired
    private SampleTaskDaoJpa sampleTaskDaoJpa;

    @Autowired
    private UserDaoJpa userDaoJpa;

    @Autowired
    private RuleDaoJpa ruleDaoJpa;

    @GetMapping(NEXT_STEP_ROUTE)
    public NextStep getNext(
            @NotNull @PathVariable Long stepId,
            @NotNull @RequestParam List<Long> selections,
            @NotNull @RequestParam(required = false, defaultValue = "false") boolean first
    ) {
        Gson gson = new Gson();
        if (first) {
            List<CategoryRule> categoryRules = ruleDaoJpa.getAllCategoryRules();
            categoryRules.forEach(categoryRule -> {
                RuleExpression ruleExpression = gson.fromJson(categoryRule.getRuleExpression(), RuleExpression.class);
            });
        } else {
            List<StepRule> stepRules = ruleDaoJpa.getAllStepRules();
        }
        return null;
    }

    @PostMapping(SAMPLE_TASKS_ROUTE)
    public SampleTask createSampleTask(@Valid @RequestBody CreateSampleTaskParams createSampleTaskParams) {
        validateRequester();
        return sampleTaskDaoJpa.createSampleTask(createSampleTaskParams).toPresentationModel();
    }

    @GetMapping(SAMPLE_TASK_ROUTE)
    public SampleTask getSampleTask(@NotNull @PathVariable Long sampleTaskId) {
        validateRequester();
        return sampleTaskDaoJpa.findSampleTaskById(sampleTaskId).toPresentationModel();
    }

    @PutMapping(SAMPLE_TASK_ROUTE)
    public SampleTask updateSampleTask(@NotNull @PathVariable Long sampleTaskId, @Valid @RequestBody UpdateSampleTaskParams updateSampleTaskParams) {
        validateRequester();
        return sampleTaskDaoJpa.updateSampleTask(sampleTaskId, updateSampleTaskParams).toPresentationModel();
    }

    @DeleteMapping(SAMPLE_TASK_ROUTE)
    public void deleteSampleTask(@NotNull @PathVariable Long sampleTaskId) {
        validateRequester();
        sampleTaskDaoJpa.deleteSampleTaskById(sampleTaskId);
    }

    private void validateRequester() {
        String requester = MDC.get(UserClient.USER_NAME_KEY);

        if (!this.userDaoJpa.isAdmin(requester)) {
            throw new UnAuthorizedException("User: " + requester + " is not admin");
        }
    }
}