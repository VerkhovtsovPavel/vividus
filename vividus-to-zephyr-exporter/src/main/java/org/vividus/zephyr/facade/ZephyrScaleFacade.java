/*
 * Copyright 2019-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.vividus.zephyr.facade;

import java.io.IOException;
import java.util.Optional;
import java.util.OptionalInt;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.vividus.jira.JiraClient;
import org.vividus.jira.JiraClientProvider;
import org.vividus.jira.JiraConfigurationException;
import org.vividus.jira.JiraFacade;
import org.vividus.jira.model.Project;
import org.vividus.util.json.JsonPathUtils;
import org.vividus.zephyr.configuration.ZephyrConfiguration;
import org.vividus.zephyr.configuration.ZephyrExporterConfiguration;
import org.vividus.zephyr.configuration.ZephyrExporterProperties;

@Configuration
@ConditionalOnProperty(value = "zephyr.exporter.api-type", havingValue = "SCALE")
public class ZephyrScaleFacade implements IZephyrFacade
{
    private static final String BASE_ENDPOINT = "/rest/atm/1.0";
    private static final String TEST_RESULT_ENDPOINT = BASE_ENDPOINT + "/testrun/%s/testcase/%s/testresult";

    private final JiraFacade jiraFacade;
    private final JiraClientProvider jiraClientProvider;
    private final ZephyrExporterConfiguration zephyrExporterConfiguration;
    private final ZephyrExporterProperties zephyrExporterProperties;
    private ZephyrConfiguration zephyrConfiguration;

    public ZephyrScaleFacade(JiraFacade jiraFacade, JiraClientProvider jiraClientProvider,
                             ZephyrExporterConfiguration zephyrExporterConfiguration,
                             ZephyrExporterProperties zephyrExporterProperties)
    {
        this.jiraFacade = jiraFacade;
        this.jiraClientProvider = jiraClientProvider;
        this.zephyrExporterConfiguration = zephyrExporterConfiguration;
        this.zephyrExporterProperties = zephyrExporterProperties;
    }

    @Override
    public Integer createExecution(String execution) throws IOException, JiraConfigurationException
    {
        String testCaseResultUrl = String.format(TEST_RESULT_ENDPOINT,
                zephyrConfiguration.getCycleId(),
                execution);
        String responseBody = getJiraClient().executePost(
                testCaseResultUrl,
                "{}");
        return JsonPathUtils.getData(responseBody, "$.id");
    }

    @Override
    public void updateExecutionStatus(String executionId, String executionBody)
            throws IOException, JiraConfigurationException
    {
        String testCaseResultUrl = String.format(TEST_RESULT_ENDPOINT,
                zephyrConfiguration.getCycleId(),
                executionId);
        getJiraClient().executePut(testCaseResultUrl, executionBody);
    }

    @Override
    public ZephyrConfiguration prepareConfiguration() throws IOException, JiraConfigurationException
    {
        zephyrConfiguration = new ZephyrConfiguration();

        Project project = jiraFacade.getProject(zephyrExporterConfiguration.getProjectKey());
        String projectId = project.getId();
        zephyrConfiguration.setProjectId(projectId);

        String cycleId = createTestCycle(
                zephyrExporterConfiguration.getCycleName(),
                zephyrExporterConfiguration.getProjectKey(),
                zephyrExporterConfiguration.getFolderName());
        zephyrConfiguration.setCycleId(cycleId);

        zephyrConfiguration.setTestStatusPerZephyrStatusMapping(zephyrExporterConfiguration.getStatuses());

        return zephyrConfiguration;
    }

    private String createTestCycle(String cycleName,
                                   String projectKey,
                                   String folderName) throws IOException, JiraConfigurationException
    {
        String testCycleFormat = "{\"name\": \"%s\", \"projectKey\": \"%s\",\"folder\": \"/%s\"}";
        String body = String.format(testCycleFormat, cycleName, projectKey, folderName);
        String json = getJiraClient().executePost(BASE_ENDPOINT + "/testrun", body);
        return JsonPathUtils.getData(json, "$.key");
    }

    @Override
    public OptionalInt findExecutionId(String issueId) throws IOException, JiraConfigurationException
    {
        // Execution ID is not applicable for Zephyr Scale. All interaction occurs using the id of the test case.
        return OptionalInt.empty();
    }

    private JiraClient getJiraClient() throws JiraConfigurationException
    {
        return jiraClientProvider
                .getByJiraConfigurationKey(Optional.ofNullable(zephyrExporterProperties.getJiraInstanceKey()));
    }
}
