# Setting up development environment

1. Check out this repository
1. Open a folder with the project in the IntelliJ Ultimate using the `open` action button.
1. Make sure that you on the latest develop branch (e.g 1.0.0-develop)
1. Right-click on the `build.gradle` file, choose "Import Gradle project"
1. When the Gradle sections appeared in the right bar, navigate there and right-click `magento-2-php-storm-plguin > Tasks -> Intellij -> runIde`
1. Click on "Run "magento-2-php-storm-plugin" to run the plugin. You should see a new instance of IntelliJ launched with the plugin installed. Make sure that the plugin is enabled in IntelliJ settings and indexing is finished. Plugin features should be accessible at this point.

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
