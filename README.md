# SonarQube Gosu Plugin
[![.github/workflows/gradle.yml](https://github.com/Topdanmark/sonar-gosu-plugin/actions/workflows/gradle.yml/badge.svg)](https://github.com/Topdanmark/sonar-gosu-plugin/actions/workflows/build.yml) [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=Topdanmark_sonar-gosu-plugin&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=Topdanmark_sonar-gosu-plugin)

[Gosu Programming Language](https://gosu-lang.github.io/) Plugin for SonarQube.

For plugin configurations and a list of all coding rules included in the plugin, see: [plugin documentation](docs/README.md).

## Compatibility
| SonarQube Version | Plugin Version               |
|-------------------|------------------------------|
| v25+              | [1.0.0] |

## Installation
### SonarQube Marketplace
Available in the SonarQube marketplace.

### SonarQube On-premise
Download the latest JAR file and put it into SonarQube's plugin directory (`./extensions/plugins`). 
After restarting the server, a new Quality Profile for Gosu with all the plugin rules should be available on `Quality 
Profiles`.

## License
The SonarQube Plugin for Gosu Programming Language is released under the [GNU AGPL License, Version 3.0](https://www.gnu.org/licenses/agpl-3.0.en.html).

## Why ANTLR4?
The SonarQube Gosu Plugin uses [ANTLR (Another Tool For Language Recognition)](https://www.antlr.org/) to execute static analysis of Gosu
code.
The grammar was written by FRI-DAY in 2023. The [official Gosu grammar](https://gosu-lang.github.io/grammar.html) is
written in a now unsupported version of ANTLR.

### Working with ANTLR4
If there are bugs in the grammar or a new release of the Gosu language contains new features, and the grammar needs to 
be modified, there is an [ANTLR v4](https://github.com/antlr/intellij-plugin-v4/blob/master/README.md) plugin for IntelliJ IDEA, that can verify the parse tree built by ANTLR.

#### How does it work?
The Gosu ANTLR v4 Grammar is split into two files: `GosuLexer.g4` and `GosuParser.g4`.
The Lexer is responsible for file tokenization. It will process an input sequence of characters into an output sequence 
of tokens.
The Parser is responsible for grouping tokens into contexts and then building the parse tree.
From those two files, ANTLR v4 will generate the `GosuParserBaseListener` that is used in the rules.
The `GosuParserBaseListener` ([Observer Pattern](https://en.wikipedia.org/wiki/Observer_pattern#:~:text=In%20software%20design%20and%20engineering,calling%20one%20of%20their%20methods)) has a set of hooks that can be used for each parsing context. 
For each of the contexts, there is an `enter` and `exit` hook.
E.g:
```java
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterIfStatement(GosuParser.IfStatementContext ctx) { }

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitIfStatement(GosuParser.IfStatementContext ctx) { }
```
In this case, the hooks will be triggered on every Gosu `if` statement the parser finds for a given Gosu file.

## Adding new Gosu rules
### BaseGosuRule
All the rules inherit from `BaseGosuRule` class. The rules are then injected into the GosuFileParser using 
[Google Guice](https://github.com/google/guice).

The `BaseGosuRule` has hooks for all parsing contexts, since it is a subclass of the `GosuParserBaseListener`, and some 
common behaviour for Rules.

The `BaseGosuRule` also provides a method to add SonarQube issues for the file being analysed. A `GosuIssue` can be 
build using its builder, and issues are added using the `BaseGosuRule.addIssue()`.
E.g:
```java
addIssue(
        new GosuIssue.GosuIssueBuilder(this)
        .onToken(token).withMessage("Complete the task associated to this TODO comment.")
        .build()
);
```
The message will be shown on the SonarQube interface on the Gosu file line that contains the token specified in the 
`.onToken()` method.

#### Writing a new Gosu rule
These are the steps to write a new Gosu rule:
1. Create a new Rule implementation in the appropriate package:
   E.g.:
```java
package dk.ifforsikring.sonarqube.gosu.plugin.rules.bugs;

public class MyAwesomeNewRule extends BaseGosuRule {
    
}
```
2. Add a unique key for the new Rule:
   E.g.:
```java
package dk.ifforsikring.sonarqube.gosu.plugin.rules.bugs;

@Rule(key = MyNewRule.KEY)
public class MyNewRule extends BaseGosuRule {

    static final String KEY = "MyNewRule";
    
}
```
3. Add the metadata and rule description files in the `resources/sonar` folder:
:warning: `Both files must have the same name as the Rule key. This is required for the plugin to automatically load the 
rule metadata and description of each rule.`
E.g.:
`resources/sonar/MyNewRule.json`:
```json
{
   "title": "My new rule title",
   "type": "BUG",
   "status": "ready",
   "tags": [
      "pitfall",
      "cert",
      "unused"
   ],
   "remediation": {
      "func": "Constant\/Issue",
      "constantCost": "10min"
   },
   "defaultSeverity": "Major",
   "scope": "Main"
}
```
This is the description shown on the SonarQube interface. As a good practice add examples of compliant and non-compliant 
code snippets.
`resources/sonar/MyNewRule.html`:
```html
<p>My new rule description. </p>
<h2>Non-compliant Code Example</h2>
<pre>
   Non-compliant code snippet
</pre>
<h2>Compliant Solution</h2>
<pre>
   Compliant version of previous non-complaint code snippet
</pre>
```
4. Write the logic for the rule using ANTLR. Note that on the `enter` hooks **there is no parsed context data**. This is 
because hooks are triggered during the parsing process and the context will only be available on the `exit` hooks.
```java
package dk.ifforsikring.sonarqube.gosu.plugin.rules.bugs;

@Rule(key = MyANewRule.KEY)
public class MyNewRule extends BaseGosuRule {

    static final String KEY = "MyNewRule";

   @Override
   public void exitIfStatement(GosuParser.IfStatementContext ctx) {
      //My rule logic
   }
}
```
Some rules require the file that is being analysed and/or the token stream generate by the Lexer for that file.
To get this information, inject the GosuFileProperties on the rule class:
```java
package dk.ifforsikring.sonarqube.gosu.plugin.rules.bugs;

@Rule(key = MyNewRule.KEY)
public class MyNewRule extends BaseGosuRule {

   static final String KEY = "MyNewRule";

   private final GosuFileProperties gosuFileProperties;

   @Inject
   public LinesOfCodeMetric(GosuFileProperties gosuFileProperties) {
       this.gosuFileProperties = gosuFileProperties;
   }
   
   /* Omitted */
}
```
5. Add a SonarQube issue matching the rule criteria:
```java
package dk.ifforsikring.sonarqube.gosu.plugin.rules.bugs;

@Rule(key = MyNewRule.KEY)
public class MyNewRule extends BaseGosuRule {
    static final String KEY = "MyNewRule";
    
    private int ifCounter = 0;
    
    @Override
    public void enterIfStatement(GosuParser.IfStatementContext context) {
        ifCounter++;
    }

    @Override
    public void exitIfStatement(GosuParser.IfStatementContext context) {
        if(ifCounter >= 5) {
          addIssue(
                  new GosuIssue.GosuIssueBuilder(this)
                          .onContext(context.expression())
                          .withMessage("Class has too many ifs statements.")
                          .build()
          );   
       }
    }
}
```
### Testing
1. Add files containing compliant and non-compliant code under `test/resources/rules/<Rule Key>/` folder.
   E.g:
   `test/resources/rules/MyNewRule/ok.gs`
```gosu
package rules.MyNewRule

final class ok {

  function doSomething() {
    if(invalid()) {
      throw new RuntimeException("Somethign went wrong")
    }
    
    process()
  }
  
  private function invalid(): boolean { return true; } 
  
  private function process() { /* Do something */ }

}
```

E.g:
`test/resources/rules/MyNewRule/nok.gs`
```gosu
package rules.MyNewRule

final class nok {

  function doSomething() {
    if(invalid()) { throw new RuntimeException("Kaboom!") }
    if(enable()) { /* do something */ }
    if(otherCondition()) { /* do something else */ }
    if(anotherCondition()) { /* another thing going on here */ }
    if(yetAnotherCondition()) { /* ok, that's too many conditions */ }
  }

}
```
2. Write the test, covering compliant and non-compliant cases.
   a. For compliant cases, assert that the rule did not find any issues on the Gosu file:
```java
package dk.ifforsikring.sonarqube.gosu.plugin.rules.bugs;

import org.junit.jupiter.api.Test;
import static de.friday.test.support.rules.dsl.gosu.GosuRuleTestDsl.given;

public class MyNewRuleTest {
    
   @Test
   void findsNoIssuesWhenIfConditionsAreLessThanThreshold() {
      given("MyNewRule/ok.gs")
              .whenCheckedAgainst(MyNewRule.class)
              .then()
              .noIssuesFound();
   }
   
   
}
```
b. For non-compliant cases, assert that the issue was found on the file and, optionally, if the issue was found in an
expected location:
```java
package dk.ifforsikring.sonarqube.gosu.plugin.rules.bugs;

import org.junit.jupiter.api.Test;
import static de.friday.test.support.rules.dsl.gosu.GosuRuleTestDsl.given;

public class MyNewRuleTest {
    
   @Test
   void findsIssuesWhenIfConditionsAreGreaterThanThreshold() {
      given("MyNewRule/nok.gs")
              .whenCheckedAgainst(MyNewRule.class)
              .withRuleProperty("Param1", "100")
              .then()
              .issuesFound()
              .hasSizeEqualTo(1)
              .and()
              .areLocatedOn(
                      GosuIssueLocations.of(Arrays.asList(3, 1, 3, 28))
              );
   }
}
```
### Testing the plugin on a local SonarQube server
1. Execute the `docker/start-sonar-server.sh` script. Eg.:
```shell
$ cd docker
$ ./start-sonar-server.sh
```
The script will build the plugin JAR, containerize it and start a Docker compose service with it.
Once it finishes, SonarQube will be available at `http://localhost:9000`.

2. With the server up and running, create an authentication token as described [here](https://docs.sonarqube.org/latest/user-guide/user-token/).

3. Scan a Gosu project as follows:
```shell
docker run \
  --network=host \
  --rm \
  -e SONAR_HOST_URL="http://localhost:9000" \
  -e SONAR_LOGIN="<AUTHENTICATION TOKEN>" \
  -v "$(pwd):/usr/src" \
  sonarsource/sonar-scanner-cli \
  -Dsonar.projectKey=<PROJECT KEY> \
  -Dsonar.sources=<PROJECT SOURCE FOLDER> \
  -Dsonar.coverage.jacoco.xmlReportPaths=<PROJECT JACOCO XML REPORT PATHS> \
  -Dsonar.jacoco.reportPath=PROJECT JACOCO BINARY> -X
```
#### Installing the plugin on a running SonarQube Docker container
In case you already have the Docker container with the SonarQube server running, you can install the plugin as follows:

1. Build the plugin JAR file with your changes;
2. Copy the JAR file to the running container;
   E.g.:
```shell
$ docker cp build/libs/sonar-gosu-plugin-*.jar sonarqube:/opt/sonarqube/extensions/plugins
```
3. Restart SonarQube Docker container;
   E.g.:
```shell
$ docker container restart <SonarQube Container ID>
```
#### Elasticsearch issue
SonarQube has an embedded Elasticsearch instance. It might fail because of low memory:

Follow the instructions described [here](https://www.elastic.co/guide/en/elasticsearch/reference/current/vm-max-map-count.html) and increase the `vm.max_map_count`.
E.g.: `$ sysctl -w vm.max_map_count=262144`

The service should start normally after it has been restarted with the new configuration.

-------
## Resources
- [How to contribute to Open Source?](https://opensource.guide/how-to-contribute/);
- [ANTLR4 Documentation](https://github.com/antlr/antlr4/blob/master/doc/index.md);

