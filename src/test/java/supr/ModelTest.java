package supr;

import org.junit.Assert;
import org.junit.Test;
import supr.core.model.Engine;

import java.util.List;

/**
 */
public class ModelTest {

    @Test
    public void basicSQL(){
        Engine e = new Engine("samples/model.yml");
        Object r = e.execute("sql_inline");
        Assert.assertTrue( r instanceof List);
        System.out.println(r);
    }
}
