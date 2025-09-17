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
package dk.ifforsikring.sonarqube.gosu.plugin.rules.smells;

import com.google.inject.Inject;
import dk.ifforsikring.sonarqube.gosu.antlr.GosuLexer;
import dk.ifforsikring.sonarqube.gosu.antlr.GosuParser;
import dk.ifforsikring.sonarqube.gosu.plugin.GosuFileProperties;
import dk.ifforsikring.sonarqube.gosu.plugin.issues.GosuIssue;
import dk.ifforsikring.sonarqube.gosu.plugin.rules.BaseGosuRule;
import org.antlr.v4.runtime.Token;
import org.apache.commons.lang3.Strings;
import org.sonar.check.Rule;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static dk.ifforsikring.sonarqube.gosu.plugin.rules.smells.TODOsRule.KEY;

@Rule(key = KEY)
public class TODOsRule extends BaseGosuRule {
    static final String KEY = "TODOsRule";
    private Set<Integer> commentTokens = new HashSet<>(Arrays.asList(GosuLexer.COMMENT, GosuLexer.LINE_COMMENT));
    private GosuFileProperties gosuFileProperties;

    @Inject
    TODOsRule(GosuFileProperties gosuFileProperties) {
        this.gosuFileProperties = gosuFileProperties;
    }

    @Override
    public void exitStart(GosuParser.StartContext ctx) {
        final List<Token> tokens = gosuFileProperties.getTokenStream()
                .getTokens()
                .stream()
                .filter(token -> commentTokens.contains(token.getType()))
                .collect(Collectors.toList());

        for (Token token : tokens) {
            if (Strings.CI.contains(token.getText(), "TODO")) {
                addIssue(new GosuIssue.GosuIssueBuilder(this)
                        .onToken(token)
                        .withMessage("Complete the task associated to this TODO comment.")
                        .build());
            }
        }
    }

    @Override
    protected String getKey() {
        return KEY;
    }
}
