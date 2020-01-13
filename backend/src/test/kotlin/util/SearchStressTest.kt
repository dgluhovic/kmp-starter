import com.kmp_starter.backend.database.DatabaseFactory
import com.typesafe.config.ConfigFactory
import io.ktor.config.HoconApplicationConfig
import org.junit.Test
import org.junit.internal.runners.JUnit38ClassRunner
import org.junit.runner.RunWith


class SearchStressTest {
    @Test
    fun runStressTest() {

        DatabaseFactory.init(ConfigFactory.load("test-application.conf"))

    }
}