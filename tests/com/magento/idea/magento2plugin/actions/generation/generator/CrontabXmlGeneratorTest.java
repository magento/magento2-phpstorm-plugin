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
    private static final String expectedDirectory = "src/app/code/Foo/Bar/etc";
    private static final String moduleName = "Foo_Bar";
    private static final String defaultCronGroup = "default";
    private static final String indexCronGroup = "index";
    private static final String cronjobInstanceOne = "Foo\\Bar\\Cron\\TestOne";
    private static final String cronjobNameOne = "test_cron_job_one";
    private static final String cronjobInstanceTwo = "Foo\\Bar\\Cron\\TestTwo";
    private static final String cronjobNameTwo = "test_cron_job_two";
    private static final String cronjobSchedule = "* * * * *";
    private static final String cronjobScheduleConfigPath = "path/to/config";

    /**
     * Test generating crontab with schedule.
     */
    public void testGenerateCronTabXmlFileWithSchedule() {
        final String filePath = this.getFixturePath(CrontabXmlTemplate.FILE_NAME);
        final PsiFile expectedFile = myFixture.configureByFile(filePath);
        final PsiFile cronJobFile = addCronJobToCronTabXml(
                defaultCronGroup,
                cronjobNameOne,
                cronjobInstanceOne,
                cronjobSchedule,
                null
        );

        assertGeneratedFileIsCorrect(expectedFile, expectedDirectory, cronJobFile);
    }

    /**
     * Test generating crontab with schedule config path.
     */
    public void testGenerateCronTabXmlFileWithScheduleConfig() {
        final String filePath = this.getFixturePath(CrontabXmlTemplate.FILE_NAME);
        final PsiFile expectedFile = myFixture.configureByFile(filePath);
        final PsiFile cronJobFile = addCronJobToCronTabXml(
                defaultCronGroup,
                cronjobNameTwo,
                cronjobInstanceTwo,
                null,
                cronjobScheduleConfigPath
        );

        assertGeneratedFileIsCorrect(expectedFile, expectedDirectory, cronJobFile);
    }

    /**
     * Test adding two cronjobs to the crontab.xml with one cron groups.
     */
    public void testAddTwoCronJobsToOneCronTab() {
        final String filePath = this.getFixturePath(CrontabXmlTemplate.FILE_NAME);
        final PsiFile expectedFile = myFixture.configureByFile(filePath);
        addCronJobToCronTabXml(
                defaultCronGroup,
                cronjobNameOne,
                cronjobInstanceOne,
                cronjobSchedule,
                null
        );
        final PsiFile cronJobFile = addCronJobToCronTabXml(
                defaultCronGroup,
                cronjobNameTwo,
                cronjobInstanceTwo,
                null,
                cronjobScheduleConfigPath
        );

        assertGeneratedFileIsCorrect(expectedFile, expectedDirectory, cronJobFile);
    }

    /**
     * Test adding two cronjobs to the crontab.xml with different cron groups.
     */
    public void testAddTwoCronJobsToDifferentCronTabs() {
        final String filePath = this.getFixturePath(CrontabXmlTemplate.FILE_NAME);
        final PsiFile expectedFile = myFixture.configureByFile(filePath);
        addCronJobToCronTabXml(
                defaultCronGroup,
                cronjobNameOne,
                cronjobInstanceOne,
                cronjobSchedule,
                null
        );
        final PsiFile cronJobFile = addCronJobToCronTabXml(
                indexCronGroup,
                cronjobNameTwo,
                cronjobInstanceTwo,
                null,
                cronjobScheduleConfigPath
        );

        assertGeneratedFileIsCorrect(expectedFile, expectedDirectory, cronJobFile);
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
                moduleName,
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
