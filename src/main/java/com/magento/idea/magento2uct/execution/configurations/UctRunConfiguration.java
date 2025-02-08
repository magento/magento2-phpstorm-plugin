/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.execution.configurations;

import static com.magento.idea.magento2uct.execution.configurations.UctSettingsEditor.MAGENTO_VERSION_PATTERN;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.RunManager;
import com.intellij.execution.configurations.CommandLineState;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.LocatableConfigurationBase;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessHandlerFactory;
import com.intellij.execution.process.ProcessTerminatedListener;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsActions;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.jetbrains.php.config.commandLine.PhpCommandSettings;
import com.jetbrains.php.config.commandLine.PhpCommandSettingsBuilder;
import com.jetbrains.php.config.interpreters.PhpInterpreter;
import com.magento.idea.magento2uct.execution.filters.UctPhpFileFilter;
import com.magento.idea.magento2uct.execution.filters.UctResultFileFilter;
import com.magento.idea.magento2uct.packages.IssueSeverityLevel;
import com.magento.idea.magento2uct.settings.UctSettingsService;
import com.magento.idea.magento2uct.util.UctExecutableValidatorUtil;
import com.magento.idea.magento2uct.util.module.UctModulePathValidatorUtil;
import java.util.regex.Matcher;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({
        "PMD.NPathComplexity",
        "PMD.CyclomaticComplexity",
        "PMD.ExcessiveImports",
        "PMD.CognitiveComplexity"
})
public class UctRunConfiguration extends LocatableConfigurationBase<UctRunConfigurationOptions> {

    /**
     * UCT run configuration constructor.
     *
     * @param project Project
     * @param factory ConfigurationFactory
     * @param name String
     */
    protected UctRunConfiguration(
            final @NotNull Project project,
            final @NotNull ConfigurationFactory factory,
            final @Nullable String name
    ) {
        super(project, factory, name);
    }

    @Override
    protected @NotNull UctRunConfigurationOptions getOptions() {
        return (UctRunConfigurationOptions) super.getOptions();
    }

    /**
     * Set script name setting.
     *
     * @param scriptName String
     */
    public void setScriptName(final String scriptName) {
        getOptions().setScriptName(scriptName);
    }

    /**
     * Get script name setting.
     *
     * @return String
     */
    public String getScriptName() {
        return getOptions().getScriptName();
    }

    /**
     * Set project root setting.
     *
     * @param projectRoot String
     */
    public void setProjectRoot(final String projectRoot) {
        getOptions().setProjectRoot(projectRoot);
    }

    /**
     * Get project root setting.
     *
     * @return String
     */
    public String getProjectRoot() {
        return getOptions().getProjectRoot();
    }

    /**
     * Set path to analyse setting.
     *
     * @param modulePath String
     */
    public void setModulePath(final String modulePath) {
        getOptions().setModulePath(modulePath);
    }

    /**
     * Get path to analyse.
     *
     * @return String
     */
    public String getModulePath() {
        return getOptions().getModulePath();
    }

    /**
     * Set coming version setting.
     *
     * @param comingVersion String
     */
    public void setComingVersion(final String comingVersion) {
        getOptions().setComingVersion(comingVersion);
    }

    /**
     * Get coming version setting.
     *
     * @return String
     */
    public String getComingVersion() {
        return getOptions().getComingVersion();
    }

    /**
     * Set minimum issue severity level setting.
     *
     * @param minIssueLevel int
     */
    public void setMinIssueLevel(final int minIssueLevel) {
        getOptions().setMinIssueLevel(minIssueLevel);
    }

    /**
     * Get minimum issue severity level setting.
     *
     * @return int
     */
    public int getMinIssueLevel() {
        return getOptions().getMinIssueLevel();
    }

    /**
     * Set ignoring for current version issues setting.
     *
     * @param hasIgnoreCurrentVersionIssues boolean
     */
    public void setHasIgnoreCurrentVersionIssues(final boolean hasIgnoreCurrentVersionIssues) {
        getOptions().setIgnoreCurrentVersionIssues(hasIgnoreCurrentVersionIssues);
    }

    /**
     * Check if has ignoring for current version issues setting.
     *
     * @return boolean
     */
    public boolean hasIgnoreCurrentVersionIssues() {
        return getOptions().hasIgnoreCurrentVersionIssues();
    }

