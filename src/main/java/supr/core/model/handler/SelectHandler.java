package supr.core.model.handler;

import com.mashape.unirest.http.JsonNode;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SelectItem;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 */
public class SelectHandler {

    final Select select;

    public SelectHandler(Select select){
        this.select = select ;
    }

    private List toList(JsonNode node, List<String> fields){
        List rows = new ArrayList<>();
        if ( node.isArray() ){
            JSONArray jsonArray = node.getArray();
            for ( int i = 0 ; i < jsonArray.length(); i++ ){
                JSONObject o = (JSONObject) jsonArray.get(i);
                JSONObject p = new JSONObject();
                for ( String f : fields ){
                    p.put(f,o.get(f));
                }
                rows.add(p);
            }
        }
        return rows;
    }

    public List execute(Map<String,String> tables){
        SelectBody selectBody = select.getSelectBody();
        if ( selectBody instanceof PlainSelect ){
           Table table =  (Table)((PlainSelect)selectBody).getFromItem();
           String tableName = table.getName();
           List<SelectItem> selectItems = ((PlainSelect)selectBody).getSelectItems();
           List<String> fields = new ArrayList<>();
           for ( SelectItem item : selectItems ){
               fields.add( item.toString() );
           }

           String url = tables.get(tableName);
           RestHandler restHandler = new RestHandler(url);
           JsonNode res = restHandler.getAndWaitAsync();
           if ( res != null ){
                return toList(res, fields );
           }
        }

        return Collections.emptyList();
    }
}
