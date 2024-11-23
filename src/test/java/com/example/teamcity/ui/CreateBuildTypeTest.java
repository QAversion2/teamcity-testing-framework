package com.example.teamcity.ui;

import com.codeborne.selenide.Condition;
import com.example.teamcity.api.enums.Endpoint;
import com.example.teamcity.api.generators.TestDataStorage;
import com.example.teamcity.api.models.BuildType;
import com.example.teamcity.api.models.Project;
import com.example.teamcity.api.requests.CheckedRequests;
import com.example.teamcity.api.spec.Specifications;
import com.example.teamcity.ui.pages.admin.BuildTypePage;
import com.example.teamcity.ui.pages.admin.CreateBuildTypePage;
import org.testng.annotations.Test;

import static com.example.teamcity.api.enums.Endpoint.BUILD_TYPES;
import static com.example.teamcity.api.enums.Endpoint.PROJECTS;
import static com.example.teamcity.api.generators.TestDataGenerator.generate;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;

@Test(groups = {"Regression"})
public class CreateBuildTypeTest extends BaseUiTest {

    private static final String REPO_URL_SECOND_BUILD = "https://github.com/AlexandraNayanzina/teamcity-testing-framework";

    @Test(description = "Создание пользователем Build type", groups = {"Positive"})
    public void userCreationBuildType() {
        // Подготовка
        loginAs(testData.getUser());

        var buildType = generate(BuildType.class);

        // Создание проекта
        var userAuth = Specifications.authSpec(testData.getUser());
        var project = testData.getProject();
        var requests = new CheckedRequests(userAuth);
        requests.getRequest(PROJECTS).create(project);

        // Проверить, что проект создался
        var createdProject = superUserCheckRequests.<Project>getRequest(Endpoint.PROJECTS).read("name:" +
                testData.getProject().getName());
        softy.assertNotNull(createdProject);

        // Создаём build type на UI
        CreateBuildTypePage.open(createdProject.getId())
                .createForm(REPO_URL_SECOND_BUILD)
                .setupBuildType(buildType.getName());

        // Проверяю что build type был создан
        var createdBuildType = superUserCheckRequests.<BuildType>getRequest(Endpoint.BUILD_TYPES).read("name:"
                + buildType.getName());
        softy.assertNotNull(createdBuildType);

        // Проверяю что build type  правильно отображается на UI
        BuildTypePage.open(createdBuildType.getId())
                .title.shouldHave(Condition.exactText(buildType.getName()));

        // Удаление данных после теста
        TestDataStorage.getStorage().addCreatedEntity(PROJECTS, createdProject);

    }


    @Test(description = "Пользователь не может создать Build type без имени", groups = {"Negative"})
    public void creatingAnAssemblyWithAnEmptyNameErrorMessage() {
        // Подготовка
        loginAs(testData.getUser());
        var buildType = generate(BuildType.class);

        // Создание проекта
        var userAuth = Specifications.authSpec(testData.getUser());
        var project = testData.getProject();
        var requests = new CheckedRequests(userAuth);
        requests.getRequest(PROJECTS).create(project);

        // Проверяю что build type был создан
        var createdProject = superUserCheckRequests.<Project>getRequest(Endpoint.PROJECTS).read("name:" +
                testData.getProject().getName());
        softy.assertNotNull(createdProject);

        // Открыть на UI страницу создания BuildTyp и попробовать создать с пустым именем
        CreateBuildTypePage.open(createdProject.getId())
                .createForm(REPO_URL_SECOND_BUILD)
                .errorMessageEmptyBuildTypeName("");

        // Проверить что тип сборки не был создан
        var response = superUserUncheckRequests.getRequest(BUILD_TYPES).read(buildType.getId())
                .then()
                .assertThat()
                .statusCode(SC_NOT_FOUND);

        TestDataStorage.getStorage().addCreatedEntity(PROJECTS, createdProject);
    }
}