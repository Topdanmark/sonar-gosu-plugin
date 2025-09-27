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
package dk.ifforsikring.sonarqube.gosu.plugin.rules.bugs;

import dk.ifforsikring.sonarqube.gosu.antlr.GosuParser;
import dk.ifforsikring.sonarqube.gosu.plugin.issues.GosuIssue;
import dk.ifforsikring.sonarqube.gosu.plugin.rules.BaseGosuRule;
import org.sonar.check.Rule;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

@Rule(key = SameConditionsInIfRule.KEY)
public class SameConditionsInIfRule extends BaseGosuRule {
    static final String KEY = "SameConditionsInIfRule";
    private int counter;
    private final HashMap<Integer, Set<String>> ifExpressions = new HashMap<>();

    @Override
    public void enterIfStatement(GosuParser.IfStatementContext ctx) {
        if (ctx.getParent().getParent() instanceof GosuParser.ElseStatementContext) {
            return;
        }
        counter++;
        if (ifExpressions.containsKey(counter)) {
            ifExpressions.get(counter).clear();
        } else {
            ifExpressions.put(counter, new HashSet<>());
        }
    }

    @Override
    public void exitIfStatement(GosuParser.IfStatementContext ctx) {
        String condition = ctx.expression().getText();

        if (!ifExpressions.get(counter).add(condition)) {
            addIssue(new GosuIssue.GosuIssueBuilder(this)
                    .onContext(ctx.expression())
                    .withMessage("Related \"if/else if\" statements should not have the same condition")
                    .build());
        }

        if (ctx.getParent().getParent() instanceof GosuParser.ElseStatementContext) {
            return;
        }

        counter--;
        if (counter == 0) {
            ifExpressions.clear();
        }
    }

    @Override
    protected String getKey() {
        return KEY;
    }
}
