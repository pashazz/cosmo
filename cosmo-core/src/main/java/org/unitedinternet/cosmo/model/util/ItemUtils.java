package org.unitedinternet.cosmo.model.util;

import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.CollectionItemDetails;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.hibernate.HibItem;

import java.util.*;

public class ItemUtils {
    public static Set<CollectionItem> getAllParents(Item me) {
        Set<CollectionItem> allParents = new HashSet<>();
        Queue<Item> queue = new LinkedList<>();
        queue.add(me);
        while (!queue.isEmpty()) {
            Item current = queue.poll();
            try {
                if (allParents.contains(current)) {
                    continue;
                }
            } catch (ClassCastException e) {
                continue; // this may as well not be a CollectionItem and thus there will never be a loop
            }
            allParents.addAll(current.getParents());
            queue.addAll(current.getParents());
        }
        return Collections.unmodifiableSet(allParents);
    }

    public static Set<CollectionItem> getParents(Set<CollectionItemDetails> details) {

        Set<CollectionItem> parents = new HashSet<CollectionItem>();
        for (CollectionItemDetails cid:  details) {
            parents.add(cid.getCollection());
        }

        return Collections.unmodifiableSet(parents);
    }
}
