package com.example.teamcity.api;

import com.example.teamcity.api.models.Roles;
import com.example.teamcity.api.requests.UncheckedRequests;
import com.example.teamcity.api.spec.Specifications;
import org.testng.annotations.Test;

import static com.example.teamcity.api.enums.Endpoint.*;
import static com.example.teamcity.api.generators.TestDataGenerator.generate;
import static org.apache.http.HttpStatus.SC_FORBIDDEN;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.Matchers.containsString;

@Test(groups = {"Regression"})
public class HomeWorkBuildTypeTest extends BaseApiTest {

    @Test(description = "У администратора должна быть возможность создавать build type для своего проекта", groups = {"Positive", "Roles"})

    public void projectAdminCreatesBuildTypeTest() {

        // Создание проекта за суперпользователя
        var project = testData.getProject();
        superUserCheckRequests.getRequest(PROJECTS).create(project);

        // Суперпользователь создаёт админа и назначает его на проект
        var projectAdmin = testData.getUser();
        var roles = generate(Roles.class, "PROJECT_ADMIN", "p:" + testData.getProject().getId());
        projectAdmin.setRoles(roles);
        superUserCheckRequests.getRequest(USERS).create(projectAdmin);

        // Админ проекта создаёт  buildType для проекта
        var buildType = testData.getBuildType();
        var requester = new UncheckedRequests(Specifications.authSpec(projectAdmin));
        var response = requester.getRequest(BUILD_TYPES).create(buildType);

        // делаем assert на OK
        response.then()
                .assertThat()
                .statusCode(SC_OK);
    }

    @Test(description = "Администратор не должен иметь возможность создавать build type не для своего проекта", groups = {"Negative", "Roles"})

    public void projectAdminCreatesBuildTypeForAnotherUserProjectTest() {

        // Суперпользоватеь создаёт первый проект
        var firstProject = testData.getProject();
        superUserCheckRequests.getRequest(PROJECTS).create(firstProject);

        // Суперюзер создаёт админа на 1-й проект
        var adminOneProject = testData.getUser();
        var firstRole = generate(Roles.class, "PROJECT_ADMIN", "p:" + firstProject.getId());
        adminOneProject.setRoles(firstRole);
        superUserCheckRequests.getRequest(USERS).create(adminOneProject);

        // Суперпользователь создаёт 2-рой проект
        var testData2 = generate();
        var secondProject = testData2.getProject();
        superUserCheckRequests.getRequest(PROJECTS).create(secondProject);

        // За суперпользователя создаю админа 2-го
        var adminSecondProject = testData2.getUser();
        var secondRole = generate(Roles.class, "PROJECT_ADMIN", "p:" + secondProject.getId());
        adminSecondProject.setRoles(secondRole);
        superUserCheckRequests.getRequest(USERS).create(adminSecondProject);

        // Создаю buildType для 1-го проекта за 2-го админа
        var buildType = testData.getBuildType();
        buildType.setProject(firstProject);
        var requester = new UncheckedRequests(Specifications.authSpec(adminSecondProject));
        var response = requester.getRequest(BUILD_TYPES).create(buildType);

        // проверяю на BAD_REQUEST
        response.then()
                .assertThat()
                .statusCode(SC_FORBIDDEN)
                .body(containsString("You do not have enough permissions to edit project with"));
    }
}