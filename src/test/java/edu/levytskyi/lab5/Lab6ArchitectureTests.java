package edu.levytskyi.lab5;
/* @author Sandoplay
 * @project lab5
 * @class Lab6ArchitectureTests
 * @version 1.0.0
 * @since 24.04.2025 - 21.08
 */


import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import jakarta.persistence.Entity;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.library.GeneralCodingRules.*;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

@AnalyzeClasses(packages = "edu.levytskyi.lab5", importOptions = {ImportOption.DoNotIncludeTests.class})
class Lab6ArchitectureTests {

    private static final String CONTROLLER_LAYER = "Controller";
    private static final String SERVICE_LAYER = "Service";
    private static final String REPOSITORY_LAYER = "Repository";
    private static final String MODEL_LAYER = "Model";
    private static final String MAIN_LAYER = "Main";

    private static final String CONTROLLER_PKG = "..controller..";
    private static final String SERVICE_PKG = "..service..";
    private static final String REPOSITORY_PKG = "..repository..";
    private static final String MODEL_PKG = "..model..";
    private static final String MAIN_PKG = "edu.levytskyi.lab5";

    @ArchTest
    static final ArchRule layered_architecture_respected =
            layeredArchitecture()
                    .consideringAllDependencies()
                    .layer(CONTROLLER_LAYER).definedBy(CONTROLLER_PKG)
                    .layer(SERVICE_LAYER).definedBy(SERVICE_PKG)
                    .layer(REPOSITORY_LAYER).definedBy(REPOSITORY_PKG)
                    .layer(MODEL_LAYER).definedBy(MODEL_PKG)
                    .layer(MAIN_LAYER).definedBy(MAIN_PKG)

                    .whereLayer(CONTROLLER_LAYER).mayNotBeAccessedByAnyLayer()
                    .whereLayer(SERVICE_LAYER).mayOnlyBeAccessedByLayers(CONTROLLER_LAYER, SERVICE_LAYER, MAIN_LAYER)
                    .whereLayer(REPOSITORY_LAYER).mayOnlyBeAccessedByLayers(SERVICE_LAYER);

    @ArchTest
    static final ArchRule controllers_should_be_named_controller =
            classes()
                    .that().resideInAPackage(CONTROLLER_PKG)
                    .should().haveSimpleNameEndingWith("Controller")
                    .because("Controllers should follow the naming convention 'XController'");

    @ArchTest
    static final ArchRule services_should_be_named_service =
            classes()
                    .that().resideInAPackage(SERVICE_PKG)
                    .and().areNotInterfaces()
                    .should().haveSimpleNameEndingWith("Service")
                    .because("Service implementations should follow the naming convention 'XService'");

    @ArchTest
    static final ArchRule repositories_should_be_named_repository =
            classes()
                    .that().resideInAPackage(REPOSITORY_PKG)
                    .and().areInterfaces()
                    .should().haveSimpleNameEndingWith("Repository")
                    .because("Repository interfaces should follow the naming convention 'XRepository'");

    @ArchTest
    static final ArchRule models_should_not_have_layer_suffixes =
            classes()
                    .that().resideInAPackage(MODEL_PKG)
                    .should().haveSimpleNameNotEndingWith("Controller")
                    .andShould().haveSimpleNameNotEndingWith("Service")
                    .andShould().haveSimpleNameNotEndingWith("Repository")
                    .because("Models represent data and should not use layer-specific suffixes");


    @ArchTest
    static final ArchRule controllers_should_be_annotated_with_restcontroller =
            classes()
                    .that().resideInAPackage(CONTROLLER_PKG)
                    .should().beAnnotatedWith(RestController.class)
                    .because("Controllers should be annotated with @RestController for Spring MVC");

    @ArchTest
    static final ArchRule services_should_be_annotated_with_service =
            classes()
                    .that().resideInAPackage(SERVICE_PKG)
                    .and().areNotInterfaces()
                    .should().beAnnotatedWith(Service.class)
                    .because("Service implementations should be annotated with @Service for component scanning");

