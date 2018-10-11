package et.browser.test;

import static java.lang.System.getProperty;
import static java.lang.invoke.MethodHandles.lookup;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.slf4j.LoggerFactory.getLogger;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;

import et.docker.DockerService;
import io.github.bonigarcia.SeleniumExtension;

@Tag("browsers")
@DisplayName("Test the right browser operation with selenium.")
@ExtendWith(SeleniumExtension.class)
@TestInstance(Lifecycle.PER_CLASS)
public class BrowserTest {

    private static final Logger LOG = getLogger(lookup().lookupClass());
    private DockerService docker;
    public WebDriver driver;
    private String bName = "chrome";
    private String browser = "elastestbrowsers/chrome";
    private String browserVersion = "latest";
    public String containerId;
    public String commands = "git clone https://github.com/elastest/elastest-user-emulator-service;"
            + "cd elastest-user-emulator-service/tjob-test;mvn test;";
    public String eusUrl = null;

    @BeforeAll
    public void init() {
        //Check if the ET_EUS_API env var exists
        eusUrl = System.getenv("ET_EUS_API");
        
        // Load browser name and version
        if (getProperty("eBrowser") != null) {
            browser = getProperty("eBrowser");
        }
        
        if (getProperty("eBName") != null) {
            bName = getProperty("eBName");
        }

        if (getProperty("eBVersion") != null) {
            browserVersion = getProperty("eBVersion");
        }

        String hubUrl = null;
        if (eusUrl != null && !eusUrl.isEmpty()) {
            hubUrl = eusUrl;
        } else {
            docker = DockerService
                    .getDockerService(DockerService.DOCKER_HOST_BY_DEFAULT);
            containerId = this.startBrowser();
            hubUrl = "http://" + docker.getContainerIp(containerId) + ":4444/wd/hub";
        }
        try {
            driver = new RemoteWebDriver(new URL(hubUrl), setupBrowser());
            driver.manage().timeouts().implicitlyWait(5, SECONDS);
        } catch (MalformedURLException mue) {

        }
    }

    @AfterAll
    public void teardown() {
        if (eusUrl == null) {
            stopBrowser(containerId);
        }
    }

    public String startBrowser() {
        String containerId = docker.executeDockerCommand("docker", "run", "-d",
                "--name", "chrome", "-p", "4444:4444", "-p", "6080:6080", "-p",
                "5900:5900", "--cap-add=SYS_ADMIN", "-v",
                System.getenv("PWD") + "/recordings:/home/ubuntu/recordings",
                browser + ":" + browserVersion);

        return containerId;
    }

    public void stopBrowser(String containerId) {
        docker.executeDockerCommand("docker", "rm", "-f", containerId, "");
    }

    public DesiredCapabilities setupBrowser() throws MalformedURLException {
        DesiredCapabilities caps;
        caps = !bName.equals("chrome") ? DesiredCapabilities.firefox()
                : DesiredCapabilities.chrome();
        caps.setCapability("browserId", bName + "_" + browserVersion);
        caps.setCapability("version", browserVersion);
        return caps;
    }

    @Test
    @DisplayName("Check the Sendkeys operation with a textarea")
    public void testSendKeysWithTextArea() throws InterruptedException {
        Thread.sleep(5000);
        LOG.info("Check browser operation.");
        driver.get(
                "https://www.w3schools.com/tags/tryit.asp?filename=tryhtml_textarea");
        driver.switchTo().frame(driver.findElement(By.id("iframeResult")));
        driver.findElement(By.xpath("//textarea")).clear();
        driver.findElement(By.xpath("//textarea")).sendKeys(commands);
        String textAreaValue = driver.findElement(By.xpath("//textarea")).getAttribute("value");
        LOG.info("Text in the textarea: {}", textAreaValue);
        assertEquals(commands, textAreaValue, "The content is different");

    }

}
