/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.packages;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.xml.XmlFile;
import com.jetbrains.php.lang.psi.PhpFile;
import com.magento.idea.magento2uct.bundles.UctInspectionBundle;
import com.magento.idea.magento2uct.inspections.UctProblemsHolder;
import com.magento.idea.magento2uct.inspections.php.api.CalledNonApiMethod;
import com.magento.idea.magento2uct.inspections.php.api.CalledNonInterfaceMethod;
import com.magento.idea.magento2uct.inspections.php.api.ExtendedNonApiClass;
import com.magento.idea.magento2uct.inspections.php.api.ImplementedNonApiInterface;
import com.magento.idea.magento2uct.inspections.php.api.ImportedNonApiClass;
import com.magento.idea.magento2uct.inspections.php.api.ImportedNonApiInterface;
import com.magento.idea.magento2uct.inspections.php.api.InheritedNonApiInterface;
import com.magento.idea.magento2uct.inspections.php.api.OverriddenNonApiConstant;
import com.magento.idea.magento2uct.inspections.php.api.OverriddenNonApiProperty;
import com.magento.idea.magento2uct.inspections.php.api.PossibleDependencyOnImplDetails;
import com.magento.idea.magento2uct.inspections.php.api.UsedNonApiConstant;
import com.magento.idea.magento2uct.inspections.php.api.UsedNonApiProperty;
import com.magento.idea.magento2uct.inspections.php.api.UsedNonApiType;
import com.magento.idea.magento2uct.inspections.php.deprecation.CallingDeprecatedMethod;
import com.magento.idea.magento2uct.inspections.php.deprecation.ExtendingDeprecatedClass;
import com.magento.idea.magento2uct.inspections.php.deprecation.ImplementedDeprecatedInterface;
import com.magento.idea.magento2uct.inspections.php.deprecation.ImportingDeprecatedClass;
import com.magento.idea.magento2uct.inspections.php.deprecation.ImportingDeprecatedInterface;
import com.magento.idea.magento2uct.inspections.php.deprecation.InheritedDeprecatedInterface;
import com.magento.idea.magento2uct.inspections.php.deprecation.OverridingDeprecatedConstant;
import com.magento.idea.magento2uct.inspections.php.deprecation.OverridingDeprecatedProperty;
import com.magento.idea.magento2uct.inspections.php.deprecation.UsingDeprecatedClass;
import com.magento.idea.magento2uct.inspections.php.deprecation.UsingDeprecatedConstant;
import com.magento.idea.magento2uct.inspections.php.deprecation.UsingDeprecatedInterface;
import com.magento.idea.magento2uct.inspections.php.deprecation.UsingDeprecatedProperty;
import com.magento.idea.magento2uct.inspections.php.existence.CalledNonExistentMethod;
import com.magento.idea.magento2uct.inspections.php.existence.ExtendedNonExistentClass;
import com.magento.idea.magento2uct.inspections.php.existence.ImplementedNonExistentInterface;
import com.magento.idea.magento2uct.inspections.php.existence.ImportingNonExistentClass;
import com.magento.idea.magento2uct.inspections.php.existence.ImportingNonExistentInterface;
import com.magento.idea.magento2uct.inspections.php.existence.InheritedNonExistentInterface;
import com.magento.idea.magento2uct.inspections.php.existence.OverriddenNonExistentConstant;
import com.magento.idea.magento2uct.inspections.php.existence.OverriddenNonExistentProperty;
import com.magento.idea.magento2uct.inspections.php.existence.UsedNonExistentConstant;
import com.magento.idea.magento2uct.inspections.php.existence.UsedNonExistentProperty;
import com.magento.idea.magento2uct.inspections.php.existence.UsedNonExistentType;
import com.magento.idea.magento2uct.inspections.xml.UsedDeprecatedConstantInConfig;
import com.magento.idea.magento2uct.inspections.xml.UsedDeprecatedMethodInConfig;
import com.magento.idea.magento2uct.inspections.xml.UsedDeprecatedTypeInConfig;
import com.magento.idea.magento2uct.inspections.xml.UsedNonExistentConstantInConfig;
import com.magento.idea.magento2uct.inspections.xml.UsedNonExistentMethodInConfig;
import com.magento.idea.magento2uct.inspections.xml.UsedNonExistentTypeInConfig;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("PMD.ExcessiveImports")
public enum SupportedIssue {

