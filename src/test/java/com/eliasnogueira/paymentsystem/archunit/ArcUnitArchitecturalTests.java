package com.eliasnogueira.paymentsystem.archunit;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.core.importer.ImportOption.Predefined.DO_NOT_INCLUDE_TESTS;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

class ArcUnitArchitecturalTests {

    private final JavaClasses importedClasses = new ClassFileImporter().withImportOption(DO_NOT_INCLUDE_TESTS)
            .importPackages("com.eliasnogueira.paymentsystem");

    @Test
    void controllersShouldBeNamedProperly() {
        ArchRule rule = classes()
                .that().resideInAPackage("..controller..")
                .should().haveSimpleNameEndingWith("Controller");

        rule.check(importedClasses);
    }

    @Test
    void repositoriesShouldBeNamedProperly() {
        ArchRule rule = classes()
                .that().resideInAPackage("..repository..")
                .should().haveSimpleNameEndingWith("Repository");

        rule.check(importedClasses);
    }

    @Test
    void servicesShouldBeNamedProperly() {
        ArchRule rule = classes()
                .that().resideInAPackage("..service..")
                .should().haveSimpleNameEndingWith("Service");

        rule.check(importedClasses);
    }

    @Test
    void modelShouldBeRecords() {
        ArchRule rule = classes().that().resideInAPackage("..model..").should().beRecords();
        rule.check(importedClasses);
    }
}
