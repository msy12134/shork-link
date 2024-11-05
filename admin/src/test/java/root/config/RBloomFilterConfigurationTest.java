package root.config;
import org.junit.jupiter.api.Test;
import org.redisson.api.RBloomFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
@SpringBootTest
class RBloomFilterConfigurationTest {
    @Autowired
    private RBloomFilter<String> userRegisterCachePenetrationBloomFilter;
    @Test
    void userRegisterCachePenetrationBloomFilter() {
        System.out.println(userRegisterCachePenetrationBloomFilter.add("maomao"));
        System.out.println(userRegisterCachePenetrationBloomFilter.add("mading"));
    }
}