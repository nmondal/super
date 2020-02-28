package supr.core.model.handler;

import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import net.sf.jsqlparser.expression.Expression;
import zoomba.lang.core.types.ZTypes;

import javax.script.*;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 *
 */
public class ExpressionHandler implements Predicate<Bindings>, Function<Bindings, Object> {

    private final CompiledScript compiledScript;

    private String script;

    private static Map<String, String> sqlOperators = new HashMap<>();

    private static Map<String, String> defaultOperationsMappers = new HashMap<>();

    private static Map<String, Map<String,String>> operationMappers = new HashMap<>();

    static {
        sqlOperators.put("and", "[aA][nN][dD]");
        sqlOperators.put("or", "[oO][rR]");
        sqlOperators.put("equals", "=");

        defaultOperationsMappers.put("and", "&&");
        defaultOperationsMappers.put("or", "||");
        defaultOperationsMappers.put("equals", "==");
        YamlReader yamlReader = null;
        try {
            yamlReader = new YamlReader(new FileReader("mappers/DialectMapper.yaml"));
            operationMappers = (Map) yamlReader.read();
        } catch (FileNotFoundException | YamlException e) {
            e.printStackTrace();
        }
    }

    public static String translateSQLExpressionToDialect(String script, String dialect) {
        Set<String> operators = sqlOperators.keySet();
        Map<String,String> opMapper = operationMappers.getOrDefault(dialect,defaultOperationsMappers );
        for (String key : operators) {
            String regex = sqlOperators.get(key);
            String replacement = opMapper.get(key);
            script = script.replaceAll(regex, replacement);
        }
        return script;
    }

    public ExpressionHandler(Expression expression, String dialect) {
        try {
            ScriptEngine engine = new ScriptEngineManager().getEngineByName(dialect);
            script = (expression == null) ? "true" : expression.toString();
            script = translateSQLExpressionToDialect(script, dialect);
            compiledScript = ((Compilable) engine).compile(script);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public ExpressionHandler(Expression expression) {
        this(expression,"zoomba");
    }

    @Override
    public boolean test(Bindings bindings) {
        Boolean res = ZTypes.bool(apply(bindings));
        if (res == null) {
            throw new IllegalArgumentException(String.format("Expression did not yield boolean! [ %s]", script));
        }
        return res;
    }

    @Override
    public Object apply(Bindings bindings) {
        try {
            Object o = compiledScript.eval(bindings);
            return o;
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }
}
