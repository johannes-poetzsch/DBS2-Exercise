package de.hpi.dbs2.exercise3;

import de.hpi.dbs2.dbms.*;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class NestedLoopEquiInnerJoin extends InnerJoinOperation {

	public NestedLoopEquiInnerJoin(
		@NotNull BlockManager blockManager, int leftColumnIndex, int rightColumnIndex
	) {
		super(blockManager, new JoinAttributePair.EquiJoinAttributePair(leftColumnIndex, rightColumnIndex));
	}

	@Override
	public void join(
		@NotNull Relation leftInputRelation, @NotNull Relation rightInputRelation,
		@NotNull Relation outputRelation
	) {
		// use smaller relation as outer relation
		boolean swapped = rightInputRelation.estimatedBlockCount() < leftInputRelation.estimatedBlockCount();
		Relation outerRelation = (swapped) ? rightInputRelation : leftInputRelation;
		Relation innerRelation = (swapped) ? leftInputRelation : rightInputRelation;

		TupleAppender tupleAppender = new TupleAppender(outputRelation.getBlockOutput());
		for(Block outerBlockRef : outerRelation) {
			Block outerBlock = getBlockManager().load(outerBlockRef);
			for(Block innerBlockRef : innerRelation) {
				Block innerBlock = getBlockManager().load(innerBlockRef);
				joinBlocks(
					swapped ? innerBlock : outerBlock,
					swapped ? outerBlock : innerBlock,
					outputRelation.getColumns(),
					tupleAppender
				);
				getBlockManager().release(innerBlock, false);
			}
			getBlockManager().release(outerBlock, false);
		}
		tupleAppender.close();
	}

	class TupleAppender implements AutoCloseable, Consumer<Tuple> {

		BlockOutput blockOutput;

		TupleAppender(BlockOutput blockOutput) {
			this.blockOutput = blockOutput;
		}

		Block outputBlock = getBlockManager().allocate(true);

		@Override
		public void accept(Tuple tuple) {
			if(outputBlock.isFull()) {
				blockOutput.move(outputBlock);
				outputBlock = getBlockManager().allocate(true);
			}
			outputBlock.append(tuple);
		}

		@Override
		public void close() {
			if(!outputBlock.isEmpty()) {
				blockOutput.move(outputBlock);
			} else {
				getBlockManager().release(outputBlock, false);
			}
		}
	}

	@Override
	public int estimatedIOCost(@NotNull Relation leftInputRelation, @NotNull Relation rightInputRelation) {
		boolean swapped = rightInputRelation.estimatedBlockCount() < leftInputRelation.estimatedBlockCount();
		Relation outerRelation = (swapped) ? rightInputRelation : leftInputRelation;
		Relation innerRelation = (swapped) ? leftInputRelation : rightInputRelation;

		return outerRelation.estimatedBlockCount() + outerRelation.estimatedBlockCount() * innerRelation.estimatedBlockCount();
	}
}
