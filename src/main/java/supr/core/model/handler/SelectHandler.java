package supr.core.model.handler;

import com.mashape.unirest.http.JsonNode;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;
import org.json.JSONArray;
import org.json.JSONObject;
import zoomba.lang.core.operations.Function;
import zoomba.lang.core.operations.ZJVMAccess;

import javax.script.Bindings;
import javax.script.SimpleBindings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 */
public class SelectHandler {

    final PlainSelect selectBody;

    public SelectHandler(Select select){
        this.selectBody = (PlainSelect)select.getSelectBody() ;
    }

    public static Map<String,Object> fromJSON(JSONObject jsonObject){
       Function.MonadicContainer c = ZJVMAccess.getProperty(jsonObject, "map");
       return (Map<String,Object>)c.value();
    }

    private List toList(String tableName, JsonNode node, List<String> fields){
        List rows = new ArrayList<>();
        ExpressionHandler expressionHandler = new ExpressionHandler( selectBody.getWhere() );

        final boolean noProject = fields.isEmpty();
        if ( node.isArray() ){
            Bindings context = new SimpleBindings();
            JSONArray jsonArray = node.getArray();
            for ( int i = 0 ; i < jsonArray.length(); i++ ){
                JSONObject o = (JSONObject) jsonArray.get(i);
                Map m = fromJSON(o);
                context.put(tableName, m );
                if (! expressionHandler.test(context)){ continue; }
                if ( noProject ){
                    rows.add(o);
                } else {
                    JSONObject p = new JSONObject();
                    for ( String f : fields ){
                        p.put(f,o.get(f));
                    }
                    rows.add(p);
                }
            }
        }
        return rows;
    }

    public List join( Map<String,String> tables , List<String> fields){
        List<Join> joins = selectBody.getJoins();
        // support only one as of now, demo ...
        Join join = joins.get(0);
        String lTableName =  ((Table)selectBody.getFromItem()).getName();
        String rTableName =  ((Table)join.getRightItem()).getName();

        String lUrl = tables.get(lTableName);
        RestHandler restHandler = new RestHandler(lUrl);
        JsonNode lRes = restHandler.getAndWaitAsync();

        String rUrl = tables.get(rTableName);
        restHandler = new RestHandler(rUrl);
        JsonNode rRes = restHandler.getAndWaitAsync();
        if ( lRes == null || rRes == null ){
            return Collections.emptyList();
        }
        if ( !lRes.isArray() || !rRes.isArray() ){
            return Collections.emptyList();
        }
        JSONArray lArray = lRes.getArray();
        JSONArray rArray = rRes.getArray();

        ExpressionHandler joinExpression = new ExpressionHandler( join.getOnExpression() );
        List rows = new ArrayList<>();
        Bindings context = new SimpleBindings();

        for ( int i = 0 ; i < lArray.length(); i++ ){
            Map l = fromJSON((JSONObject) lArray.get(i));
            for ( int j = 0 ; j < rArray.length(); j++ ){
                Map r = fromJSON((JSONObject) rArray.get(j));
                context.put(lTableName,l );
                context.put(rTableName,r);
                if (! joinExpression.test(context) ){ continue; }
                JSONObject p = new JSONObject();
                for ( String f : fields ){
                    String[] arr = f.split("\\.");
                    if ( arr[0].equals(lTableName ) ){
                        p.put( arr[1], l.get(arr[1] ) );
                    } else if ( arr[0].equals(rTableName ) ){
                        p.put( arr[1], r.get(arr[1] ) );
                    } else {

                    }
                }
                rows.add(p);
            }
        }
        return rows;
    }

    public List simple( Map<String,String> tables, List<String> fields ){
        Table table =  (Table)selectBody.getFromItem();
        String tableName = table.getName();

        String url = tables.get(tableName);
        RestHandler restHandler = new RestHandler(url);
        JsonNode res = restHandler.getAndWaitAsync();
        if ( res != null ){
            return toList(tableName, res, fields );
        }
        return Collections.emptyList();
    }

    public List execute(Map<String,String> tables){
        List<SelectItem> selectItems = selectBody.getSelectItems();
        List<String> fields = Collections.emptyList();
        if ( !String.valueOf(selectItems.get(0)).equals("*") ){
            fields = new ArrayList<>();
            for ( SelectItem item : selectItems ){
                fields.add( item.toString() );
            }
        }
        if ( selectBody.getJoins() == null ){
            return simple(tables,fields);
        }
        return join(tables,fields);
    }
}