    EXTENDING_DEPRECATED_CLASS(
            1131,
            IssueSeverityLevel.WARNING,
            "customCode.warnings.deprecated.1131",
            ExtendingDeprecatedClass.class
    ),
    IMPORTING_DEPRECATED_CLASS(
            1132,
            IssueSeverityLevel.WARNING,
            "customCode.warnings.deprecated.1132",
            ImportingDeprecatedClass.class
    ),
    IMPORTING_DEPRECATED_INTERFACE(
            1332,
            IssueSeverityLevel.WARNING,
            "customCode.warnings.deprecated.1332",
            ImportingDeprecatedInterface.class
    ),
    USING_DEPRECATED_CLASS(
            1134,
            IssueSeverityLevel.WARNING,
            "customCode.warnings.deprecated.1134",
            UsingDeprecatedClass.class
    ),
    USING_DEPRECATED_INTERFACE(
            1334,
            IssueSeverityLevel.WARNING,
            "customCode.warnings.deprecated.1334",
            UsingDeprecatedInterface.class
    ),
    USING_DEPRECATED_CONSTANT(
            1234,
            IssueSeverityLevel.WARNING,
            "customCode.warnings.deprecated.1234",
            UsingDeprecatedConstant.class
    ),
    OVERRIDING_DEPRECATED_CONSTANT(
            1235,
            IssueSeverityLevel.WARNING,
            "customCode.warnings.deprecated.1235",
            OverridingDeprecatedConstant.class
    ),
    OVERRIDING_DEPRECATED_PROPERTY(
            1535,
            IssueSeverityLevel.WARNING,
            "customCode.warnings.deprecated.1535",
            OverridingDeprecatedProperty.class
    ),
    INHERITED_DEPRECATED_INTERFACE(
            1337,
            IssueSeverityLevel.WARNING,
            "customCode.warnings.deprecated.1337",
            InheritedDeprecatedInterface.class
    ),
    IMPLEMENTED_DEPRECATED_INTERFACE(
            1338,
            IssueSeverityLevel.WARNING,
            "customCode.warnings.deprecated.1338",
            ImplementedDeprecatedInterface.class
    ),
    CALLING_DEPRECATED_METHOD(
            1439,
            IssueSeverityLevel.WARNING,
            "customCode.warnings.deprecated.1439",
            CallingDeprecatedMethod.class
    ),
    USING_DEPRECATED_PROPERTY(
            1534,
            IssueSeverityLevel.WARNING,
            "customCode.warnings.deprecated.1534",
            UsingDeprecatedProperty.class
    ),
    IMPORTED_NON_EXISTENT_CLASS(
            1112,
            IssueSeverityLevel.CRITICAL,
            "customCode.critical.existence.1112",
            ImportingNonExistentClass.class
    ),
    IMPORTED_NON_EXISTENT_INTERFACE(
            1312,
            IssueSeverityLevel.CRITICAL,
            "customCode.critical.existence.1312",
            ImportingNonExistentInterface.class
    ),
    INHERITED_NON_EXISTENT_INTERFACE(
            1317,
            IssueSeverityLevel.CRITICAL,
            "customCode.critical.existence.1317",
            InheritedNonExistentInterface.class
    ),
    IMPLEMENTED_NON_EXISTENT_INTERFACE(
            1318,
            IssueSeverityLevel.CRITICAL,
            "customCode.critical.existence.1318",
            ImplementedNonExistentInterface.class
    ),
    EXTENDED_NON_EXISTENT_CLASS(
            1111,
            IssueSeverityLevel.CRITICAL,
            "customCode.critical.existence.1111",
            ExtendedNonExistentClass.class
    ),
    OVERRIDDEN_NON_EXISTENT_CONSTANT(
            1215,
            IssueSeverityLevel.CRITICAL,
            "customCode.critical.existence.1215",
            OverriddenNonExistentConstant.class
    ),
    OVERRIDDEN_NON_EXISTENT_PROPERTY(
            1515,
            IssueSeverityLevel.CRITICAL,
            "customCode.critical.existence.1515",
            OverriddenNonExistentProperty.class
    ),
    CALLED_NON_EXISTENT_METHOD(
            1410,
            IssueSeverityLevel.CRITICAL,
            "customCode.critical.existence.1410",
            CalledNonExistentMethod.class
    ),
    USED_NON_EXISTENT_TYPE(
            1110,
            IssueSeverityLevel.CRITICAL,
            "customCode.critical.existence.1110",
            UsedNonExistentType.class
    ),
    USED_NON_EXISTENT_CONSTANT(
            1214,
            IssueSeverityLevel.CRITICAL,
            "customCode.critical.existence.1214",
            UsedNonExistentConstant.class
    ),
    USED_NON_EXISTENT_PROPERTY(
            1514,
            IssueSeverityLevel.CRITICAL,
            "customCode.critical.existence.1514",
            UsedNonExistentProperty.class
    ),
    IMPORTED_NON_API_CLASS(
            1122,
            IssueSeverityLevel.ERROR,
            "customCode.errors.api.1122",
            ImportedNonApiClass.class
    ),
    IMPORTED_NON_API_INTERFACE(
            1322,
            IssueSeverityLevel.ERROR,
            "customCode.errors.api.1322",
            ImportedNonApiInterface.class
    ),
    CALLED_NON_API_METHOD(
            1429,
            IssueSeverityLevel.ERROR,
            "customCode.errors.api.1429",
            CalledNonApiMethod.class
    ),
    OVERRIDDEN_NON_API_CONSTANT(
            1225,
            IssueSeverityLevel.ERROR,
            "customCode.errors.api.1225",
            OverriddenNonApiConstant.class
    ),
    OVERRIDDEN_NON_API_PROPERTY(
            1525,
            IssueSeverityLevel.ERROR,
            "customCode.errors.api.1525",
            OverriddenNonApiProperty.class
    ),
    USED_NON_API_CONSTANT(
            1224,
            IssueSeverityLevel.ERROR,
            "customCode.errors.api.1224",
            UsedNonApiConstant.class
    ),
    USED_NON_API_PROPERTY(
            1524,
            IssueSeverityLevel.ERROR,
            "customCode.errors.api.1524",
            UsedNonApiProperty.class
    ),
    USED_NON_API_TYPE(
            1124,
            IssueSeverityLevel.ERROR,
            "customCode.errors.api.1124",
            UsedNonApiType.class
    ),
    IMPLEMENTED_NON_API_INTERFACE(
            1328,
            IssueSeverityLevel.ERROR,
            "customCode.errors.api.1328",
            ImplementedNonApiInterface.class
    ),
    EXTENDED_NON_API_CLASS(
            1121,
            IssueSeverityLevel.ERROR,
            "customCode.errors.api.1121",
            ExtendedNonApiClass.class
    ),
    INHERITED_NON_API_INTERFACE(
            1327,
            IssueSeverityLevel.ERROR,
            "customCode.errors.api.1327",
            InheritedNonApiInterface.class
    ),
    POSSIBLE_DEPENDENCY_ON_IMPL_DETAILS(
            1428,
            IssueSeverityLevel.ERROR,
            "customCode.errors.api.1428",
            PossibleDependencyOnImplDetails.class
    ),
    CALLED_NON_INTERFACE_METHOD(
            1449,
            IssueSeverityLevel.ERROR,
            "customCode.errors.api.1449",
            CalledNonInterfaceMethod.class
    ),
    USED_NON_EXISTENT_TYPE_IN_CONFIG(
            1110,
            IssueSeverityLevel.CRITICAL,
            "customCode.critical.existence.1110",
            UsedNonExistentTypeInConfig.class
    ),
    USED_DEPRECATED_TYPE_IN_CONFIG(
            1134,
            IssueSeverityLevel.WARNING,
            "customCode.warnings.deprecated.1134",
            UsedDeprecatedTypeInConfig.class
    ),
    USED_DEPRECATED_CONSTANT_IN_CONFIG(
            1234,
            IssueSeverityLevel.WARNING,
            "customCode.warnings.deprecated.1234",
            UsedDeprecatedConstantInConfig.class
    ),
    USED_DEPRECATED_METHOD_IN_CONFIG(
            1439,
            IssueSeverityLevel.WARNING,
            "customCode.warnings.deprecated.1439",
            UsedDeprecatedMethodInConfig.class
    ),
    USED_NON_EXISTENT_CONSTANT_IN_CONFIG(
            1214,
            IssueSeverityLevel.WARNING,
            "customCode.critical.existence.1214",
            UsedNonExistentConstantInConfig.class
    ),
    USED_NON_EXISTENT_METHOD_IN_CONFIG(
            1410,
            IssueSeverityLevel.WARNING,
            "customCode.critical.existence.1410",
            UsedNonExistentMethodInConfig.class
    );

