package supr;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import supr.core.model.Engine;

import java.util.List;

/**
 */
public class ModelTest {

    private static  Engine engine;

    @BeforeClass
    public static void engineCreate(){
        engine = new Engine("samples/model.yml");
    }

    @Test
    public void basicSelect(){
        Object r = engine.execute("sql_inline");
        Assert.assertTrue( r instanceof List);
        Assert.assertFalse( ((List)r).isEmpty() );
        System.out.println(r);
    }

    @Test
    public void joinSelect(){
        Object r = engine.execute("sql_join");
        Assert.assertTrue( r instanceof List);
        Assert.assertFalse( ((List)r).isEmpty() );
        System.out.println(r);
    }
}