    @ArchTest
    static final ArchRule repositories_should_be_annotated_with_repository =
            classes()
                    .that().resideInAPackage(REPOSITORY_PKG)
                    .and().areInterfaces()
                    .should().beAnnotatedWith(Repository.class)
                    .because("Repository interfaces should be annotated with @Repository (or extend a Spring Data interface)");

    @ArchTest
    static final ArchRule models_should_be_annotated_with_entity_or_be_simple_pojos =
            classes()
                    .that().resideInAPackage(MODEL_PKG)
                    .should().beAnnotatedWith(Entity.class)
                    .orShould().notBeAnnotatedWith(Service.class)
                    .andShould().notBeAnnotatedWith(Repository.class)
                    .andShould().notBeAnnotatedWith(RestController.class)
                    .because("Models should typically be @Entity or simple POJOs, not Spring components");

    @ArchTest
    static final ArchRule main_class_should_be_annotated_with_springbootapplication =
            classes()
                    .that().resideInAPackage(MAIN_PKG)
                    .and().haveSimpleName("Lab5Application")
                    .should().beAnnotatedWith(SpringBootApplication.class)
                    .because("The main application class should be annotated with @SpringBootApplication");


    @ArchTest
    static final ArchRule services_should_not_depend_on_controllers =
            noClasses()
                    .that().resideInAPackage(SERVICE_PKG)
                    .should().dependOnClassesThat().resideInAPackage(CONTROLLER_PKG)
                    .because("Services should not have dependencies on Controllers (violates layering)");

    @ArchTest
    static final ArchRule repositories_should_not_depend_on_services =
            noClasses()
                    .that().resideInAPackage(REPOSITORY_PKG)
                    .should().dependOnClassesThat().resideInAPackage(SERVICE_PKG)
                    .because("Repositories should not have dependencies on Services (violates layering)");

    @ArchTest
    static final ArchRule controllers_should_not_depend_on_repositories =
            noClasses()
                    .that().resideInAPackage(CONTROLLER_PKG)
                    .should().dependOnClassesThat().resideInAPackage(REPOSITORY_PKG)
                    .because("Controllers should depend on Services, not directly on Repositories");

    @ArchTest
    static final ArchRule models_should_not_depend_on_other_layers =
            noClasses()
                    .that().resideInAPackage(MODEL_PKG)
                    .should().dependOnClassesThat().resideInAnyPackage(CONTROLLER_PKG, SERVICE_PKG, REPOSITORY_PKG)
                    .because("Models should be self-contained or depend only on other models/JDK classes");


    @ArchTest
    static final ArchRule repositories_must_be_interfaces =
            classes()
                    .that().resideInAPackage(REPOSITORY_PKG)
                    .should().beInterfaces()
                    .because("Repositories should be defined as interfaces");

    @ArchTest
    static final ArchRule no_field_injection = NO_CLASSES_SHOULD_USE_FIELD_INJECTION;

    @ArchTest
    static final ArchRule model_fields_should_be_private =
            fields()
                    .that().areDeclaredInClassesThat().resideInAPackage(MODEL_PKG)
                    .and().areNotStatic()
                    .should().bePrivate()
                    .because("Model fields should be private to enforce encapsulation");

    @ArchTest
    static final ArchRule constants_should_be_static_final_uppercase =
            fields()
                    .that().areStatic().and().areFinal()
                    .and().areDeclaredInClassesThat().resideInAnyPackage( "edu.levytskyi.lab5..")
                    .should().haveNameMatching("^[A-Z0-9_]+$")
                    .because("Constants (static final fields) should be named in uppercase with underscores")
                    .allowEmptyShould(true);


    @ArchTest
    static final ArchRule no_cycles_between_packages =
            slices().matching( "edu.levytskyi.lab5.(*)..").should().beFreeOfCycles();

    @ArchTest
    static final ArchRule no_cycles_in_service_layer_internals_if_split =
            slices().matching( "edu.levytskyi.lab5.service.(*)..").should().beFreeOfCycles()
                    .allowEmptyShould(true);

}