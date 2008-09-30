package org.sonatype.nexus.proxy.target;

import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.utils.StoreWalkerFilter;

/**
 * A Walker filter that will walk only agains a Repository target. Ie. remove snapshots only from Maven target.
 * 
 * @author cstamas
 */
public class TargetStoreWalkerFilter
    implements StoreWalkerFilter
{
    private final Target target;

    public TargetStoreWalkerFilter( Target target )
        throws IllegalArgumentException
    {
        super();

        if ( target == null )
        {
            throw new IllegalArgumentException( "The target cannot be null!" );
        }

        this.target = target;
    }

    public boolean shouldProcess( StorageItem item )
    {
        return target.isPathContained( item.getRepositoryItemUid().getRepository(), item.getPath() );
    }

    public boolean shouldProcessRecursively( StorageCollectionItem coll )
    {
        // TODO: initial naive implementation. Later, we could evaluate target patterns: are those "slicing" the repo
        // (ie. forbids /a/b but allows /a and /a/b/c) or "cutting" (ie. allows only /a and nothing below). That would
        // need some pattern magic. We are now naively saying "yes, dive into" but the shouldProcess will do it's work
        // anyway.
        return true;
    }
}
