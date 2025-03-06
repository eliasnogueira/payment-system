package com.eliasnogueira.paymentsystem.archunit;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.tngtech.archunit.core.importer.ImportOption.Predefined.DO_NOT_INCLUDE_TESTS;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

class ArcUnitArchitecturalTests {

    private final JavaClasses importedClasses = new ClassFileImporter().withImportOption(DO_NOT_INCLUDE_TESTS)
            .importPackages("com.eliasnogueira.paymentsystem");

    @Test
    void controllersShouldBeNamedProperly() {
        classes().that().resideInAPackage("..controller..")
                .should().haveSimpleNameEndingWith("Controller").check(importedClasses);
    }

    @Test
    void repositoriesShouldBeNamedProperly() {
        classes().that().resideInAPackage("..repository..")
                .should().haveSimpleNameEndingWith("Repository").check(importedClasses);
    }

    @Test
    void servicesShouldBeNamedProperly() {
        classes().that().resideInAPackage("..service..")
                .should().haveSimpleNameEndingWith("Service").check(importedClasses);
    }

    @Test
    void serviceClassesShouldOnlyBeAccessedByController() {
        classes().that().resideInAPackage("..service..").should().onlyBeAccessed()
                .byAnyPackage("..service..", "..controller").check(importedClasses);
    }

    @Test
    void layeredArchitectureShouldBeFollowed() {
        layeredArchitecture().consideringOnlyDependenciesInLayers()
                .layer("Controller").definedBy("com.eliasnogueira.paymentsystem.controller")
                .layer("Repository").definedBy("com.eliasnogueira.paymentsystem.repository")
                .layer("Service").definedBy("com.eliasnogueira.paymentsystem.service")
                .whereLayer("Controller").mayNotBeAccessedByAnyLayer()
                .whereLayer("Repository").mayOnlyBeAccessedByLayers("Service")
                .whereLayer("Service").mayOnlyBeAccessedByLayers("Controller")
                .check(importedClasses);
    }

    @Test
    void noAutowired() {
        noFields().should().beAnnotatedWith(Autowired.class).check(importedClasses);
    }
}