    /**
     * Set is settings is newly created.
     *
     * @param isNewlyCreated boolean
     */
    public void setIsNewlyCreated(final boolean isNewlyCreated) {
        getOptions().setNewlyCreated(isNewlyCreated);
    }

    /**
     * Check if run configuration settings is newly created setting.
     *
     * @return boolean
     */
    public boolean isNewlyCreated() {
        return getOptions().isNewlyCreated();
    }

    @Override
    public @NotNull SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        return new UctSettingsEditor(getProject());
    }

    @Override
    public @Nullable @NlsActions.ActionText String suggestedName() {
        return this.getName().isEmpty() ? RunManager.getInstance(getProject()).suggestUniqueName(
                UctRunConfigurationType.SHORT_TITLE,
                this.getType()
        ) : this.getName();
    }

    @Override
    public @Nullable RunProfileState getState(
            final @NotNull Executor executor,
            final @NotNull ExecutionEnvironment environment
    ) throws ExecutionException {
        return new CommandLineState(environment) {

            @Override
            protected @NotNull ProcessHandler startProcess() throws ExecutionException {
                final PhpInterpreter interpreter = PhpProjectConfigurationFacade
                        .getInstance(getProject())
                        .getInterpreter();

                if (interpreter == null) {
                    throw new ExecutionException(
                            "Please, specify interpreter option in the PHP settings"
                    );
                }

                final boolean isValidPath = UctExecutableValidatorUtil.validate(getScriptName());
                final UctSettingsService settingsService =
                        UctSettingsService.getInstance(getProject());

                if (getScriptName().isEmpty()) {
                    throw new ExecutionException("The UCT executable path is not specified");
                } else {
                    if (settingsService != null && isValidPath) {
                        settingsService.setUctExecutablePath(getScriptName());
                    }
                }

                if (!isValidPath) {
                    throw new ExecutionException("The UCT executable path is not valid");
                }

                if (getComingVersion().isEmpty()) {
                    throw new ExecutionException("The coming/target version is not specified");
                }

                final Matcher matcher = MAGENTO_VERSION_PATTERN.matcher(getComingVersion());
                if (!matcher.find()) {
                    throw new ExecutionException("The coming/target version is not correct");
                }

                if (getProjectRoot().isEmpty()) {
                    throw new ExecutionException("The project root is not specified");
                }

                final PhpCommandSettings commandSettingsBuilder =
                        PhpCommandSettingsBuilder.create(getProject(), interpreter, false);

                commandSettingsBuilder.setScript(getScriptName());
                commandSettingsBuilder.addArgument("upgrade:check");

                if (!getComingVersion().isEmpty()) {
                    commandSettingsBuilder.addArgument("--coming-version=" + getComingVersion());
                }

                final GeneralCommandLine commandLine =
                        commandSettingsBuilder.createGeneralCommandLine();

                if (!getModulePath().isEmpty()) {
                    if (UctModulePathValidatorUtil.validate(getModulePath())) {
                        commandLine.addParameter(getModulePath());
                    } else {
                        throw new ExecutionException("The path to analyse is not valid");
                    }
                }
                final IssueSeverityLevel severityLevel =
                        IssueSeverityLevel.getByLevel(getMinIssueLevel());
                final IssueSeverityLevel defaultSeverityLevel =
                        IssueSeverityLevel.getDefaultIssueSeverityLevel();

                if (!severityLevel.equals(defaultSeverityLevel)) {
                    commandLine
                            .addParameter("--min-issue-level=".concat(severityLevel.getLabel()));
                }

                if (hasIgnoreCurrentVersionIssues()) {
                    commandLine.addParameter("--ignore-current-version-compatibility-issues");
                }
                commandLine.addParameter("--context=phpstorm");
                commandLine.addParameter("--ansi");

                final OSProcessHandler processHandler = ProcessHandlerFactory
                        .getInstance()
                        .createColoredProcessHandler(commandLine);

                ProcessTerminatedListener.attach(processHandler);

                this.addConsoleFilters(
                        new UctResultFileFilter(getProject()),
                        new UctPhpFileFilter(getProject())
                );

                return processHandler;
            }
        };
    }
}
