package exercise1;

import de.hpi.dbs2.ChosenImplementation;
import de.hpi.dbs2.dbms.BlockManager;
import de.hpi.dbs2.dbms.BlockOutput;
import de.hpi.dbs2.dbms.Relation;
import de.hpi.dbs2.exercise1.SortOperation;
import org.jetbrains.annotations.NotNull;

@ChosenImplementation(false)
public class TPMMSJava extends SortOperation {
    public TPMMSJava(@NotNull BlockManager manager, int sortColumnIndex) {
        super(manager, sortColumnIndex);
    }

    @Override
    public int estimatedIOCost(@NotNull Relation relation) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public void sort(@NotNull Relation relation, @NotNull BlockOutput output) {
        throw new UnsupportedOperationException("TODO");
    }
}
