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
package dk.ifforsikring.sonarqube.gosu.plugin.rules;

import com.google.inject.Inject;
import dk.ifforsikring.sonarqube.gosu.antlr.GosuParserBaseListener;
import dk.ifforsikring.sonarqube.gosu.plugin.issues.Issue;
import dk.ifforsikring.sonarqube.gosu.plugin.issues.IssueCollector;
import org.sonar.api.rule.RuleKey;

import static dk.ifforsikring.sonarqube.gosu.language.GosuLanguage.REPOSITORY_KEY;

/**
 * Base class for all Gosu Sonarqube rules.
 */
public abstract class BaseGosuRule extends GosuParserBaseListener {

    @Inject
    private IssueCollector collector;

    /**
     * @return Rule Key
     */
    protected abstract String getKey();

    /**
     * Returns a Gosu Rule specific Rule Key
     * Used to associate with the Rule description
     *
     * @return Rule Key
     */
    public RuleKey getRuleKey() {
        return RuleKey.of(REPOSITORY_KEY, getKey());
    }

    /**
     * Add Issue to the IssueCollector
     *
     * @param gosuIssue - Issue with text range and message to be displayed in SonarQube
     */
    protected void addIssue(Issue gosuIssue) {
        collector.addIssue(gosuIssue);
    }
}
