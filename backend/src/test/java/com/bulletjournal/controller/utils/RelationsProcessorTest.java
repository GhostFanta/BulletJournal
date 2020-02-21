package com.bulletjournal.controller.utils;

import com.bulletjournal.controller.models.Project;
import com.bulletjournal.repository.models.Group;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests {@link RelationsProcessor}
 */
public class RelationsProcessorTest {

    public static List<Project> createSampleProjectRelations(
            Project p1, Project p2, Project p3, Project p4, Project p5, Project p6) {
        /**
         *  p1
         *   |
         *    -- p2
         *   |   |
         *   |    -- p3
         *   |
         *    -- p4
         *
         *  p5
         *   |
         *    -- p6
         */
        List<Project> projectRelations = new ArrayList<>();
        projectRelations.add(p1);
        p1.addSubProject(p2);
        p1.addSubProject(p4);
        p2.addSubProject(p3);
        projectRelations.add(p5);
        p5.addSubProject(p6);
        return projectRelations;
    }

    /**
     * Tests {@link RelationsProcessor#removeTargetProject(Project, Project, List)}
     */
    @Test
    public void testRemoveTargetProject() {
        List<Project> projects = new ArrayList<>();
        for (long i = 1; i <= 6; i++) {
            projects.add(createProject(i));
        }
        Project p1 = projects.get(0);
        Project p2 = projects.get(1);
        Project p3 = projects.get(2);
        Project p4 = projects.get(3);
        Project p5 = projects.get(4);
        Project p6 = projects.get(5);
        List<Project> projectHierarchy = createSampleProjectRelations(p1, p2, p3, p4, p5, p6);

        /**
         * After remove p2
         *  p1
         *   |
         *    -- p4
         *
         *  p5
         *   |
         *    -- p6
         */
        List<Long> subProjects = RelationsProcessor.findSubProjects(p2);
        Collections.sort(subProjects);
        assertEquals(ImmutableList.of(2L, 3L), subProjects);

        String projectRelations = RelationsProcessor.processProjectRelations(projectHierarchy);
        Pair<Project, List<Project>> result = RelationsProcessor
                .removeTargetProject(projectRelations, p2.getId());
        projectHierarchy = result.getRight();
        assertEquals(2, projectHierarchy.size());
        assertEquals(1L, projectHierarchy.get(0).getId().longValue());
        assertEquals(1, projectHierarchy.get(0).getSubProjects().size());
        assertEquals(4L, projectHierarchy.get(0).getSubProjects().get(0).getId().longValue());
        assertEquals(5L, projectHierarchy.get(1).getId().longValue());
        assertEquals(1, projectHierarchy.get(1).getSubProjects().size());
        assertEquals(6L, projectHierarchy.get(1).getSubProjects().get(0).getId().longValue());
    }