    private final int code;
    private final IssueSeverityLevel level;
    private final String message;
    private final Class<? extends LocalInspectionTool> inspectionClass;
    private static final UctInspectionBundle BUNDLE = new UctInspectionBundle();

    /**
     * Known issue ENUM.
     *
     * @param code IssueSeverityLevel
     * @param level IssueSeverityLevel
     * @param message String
     * @param inspectionClass Class
     */
    SupportedIssue(
            final int code,
            final IssueSeverityLevel level,
            final String message,
            final Class<? extends LocalInspectionTool> inspectionClass
    ) {
        this.code = code;
        this.level = level;
        this.message = message;
        this.inspectionClass = inspectionClass;
    }

    /**
     * Get issue code.
     *
     * @return int
     */
    public int getCode() {
        return code;
    }

    /**
     * Get issue severity level.
     *
     * @return IssueSeverityLevel
     */
    public IssueSeverityLevel getLevel() {
        return level;
    }

    /**
     * Get bundle message for the issue.
     *
     * @param args Object
     *
     * @return String
     */
    public String getMessage(final Object... args) {
        return BUNDLE.message(message, args);
    }

    /**
     * Get registered inspection class.
     *
     * @return Class
     */
    public Class<? extends LocalInspectionTool> getInspectionClass() {
        return inspectionClass;
    }

