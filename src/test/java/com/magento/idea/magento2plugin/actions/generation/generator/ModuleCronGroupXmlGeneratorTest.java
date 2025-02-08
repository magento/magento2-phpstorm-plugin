/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.data.CronGroupXmlData;
import com.magento.idea.magento2plugin.magento.files.CronGroupXmlTemplate;

public class ModuleCronGroupXmlGeneratorTest extends BaseGeneratorTestCase {
    private static final String EXPECTED_DIRECTORY = "src/app/code/Foo/Bar/etc";
    private static final String MODULE_NAME = "Foo_Bar";
    private static final String CRON_GROUP_NAME_ONE = "custom_cron_group_one";
    private static final String CRON_GROUP_NAME_TWO = "custom_cron_group_two";
    private static final int SCHEDULE_GENERATE_EVERY = 1;
    private static final int SCHEDULE_AHEAD_FOR = 4;
    private static final int SCHEDULE_LIFETIME = 2;
    private static final int HISTORY_CLEANUP_EVERY = 10;
    private static final int HISTORY_SUCCESS_LIFETIME = 60;
    private static final int HISTORY_FAILURE_LIFETIME = 600;
    private static final int USE_SEPARATE_PROCESS = 1;

    /**
     * Test generating CRON group with no options.
     */
    public void testGenerateEmptyCronGroup() {
        final String filePath = this.getFixturePath(CronGroupXmlTemplate.FILE_NAME);
        final PsiFile expectedFile = myFixture.configureByFile(filePath);
        final PsiFile cronGroupsXmlFile = addCronGroupToCronGroupsXml(
                CRON_GROUP_NAME_ONE,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        assertGeneratedFileIsCorrect(expectedFile, EXPECTED_DIRECTORY, cronGroupsXmlFile);
    }

    /**
     * Test generating CRON group with all options.
     */
    public void testGenerateCronGroupWithAllOptions() {
        final String filePath = this.getFixturePath(CronGroupXmlTemplate.FILE_NAME);
        final PsiFile expectedFile = myFixture.configureByFile(filePath);
        final PsiFile cronGroupsXmlFile = addCronGroupToCronGroupsXml(
                CRON_GROUP_NAME_TWO,
                SCHEDULE_GENERATE_EVERY,
                SCHEDULE_AHEAD_FOR,
                SCHEDULE_LIFETIME,
                HISTORY_CLEANUP_EVERY,
                HISTORY_SUCCESS_LIFETIME,
                HISTORY_FAILURE_LIFETIME,
                USE_SEPARATE_PROCESS
        );

        assertGeneratedFileIsCorrect(expectedFile, EXPECTED_DIRECTORY, cronGroupsXmlFile);
    }

    /**
     * Test adding two CRON groups to the cron_groups.xml.
     */
    public void testAddTwoCronGroupsToCronGroupsXmlFile() {
        final String filePath = this.getFixturePath(CronGroupXmlTemplate.FILE_NAME);
        final PsiFile expectedFile = myFixture.configureByFile(filePath);
        addCronGroupToCronGroupsXml(
                CRON_GROUP_NAME_ONE,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
        final PsiFile cronGroupsXmlFile = addCronGroupToCronGroupsXml(
                CRON_GROUP_NAME_TWO,
                SCHEDULE_GENERATE_EVERY,
                SCHEDULE_AHEAD_FOR,
                SCHEDULE_LIFETIME,
                HISTORY_CLEANUP_EVERY,
                HISTORY_SUCCESS_LIFETIME,
                HISTORY_FAILURE_LIFETIME,
                USE_SEPARATE_PROCESS
        );

        assertGeneratedFileIsCorrect(expectedFile, EXPECTED_DIRECTORY, cronGroupsXmlFile);
    }

    /**
     * Add CRON group to cron_groups.xml.
     *
     * @param cronGroupName CRON group name
     * @param scheduleGenerateEvery Schedule generate every
     * @param scheduleAheadFor Schedule ahead for
     * @param scheduleLifetime Schedule lifetime
     * @param historyCleanupEvery History cleanup every
     * @param historySuccessLifetime History success lifetime
     * @param historyFailureLifetime History failure lifetime
     * @param useSeparateProcess Use separate process
     * @return PsiFile
     */
    private PsiFile addCronGroupToCronGroupsXml(
            final String cronGroupName,
            final Integer scheduleGenerateEvery,
            final Integer scheduleAheadFor,
            final Integer scheduleLifetime,
            final Integer historyCleanupEvery,
            final Integer historySuccessLifetime,
            final Integer historyFailureLifetime,
            final Integer useSeparateProcess
    ) {
        final Project project = myFixture.getProject();
        final CronGroupXmlData cronGroupXmlData = new CronGroupXmlData(
                MODULE_NAME,
                cronGroupName,
                scheduleGenerateEvery,
                scheduleAheadFor,
                scheduleLifetime,
                historyCleanupEvery,
                historySuccessLifetime,
                historyFailureLifetime,
                useSeparateProcess
        );
        final ModuleCronGroupXmlGenerator cronGroupXmlGenerator = new ModuleCronGroupXmlGenerator(
                cronGroupXmlData,
                project
        );

        return cronGroupXmlGenerator.generate("test");
    }
}
