package supr.core.model;

import com.esotericsoftware.yamlbeans.YamlReader;
import zoomba.lang.core.collections.ZArray;

import java.io.FileReader;
import java.util.Collections;
import java.util.Map;

/**
 */
public class Engine {

    final String modelFile;

    Map<String,Object> config;

    public Map<String,String> queries(){
       return  (Map<String,String>)config.getOrDefault("queries", Collections.emptyMap());
    }

    public Map<String,String> tables(){
        return  (Map<String,String>)config.getOrDefault("tables", Collections.emptyMap());
    }

    public Engine(String fileLocation){
        try {
            YamlReader yamlReader = new YamlReader(new FileReader(fileLocation));
            config = (Map)yamlReader.read();
            modelFile = fileLocation ;

        }catch (Throwable t){
            throw new RuntimeException(t);
        }
    }

    public Object execute(String query, Object[] args){
        String sql = queries().getOrDefault(query,"");
        if ( sql.isEmpty() ){ return null; }
        SQLModel sqlModel = new SQLModel( sql , tables() );
        return sqlModel.execute(args);
    }

    public Object execute(String query){
        return execute(query, ZArray.EMPTY_ARRAY);
    }
}