    /**
     * Tests {@link RelationsProcessor#processProjectRelations(Map, String, Set)}
     */
    @Test
    public void testProjectRelationsProcessor() {
        List<Project> projects = new ArrayList<>();
        for (long i = 1; i <= 6; i++) {
            projects.add(createProject(i));
        }
        Project p1 = projects.get(0);
        Project p2 = projects.get(1);
        Project p3 = projects.get(2);
        Project p4 = projects.get(3);
        Project p5 = projects.get(4);
        Project p6 = projects.get(5);

        String projectRelations = RelationsProcessor.processProjectRelations(
                createSampleProjectRelations(p1, p2, p3, p4, p5, p6));
        Map<Long, com.bulletjournal.repository.models.Project> projectMap = new HashMap<>();
        for (int i = 1; i <= 6; i++) {
            com.bulletjournal.repository.models.Project p =
                    new com.bulletjournal.repository.models.Project();
            Long id = Long.valueOf(i);
            p.setId(id);
            p.setType(0);
            p.setGroup(new Group());
            p.setName("P" + i);
            projectMap.put(id, p);
        }
        projects = RelationsProcessor.processProjectRelations(projectMap, projectRelations, null);
        assertEquals(2, projects.size());
        assertEquals(p1.getId(), projects.get(0).getId());
        assertEquals(p5.getId(), projects.get(1).getId());
        assertEquals(2, projects.get(0).getSubProjects().size());
        assertEquals(p2.getId(), projects.get(0).getSubProjects().get(0).getId());
        assertEquals(p4.getId(), projects.get(0).getSubProjects().get(1).getId());
        assertEquals(1, projects.get(1).getSubProjects().size());
        assertEquals(p6.getId(), projects.get(1).getSubProjects().get(0).getId());
        assertEquals(1, projects.get(0).getSubProjects().get(0).getSubProjects().size());
        assertEquals(p3.getId(), projects.get(0).getSubProjects().get(0).getSubProjects().get(0).getId());

        projects = RelationsProcessor.processProjectRelations(projectMap, projectRelations,
                ImmutableSet.of(1L, 2L, 3L, 4L));
        assertEquals(1, projects.size());
        assertEquals(p1.getId(), projects.get(0).getId());
        assertEquals(2, projects.get(0).getSubProjects().size());
        assertEquals(p2.getId(), projects.get(0).getSubProjects().get(0).getId());
        assertEquals(p4.getId(), projects.get(0).getSubProjects().get(1).getId());
        assertEquals(1, projects.get(0).getSubProjects().get(0).getSubProjects().size());
        assertEquals(p3.getId(), projects.get(0).getSubProjects().get(0).getSubProjects().get(0).getId());

        projects = RelationsProcessor.processProjectRelations(projectMap, projectRelations,
                ImmutableSet.of(5L, 6L));
        assertEquals(1, projects.size());
        assertEquals(p5.getId(), projects.get(0).getId());
        assertEquals(1, projects.get(0).getSubProjects().size());
        assertEquals(p6.getId(), projects.get(0).getSubProjects().get(0).getId());

        projects = RelationsProcessor.processProjectRelations(projectMap, projectRelations,
                ImmutableSet.of(3L, 4L, 6L));
        assertEquals(2, projects.size());
        assertEquals(p1.getId(), projects.get(0).getId());
        assertNull(projects.get(0).getName());
        assertEquals(p5.getId(), projects.get(1).getId());
        assertNull(projects.get(1).getName());
        assertEquals(2, projects.get(0).getSubProjects().size());
        assertEquals(p2.getId(), projects.get(0).getSubProjects().get(0).getId());
        assertNull(projects.get(0).getSubProjects().get(0).getName());
        assertEquals(p4.getId(), projects.get(0).getSubProjects().get(1).getId());
        assertNotNull(projects.get(0).getSubProjects().get(1).getName());
        assertEquals(1, projects.get(1).getSubProjects().size());
        assertEquals(p6.getId(), projects.get(1).getSubProjects().get(0).getId());
        assertNotNull(projects.get(1).getSubProjects().get(0).getName());
        assertEquals(1, projects.get(0).getSubProjects().get(0).getSubProjects().size());
        assertEquals(p3.getId(), projects.get(0).getSubProjects().get(0).getSubProjects().get(0).getId());
        assertNotNull(projects.get(0).getSubProjects().get(0).getSubProjects().get(0).getName());

        projects = RelationsProcessor.processProjectRelations(projectMap, projectRelations,
                ImmutableSet.of(1L, 5L));
        assertEquals(2, projects.size());
        assertEquals(p1.getId(), projects.get(0).getId());
        assertNotNull(projects.get(0).getName());
        assertEquals(p5.getId(), projects.get(1).getId());
        assertNotNull(projects.get(1).getName());
        assertTrue(projects.get(0).getSubProjects().isEmpty());
        assertTrue(projects.get(1).getSubProjects().isEmpty());

        projects = RelationsProcessor.processProjectRelations(projectMap, projectRelations,
                ImmutableSet.of(1L, 2L));
        assertEquals(1, projects.size());
        assertEquals(p1.getId(), projects.get(0).getId());
        assertNotNull(projects.get(0).getName());
        assertEquals(1, projects.get(0).getSubProjects().size());
        assertEquals(p2.getId(), projects.get(0).getSubProjects().get(0).getId());
        assertNotNull(projects.get(0).getSubProjects().get(0).getName());
        assertTrue(projects.get(0).getSubProjects().get(0).getSubProjects().isEmpty());
    }

    private Project createProject(Long id) {
        Project project = new Project(id);
        project.setName("P" + id);
        return project;
    }
}
