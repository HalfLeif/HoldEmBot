import org.junit.Test;
import se.cygni.texasholdem.player.Statistics;

/**
 * Created by HalfLeif on 2015-04-21.
 */
public class StatTest {

    @Test
    public void testCombinations(){
        System.out.println(Statistics.combinations(8,3));
        assert Statistics.combinations(11,3) == 165.0;
        assert Statistics.combinations(8,3) == 56.0;
        assert Statistics.combinations(8,8) == 1.0;
        assert Statistics.combinations(8,0) == 1.0;
    }
}