    /**
     * Get issue by code.
     *
     * @param code int
     *
     * @return SupportedIssue
     */
    public static SupportedIssue getByCode(final int code) {
        for (final SupportedIssue issue : SupportedIssue.values()) {
            if (issue.getCode() == code) {
                return issue;
            }
        }
        // unsupported issue code.
        return null;
    }

    /**
     * Get visitors for all registered inspections.
     *
     * @param problemsHolder UctProblemsHolder
     *
     * @return List[PsiElementVisitor]
     */
    public static List<PsiElementVisitor> getVisitors(
            final UctProblemsHolder problemsHolder
    ) {
        final List<PsiElementVisitor> visitors = new LinkedList<>();

        for (final SupportedIssue issue : SupportedIssue.values()) {
            final PsiElementVisitor visitor = buildInspectionVisitor(
                    problemsHolder,
                    issue.getInspectionClass()
            );

            if (visitor != null) {
                visitors.add(visitor);
            }
        }

        return visitors;
    }

    /**
     * Get supported file types.
     *
     * @return List
     */
    public static List<Class<?>> getSupportedFileTypes() {
        final List<Class<?>> types = new ArrayList<>();
        types.add(PhpFile.class);
        types.add(XmlFile.class);

        return types;
    }

    /**
     * Build inspection visitor for file.
     *
     * @param problemsHolder UctProblemsHolder
     * @param inspectionClass Class
     *
     * @return PsiElementVisitor
     */
    private static @Nullable PsiElementVisitor buildInspectionVisitor(
            final UctProblemsHolder problemsHolder,
            final Class<? extends LocalInspectionTool> inspectionClass
    ) {
        LocalInspectionTool inspection;

        try {
            inspection = inspectionClass.getConstructor().newInstance();
        } catch (NoSuchMethodException
                | IllegalAccessException
                | InvocationTargetException
                | InstantiationException exception) {
            return null;
        }

        return inspection.buildVisitor(problemsHolder, false);
    }
}
