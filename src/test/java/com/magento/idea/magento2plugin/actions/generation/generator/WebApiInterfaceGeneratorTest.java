/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.psi.PsiFile;
import com.jetbrains.php.codeInsight.PhpCodeInsightUtil;
import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.jetbrains.php.lang.documentation.phpdoc.PhpDocUtil;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.psi.elements.ClassReference;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import com.jetbrains.php.lang.psi.elements.PhpUse;
import com.jetbrains.php.lang.psi.elements.PhpUseList;
import com.magento.idea.magento2plugin.actions.generation.data.php.WebApiInterfaceData;
import com.magento.idea.magento2plugin.actions.generation.generator.php.WebApiInterfaceGenerator;
import com.magento.idea.magento2plugin.magento.files.WebApiInterfaceFile;
import com.magento.idea.magento2plugin.util.GetPhpClassByFQN;
import com.magento.idea.magento2plugin.util.php.PhpTypeMetadataParserUtil;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public class WebApiInterfaceGeneratorTest extends BaseGeneratorTestCase {

    private static final String MODULE_NAME = "Foo_Bar";
    private static final String EXPECTED_DIRECTORY = "src/app/code/Foo/Bar/Api";

    private static final String COULD_NOT_GENERATE_MESSAGE =
            WebApiInterfaceFile.TEMPLATE + " could not be generated!";
    private static final String COULD_NOT_FIND_SERVICE_MESSAGE =
            "A service for the test could not be found!";
    private static final String METHOD_DOES_NOT_HAVE_INHERIT_DOC =
            "Service method does not have @inheritDoc block";
    private static final String SERVICE_SHOULD_HAVE_INTERFACE_IMPORTED =
            "Service should have generated interface in the use block";
    private static final String SERVICE_SHOULD_IMPLEMENT_INTERFACE =
            "Service should implement generated interface";

    private static final String FIRST_SERVICE_FQN = "Foo\\Bar\\Service\\SimpleService";
    private static final String FIRST_SERVICE_METHODS = "execute";
    private static final String FIRST_INTERFACE_NAME = "SimpleServiceInterface";
    private static final String FIRST_INTERFACE_DESCRIPTION = "Simple service description.";

    private static final String SECOND_SERVICE_FQN = "Foo\\Bar\\Service\\SimpleServiceTwo";
    private static final String SECOND_SERVICE_METHODS = "execute,fetch";
    private static final String SECOND_INTERFACE_NAME = "SimpleServiceTwoInterface";
    private static final String SECOND_INTERFACE_DESCRIPTION = "Simple service two description.";

    @Override
    public void setUp() throws Exception {
        super.setUp();
        PhpProjectConfigurationFacade.getInstance(myFixture.getProject())
                .setLanguageLevel(PhpLanguageLevel.PHP720);
    }

    /**
     * Test generation of Web API interface for a service with primitive types.
     */
    @SuppressWarnings({"PMD.JUnitTestContainsTooManyAsserts"})
    public void testWithPrimitiveTypes() {
        final PhpClass service = extractServiceByFqn(FIRST_SERVICE_FQN);
        final List<Method> publicMethods = PhpTypeMetadataParserUtil.getPublicMethods(service);

        final PsiFile result = generateInterfaceForService(
                FIRST_SERVICE_FQN,
                FIRST_INTERFACE_NAME,
                FIRST_INTERFACE_DESCRIPTION,
                publicMethods.stream()
                        .filter(
                                method -> Arrays.asList(FIRST_SERVICE_METHODS.split(","))
                                        .contains(method.getName())
                        )
                        .collect(Collectors.toList())
        );

        if (result == null) {
            fail(COULD_NOT_GENERATE_MESSAGE);
        }

        assertGeneratedFileIsCorrect(
                myFixture.configureByFile(this.getFixturePath(FIRST_INTERFACE_NAME.concat(".php"))),
                EXPECTED_DIRECTORY,
                result
        );

        assertServiceMethodsHaveInheritDoc(
                service,
                Arrays.asList(FIRST_SERVICE_METHODS.split(","))
        );

        assertServiceHasInterfaceReference(
                service,
                FIRST_INTERFACE_NAME
        );
    }

    /**
     * Test generation of Web API interface for a service with Object types.
     */
    @SuppressWarnings({"PMD.JUnitTestContainsTooManyAsserts"})
    public void testWithObjectTypesAndPhpDocComments() {
        final PhpClass service = extractServiceByFqn(SECOND_SERVICE_FQN);
        final List<Method> publicMethods = PhpTypeMetadataParserUtil.getPublicMethods(service);

        final PsiFile result = generateInterfaceForService(
                SECOND_SERVICE_FQN,
                SECOND_INTERFACE_NAME,
                SECOND_INTERFACE_DESCRIPTION,
                publicMethods.stream()
                        .filter(
                                method -> Arrays.asList(SECOND_SERVICE_METHODS.split(","))
                                        .contains(method.getName())
                        )
                        .collect(Collectors.toList())
        );

        if (result == null) {
            fail(COULD_NOT_GENERATE_MESSAGE);
        }

        assertGeneratedFileIsCorrect(
                myFixture.configureByFile(
                        this.getFixturePath(SECOND_INTERFACE_NAME.concat(".php"))
                ),
                EXPECTED_DIRECTORY,
                result
        );

        assertServiceMethodsHaveInheritDoc(
                service,
                Arrays.asList(SECOND_SERVICE_METHODS.split(","))
        );

        assertServiceHasInterfaceReference(
                service,
                SECOND_INTERFACE_NAME
        );
    }

    /**
     * Asset that service methods have inherit doc after Web API interface generation.
     *
     * @param service PhpClass
     * @param checkingMethods List[String]
     */
    private void assertServiceMethodsHaveInheritDoc(
            final @NotNull PhpClass service,
            final @NotNull List<String> checkingMethods
    ) {
        for (final Method method : service.getMethods()) {
            if (checkingMethods.contains(method.getName())) {
                final PhpDocComment methodDoc = method.getDocComment();

                if (methodDoc == null || !methodDoc.getText().contains(PhpDocUtil.INHERITDOC_TAG)) {
                    fail(METHOD_DOES_NOT_HAVE_INHERIT_DOC);
                }
            }
        }
    }

    /**
     * Assert that service has interface imported in use block and in implements part.
     *
     * @param service PhpClass
     * @param generatedInterfaceName String
     */
    @SuppressWarnings({"PMD.CyclomaticComplexity"})
    private void assertServiceHasInterfaceReference(
            final @NotNull PhpClass service,
            final @NotNull String generatedInterfaceName
    ) {
        boolean implementFound = false;

        for (final ClassReference reference : service.getImplementsList().getReferenceElements()) {
            if (reference.getName() != null && reference.getName().equals(generatedInterfaceName)) {
                implementFound = true;
                break;
            }
        }

        if (!implementFound) {
            fail(SERVICE_SHOULD_IMPLEMENT_INTERFACE);
        }

        final PhpPsiElement scopeForUseOperator =
                PhpCodeInsightUtil.findScopeForUseOperator(service);

        if (scopeForUseOperator == null) {
            fail(SERVICE_SHOULD_IMPLEMENT_INTERFACE);
        }
        final List<PhpUseList> imports = PhpCodeInsightUtil.collectImports(scopeForUseOperator);
        boolean importFound = false;

        for (final PhpUseList useList : imports) {
            final PhpUse[] uses = useList.getDeclarations();

            for (final PhpUse use : uses) {
                if (use.getName().equals(generatedInterfaceName)) {
                    importFound = true;
                    break;
                }
            }
        }

        if (!importFound) {
            fail(SERVICE_SHOULD_HAVE_INTERFACE_IMPORTED);
        }
    }

    /**
     * Extract service by FQN.
     *
     * @param classFqn String
     *
     * @return PhpClass
     */
    private PhpClass extractServiceByFqn(final @NotNull String classFqn) {
        final PhpClass service = GetPhpClassByFQN
                .getInstance(myFixture.getProject()).execute(classFqn);

        if (service == null) {
            fail(COULD_NOT_FIND_SERVICE_MESSAGE);
        }

        return service;
    }

    /**
     * Generate interface for specified service.
     *
     * @param serviceFqn String
     * @param interfaceName String
     * @param interfaceDescription String
     * @param methodList List[Method]
     *
     * @return PsiFile
     */
    private PsiFile generateInterfaceForService(
            final @NotNull String serviceFqn,
            final @NotNull String interfaceName,
            final @NotNull String interfaceDescription,
            final @NotNull List<Method> methodList
    ) {
        final WebApiInterfaceData data = new WebApiInterfaceData(
                MODULE_NAME,
                serviceFqn,
                interfaceName,
                interfaceDescription,
                methodList
        );
        final WebApiInterfaceGenerator generator = new WebApiInterfaceGenerator(
                data,
                myFixture.getProject()
        );

        return generator.generate("test");
    }
}
