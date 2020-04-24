package com.magento.idea.magento2plugin.inspections.graphqls;

public class SchemaResolverInspectionTest extends InspectionGraphqlsFixtureTestCase {

    private final String errorMessage =  "Class must implements \\Magento\\Framework\\GraphQl\\Query\\ResolverInterface";

    @Override
    public void setUp() throws Exception {
        super.setUp();
        myFixture.enableInspections(SchemaResolverInspection.class);
    }

    protected boolean isWriteActionRequired() {
        return false;
    }

    public void testWithValidSchemaResolverInterface() throws Exception {
        myFixture.configureByFile(getFixturePath("schema.graphqls"));
        assertHasNoHighlighting(errorMessage);
    }

    public void testWithInvalidSchemaResolverInterface() throws Exception {
        myFixture.configureByFile(getFixturePath("schema.graphqls"));
        assertHasHighlighting(errorMessage);
    }
}
