/*
 * Copyright (C) 2023 FRIDAY Insurance S.A.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package dk.ifforsikring.sonarqube.gosu.plugin;

import com.sonar.orchestrator.build.BuildResult;
import dk.ifforsikring.test.config.IntegrationTest;
import dk.ifforsikring.test.framework.sonar.server.SonarServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonarqube.ws.Common;
import org.sonarqube.ws.Issues;

import java.util.List;

import static dk.ifforsikring.test.config.Projects.SIMPLE_GOSU_PROJECT;
import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
public class SimpleGosuProjectIT {

    private final SonarServer sonarServer;

    private BuildResult buildResult;

    SimpleGosuProjectIT(SonarServer sonarServer) {
        this.sonarServer = sonarServer;
    }

    @BeforeEach
    void runScanner() {
        this.buildResult = sonarServer.executeBuild(SIMPLE_GOSU_PROJECT.asGradleBuild());
    }

    @Test
    void shouldScanGosuGradleProjectSuccessfully() {
        assertThat(buildResult.isSuccess());
    }

    @Test
    void shouldFindIssuesOnGosuGradleProject() {
        assertThat(allProjectIssues()).hasSize(10);
    }

    @Test
    void shouldFindTodoIssueOnProject() {
        assertThat(allIssuesOn("gosu/dk/ifforsikring/gosu/simple/StringEnhancement.gsx"))
                .hasSize(1)
                .allSatisfy(
                        issue -> {
                            assertThat(issue.getRule()).isEqualTo("gosu:TODOsRule");
                            assertThat(issue.getType()).isEqualTo(Common.RuleType.CODE_SMELL);
                            assertThat(issue.getScope()).isEqualTo("MAIN");
                            assertThat(issue.getSeverity()).isEqualTo(Common.Severity.INFO);
                            assertThat(issue.getMessage()).isEqualTo("Complete the task associated to this TODO comment.");
                            assertThat(issue.getTextRange()).satisfies(textRange -> {
                                assertThat(textRange.getStartLine()).isEqualTo(9);
                                assertThat(textRange.getStartOffset()).isEqualTo(2);
                                assertThat(textRange.getEndLine()).isEqualTo(9);
                                assertThat(textRange.getEndOffset()).isEqualTo(15);
                            });
                        }
                );
    }

    @Test
    void shouldFindMagicNumberIssuesOnProject() {
        assertThat(allIssuesOn("gosu/dk/ifforsikring/gosu/simple/FizzBuzz.gs"))
                .filteredOn(issue -> issue.getRule().equals("gosu:MagicNumbersRule"))
                .hasSize(3)
                .allSatisfy(
                        issue -> {
                            assertThat(issue.getType()).isEqualTo(Common.RuleType.CODE_SMELL);
                            assertThat(issue.getScope()).isEqualTo("MAIN");
                            assertThat(issue.getSeverity()).isEqualTo(Common.Severity.MAJOR);
                            assertThat(issue.getMessage()).matches(
                                    "Assign this magic number \\d+ to a well-named constant, and use the constant instead\\."
                            );
                        }
                );
    }

    @Test
    void shouldFindUnnecessaryJavaImportsIssuesOnProject() {
        assertThat(allProjectIssues())
                .filteredOn(issue -> issue.getRule().equals("gosu:UnnecessaryImportRule"))
                .hasSize(6)
                .allSatisfy(
                        issue -> {
                            assertThat(issue.getComponent()).isIn(
                                    componentKeyOf("gosu/dk/ifforsikring/gosu/simple/Fizz.gs"),
                                    componentKeyOf("gosu/dk/ifforsikring/gosu/simple/Buzz.gs"),
                                    componentKeyOf("gosu/dk/ifforsikring/gosu/simple/FizzBuzz.gs")
                            );
                            assertThat(issue.getType()).isEqualTo(Common.RuleType.CODE_SMELL);
                            assertThat(issue.getScope()).isEqualTo("MAIN");
                            assertThat(issue.getSeverity()).isEqualTo(Common.Severity.MINOR);
                            assertThat(issue.getMessage()).isEqualTo("Unnecessary import, Java Classes are always available.");
                        }
                );
    }

    private String componentKeyOf(String component) {
        return SIMPLE_GOSU_PROJECT.getMainComponentKeyOf(component);
    }

    private List<Issues.Issue> allIssuesOn(String component) {
        return sonarServer.getClient().issues().findIssuesOnComponent(componentKeyOf(component));
    }

    private List<Issues.Issue> allProjectIssues() {
        return sonarServer.getClient().issues().find(SIMPLE_GOSU_PROJECT.getProjectKey());
    }
}
