package supr.core.model;

import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import zoomba.lang.core.collections.ZArray;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 */
public class SQLModel {

    public final String sql;

    public final List<Statement> statements;

    public final Map<String,String> tables;

    public SQLModel(String sql, Map<String,String> tables){
        try {
            statements = CCJSqlParserUtil.parseStatements(sql).getStatements();
            this.sql = sql;
            this.tables = tables ;
        }catch (Throwable t){
            throw new RuntimeException(t);
        }
    }

    public Object execute(Map<String,Object> context, Object[] args){
        SuperStatementVisitor visitor = new SuperStatementVisitor(tables,args);
        for ( Statement statement : statements ){
            statement.accept( visitor );
        }
        return visitor.result();
    }

    public Object execute(Object[] args){
        return execute(Collections.emptyMap(), args);
    }

    public Object execute(){
        return execute(ZArray.EMPTY_ARRAY);
    }
}
