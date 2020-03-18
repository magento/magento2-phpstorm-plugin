# Setting up development environment

1. Check out this repository
1. Download PhpStorm build specified in the [META-INF/plugin.xml](resources/META-INF/plugin.xml). Older builds can be found [here](https://confluence.jetbrains.com/display/PhpStorm/Previous+PhpStorm+Releases)
1. Create a new project in the InteliJ. The type of the project must be "InteliJ Platform Plugin". Specify a path to the PhpStorm from the previous step as SDK for this project
1. Instead of creating a project in a new directory, point it to an existing one checked out in the first step. When prompted, replace all configs
1. Revert any local modifications (which were done by the IDE during project creation)
1. Follow [these steps](http://www.jetbrains.org/intellij/sdk/docs/products/phpstorm/setting_up_environment.html) and additionally include dependency on the `javascript-openapi`
1. You may also need to install JDK 1.8
1. To make sure that environment is configured correctly, `Run` the plugin. You should see a new instance of PhpStorm launched with the plugin installed. Make sure that plugin is enabled in PhpStorm settings and indexing is finished. Plugin features should be accessible at this point

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
