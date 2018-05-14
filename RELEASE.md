# OpenIO Java API release process

Useful links:
* [Sonatype: Deploying to OSSRH with Gradle](http://central.sonatype.org/pages/gradle.html)
* [Sonatype: Releasing the Deployment](http://central.sonatype.org/pages/releasing-the-deployment.html)
* [Linux/Python Compatible Semantic Versioning](https://docs.openstack.org/pbr/latest/user/semver.html)
* [OpenIO Java API GitHub page](https://github.com/open-io/oio-api-java)
* [OpenIO Java API Travis CI page](https://travis-ci.org/open-io/oio-api-java)
* [OpenIO Java API Codecov page](https://codecov.io/gh/open-io/oio-api-java)

## Releasing a snapshot version

1. Ensure there is no pending pull request that you would like to merge.
2. Ensure the version specified in [`version.txt`](version.txt) is correct. It should be greater than the last released version. If not, update it and commit.
3. Run the tests suites on Travis CI. **OR** Deploy an openio-sds namespace, set `OIO_NS` environment variable and run the tests locally, using `./gradlew test --rerun-tasks --info`.
4. Ensure there is no compilation warning (including during javadoc build). You can run the compilation process without tests with `./gradlew assemble --rerun-tasks`.
5. Complete `~/.gradle/gradle.properties` with the appropriate username, password and key name (the key associated with the email address you use on GitHub)

```
ossrhUsername=openio
ossrhPassword=obviouslythispasswordisinvalid
signing.gnupg.keyName=EDD618881C889BBI6B00B5CAFEBABEE769498FAF
```
At this point, GPG key is not mandatory since we will just build a snapshot version.

6. Run `./gradlew uploadArchives`. Unless your credentials are invalid, this should upload a snapshot version [here](https://oss.sonatype.org/content/groups/public/io/openio/sds/openio-api/).
7. You can now (or after a few minutes?) use this version in dependent projects, and send it to QA.

## Releasing a stable version

Steps 1 to 5 are the same as for the snapshot version.

6. Run `./gradlew -Pbuild.type=release uploadArchives --rerun-tasks`. Unless your credentials are invalid, this should upload a new version [here](https://oss.sonatype.org/content/groups/public/io/openio/sds/openio-api/).
7. Log-in to [Nexus Repository Manager](https://oss.sonatype.org/#nexus-search;quick~openio) with the `openio` user. In the left bar, click *Staging Repositories*, and then find openio in the list. Check everything is correct, then press *Close* button in the top bar.
8. After one minute, refresh the page. After selecting the openio entry, select the *Activity* tab and check everything is green. Typical failures are javadoc warnings or missing signature for the jars. Fix the problems, click the *Drop* button in the top bar and restart step 6.
9. Click *Release* button in the top bar. After a few hours your build should be synchronized with Maven Central. Notice that the new version may be available even if the web pages still say the previous version is the latest.
10. Write the changelog with the title of all pull requests merged to the current branch since the last version.
11. Create a signed git tag with `git tag -a -s -u <key_name> <version> HEAD`, fill the tag with the changelog. Then push the tag to GitHub.
