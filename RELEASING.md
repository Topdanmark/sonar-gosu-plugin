## Releasing
These are the general steps to release a new version of the plugin:

1. Check that the plugin version in `gradle.properties` does not contain any suffix, e.g. `SNAPSHOT`
2. Update the `CHANGELOG.md` file
3. Create a pull request with the changes
4. Once the pull request is completed, create a tag and a draft [Github release](https://docs.github.com/en/repositories/releasing-projects-on-github/about-releases) with the 
plugin JAR and change notes
5. Review the draft GitHub release and, if everything is ok, release it

### Sonarqube Marketplace
After a new version is released, [announce](https://community.sonarsource.com/t/deploying-to-the-marketplace/35236#announcing-new-releases-2) it on Sonarqube Marketplace.

Follow the steps bellow to publish the new release on Sonarqube Marketplace:
1. Fork [sonar-update-center.properties](https://github.com/SonarSource/sonar-update-center-properties) repository
2. Add the new version of the plugin on the [communitygosu.properties](https://github.com/SonarSource/sonar-update-center-properties/blob/master/communitygosu.properties)
3. Create a pull request with your changes. [Example](https://github.com/SonarSource/sonar-update-center-properties/pull/490)
4. Once a SonarSource representative review and approve your pull request, it will be automatically published to the marketplace
