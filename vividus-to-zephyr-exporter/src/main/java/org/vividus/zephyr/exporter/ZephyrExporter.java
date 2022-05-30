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

package org.vividus.zephyr.exporter;

import java.io.IOException;
import java.util.List;
import java.util.OptionalInt;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.jira.JiraConfigurationException;
import org.vividus.jira.JiraFacade;
import org.vividus.jira.model.JiraEntity;
import org.vividus.zephyr.configuration.ZephyrConfiguration;
import org.vividus.zephyr.configuration.ZephyrExporterProperties;
import org.vividus.zephyr.databind.TestCaseDeserializer;
import org.vividus.zephyr.facade.IZephyrFacade;
import org.vividus.zephyr.facade.ZephyrFacadeFactory;
import org.vividus.zephyr.model.ExecutionStatus;
import org.vividus.zephyr.model.TestCase;
import org.vividus.zephyr.model.ZephyrExecution;
import org.vividus.zephyr.parser.TestCaseParser;

public class ZephyrExporter
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ZephyrExporter.class);

    private final JiraFacade jiraFacade;

    private ZephyrFacadeFactory zephyrFacadeFactory;
    private TestCaseParser testCaseParser;
    private ZephyrExporterProperties zephyrExporterProperties;
    private final ObjectMapper objectMapper;

    public ZephyrExporter(JiraFacade jiraFacade, ZephyrFacadeFactory zephyrFacadeFactory, TestCaseParser testCaseParser,
                          ZephyrExporterProperties zephyrExporterProperties)
    {
        this.jiraFacade = jiraFacade;
        this.zephyrFacadeFactory = zephyrFacadeFactory;
        this.testCaseParser = testCaseParser;
        this.zephyrExporterProperties = zephyrExporterProperties;
        this.objectMapper = JsonMapper.builder()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true)
                .build()
                .registerModule(new SimpleModule().addDeserializer(TestCase.class, new TestCaseDeserializer()));
    }

    public void exportResults() throws IOException, JiraConfigurationException
    {
        List<TestCase> testCasesForImporting = testCaseParser.createTestCases(objectMapper);
        ZephyrConfiguration configuration = getZephyrFacade().prepareConfiguration();
        for (TestCase testCase : testCasesForImporting)
        {
            exportTestExecution(testCase, configuration);
        }
    }

    public void exportTestExecution(TestCase testCase, ZephyrConfiguration configuration)
            throws IOException, JiraConfigurationException
    {
        JiraEntity issue = jiraFacade.getIssue(testCase.getKey());
        ZephyrExecution execution = new ZephyrExecution(configuration, issue.getId(), testCase.getStatus());
        OptionalInt executionId;

        if (zephyrExporterProperties.getUpdateExecutionStatusesOnly())
        {
            executionId = getZephyrFacade().findExecutionId(issue.getId());
        }
        else
        {
            String createExecution = objectMapper.writeValueAsString(execution);
            executionId = OptionalInt.of(getZephyrFacade().createExecution(createExecution));
        }
        if (executionId.isPresent())
        {
            String executionBody = objectMapper.writeValueAsString(new ExecutionStatus(
                String.valueOf(configuration.getTestStatusPerZephyrMapping().get(execution.getTestCaseStatus()))));
            getZephyrFacade().updateExecutionStatus(String.valueOf(executionId.getAsInt()), executionBody);
        }
        else
        {
            LOGGER.atInfo().addArgument(testCase::getKey).log("Test case result for {} was not exported, "
                    + "because execution does not exist");
        }
    }

    public IZephyrFacade getZephyrFacade()
    {
        return zephyrFacadeFactory.getZephyrFacade();
    }

    public ObjectMapper getObjectMapper()
    {
        return objectMapper;
    }
}
