package supr.core.model.handler;

import net.sf.jsqlparser.expression.Expression;
import zoomba.lang.core.types.ZTypes;

import javax.script.*;

/**
 */
public class ExpressionHandler {

    private static final ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");

    private final CompiledScript compiledScript ;

    public ExpressionHandler(Expression expression){
        try {
            String script = (expression == null) ? "true" : expression.toString();
            script = script.replaceAll("[aA][nN][dD]", "&&") ;
            script = script.replaceAll("[oO][rR]", "&&") ;
            script = script.replace("=", "==") ;
            compiledScript = ((Compilable) engine).compile(script);
        }catch (Throwable t){
            throw new RuntimeException(t);
        }
    }

    public boolean predicate(ScriptContext scriptContext){
        return ZTypes.bool(execute(scriptContext),false);
    }

    public Object execute(ScriptContext scriptContext){
        try {
            Object o = compiledScript.eval(scriptContext);
            return o;
        }catch (Throwable t){
            throw new RuntimeException(t);
        }
    }
}
