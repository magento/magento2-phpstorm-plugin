# Setting up development environment

1. Check out this repository.
1. Open the project in IntelliJ IDEA.
1. Make sure that you are on the latest develop branch (for example `5.4.0-develop`).
1. Import the Gradle project from `build.gradle.kts`.
1. Use **JDK 21** for both the project SDK and the Gradle JVM.
1. In the Gradle tool window, run `Tasks > Intellij platform > runIde`.
1. The task launches a PhpStorm sandbox with the plugin installed. Make sure that the plugin is enabled and indexing is finished before testing features.

# Plugin publication

1. [Build a new version of the plugin](https://www.jetbrains.org/intellij/sdk/docs/basics/getting_started/deploying_plugin.html)
1. [Install the newly created archive/jar file from disk](https://www.jetbrains.com/help/idea/managing-plugins.html#installing-plugins-from-disk) for testing
1. [Publish](https://www.jetbrains.org/intellij/sdk/docs/basics/getting_started/publishing_plugin.html)

# Contribution process

If you are a new GitHub user, we recommend that you create your own [free github account](https://github.com/signup/free). This will allow you to collaborate with the Magento 2 development team, fork this project and send pull requests.

1. Search current [listed issues](https://github.com/magento/magento2-phpstorm-plugin/issues) (open or closed) for similar proposals of intended contribution before starting work on a new contribution.
2. Review and sign the [Contributor License Agreement (CLA)](https://opensource.adobe.com/cla.html) if this is your first time contributing. You only need to sign the CLA once.
3. Create and test your work.
4. Fork this repository and create a pull request.
5. Once your contribution is received the Magento 2 development team will review the contribution and collaborate with you as needed.
