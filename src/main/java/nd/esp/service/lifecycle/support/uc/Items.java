package nd.esp.service.lifecycle.support.uc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;


public class Items<T> implements Iterable<T> {
    private Collection<T> items;

    public Collection<T> getItems() {
        return items;
    }
    
    public Items() {
        items = new ArrayList<T>();
    }
    
    public Items(Collection<T> items){
        this.items = items;
    }

    public void setItems(Collection<T> items) {
        this.items = items;
    }

    @Override
    public Iterator<T> iterator() {
        return items.iterator();
    }
    
    public boolean isEmpty() {
        return items == null || items.isEmpty();
    }
    
    public int size() {
        if (items == null) return 0;
        return items.size();
    }
}