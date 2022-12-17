package exercise3;

import de.hpi.dbs2.ChosenImplementation;
import de.hpi.dbs2.dbms.*;
import de.hpi.dbs2.exercise3.InnerJoinOperation;
import de.hpi.dbs2.exercise3.JoinAttributePair;
import org.jetbrains.annotations.NotNull;

@ChosenImplementation(true)
public class HashEquiInnerJoinJava extends InnerJoinOperation {

	public HashEquiInnerJoinJava(
		@NotNull BlockManager blockManager, int leftColumnIndex, int rightColumnIndex
	) {
		super(blockManager, new JoinAttributePair.EquiJoinAttributePair(leftColumnIndex, rightColumnIndex));
	}

	@Override
	public int estimatedIOCost(
		@NotNull Relation leftInputRelation, @NotNull Relation rightInputRelation
	) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void join(
		@NotNull Relation leftInputRelation, @NotNull Relation rightInputRelation,
		@NotNull Relation outputRelation
	) {
		int bucketCount = getBlockManager().getFreeBlocks() - 1;
		// TODO:
		//  - calculate a sensible bucket count
		//  - hash relation
		//  - join hashed blocks

		throw new UnsupportedOperationException();
	}
}
