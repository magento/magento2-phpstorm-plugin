/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.data.CrontabXmlData;
import com.magento.idea.magento2plugin.magento.files.CrontabXmlTemplate;

public class CrontabXmlGeneratorTest extends BaseGeneratorTestCase {
    private static final String EXPECTED_DIRECTORY = "src/app/code/Foo/Bar/etc";
    private static final String MODULE_NAME = "Foo_Bar";
    private static final String DEFAULT_CRON_GROUP = "default";
    private static final String INDEX_CRON_GROUP = "index";
    private static final String CRONJOB_INSTANCE_ONE = "Foo\\Bar\\Cron\\TestOne";
    private static final String CRONJOB_NAME_ONE = "test_cron_job_one";
    private static final String CRONJOB_INSTANCE_TWO = "Foo\\Bar\\Cron\\TestTwo";
    private static final String CRONJOB_NAME_TWO = "test_cron_job_two";
    private static final String CRONJOB_SCHEDULE = "* * * * *";
    private static final String CRONJOB_SCHEDULE_CONFIG_PATH = "path/to/config";

    /**
     * Test generating crontab with schedule.
     */
    public void testGenerateCronTabXmlFileWithSchedule() {
        final String filePath = this.getFixturePath(CrontabXmlTemplate.FILE_NAME);
        final PsiFile expectedFile = myFixture.configureByFile(filePath);
        final PsiFile cronJobFile = addCronJobToCronTabXml(
                DEFAULT_CRON_GROUP,
                CRONJOB_NAME_ONE,
                CRONJOB_INSTANCE_ONE,
                CRONJOB_SCHEDULE,
                null
        );

        assertGeneratedFileIsCorrect(expectedFile, EXPECTED_DIRECTORY, cronJobFile);
    }

    /**
     * Test generating crontab with schedule config path.
     */
    public void testGenerateCronTabXmlFileWithScheduleConfig() {
        final String filePath = this.getFixturePath(CrontabXmlTemplate.FILE_NAME);
        final PsiFile expectedFile = myFixture.configureByFile(filePath);
        final PsiFile cronJobFile = addCronJobToCronTabXml(
                DEFAULT_CRON_GROUP,
                CRONJOB_NAME_TWO,
                CRONJOB_INSTANCE_TWO,
                null,
                CRONJOB_SCHEDULE_CONFIG_PATH
        );

        assertGeneratedFileIsCorrect(expectedFile, EXPECTED_DIRECTORY, cronJobFile);
    }

    /**
     * Test adding two cronjobs to the crontab.xml with one cron groups.
     */
    public void testAddTwoCronJobsToOneCronTab() {
        final String filePath = this.getFixturePath(CrontabXmlTemplate.FILE_NAME);
        final PsiFile expectedFile = myFixture.configureByFile(filePath);
        addCronJobToCronTabXml(
                DEFAULT_CRON_GROUP,
                CRONJOB_NAME_ONE,
                CRONJOB_INSTANCE_ONE,
                CRONJOB_SCHEDULE,
                null
        );
        final PsiFile cronJobFile = addCronJobToCronTabXml(
                DEFAULT_CRON_GROUP,
                CRONJOB_NAME_TWO,
                CRONJOB_INSTANCE_TWO,
                null,
                CRONJOB_SCHEDULE_CONFIG_PATH
        );

        assertGeneratedFileIsCorrect(expectedFile, EXPECTED_DIRECTORY, cronJobFile);
    }

    /**
     * Test adding two cronjobs to the crontab.xml with different cron groups.
     */
    public void testAddTwoCronJobsToDifferentCronTabs() {
        final String filePath = this.getFixturePath(CrontabXmlTemplate.FILE_NAME);
        final PsiFile expectedFile = myFixture.configureByFile(filePath);
        addCronJobToCronTabXml(
                DEFAULT_CRON_GROUP,
                CRONJOB_NAME_ONE,
                CRONJOB_INSTANCE_ONE,
                CRONJOB_SCHEDULE,
                null
        );
        final PsiFile cronJobFile = addCronJobToCronTabXml(
                INDEX_CRON_GROUP,
                CRONJOB_NAME_TWO,
                CRONJOB_INSTANCE_TWO,
                null,
                CRONJOB_SCHEDULE_CONFIG_PATH
        );

        assertGeneratedFileIsCorrect(expectedFile, EXPECTED_DIRECTORY, cronJobFile);
    }

    /**
     * Add cronjob to crontab.xml.
     *
     * @param cronGroup Cron group name
     * @param cronjobName Cron job name
     * @param cronjobInstance Cron job instance FQN
     * @param cronjobSchedule Cron job schedule
     * @param cronjobScheduleConfigPath Cron job schedule config path
     * @return PsiFile
     */
    private PsiFile addCronJobToCronTabXml(
            final String cronGroup,
            final String cronjobName,
            final String cronjobInstance,
            final String cronjobSchedule,
            final String cronjobScheduleConfigPath
    ) {
        final Project project = myFixture.getProject();
        final CrontabXmlData crontabXmlData = new CrontabXmlData(
                MODULE_NAME,
                cronGroup,
                cronjobName,
                cronjobInstance,
                cronjobSchedule,
                cronjobScheduleConfigPath
        );
        final CrontabXmlGenerator cronjobClassGenerator = new CrontabXmlGenerator(
                project,
                crontabXmlData
        );

        return cronjobClassGenerator.generate("test");
    }
}
